package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.config.PixConfig;
import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.common.pix.PixException;
import br.com.sistematenda.common.pix.PixProviderService;
import br.com.sistematenda.common.pix.PixWebhookNotification;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EfiPixProvider implements PixProviderService {

    private static final Logger LOG = Logger.getLogger(EfiPixProvider.class);

    @Inject
    PixConfig config;

    private Client client;
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String certPath;
    private String certPassword;
    private String accessToken;
    private long tokenExpiresAt;

    private synchronized void initClient() {
        if (client != null) return;

        PixConfig.EfiConfig efiConfig = config.efi().orElseThrow(
            () -> new PixException("EFI_CONFIG_MISSING", "ConfiguraÃ§Ã£o da EfÃ­ nÃ£o encontrada", 500)
        );

        this.clientId = efiConfig.clientId().orElse(null);
        this.clientSecret = efiConfig.clientSecret().orElse(null);
        this.certPath = efiConfig.certificadoP12Path().orElse(null);
        this.certPassword = efiConfig.certificadoSenha().orElse(null);

        if (config.sandbox()) {
            this.baseUrl = "https://pix-h.api.efipay.com.br";
        } else {
            this.baseUrl = "https://pix.api.efipay.com.br";
        }

        try {
            ClientBuilder builder = ClientBuilder.newBuilder();

            if (certPath != null && !certPath.isEmpty()) {
                builder.sslContext(createSSLContext())
                    .hostnameVerifier((hostname, session) -> true);
            }

            client = builder.build();
        } catch (Exception e) {
            LOG.error("Erro ao criar cliente HTTP da EfÃ­", e);
            throw new PixException("EFI_SSL_ERROR", "Erro ao configurar cliente EfÃ­: " + e.getMessage(), 500, e);
        }
    }

    private SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream certStream = new FileInputStream(certPath)) {
            keyStore.load(certStream, certPassword != null ? certPassword.toCharArray() : null);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, certPassword != null ? certPassword.toCharArray() : null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt - 60000) {
            return accessToken;
        }

        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        var request = client
            .target(baseUrl + "/oauth/token")
            .request(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Basic " + auth);

        if (config.sandbox()) {
            request.header("x-skip-mtls-checking", "true");
        }

        jakarta.ws.rs.core.Form form = new jakarta.ws.rs.core.Form();
        form.param("grant_type", "client_credentials");
        Response response = request.post(Entity.form(form));

        if (response.getStatus() >= 400) {
            String error = response.readEntity(String.class);
            throw new PixException("EFI_AUTH_ERROR", "Erro ao obter token EfÃ­: " + error, response.getStatus());
        }

        Map<String, Object> tokenData = response.readEntity(Map.class);
        accessToken = (String) tokenData.get("access_token");
        Object expiresIn = tokenData.get("expires_in");
        if (expiresIn != null) {
            tokenExpiresAt = System.currentTimeMillis() + (((Number) expiresIn).longValue() * 1000L);
        }
        return accessToken;
    }

    @Override
    public String getProviderName() {
        return "EfÃ­ (Gerencianet)";
    }

    @Override
    public PixProvider getProviderType() {
        return PixProvider.EFI;
    }

    @Override
    public PixChargeResponse createCharge(PixChargeRequest request) throws PixException {
        try {
            initClient();
            String token = getAccessToken();

            Map<String, Object> body = new HashMap<>();
            body.put("calendario", Map.of("expiracao", request.expiracaoSegundos != null ? request.expiracaoSegundos : config.expiracaoPadraoSegundos()));
            body.put("valor", Map.of("original", request.valor.toPlainString()));

            String chave = request.chavePix != null ? request.chavePix : config.chavePix().orElse(null);
            if (chave != null && !chave.isEmpty()) {
                body.put("chave", chave);
            }

            if (request.solicitacaoPagador != null && !request.solicitacaoPagador.isEmpty()) {
                body.put("solicitacaoPagador", request.solicitacaoPagador);
            }

            if (request.pagador != null) {
                Map<String, Object> pagador = new HashMap<>();
                if (request.pagador.cpf != null) pagador.put("cpf", request.pagador.cpf);
                if (request.pagador.cnpj != null) pagador.put("cnpj", request.pagador.cnpj);
                if (request.pagador.nome != null) pagador.put("nome", request.pagador.nome);
                body.put("pagador", pagador);
            }

            if (request.infoAdicionais != null && request.infoAdicionais.length > 0) {
                Map<String, Object>[] infoAdicionais = new Map[request.infoAdicionais.length];
                for (int i = 0; i < request.infoAdicionais.length; i++) {
                    infoAdicionais[i] = Map.of("nome", request.infoAdicionais[i].nome, "valor", request.infoAdicionais[i].valor);
                }
                body.put("infoAdicionais", infoAdicionais);
            }

            Response response;
            if (request.txid != null && !request.txid.isEmpty()) {
                response = client
                    .target(baseUrl + "/v2/cob/" + request.txid)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("x-skip-mtls-checking", String.valueOf(config.sandbox()))
                    .put(Entity.json(body));
            } else {
                response = client
                    .target(baseUrl + "/v2/cob")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("x-skip-mtls-checking", String.valueOf(config.sandbox()))
                    .post(Entity.json(body));
            }

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("EFI_API_ERROR", error, response.getStatus());
            }

            Map<String, Object> data = response.readEntity(Map.class);
            PixChargeResponse charge = parseChargeResponse(data);
            fillQrCode(charge, token);
            return charge;

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao criar cobranÃ§a PIX na EfÃ­", e);
            throw new PixException("EFI_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public PixChargeResponse getCharge(String txid) throws PixException {
        try {
            initClient();
            String token = getAccessToken();

            Response response = client
                .target(baseUrl + "/v2/cob/" + txid)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("x-skip-mtls-checking", String.valueOf(config.sandbox()))
                .get();

            if (response.getStatus() == 404) {
                throw new PixException("EFI_NOT_FOUND", "CobranÃ§a nÃ£o encontrada", 404);
            }

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("EFI_API_ERROR", error, response.getStatus());
            }

            Map<String, Object> data = response.readEntity(Map.class);
            PixChargeResponse charge = parseChargeResponse(data);
            fillQrCode(charge, token);
            return charge;

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao consultar cobranÃ§a PIX na EfÃ­: " + txid, e);
            throw new PixException("EFI_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public void cancelCharge(String txid) throws PixException {
        try {
            initClient();
            String token = getAccessToken();

            Map<String, Object> body = Map.of("status", "REMOVIDA_PELO_USUARIO_RECEBEDOR");

            Response response = client
                .target(baseUrl + "/v2/cob/" + txid)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("x-skip-mtls-checking", String.valueOf(config.sandbox()))
                .method("PATCH", Entity.json(body));

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("EFI_API_ERROR", error, response.getStatus());
            }

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao cancelar cobranÃ§a PIX na EfÃ­: " + txid, e);
            throw new PixException("EFI_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String timestamp) throws PixException {
        if (config.sandbox()) {
            LOG.debug("Webhook EfÃ­ em sandbox - verificaÃ§Ã£o de assinatura ignorada");
            return true;
        }
        return true;
    }

    @Override
    public PixWebhookNotification parseWebhook(String payload, String signature, String timestamp) throws PixException {
        try {
            if (!verifyWebhookSignature(payload, signature, timestamp)) {
                throw new PixException("EFI_WEBHOOK_INVALID_SIGNATURE", "Assinatura do webhook invÃ¡lida", 401);
            }

            PixWebhookNotification notification = new PixWebhookNotification();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Map<String, Object> data = mapper.readValue(payload, Map.class);

            Map<String, Object> pix = extractPix(data);

            if (pix != null) {
                notification.txid = (String) pix.get("txid");
                notification.endToEndId = (String) pix.get("endToEndId");
                notification.status = (String) pix.get("status");
                Object valor = pix.get("valor");
                if (valor != null) {
                    notification.valor = new BigDecimal(valor.toString());
                }
                notification.chavePix = (String) pix.get("chave");

                String horario = (String) pix.get("horario");
                if (horario != null) {
                    notification.criadoEm = LocalDateTime.parse(horario, DateTimeFormatter.ISO_DATE_TIME);
                    notification.atualizadoEm = notification.criadoEm;
                }
            }

            notification.rawPayload = data;
            return notification;

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao processar webhook EfÃ­", e);
            throw new PixException("EFI_WEBHOOK_PARSE_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public boolean supportsWebhook() {
        return true;
    }

    @Override
    public String getWebhookUrl() {
        return config.webhookBaseUrl().orElse("") + "/api/pix/webhook/efi";
    }

    @Override
    public boolean isSandbox() {
        return config.sandbox();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPix(Map<String, Object> data) {
        Object pixPayload = data.get("pix");
        if (pixPayload instanceof List<?> list && !list.isEmpty()) {
            return (Map<String, Object>) list.get(0);
        }
        if (pixPayload instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private void fillQrCode(PixChargeResponse charge, String token) {
        if (charge.locationId == null) {
            return;
        }
        try {
            Response response = client
                .target(baseUrl + "/v2/loc/" + charge.locationId + "/qrcode")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("x-skip-mtls-checking", String.valueOf(config.sandbox()))
                .get();

            if (response.getStatus() >= 400) {
                return;
            }

            Map<String, Object> data = response.readEntity(Map.class);
            if (data.get("qrcode") != null) {
                charge.qrCode = (String) data.get("qrcode");
            }
            if (data.get("imagemQrcode") != null) {
                charge.qrCodeBase64 = (String) data.get("imagemQrcode");
            }
        } catch (Exception e) {
            LOG.warn("NÃ£o foi possÃ­vel obter o QR Code da cobranÃ§a EfÃ­", e);
        }
    }

    @SuppressWarnings("unchecked")
    private PixChargeResponse parseChargeResponse(Map<String, Object> data) {
        PixChargeResponse response = new PixChargeResponse();
        response.txid = (String) data.get("txid");
        response.status = (String) data.get("status");
        response.chavePix = (String) data.get("chave");
        response.pixCopiaECola = (String) data.get("pixCopiaECola");

        Object loc = data.get("loc");
        if (loc instanceof Map<?, ?> locMap) {
            Object id = ((Map<String, Object>) locMap).get("id");
            if (id != null) {
                response.locationId = id.toString();
            }
        }

        Object valor = data.get("valor");
        if (valor instanceof Map<?, ?> valorMap) {
            Object original = ((Map<String, Object>) valorMap).get("original");
            if (original != null) {
                response.valor = new BigDecimal(original.toString());
            }
        }

        Object calendario = data.get("calendario");
        if (calendario instanceof Map<?, ?> calMap) {
            Object criacao = ((Map<String, Object>) calMap).get("criacao");
            if (criacao != null) {
                response.criadoEm = LocalDateTime.parse(criacao.toString(), DateTimeFormatter.ISO_DATE_TIME);
            }
        }

        return response;
    }
}
