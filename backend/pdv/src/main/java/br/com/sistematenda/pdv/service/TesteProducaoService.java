package br.com.sistematenda.pdv.service;

import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.common.pix.PixException;
import br.com.sistematenda.common.pix.PixProviderService;
import br.com.sistematenda.pdv.dto.StatusIntegracaoDTO;
import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.pdv.pix.PixProviderFactory;
import br.com.sistematenda.pdv.repository.PdvRepository;
import br.com.sistematenda.pdv.tef.TefService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

/**
 * Executa testes de produção reais de TEF, PIX e impressora fiscal.
 *
 * <p>TEF e impressora são acionados através do {@code tef-agent}, que roda no
 * computador do PDV e é o único com acesso físico ao pinpad e à impressora.
 * O PIX fala diretamente com o PSP configurado.</p>
 */
@ApplicationScoped
public class TesteProducaoService {

    private static final Logger LOG = Logger.getLogger(TesteProducaoService.class);

    private static final int MAX_HISTORICO = 20;
    private static final Duration TIMEOUT_RAPIDO = Duration.ofSeconds(3);
    private static final Duration TIMEOUT_TEF = Duration.ofSeconds(20);
    private static final Duration TIMEOUT_IMPRESSORA = Duration.ofSeconds(30);

    @Inject
    TefService tefService;

    @Inject
    PixProviderFactory pixProviderFactory;

    @Inject
    PdvRepository pdvRepository;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Config config;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Deque<Map<String, Object>> historico = new ConcurrentLinkedDeque<>();

    // -------------------------------------------------------------------------
    // Status rápido (carregamento da página, sem testes lentos)
    // -------------------------------------------------------------------------

