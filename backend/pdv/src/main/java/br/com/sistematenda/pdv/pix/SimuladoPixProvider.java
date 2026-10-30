package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.config.PixConfig;
import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.enums.PixStatus;
import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.common.pix.PixException;
import br.com.sistematenda.common.pix.PixProviderService;
import br.com.sistematenda.common.pix.PixWebhookNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provedor PIX de desenvolvimento.
 *
 * <p>Não conversa com nenhum PSP: gera um BR Code (PIX Copia e Cola) local e
 * mantém o estado das cobranças em memória. É útil para testar o fluxo do PDV
 * (QR Code, polling, confirmação e baixa da venda) sem credenciais reais.</p>
 *
 * <p>As cobranças são confirmadas automaticamente após
 * {@code pix.simulado-confirmar-segundos} (padrão 15s). Com o valor 0, a
 * confirmação acontece apenas via webhook manual em
 * {@code POST /api/pix/webhook/simulado}.</p>
 *
 * <p>NÃO use em produção: nenhum valor é efetivamente cobrado.</p>
 */
@ApplicationScoped
public class SimuladoPixProvider implements PixProviderService {

    private static final Logger LOG = Logger.getLogger(SimuladoPixProvider.class);

    private static final String CHAVE_PADRAO = "simulado@sistematenda.local";
    private static final String NOME_RECEBEDOR = "SISTEMA TENDA";
    private static final String CIDADE_RECEBEDOR = "LAJEADO";
    private static final int MAX_TXID_BRCODE = 25;

    @Inject
    PixConfig config;

    private final Map<String, CobrancaSimulada> cobrancas = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getProviderName() {
        return "Simulado";
    }

    @Override
    public PixProvider getProviderType() {
        return PixProvider.SIMULADO;
    }

    @Override
    public PixChargeResponse createCharge(PixChargeRequest request) throws PixException {
        if (request == null || request.valor == null || request.valor.signum() <= 0) {
            throw new PixException("SIMULADO_VALOR_INVALIDO", "Valor da cobrança inválido", 400);
        }

        String txid = request.txid != null && !request.txid.isBlank()
                ? request.txid
                : "SIM" + UUID.randomUUID().toString().replace("-", "").substring(0, 22).toUpperCase();

        LocalDateTime criadoEm = LocalDateTime.now();
        int expiracaoSegundos = request.expiracaoSegundos != null
                ? request.expiracaoSegundos
                : config.expiracaoPadraoSegundos();

        CobrancaSimulada cobranca = new CobrancaSimulada();
        cobranca.txid = txid;
        cobranca.valor = request.valor.setScale(2, RoundingMode.HALF_UP);
        cobranca.chave = request.chavePix != null && !request.chavePix.isBlank()
                ? request.chavePix
                : config.chavePix().orElse(CHAVE_PADRAO);
        cobranca.solicitacaoPagador = request.solicitacaoPagador;
        cobranca.criadoEm = criadoEm;
        cobranca.expiracao = criadoEm.plusSeconds(expiracaoSegundos);
        cobranca.status = PixStatus.ATIVA.name();

        int confirmarSegundos = config.simuladoConfirmarSegundos();
        if (confirmarSegundos > 0) {
            cobranca.confirmarEm = criadoEm.plusSeconds(confirmarSegundos);
        }

        cobrancas.put(txid, cobranca);
        LOG.warnf("PIX SIMULADO gerado (sem cobrança real): txid=%s valor=%s", txid, cobranca.valor);

        return toResponse(cobranca);
    }

    @Override
    public PixChargeResponse getCharge(String txid) throws PixException {
        CobrancaSimulada cobranca = cobrancas.get(txid);
        if (cobranca == null) {
            throw new PixException("SIMULADO_NOT_FOUND", "Cobrança simulada não encontrada: " + txid, 404);
        }

        if (PixStatus.ATIVA.name().equals(cobranca.status)
                && cobranca.confirmarEm != null
                && !LocalDateTime.now().isBefore(cobranca.confirmarEm)) {
            cobranca.status = PixStatus.CONCLUIDA.name();
            cobranca.endToEndId = "E" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
            cobranca.dataPagamento = LocalDateTime.now();
        }

        return toResponse(cobranca);
    }

    @Override
    public void cancelCharge(String txid) throws PixException {
        CobrancaSimulada cobranca = cobrancas.get(txid);
        if (cobranca == null) {
            throw new PixException("SIMULADO_NOT_FOUND", "Cobrança simulada não encontrada: " + txid, 404);
        }
        cobranca.status = PixStatus.REMOVIDA_PELO_USUARIO_RECEBEDOR.name();
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String timestamp) {
        return true;
    }

