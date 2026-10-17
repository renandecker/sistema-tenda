package br.com.sistematenda.pdv.tef;

import br.com.sistematenda.common.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class TefService {

    private static final Logger LOG = Logger.getLogger(TefService.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    org.eclipse.microprofile.config.Config config;

    @ConfigProperty(name = "tef.timeout.seconds", defaultValue = "120")
    int timeoutSeconds;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Map<String, CompletableFuture<TefTransacaoResponse>> pendingTransactions = new ConcurrentHashMap<>();

    private String getEffectiveTefAgentUrl() {
        return config.getOptionalValue("tef.ngrok.url", String.class)
                .filter(url -> url != null && !url.isBlank())
                .orElseGet(() -> config.getOptionalValue("tef.agent.url", String.class)
                        .filter(url -> url != null && !url.isBlank())
                        .orElse("http://localhost:8080"));
    }

    public TefTransacaoResponse iniciarTransacao(TefIniciarTransacaoRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getEffectiveTefAgentUrl() + "/tef/command"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new BusinessException("TEF_AGENT_ERROR", "Erro no agente TEF: " + response.body());
            }

            TefResponse tefResponse = objectMapper.readValue(response.body(), TefResponse.class);
            
            if (!tefResponse.success) {
                throw new BusinessException("TEF_ERROR", tefResponse.error != null ? tefResponse.error : "Erro desconhecido no TEF");
            }

            return aguardarFinalizacao(tefResponse.requestId);
        } catch (IOException | InterruptedException e) {
            LOG.error("Erro ao comunicar com agente TEF", e);
            throw new BusinessException("TEF_COMM_ERROR", "Falha na comunicação com agente TEF: " + e.getMessage());
        }
    }

    private TefTransacaoResponse aguardarFinalizacao(String requestId) throws InterruptedException {
        CompletableFuture<TefTransacaoResponse> future = new CompletableFuture<>();
        pendingTransactions.put(requestId, future);

        try {
            return future.get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            pendingTransactions.remove(requestId);
            throw new BusinessException("TEF_TIMEOUT", "Timeout aguardando finalização da transação TEF");
        } catch (Exception e) {
            pendingTransactions.remove(requestId);
            throw new BusinessException("TEF_ERROR", "Erro ao aguardar transação: " + e.getMessage());
        }
    }

    public void notificarEvento(TefEventoResponse evento) {
        CompletableFuture<TefTransacaoResponse> future = pendingTransactions.get(evento.requestId);
        if (future != null && evento.evento != null) {
            StatusTransacaoTef status = StatusTransacaoTef.valueOf(evento.evento);
            
            if (status.isFinalizado()) {
                TefTransacaoResponse response = new TefTransacaoResponse();
                response.requestId = evento.requestId;
                response.status = status;
                response.mensagem = evento.mensagem;
                
                if (evento.dados != null) {
                    response.nsu = (String) evento.dados.get("nsu");
                    response.codigoAutorizacao = (String) evento.dados.get("codigoAutorizacao");
                    response.comprovanteCliente = (String) evento.dados.get("comprovanteCliente");
                    response.comprovanteEstabelecimento = (String) evento.dados.get("comprovanteEstabelecimento");
                    response.motivoNegativa = (String) evento.dados.get("motivo");
                }
                
                response.sucesso = status.isSucesso();
                future.complete(response);
                pendingTransactions.remove(evento.requestId);
            }
        }
    }

    public TefTransacaoResponse confirmarTransacao(String requestId) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("command", "CONFIRMAR_TRANSACAO", "params", Map.of("originalRequestId", requestId)));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getEffectiveTefAgentUrl() + "/tef/command"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            return objectMapper.readValue(response.body(), TefTransacaoResponse.class);
        } catch (IOException | InterruptedException e) {
            LOG.error("Erro ao confirmar transação TEF", e);
            throw new BusinessException("TEF_COMM_ERROR", "Falha ao confirmar transação: " + e.getMessage());
        }
    }

    public void desfazerTransacao(String requestId) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("command", "DESFAZER_TRANSACAO", "params", Map.of("originalRequestId", requestId)));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getEffectiveTefAgentUrl() + "/tef/command"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.error("Erro ao desfazer transação TEF", e);
            throw new BusinessException("TEF_COMM_ERROR", "Falha ao desfazer transação: " + e.getMessage());
        }
    }

    public String reimprimirComprovante(String requestId, String via) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("command", "REIMPRIMIR_COMPROVANTE", "params", Map.of("originalRequestId", requestId, "via", via)));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getEffectiveTefAgentUrl() + "/tef/command"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            TefTransacaoResponse tefResponse = objectMapper.readValue(response.body(), TefTransacaoResponse.class);
            
            return via.equals("CLIENTE") ? tefResponse.comprovanteCliente : tefResponse.comprovanteEstabelecimento;
        } catch (IOException | InterruptedException e) {
            LOG.error("Erro ao reimprimir comprovante TEF", e);
            throw new BusinessException("TEF_COMM_ERROR", "Falha ao reimprimir comprovante: " + e.getMessage());
        }
    }

    private record TefResponse(String requestId, boolean success, String error) {}
}