    public Map<String, Object> statusRapido() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("success", true);
        resposta.put("tef", statusRapidoTef());
        resposta.put("pix", statusRapidoPix());
        resposta.put("impressora", statusRapidoImpressora());
        resposta.put("ultimaVerificacao", Instant.now().toString());
        resposta.put("manual", false);
        return resposta;
    }

    private StatusIntegracaoDTO statusRapidoTef() {
        long inicio = System.currentTimeMillis();
        StatusIntegracaoDTO dto = novo("TEF");
        String url = tefService.getAgentUrl();
        dto.setDestino(url);

        try {
            HttpResponse<String> resposta = get(url + "/health", TIMEOUT_RAPIDO);
            if (resposta.statusCode() != 200) {
                return preencherErro(dto, inicio,
                        "Agente TEF respondeu com HTTP " + resposta.statusCode(),
                        resposta.body());
            }

            JsonNode health = objectMapper.readTree(resposta.body());
            boolean simulador = "simulator".equalsIgnoreCase(health.path("mode").asText(""));
            boolean inicializado = health.path("tefInitialized").asBoolean(false);

            if (simulador) {
                dto.setStatus("atencao");
                dto.setMensagem("Agente TEF acessível em modo SIMULADOR");
                dto.setDetalhe("Nenhuma transação real será realizada. Configure USE_SIMULATOR=false e TEF_DLL_PATH em produção.");
            } else if (inicializado) {
                dto.setStatus("ok");
                dto.setMensagem("Agente TEF acessível com DLL inicializada");
                dto.setDetalhe("Modo real");
            } else {
                dto.setStatus("atencao");
                dto.setMensagem("Agente TEF acessível (DLL ainda não inicializada)");
                dto.setDetalhe("A DLL é inicializada na primeira transação ou no teste completo.");
            }
            dto.setTempoMs(System.currentTimeMillis() - inicio);
            return dto;
        } catch (Exception e) {
            return preencherErro(dto, inicio,
                    "Agente TEF não acessível",
                    url + " — " + mensagem(e));
        }
    }

    private StatusIntegracaoDTO statusRapidoPix() {
        long inicio = System.currentTimeMillis();
        StatusIntegracaoDTO dto = novo("PIX");

        PixProviderService provider;
        try {
            provider = pixProviderFactory.getProviderOrDefault();
        } catch (IllegalStateException e) {
            dto.setStatus("nao_configurado");
            dto.setMensagem("PIX não configurado");
            dto.setDetalhe(e.getMessage());
            dto.setTempoMs(System.currentTimeMillis() - inicio);
            return dto;
        } catch (Exception e) {
            return preencherErro(dto, inicio, "Erro ao resolver o provedor PIX", mensagem(e));
        }

        dto.setDestino(provider.getProviderName());

        if (provider.getProviderType() == PixProvider.SIMULADO) {
            dto.setStatus("atencao");
            dto.setMensagem("Provedor PIX SIMULADO ativo");
            dto.setDetalhe("Nenhuma cobrança real é enviada ao banco. Configure PIX_PROVIDER com um PSP real.");
        } else {
            dto.setStatus("ok");
            dto.setMensagem("Provedor PIX configurado: " + provider.getProviderName());
            dto.setDetalhe(provider.isSandbox()
                    ? "Ambiente sandbox/homologação — execute o teste completo para confirmar a comunicação"
                    : "Ambiente de produção — execute o teste completo para confirmar a comunicação");
        }
        dto.setTempoMs(System.currentTimeMillis() - inicio);
        return dto;
    }

    private StatusIntegracaoDTO statusRapidoImpressora() {
        long inicio = System.currentTimeMillis();
        StatusIntegracaoDTO dto = novo("Impressora");
        String destino = destinoPadrao();

        if (destino == null) {
            dto.setStatus("nao_configurado");
            dto.setMensagem("Nenhuma impressora fiscal configurada");
            dto.setDetalhe("Informe a impressora no cadastro do PDV ou defina IMPRESSORA_FISCAL_DESTINO no .env.");
        } else {
            dto.setStatus("pendente");
            dto.setMensagem("Impressora configurada: " + destino);
            dto.setDetalhe("Execute o teste completo para confirmar a comunicação física com a impressora.");
            dto.setDestino(destino);
        }
        dto.setTempoMs(System.currentTimeMillis() - inicio);
        return dto;
    }

    // -------------------------------------------------------------------------
    // Teste completo
    // -------------------------------------------------------------------------

    public Map<String, Object> testarTudo(Map<String, Object> corpo) {
        long inicio = System.currentTimeMillis();

        String destino = texto(corpo, "destino");
        UUID pdvId = uuid(texto(corpo, "pdvId"));
        boolean imprimirTeste = booleano(corpo, "imprimirTeste", true);

        CompletableFuture<StatusIntegracaoDTO> futuroTef = CompletableFuture.supplyAsync(
                () -> comSeguranca("TEF", this::testarTef));
        CompletableFuture<StatusIntegracaoDTO> futuroPix = CompletableFuture.supplyAsync(
                () -> comSeguranca("PIX", this::testarPix));
        CompletableFuture<StatusIntegracaoDTO> futuroImpressora = CompletableFuture.supplyAsync(
                () -> comSeguranca("Impressora", () -> testarImpressora(destino, pdvId, imprimirTeste)));

        CompletableFuture.allOf(futuroTef, futuroPix, futuroImpressora).join();

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("success", true);
        resposta.put("tef", futuroTef.join());
        resposta.put("pix", futuroPix.join());
        resposta.put("impressora", futuroImpressora.join());
        resposta.put("ultimaVerificacao", Instant.now().toString());
        resposta.put("manual", true);
        resposta.put("tempoTotalMs", System.currentTimeMillis() - inicio);

        registrarHistorico(resposta);
        return resposta;
    }

    public StatusIntegracaoDTO testarTef() {
        long inicio = System.currentTimeMillis();
        StatusIntegracaoDTO dto = novo("TEF");
        String url = tefService.getAgentUrl();
        dto.setDestino(url);

        HttpResponse<String> health;
        try {
            health = get(url + "/health", TIMEOUT_RAPIDO);
        } catch (Exception e) {
            return preencherErro(dto, inicio,
                    "Agente TEF não acessível",
                    url + " — " + mensagem(e));
        }

        if (health.statusCode() != 200) {
            return preencherErro(dto, inicio,
                    "Agente TEF respondeu com HTTP " + health.statusCode(),
                    health.body());
        }

        String modo;
        boolean simulador;
        try {
            JsonNode node = objectMapper.readTree(health.body());
            modo = node.path("mode").asText("desconhecido");
            simulador = "simulator".equalsIgnoreCase(modo);
        } catch (Exception e) {
            return preencherErro(dto, inicio, "Resposta inválida do agente TEF", mensagem(e));
        }

        try {
            HttpResponse<String> diagnostico = get(url + "/tef/diagnostico", TIMEOUT_TEF);

            if (diagnostico.statusCode() == 404) {
                dto.setStatus("atencao");
                dto.setMensagem("Agente TEF acessível, mas sem endpoint de diagnóstico");
                dto.setDetalhe("Atualize o tef-agent para a versão com /tef/diagnostico. Modo atual: " + modo);
                dto.setTempoMs(System.currentTimeMillis() - inicio);
                return dto;
            }

            if (diagnostico.statusCode() != 200) {
                return preencherErro(dto, inicio,
                        "Diagnóstico TEF respondeu com HTTP " + diagnostico.statusCode(),
                        diagnostico.body());
            }

            JsonNode node = objectMapper.readTree(diagnostico.body());
            String status = normalizarStatus(node.path("status").asText(""));
            dto.setStatus(status);
            dto.setMensagem(node.path("mensagem").asText("Diagnóstico TEF concluído"));
            dto.setDetalhe(node.path("detalhe").asText(""));
            dto.setTempoMs(System.currentTimeMillis() - inicio);

            if (simulador) {
                dto.setStatus("atencao");
                dto.setMensagem("TEF em modo SIMULADOR — comunicação com o pinpad não é real");
                dto.setDetalhe("O agente respondeu, mas nenhuma transação real será realizada. "
                        + "Configure USE_SIMULATOR=false e TEF_DLL_PATH no computador do PDV.");
            }

            if (node.hasNonNull("tempoMs")) {
                dto.setTempoMs(node.path("tempoMs").asLong());
            }

            return dto;
        } catch (Exception e) {
            return preencherErro(dto, inicio,
                    "Falha ao consultar o diagnóstico TEF",
                    mensagem(e));
        }
    }

    public StatusIntegracaoDTO testarPix() {
        long inicio = System.currentTimeMillis();
        StatusIntegracaoDTO dto = novo("PIX");

        PixProviderService provider;
        try {
            provider = pixProviderFactory.getProviderOrDefault();
        } catch (IllegalStateException e) {
            dto.setStatus("nao_configurado");
            dto.setMensagem("PIX não configurado");
            dto.setDetalhe(e.getMessage());
            dto.setTempoMs(System.currentTimeMillis() - inicio);
            return dto;
        } catch (Exception e) {
            return preencherErro(dto, inicio, "Erro ao resolver o provedor PIX", mensagem(e));
        }

        dto.setDestino(provider.getProviderName());

        if (provider.getProviderType() == PixProvider.SIMULADO) {
            return testarPixSimulado(provider, dto, inicio);
        }

        // Para PSPs reais, consulta um txid inexistente. Um 400/404/422 prova que
        // o endpoint respondeu (comunicação e credenciais válidas), sem criar
        // nenhuma cobrança de verdade.
        String txidTeste = "TENDA" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();

        try {
            PixChargeResponse resposta = provider.getCharge(txidTeste);
            dto.setStatus(provider.isSandbox() ? "atencao" : "ok");
            dto.setMensagem("PSP PIX respondeu à consulta de teste");
            dto.setDetalhe("Provedor: " + provider.getProviderName()
                    + " | txid consultado: " + txidTeste
                    + (resposta != null && resposta.txid != null ? " | resposta: " + resposta.txid : "")
                    + (provider.isSandbox() ? " | ambiente sandbox/homologação" : " | ambiente de produção"));
        } catch (PixException e) {
            int http = e.getHttpStatus();
            if (http == 401 || http == 403) {
                dto.setStatus("erro");
                dto.setMensagem("PSP PIX recusou as credenciais (HTTP " + http + ")");
                dto.setDetalhe(mensagem(e) + " — revise as chaves/tokens do provedor " + provider.getProviderName());
            } else if (http == 400 || http == 404 || http == 422) {
                dto.setStatus(provider.isSandbox() ? "atencao" : "ok");
                dto.setMensagem("PSP PIX acessível — comunicação confirmada");
                dto.setDetalhe("O PSP respondeu HTTP " + http + " para um txid de teste (esperado). "
                        + "Provedor: " + provider.getProviderName()
                        + (provider.isSandbox() ? " | ambiente sandbox/homologação" : " | ambiente de produção"));
            } else {
                dto.setStatus("erro");
                dto.setMensagem("PSP PIX retornou erro HTTP " + http);
                dto.setDetalhe(mensagem(e));
            }
        } catch (Exception e) {
            dto.setStatus("erro");
            dto.setMensagem("Falha de comunicação com o PSP PIX");
            dto.setDetalhe(mensagem(e));
        }

        dto.setTempoMs(System.currentTimeMillis() - inicio);
        return dto;
    }

    private StatusIntegracaoDTO testarPixSimulado(PixProviderService provider, StatusIntegracaoDTO dto, long inicio) {
        String txid = "SIMTESTE" + System.currentTimeMillis();

        try {
            PixChargeRequest request = new PixChargeRequest();
            request.valor = new BigDecimal("0.01");
            request.txid = txid;
            request.expiracaoSegundos = 60;
            request.solicitacaoPagador = "Teste de produção Sistema Tenda";

            PixChargeResponse cobranca = provider.createCharge(request);
            String detalheCancelamento = "";
            try {
                provider.cancelCharge(cobranca.txid);
                detalheCancelamento = " Cobrança de teste cancelada.";
            } catch (Exception e) {
                detalheCancelamento = " Atenção: não foi possível cancelar a cobrança de teste: " + mensagem(e);
            }

            dto.setStatus("atencao");
            dto.setMensagem("Provedor PIX SIMULADO ativo — nenhuma cobrança real foi feita");
            dto.setDetalhe("O fluxo local criou e cancelou uma cobrança de R$ 0,01 (txid " + txid + ")."
                    + detalheCancelamento
                    + " Configure PIX_PROVIDER com Efí, Mercado Pago, Asaas ou Open Finance para testar o PSP real.");
        } catch (Exception e) {
            dto.setStatus("erro");
            dto.setMensagem("Falha no fluxo PIX simulado");
            dto.setDetalhe(mensagem(e));
        }

        dto.setTempoMs(System.currentTimeMillis() - inicio);
        return dto;
    }

    public StatusIntegracaoDTO testarImpressora(String destino, UUID pdvId, boolean imprimirTeste) {
        long inicio = System.currentTimeMillis();
        StatusIntegracaoDTO dto = novo("Impressora");

        String destinoFinal = resolverDestino(destino, pdvId);
        dto.setDestino(destinoFinal != null ? destinoFinal : "padrão do Windows");

        Map<String, Object> corpo = new LinkedHashMap<>();
        if (destinoFinal != null) {
            corpo.put("destino", destinoFinal);
        }
        corpo.put("imprimirTeste", imprimirTeste);
        corpo.put("timeoutMs", timeoutImpressoraMs());

        String url = tefService.getAgentUrl();
        try {
            HttpResponse<String> resposta = postJson(url + "/impressora/teste", corpo, TIMEOUT_IMPRESSORA);

            if (resposta.statusCode() == 404) {
                dto.setStatus("erro");
                dto.setMensagem("Agente local sem suporte a teste de impressora");
                dto.setDetalhe("Atualize o tef-agent no computador do PDV para a versão com /impressora/teste.");
                dto.setTempoMs(System.currentTimeMillis() - inicio);
                return dto;
            }

            if (resposta.statusCode() != 200) {
                return preencherErro(dto, inicio,
                        "Agente local respondeu HTTP " + resposta.statusCode(),
                        resposta.body());
            }

            JsonNode node = objectMapper.readTree(resposta.body());
            String status = normalizarStatus(node.path("status").asText(""));
            dto.setStatus(status);
            dto.setMensagem(node.path("mensagem").asText("Teste de impressora concluído"));
            dto.setDetalhe(node.path("detalhe").asText(""));
            dto.setDestino(node.path("destino").asText(dto.getDestino()));
            if (node.hasNonNull("tempoMs")) {
                dto.setTempoMs(node.path("tempoMs").asLong());
            } else {
                dto.setTempoMs(System.currentTimeMillis() - inicio);
            }
            return dto;
        } catch (Exception e) {
            return preencherErro(dto, inicio,
                    "Falha ao comunicar com o agente local do PDV",
                    url + " — " + mensagem(e));
        }
    }

    public List<Map<String, Object>> historico() {
        return new ArrayList<>(historico);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private StatusIntegracaoDTO comSeguranca(String componente, Supplier<StatusIntegracaoDTO> teste) {
        try {
            return teste.get();
        } catch (Exception e) {
            LOG.error("Erro inesperado no teste de " + componente, e);
            StatusIntegracaoDTO dto = novo(componente);
            dto.setStatus("erro");
            dto.setMensagem("Erro inesperado no teste de " + componente);
            dto.setDetalhe(mensagem(e));
            dto.setTempoMs(0L);
            return dto;
        }
    }

    private void registrarHistorico(Map<String, Object> resultado) {
        historico.addFirst(new LinkedHashMap<>(resultado));
        while (historico.size() > MAX_HISTORICO) {
            historico.removeLast();
        }
    }

    private String resolverDestino(String destino, UUID pdvId) {
        if (destino != null && !destino.isBlank()) {
            return destino.trim();
        }

        if (pdvId != null) {
            try {
                Pdv pdv = pdvRepository.findById(pdvId);
                if (pdv != null && pdv.impressoraFiscal != null && !pdv.impressoraFiscal.isBlank()) {
                    return pdv.impressoraFiscal.trim();
                }
            } catch (Exception e) {
                LOG.warn("Não foi possível carregar a impressora do PDV " + pdvId + ": " + e.getMessage());
            }
        }

        return destinoPadrao();
    }

    private String destinoPadrao() {
        return config.getOptionalValue("impressora.fiscal.destino", String.class)
                .filter(valor -> valor != null && !valor.isBlank())
                .map(String::trim)
                .orElse(null);
    }

    private int timeoutImpressoraMs() {
        return config.getOptionalValue("impressora.timeout.segundos", Integer.class)
                .filter(valor -> valor > 0)
                .orElse(15) * 1000;
    }

    private StatusIntegracaoDTO novo(String componente) {
        StatusIntegracaoDTO dto = new StatusIntegracaoDTO();
        dto.setComponente(componente);
        dto.setHoraVerificacao(Instant.now().toString());
        return dto;
    }

    private StatusIntegracaoDTO preencherErro(StatusIntegracaoDTO dto, long inicio, String mensagem, String detalhe) {
        dto.setStatus("erro");
        dto.setMensagem(mensagem);
        dto.setDetalhe(detalhe != null ? detalhe : "");
        dto.setHoraVerificacao(Instant.now().toString());
        dto.setTempoMs(System.currentTimeMillis() - inicio);
        return dto;
    }

    private String normalizarStatus(String status) {
        if (status == null) {
            return "erro";
        }
        return switch (status.toLowerCase()) {
            case "ok", "sucesso", "funcionando" -> "ok";
            case "atencao", "atenção" -> "atencao";
            case "pendente" -> "pendente";
            case "nao_configurado", "não_configurado" -> "nao_configurado";
            default -> "erro";
        };
    }

    private String mensagem(Throwable e) {
        if (e == null) {
            return "Erro desconhecido";
        }
        String msg = e.getMessage();
        return msg != null && !msg.isBlank() ? msg : e.getClass().getSimpleName();
    }

    private String texto(Map<String, Object> corpo, String chave) {
        if (corpo == null) {
            return null;
        }
        Object valor = corpo.get(chave);
        if (valor == null) {
            return null;
        }
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
    }

    private boolean booleano(Map<String, Object> corpo, String chave, boolean padrao) {
        if (corpo == null) {
            return padrao;
        }
        Object valor = corpo.get(chave);
        if (valor instanceof Boolean b) {
            return b;
        }
        if (valor != null) {
            return Boolean.parseBoolean(String.valueOf(valor));
        }
        return padrao;
    }

    private UUID uuid(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private HttpResponse<String> get(String url, Duration timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String url, Object corpo, Duration timeout)
            throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(corpo);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
