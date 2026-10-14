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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class AsaasPixProvider implements PixProviderService {

    private static final Logger LOG = Logger.getLogger(AsaasPixProvider.class);

    @Inject
    PixConfig config;

    private Client client;
    private String baseUrl;

    private Client getClient() {
        if (client == null) {
            PixConfig.AsaasConfig asaasConfig = config.asaas().orElseThrow(
                () -> new PixException("ASAAS_CONFIG_MISSING", "Configuração do Asaas não encontrada", 500)
            );

            baseUrl = config.sandbox() 
                ? "https://sandbox.asaas.com/api/v3" 
                : "https://api.asaas.com/v3";

            client = ClientBuilder.newBuilder().build();
        }
        return client;
    }

    private String getApiKey() {
        return config.asaas().orElseThrow(
            () -> new PixException("ASAAS_CONFIG_MISSING", "Configuração do Asaas não encontrada", 500)
        ).apiKey().orElse(null);
    }

    @Override
    public String getProviderName() {
        return "Asaas";
    }

    @Override
    public PixProvider getProviderType() {
        return PixProvider.ASAAS;
    }

    @Override
    public PixChargeResponse createCharge(PixChargeRequest request) throws PixException {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("value", request.valor.toPlainString());
            body.put("billingType", "PIX");
            body.put("dueDate", LocalDateTime.now().plusSeconds(
                request.expiracaoSegundos != null ? request.expiracaoSegundos : config.expiracaoPadraoSegundos()
            ).toLocalDate().toString());

            if (request.txid != null && !request.txid.isEmpty()) {
                body.put("externalReference", request.txid);
            }

            if (request.solicitacaoPagador != null) {
                body.put("description", request.solicitacaoPagador);
            }

            if (request.pagador != null) {
                Map<String, Object> customer = new HashMap<>();
                if (request.pagador.cpf != null) customer.put("cpfCnpj", request.pagador.cpf);
                if (request.pagador.cnpj != null) customer.put("cpfCnpj", request.pagador.cnpj);
                if (request.pagador.nome != null) customer.put("name", request.pagador.nome);
                if (request.pagador.email != null) customer.put("email", request.pagador.email);
                if (request.pagador.telefone != null) customer.put("phone", request.pagador.telefone);
                if (request.pagador.endereco != null) {
                    customer.put("address", request.pagador.endereco.logradouro);
                    customer.put("addressNumber", request.pagador.endereco.numero);
                    customer.put("complement", request.pagador.endereco.complemento);
                    customer.put("province", request.pagador.endereco.bairro);
                    customer.put("city", request.pagador.endereco.cidade);
                    customer.put("state", request.pagador.endereco.uf);
                    customer.put("postalCode", request.pagador.endereco.cep);
                }
                body.put("customer", customer);
            }

            Response response = getClient()
                .target(baseUrl + "/payments")
                .request(MediaType.APPLICATION_JSON)
                .header("access_token", getApiKey())
                .post(Entity.json(body));

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("ASAAS_API_ERROR", error, response.getStatus());
            }

            Map<String, Object> data = response.readEntity(Map.class);
            return parsePaymentResponse(data);

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao criar cobrança PIX no Asaas", e);
            throw new PixException("ASAAS_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public PixChargeResponse getCharge(String txid) throws PixException {
        try {
            Response response = getClient()
                .target(baseUrl + "/payments/" + txid)
                .request(MediaType.APPLICATION_JSON)
                .header("access_token", getApiKey())
                .get();

            if (response.getStatus() == 404) {
                throw new PixException("ASAAS_NOT_FOUND", "Cobrança não encontrada", 404);
            }

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("ASAAS_API_ERROR", error, response.getStatus());
            }

            Map<String, Object> data = response.readEntity(Map.class);
            return parsePaymentResponse(data);

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao consultar cobrança PIX no Asaas: " + txid, e);
            throw new PixException("ASAAS_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public void cancelCharge(String txid) throws PixException {
        try {
            Map<String, Object> body = Map.of("status", "REMOVIDO_PELO_USUARIO_RECEBEDOR");
            
            Response response = getClient()
                .target(baseUrl + "/payments/" + txid + "/status")
                .request(MediaType.APPLICATION_JSON)
                .header("access_token", getApiKey())
                .post(Entity.json(body));

            if (response.getStatus() >= 400) {
                String error = response.readEntity(String.class);
                throw new PixException("ASAAS_API_ERROR", error, response.getStatus());
            }

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao cancelar cobrança PIX no Asaas: " + txid, e);
            throw new PixException("ASAAS_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String timestamp) throws PixException {
        try {
            PixConfig.AsaasConfig asaasConfig = config.asaas().orElseThrow(
                () -> new PixException("ASAAS_CONFIG_MISSING", "Configuração do Asaas não encontrada", 500)
            );

            if (asaasConfig.webhookToken().orElse(null) == null || asaasConfig.webhookToken().orElse(null).isEmpty()) {
                LOG.warn("Webhook token não configurado para Asaas");
                return true;
            }

            return asaasConfig.webhookToken().orElse(null).equals(signature);

        } catch (Exception e) {
            LOG.error("Erro ao verificar assinatura do webhook Asaas", e);
            throw new PixException("ASAAS_WEBHOOK_VERIFY_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public PixWebhookNotification parseWebhook(String payload, String signature, String timestamp) throws PixException {
        try {
            if (!verifyWebhookSignature(payload, signature, timestamp)) {
                throw new PixException("ASAAS_WEBHOOK_INVALID_SIGNATURE", "Assinatura do webhook inválida", 401);
            }

            PixWebhookNotification notification = new PixWebhookNotification();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Map<String, Object> data = mapper.readValue(payload, Map.class);
            
            String event = (String) data.get("event");
            Map<String, Object> payment = (Map<String, Object>) data.get("payment");

            if (payment != null) {
                notification.txid = payment.get("id").toString();
                notification.endToEndId = (String) payment.get("pixId");
                notification.status = (String) payment.get("status");
                notification.valor = new BigDecimal(payment.get("value").toString());
                
                String dateCreated = (String) payment.get("dateCreated");
                if (dateCreated != null) {
                    notification.criadoEm = LocalDateTime.parse(dateCreated, DateTimeFormatter.ISO_DATE_TIME);
                }
                
                String dateConfirmation = (String) payment.get("dateConfirmation");
                if (dateConfirmation != null) {
                    notification.atualizadoEm = LocalDateTime.parse(dateConfirmation, DateTimeFormatter.ISO_DATE_TIME);
                }

                Map<String, Object> customer = (Map<String, Object>) payment.get("customer");
                if (customer != null) {
                    notification.pagador = new PixWebhookNotification.PixPagador();
                    notification.pagador.nome = (String) customer.get("name");
                    notification.pagador.email = (String) customer.get("email");
                    notification.pagador.cpf = (String) customer.get("cpfCnpj");
                    notification.pagador.telefone = (String) customer.get("phone");
                }
            }

            notification.rawPayload = data;
            return notification;

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao processar webhook Asaas", e);
            throw new PixException("ASAAS_WEBHOOK_PARSE_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public boolean supportsWebhook() {
        return true;
    }

    @Override
    public String getWebhookUrl() {
        return config.webhookBaseUrl().orElse("") + "/api/pix/webhook/asaas";
    }

    @Override
    public boolean isSandbox() {
        return config.sandbox();
    }

    private PixChargeResponse parsePaymentResponse(Map<String, Object> data) {
        PixChargeResponse response = new PixChargeResponse();
        response.txid = data.get("id").toString();
        response.status = (String) data.get("status");
        response.valor = new BigDecimal(data.get("value").toString());
        response.pixCopiaECola = (String) data.get("pixQrCode");
        response.qrCodeBase64 = (String) data.get("pixQrCodeBase64");
        response.chavePix = (String) data.get("pixKey");
        
        String dateCreated = (String) data.get("dateCreated");
        if (dateCreated != null) {
            response.criadoEm = LocalDateTime.parse(dateCreated, DateTimeFormatter.ISO_DATE_TIME);
        }
        
        String dueDate = (String) data.get("dueDate");
        if (dueDate != null) {
            response.expiracao = LocalDateTime.parse(dueDate + "T23:59:59");
        }

        return response;
    }
}