    @Override
    public PixWebhookNotification parseWebhook(String payload, String signature, String timestamp) throws PixException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            PixWebhookNotification notification = new PixWebhookNotification();
            notification.txid = data.get("txid") != null ? data.get("txid").toString() : null;
            notification.status = data.get("status") != null ? data.get("status").toString() : "CONCLUIDA";
            notification.endToEndId = data.get("endToEndId") != null
                    ? data.get("endToEndId").toString()
                    : "E" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
            if (data.get("valor") != null) {
                notification.valor = new BigDecimal(data.get("valor").toString());
            }
            notification.rawPayload = data;

            if (notification.txid == null) {
                throw new PixException("SIMULADO_WEBHOOK_INVALIDO", "Campo 'txid' é obrigatório", 400);
            }

            // Mantém o estado em memória coerente com a confirmação manual.
            CobrancaSimulada cobranca = cobrancas.get(notification.txid);
            if (cobranca != null) {
                cobranca.status = notification.getStatusEnum().name();
                cobranca.endToEndId = notification.endToEndId;
                if (notification.getStatusEnum() == PixStatus.CONCLUIDA) {
                    cobranca.dataPagamento = LocalDateTime.now();
                }
            }

            return notification;
        } catch (PixException e) {
            throw e;
        } catch (Exception e) {
            throw new PixException("SIMULADO_WEBHOOK_PARSE_ERROR", e.getMessage(), 400, e);
        }
    }

    @Override
    public boolean supportsWebhook() {
        return true;
    }

    @Override
    public String getWebhookUrl() {
        return config.webhookBaseUrl().orElse("") + "/api/pix/webhook/simulado";
    }

    @Override
    public boolean isSandbox() {
        return true;
    }

    private PixChargeResponse toResponse(CobrancaSimulada cobranca) {
        PixChargeResponse response = new PixChargeResponse();
        response.txid = cobranca.txid;
        response.status = cobranca.status;
        response.valor = cobranca.valor;
        response.chavePix = cobranca.chave;
        response.criadoEm = cobranca.criadoEm;
        response.expiracao = cobranca.expiracao;
        response.pixCopiaECola = buildBrCode(cobranca);
        response.qrCode = response.pixCopiaECola;
        return response;
    }

    private String buildBrCode(CobrancaSimulada cobranca) {
        StringBuilder payload = new StringBuilder();
        appendField(payload, "00", "01");

        StringBuilder merchantAccount = new StringBuilder();
        appendField(merchantAccount, "00", "br.gov.bcb.pix");
        appendField(merchantAccount, "01", cobranca.chave);
        if (cobranca.solicitacaoPagador != null && !cobranca.solicitacaoPagador.isBlank()) {
            appendField(merchantAccount, "02", truncate(cobranca.solicitacaoPagador, 72));
        }
        appendField(payload, "26", merchantAccount.toString());

        appendField(payload, "52", "0000");
        appendField(payload, "53", "986");
        appendField(payload, "54", cobranca.valor.toPlainString());
        appendField(payload, "58", "BR");
        appendField(payload, "59", truncate(NOME_RECEBEDOR, 25));
        appendField(payload, "60", truncate(CIDADE_RECEBEDOR, 15));

        StringBuilder additionalData = new StringBuilder();
        appendField(additionalData, "05", truncate(cobranca.txid, MAX_TXID_BRCODE));
        appendField(payload, "62", additionalData.toString());

        payload.append("6304");
        payload.append(crc16(payload.toString()));
        return payload.toString();
    }

    private static void appendField(StringBuilder target, String id, String value) {
        String safeValue = value != null ? value : "";
        target.append(id)
                .append(String.format("%02d", safeValue.length()))
                .append(safeValue);
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String crc16(String payload) {
        int crc = 0xFFFF;
        for (int i = 0; i < payload.length(); i++) {
            crc ^= (payload.charAt(i) << 8) & 0xFFFF;
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return String.format("%04X", crc);
    }

    private static final class CobrancaSimulada {
        String txid;
        String chave;
        String solicitacaoPagador;
        BigDecimal valor;
        String status;
        LocalDateTime criadoEm;
        LocalDateTime expiracao;
        LocalDateTime confirmarEm;
        LocalDateTime dataPagamento;
        String endToEndId;
    }
}
