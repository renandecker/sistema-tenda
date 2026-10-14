package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.config.PixConfig;
import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.enums.PixStatus;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class OpenFinancePixProvider implements PixProviderService {

    private static final Logger LOG = Logger.getLogger(OpenFinancePixProvider.class);

    @Inject
    PixConfig config;

    private Client client;
    private String baseUrl;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String certPath;
    private String certPassword;
    private String pixKey;
    private String accessToken;
    private long tokenExpiresAt;

    private synchronized void initClient(PixConfig.ItauConfig itauConfig) {
        if (client != null) return;

        this.clientId = itauConfig.clientId().orElse(null);
        this.clientSecret = itauConfig.clientSecret().orElse(null);
        this.certPath = itauConfig.certificadoPath().orElse(null);
        this.certPassword = itauConfig.certificadoSenha().orElse(null);
        this.pixKey = itauConfig.chavePix().orElse(null);

        String ambiente = itauConfig.ambiente();
        
        if ("production".equalsIgnoreCase(ambiente)) {
            this.baseUrl = "https://api.itau.com.br";
            this.tokenUrl = "https://api.itau.com.br/oauth2/token";
        } else {
            this.baseUrl = "https://api.sandbox.itau.com.br";
            this.tokenUrl = "https://api.sandbox.itau.com.br/oauth2/token";
        }

        try {
            SSLContext sslContext = createSSLContext();
            client = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .hostnameVerifier((hostname, session) -> true)
                .build();
        } catch (Exception e) {
            LOG.error("Erro ao criar cliente HTTP com mTLS", e);
            throw new PixException("OPENFINANCE_SSL_ERROR", "Erro ao configurar mTLS: " + e.getMessage(), 500, e);
        }
    }

    private synchronized void initClientBradesco(PixConfig.BradescoConfig bradescoConfig) {
        if (client != null) return;

        this.clientId = bradescoConfig.clientId().orElse(null);
        this.clientSecret = bradescoConfig.clientSecret().orElse(null);
        this.certPath = bradescoConfig.certificadoPath().orElse(null);
        this.certPassword = bradescoConfig.certificadoSenha().orElse(null);
        this.pixKey = bradescoConfig.chavePix().orElse(null);

        String ambiente = bradescoConfig.ambiente();
        
        if ("production".equalsIgnoreCase(ambiente)) {
            this.baseUrl = "https://api.bradesco.com.br";
            this.tokenUrl = "https://api.bradesco.com.br/oauth2/token";
        } else {
            this.baseUrl = "https://api.sandbox.bradesco.com.br";
            this.tokenUrl = "https://api.sandbox.bradesco.com.br/oauth2/token";
        }

        try {
            SSLContext sslContext = createSSLContext();
            client = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .hostnameVerifier((hostname, session) -> true)
                .build();
        } catch (Exception e) {
            LOG.error("Erro ao criar cliente HTTP com mTLS para Bradesco", e);
            throw new PixException("OPENFINANCE_SSL_ERROR", "Erro ao configurar mTLS: " + e.getMessage(), 500, e);
        }
    }

    private SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream certStream = new FileInputStream(certPath)) {
            keyStore.load(certStream, certPassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, certPassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    private String getAccessToken() throws Exception {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt - 60000) {
            return accessToken;
        }

        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        
        jakarta.ws.rs.core.Form form = new jakarta.ws.rs.core.Form();
        form.param("grant_type", "client_credentials");
        form.param("scope", "pix.read pix.write");

        Response response = ClientBuilder.newBuilder()
            .sslContext(createSSLContext())
            .hostnameVerifier((hostname, session) -> true)
            .build()
            .target(tokenUrl)
            .request(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Basic " + auth)
            .post(Entity.form(form));

        if (response.getStatus() >= 400) {
            String error = response.readEntity(String.class);
            throw new PixException("OPENFINANCE_AUTH_ERROR", "Erro ao obter token: " + error, response.getStatus());
        }

        Map<String, Object> tokenData = response.readEntity(Map.class);
        accessToken = (String) tokenData.get("access_token");
        int expiresIn = ((Number) tokenData.get("expires_in")).intValue();
        tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
        
        return accessToken;
    }

    @Override
    public String getProviderName() {
        return "Open Finance (Itaú/Bradesco)";
    }

    @Override
    public PixProvider getProviderType() {
        return config.efi().isPresent() ? PixProvider.ITAU : PixProvider.BRADESCO;
    }

    @Override
    public PixChargeResponse createCharge(PixChargeRequest request) throws PixException {
        try {
            if (config.itau().isPresent()) {
                initClient(config.itau().get());
            } else if (config.bradesco().isPresent()) {
                initClientBradesco(config.bradesco().get());
            } else {
                throw new PixException("OPENFINANCE_CONFIG_MISSING", "Configuração Open Finance não encontrada", 500);
            }

            String token = getAccessToken();

            Map<String, Object> body = new HashMap<>();
            body.put("calendario", Map.of("expiracao", request.expiracaoSegundos != null ? request.expiracaoSegundos : config.expiracaoPadraoSegundos()));
            body.put("valor", Map.of("original", request.valor.toPlainString()));
            
            String chave = request.chavePix != null ? request.chavePix : this.pixKey;
            if (chave != null) {
                body.put("chave", chave);
            }

            if (request.txid != null && !request.txid.isEmpty()) {
                body.put("txid", request.txid);
            }

            if (request.solicitacaoPagador != null) {
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

            Response response = client
                .target(baseUrl + "/pix/v2/cob")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .post(Entity.json(body));

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("OPENFINANCE_API_ERROR", error, response.getStatus());
            }

            Map<String, Object> data = response.readEntity(Map.class);
            return parseChargeResponse(data);

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao criar cobrança PIX Open Finance", e);
            throw new PixException("OPENFINANCE_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public PixChargeResponse getCharge(String txid) throws PixException {
        try {
            if (config.itau().isPresent()) {
                initClient(config.itau().get());
            } else {
                initClientBradesco(config.bradesco().get());
            }

            String token = getAccessToken();

            Response response = client
                .target(baseUrl + "/pix/v2/cob/" + txid)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .get();

            if (response.getStatus() == 404) {
                throw new PixException("OPENFINANCE_NOT_FOUND", "Cobrança não encontrada", 404);
            }

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("OPENFINANCE_API_ERROR", error, response.getStatus());
            }

            Map<String, Object> data = response.readEntity(Map.class);
            return parseChargeResponse(data);

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao consultar cobrança PIX Open Finance: " + txid, e);
            throw new PixException("OPENFINANCE_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public void cancelCharge(String txid) throws PixException {
        try {
            if (config.itau().isPresent()) {
                initClient(config.itau().get());
            } else {
                initClientBradesco(config.bradesco().get());
            }

            String token = getAccessToken();

            Map<String, Object> body = Map.of("status", "REMOVIDA_PELO_USUARIO_RECEBEDOR");
            
            Response response = client
                .target(baseUrl + "/pix/v2/cob/" + txid)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .method("PATCH", Entity.json(body));

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("OPENFINANCE_API_ERROR", error, response.getStatus());
            }

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao cancelar cobrança PIX Open Finance: " + txid, e);
            throw new PixException("OPENFINANCE_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String timestamp) throws PixException {
        try {
            String xSignature = signature;
            String xTimestamp = timestamp;
            
            if (xSignature == null || xTimestamp == null) {
                return false;
            }

            long ts = Long.parseLong(xTimestamp);
            long now = System.currentTimeMillis() / 1000;
            if (Math.abs(now - ts) > 300) {
                LOG.warn("Timestamp do webhook fora da janela aceitável");
                return false;
            }

            String expectedSig = generateHmacSha256(xTimestamp + "." + payload, getWebhookSecret());
            return expectedSig.equals(xSignature);

        } catch (Exception e) {
            LOG.error("Erro ao verificar assinatura do webhook Open Finance", e);
            throw new PixException("OPENFINANCE_WEBHOOK_VERIFY_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public PixWebhookNotification parseWebhook(String payload, String signature, String timestamp) throws PixException {
        try {
            if (!verifyWebhookSignature(payload, signature, timestamp)) {
                throw new PixException("OPENFINANCE_WEBHOOK_INVALID_SIGNATURE", "Assinatura do webhook inválida", 401);
            }

            PixWebhookNotification notification = new PixWebhookNotification();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Map<String, Object> data = mapper.readValue(payload, Map.class);
            Map<String, Object> pix = (Map<String, Object>) data.get("pix");

            if (pix != null) {
                notification.txid = (String) pix.get("txid");
                notification.endToEndId = (String) pix.get("endToEndId");
                notification.status = (String) pix.get("status");
                notification.valor = new BigDecimal(pix.get("valor").toString());
                notification.chavePix = (String) pix.get("chave");
                
                String criadoEmStr = (String) pix.get("criadoEm");
                if (criadoEmStr != null) {
                    notification.criadoEm = LocalDateTime.parse(criadoEmStr, DateTimeFormatter.ISO_DATE_TIME);
                }
                
                String atualizadoEmStr = (String) pix.get("atualizadoEm");
                if (atualizadoEmStr != null) {
                    notification.atualizadoEm = LocalDateTime.parse(atualizadoEmStr, DateTimeFormatter.ISO_DATE_TIME);
                }
            }

            notification.rawPayload = data;
            return notification;

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao processar webhook Open Finance", e);
            throw new PixException("OPENFINANCE_WEBHOOK_PARSE_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public boolean supportsWebhook() {
        return true;
    }

    @Override
    public String getWebhookUrl() {
        return config.webhookBaseUrl().orElse("") + "/api/pix/webhook/openfinance";
    }

    @Override
    public boolean isSandbox() {
        String ambiente = config.itau().isPresent() 
            ? (config.itau().get().ambiente())
            : (config.bradesco().get().ambiente());
        return "sandbox".equalsIgnoreCase(ambiente);
    }

    private String getWebhookSecret() {
        return config.itau().isPresent() 
            ? config.itau().get().clientSecret().orElse(null) 
            : config.bradesco().get().clientSecret().orElse(null);
    }

    private String generateHmacSha256(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar HMAC", e);
        }
    }

    private PixChargeResponse parseChargeResponse(Map<String, Object> data) {
        PixChargeResponse response = new PixChargeResponse();
        response.txid = (String) data.get("txid");
        Object loc = data.get("loc");
        if (loc instanceof Map<?, ?> locMap && locMap.get("id") != null) {
            response.locationId = locMap.get("id").toString();
        }
        response.qrCode = (String) data.get("qrcode");
        response.qrCodeBase64 = (String) data.get("qrcode_base64");
        response.pixCopiaECola = (String) data.get("pixCopiaECola");
        response.status = (String) data.get("status");
        response.valor = new BigDecimal(((Map) data.get("valor")).get("original").toString());
        response.chavePix = (String) data.get("chave");
        
        String criadoEmStr = (String) data.get("criadoEm");
        if (criadoEmStr != null) {
            response.criadoEm = LocalDateTime.parse(criadoEmStr, DateTimeFormatter.ISO_DATE_TIME);
        }
        
        String expiracaoStr = (String) data.get("expiracao");
        if (expiracaoStr != null) {
            response.expiracao = LocalDateTime.parse(expiracaoStr, DateTimeFormatter.ISO_DATE_TIME);
        }

        String atualizadoEmStr = (String) data.get("atualizadoEm");
        if (atualizadoEmStr != null) {
            response.atualizadoEm = LocalDateTime.parse(atualizadoEmStr, DateTimeFormatter.ISO_DATE_TIME);
        }

        return response;
    }
}