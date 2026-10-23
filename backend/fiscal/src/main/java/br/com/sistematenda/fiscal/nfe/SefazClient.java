package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ApplicationScoped
public class SefazClient {

    private static final Logger LOG = Logger.getLogger(SefazClient.class);

    @Inject
    FiscalConfig fiscalConfig;

    @Inject
    CertificadoDigital certificadoDigital;

    public SefazResponse enviar(String url, String servico, String xmlRequisicao) {
        return enviar(url, servico, xmlRequisicao, null);
    }

    public SefazResponse enviar(String url, String servico, String xmlRequisicao, String nfeAssinada) {
        if (url == null || url.isBlank()) {
            throw new BusinessException("SEFAZ_URL_NAO_CONFIGURADA",
                "URL do web service " + servico + " da SEFAZ nao configurada");
        }
        int timeout = fiscalConfig.webserviceTimeout() > 0 ? fiscalConfig.webserviceTimeout() : 30000;
        String acao = SefazRequestBuilder.soapAction(servico);
        try {
            HttpClient client = HttpClient.newBuilder()
                .sslContext(certificadoDigital.sslContext())
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeout))
                .header("Content-Type", "application/soap+xml; charset=utf-8; action=\"" + acao + "\"")
                .header("SOAPAction", "\"" + acao + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(xmlRequisicao, StandardCharsets.UTF_8))
                .build();

            LOG.debugf("Enviando requisicao ao servico %s em %s", servico, url);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            LOG.debugf("Resposta da SEFAZ %s: HTTP %d", servico, response.statusCode());
            return SefazResponseParser.parse(response.body(), response.statusCode(), nfeAssinada);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("SEFAZ_COMUNICACAO",
                "Comunicacao com a SEFAZ interrompida: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("SEFAZ_COMUNICACAO",
                "Falha de comunicacao com a SEFAZ (" + servico + "): " + e.getMessage());
        }
    }
}
