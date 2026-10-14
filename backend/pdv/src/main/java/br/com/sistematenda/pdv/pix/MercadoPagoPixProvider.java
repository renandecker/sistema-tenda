package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.config.PixConfig;
import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.enums.PixStatus;
import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.common.pix.PixException;
import br.com.sistematenda.common.pix.PixProviderService;
import br.com.sistematenda.common.pix.PixWebhookNotification;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MercadoPagoPixProvider implements PixProviderService {

    private static final Logger LOG = Logger.getLogger(MercadoPagoPixProvider.class);

    @Inject
    PixConfig config;

    private PaymentClient paymentClient;

    private PaymentClient getClient() {
        if (paymentClient == null) {
            PixConfig.MercadoPagoConfig mpConfig = config.mercadoPago().orElseThrow(
                () -> new PixException("MP_CONFIG_MISSING", "Configuração do Mercado Pago não encontrada", 500)
            );

            MercadoPagoConfig.setAccessToken(mpConfig.accessToken().orElse(null));
            paymentClient = new PaymentClient();
        }
        return paymentClient;
    }

    @Override
    public String getProviderName() {
        return "Mercado Pago";
    }

    @Override
    public PixProvider getProviderType() {
        return PixProvider.MERCADO_PAGO;
    }

    @Override
    public PixChargeResponse createCharge(PixChargeRequest request) throws PixException {
        try {
            PaymentCreateRequest.PaymentCreateRequestBuilder builder = PaymentCreateRequest.builder()
                .transactionAmount(request.valor)
                .description(request.solicitacaoPagador != null ? request.solicitacaoPagador : "Pagamento PIX")
                .paymentMethodId("pix");

            if (request.pagador != null) {
                PaymentPayerRequest payer = PaymentPayerRequest.builder()
                    .email(request.pagador.email != null ? request.pagador.email : "cliente@email.com")
                    .firstName(request.pagador.nome != null ? request.pagador.nome.split(" ")[0] : "Cliente")
                    .lastName(request.pagador.nome != null && request.pagador.nome.contains(" ") 
                        ? request.pagador.nome.substring(request.pagador.nome.indexOf(" ") + 1) : "")
                    .identification(IdentificationRequest.builder()
                        .type(request.pagador.cpf != null ? "CPF" : "CNPJ")
                        .number(request.pagador.cpf != null ? request.pagador.cpf : request.pagador.cnpj)
                        .build())
                    .build();
                builder.payer(payer);
            }

            if (request.expiracaoSegundos != null) {
                builder.dateOfExpiration(OffsetDateTime.now().plusSeconds(request.expiracaoSegundos));
            }

            if (request.txid != null && !request.txid.isEmpty()) {
                builder.externalReference(request.txid);
            }

            if (request.infoAdicionais != null && request.infoAdicionais.length > 0) {
                Map<String, Object> metadata = new HashMap<>();
                for (PixChargeRequest.PixInfoAdicional info : request.infoAdicionais) {
                    metadata.put(info.nome, info.valor);
                }
                builder.metadata(metadata);
            }

            Payment payment = getClient().create(builder.build());
            return parsePaymentResponse(payment);

        } catch (MPApiException e) {
            LOG.error("Erro ao criar cobrança PIX no Mercado Pago", e);
            throw new PixException("MP_API_ERROR", e.getMessage(), e.getStatusCode(), e);
        } catch (MPException e) {
            LOG.error("Erro de conexão com Mercado Pago", e);
            throw new PixException("MP_CONNECTION_ERROR", e.getMessage(), 500, e);
        } catch (Exception e) {
            LOG.error("Erro inesperado ao criar cobrança PIX no Mercado Pago", e);
            throw new PixException("MP_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public PixChargeResponse getCharge(String txid) throws PixException {
        try {
            Payment payment = getClient().get(Long.valueOf(txid));
            return parsePaymentResponse(payment);
        } catch (MPApiException e) {
            LOG.error("Erro ao consultar cobrança PIX no Mercado Pago: " + txid, e);
            throw new PixException("MP_API_ERROR", e.getMessage(), e.getStatusCode(), e);
        } catch (Exception e) {
            LOG.error("Erro inesperado ao consultar cobrança PIX no Mercado Pago: " + txid, e);
            throw new PixException("MP_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public void cancelCharge(String txid) throws PixException {
        try {
            Payment payment = getClient().get(Long.valueOf(txid));
            if (payment.getStatus().equals("pending") || payment.getStatus().equals("approved")) {
                getClient().cancel(Long.valueOf(txid));
            }
        } catch (MPApiException e) {
            LOG.error("Erro ao cancelar cobrança PIX no Mercado Pago: " + txid, e);
            throw new PixException("MP_API_ERROR", e.getMessage(), e.getStatusCode(), e);
        } catch (Exception e) {
            LOG.error("Erro inesperado ao cancelar cobrança PIX no Mercado Pago: " + txid, e);
            throw new PixException("MP_UNEXPECTED_ERROR", e.getMessage(), 500, e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String timestamp) throws PixException {
        try {
            PixConfig.MercadoPagoConfig mpConfig = config.mercadoPago().orElseThrow(
                () -> new PixException("MP_CONFIG_MISSING", "Configuração do Mercado Pago não encontrada", 500)
            );

            if (mpConfig.webhookSecret().orElse(null) == null || mpConfig.webhookSecret().orElse(null).isEmpty()) {
                LOG.warn("Webhook secret não configurado para Mercado Pago");
                return true;
            }

            String expectedSignature = generateHmacSha256(payload, mpConfig.webhookSecret().orElse(null));
            return expectedSignature.equals(signature);

        } catch (Exception e) {
            LOG.error("Erro ao verificar assinatura do webhook Mercado Pago", e);
            throw new PixException("MP_WEBHOOK_VERIFY_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public PixWebhookNotification parseWebhook(String payload, String signature, String timestamp) throws PixException {
        try {
            if (!verifyWebhookSignature(payload, signature, timestamp)) {
                throw new PixException("MP_WEBHOOK_INVALID_SIGNATURE", "Assinatura do webhook inválida", 401);
            }

            PixWebhookNotification notification = new PixWebhookNotification();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            Map<String, Object> data = mapper.readValue(payload, Map.class);
            
            String type = (String) data.get("type");
            Map<String, Object> resource = (Map<String, Object>) data.get("data");

            if (resource != null) {
                notification.txid = resource.get("id").toString();
                notification.endToEndId = (String) resource.get("payment_method_id");
                notification.status = (String) resource.get("status");
                notification.valor = new BigDecimal(resource.get("transaction_amount").toString());
                
                String dateCreated = (String) resource.get("date_created");
                if (dateCreated != null) {
                    notification.criadoEm = LocalDateTime.parse(dateCreated, DateTimeFormatter.ISO_DATE_TIME);
                }
                
                String dateApproved = (String) resource.get("date_approved");
                if (dateApproved != null) {
                    notification.atualizadoEm = LocalDateTime.parse(dateApproved, DateTimeFormatter.ISO_DATE_TIME);
                }

                Map<String, Object> payer = (Map<String, Object>) resource.get("payer");
                if (payer != null) {
                    notification.pagador = new PixWebhookNotification.PixPagador();
                    notification.pagador.email = (String) payer.get("email");
                    notification.pagador.nome = (String) payer.get("first_name") + " " + payer.get("last_name");
                    
                    Map<String, Object> identification = (Map<String, Object>) payer.get("identification");
                    if (identification != null) {
                        notification.pagador.cpf = (String) identification.get("number");
                    }
                }
            }

            notification.rawPayload = data;
            return notification;

        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Erro ao processar webhook Mercado Pago", e);
            throw new PixException("MP_WEBHOOK_PARSE_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public boolean supportsWebhook() {
        return true;
    }

    @Override
    public String getWebhookUrl() {
        return config.webhookBaseUrl().orElse("") + "/api/pix/webhook/mercadopago";
    }

    @Override
    public boolean isSandbox() {
        return config.sandbox();
    }

    private PixChargeResponse parsePaymentResponse(Payment payment) {
        PixChargeResponse response = new PixChargeResponse();
        response.txid = payment.getId().toString();
        response.status = payment.getStatus();
        response.valor = payment.getTransactionAmount();
        if (payment.getPointOfInteraction() != null && payment.getPointOfInteraction().getTransactionData() != null) {
            response.pixCopiaECola = payment.getPointOfInteraction().getTransactionData().getQrCode();
            response.qrCodeBase64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
        }
        
        if (payment.getDateCreated() != null) {
            response.criadoEm = LocalDateTime.parse(payment.getDateCreated().toString(), DateTimeFormatter.ISO_DATE_TIME);
        }
        
        if (payment.getDateOfExpiration() != null) {
            response.expiracao = LocalDateTime.parse(payment.getDateOfExpiration().toString(), DateTimeFormatter.ISO_DATE_TIME);
        }

        return response;
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
}