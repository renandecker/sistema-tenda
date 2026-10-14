package br.com.sistematenda.common.pix;

import br.com.sistematenda.common.enums.PixStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class PixWebhookNotification {

    public String txid;
    public String endToEndId;
    public String status;
    public BigDecimal valor;
    public String chavePix;
    public LocalDateTime criadoEm;
    public LocalDateTime atualizadoEm;
    public PixPagador pagador;
    public String requestId;
    public String resourceId;
    public Map<String, Object> rawPayload;

    public PixStatus getStatusEnum() {
        if (status == null) return PixStatus.ATIVA;
        try {
            return PixStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> PixStatus.ATIVA;
                case "CONCLUDED", "COMPLETED", "PAID", "CONCLUIDA" -> PixStatus.CONCLUIDA;
                case "REMOVED_BY_RECEIVER", "CANCELLED", "CANCELADA" -> PixStatus.REMOVIDA_PELO_USUARIO_RECEBEDOR;
                case "EXPIRED", "EXPIRADA" -> PixStatus.EXPIRADA;
                default -> PixStatus.ATIVA;
            };
        }
    }

    public static class PixPagador {
        public String cpf;
        public String cnpj;
        public String nome;
        public String email;
        public String telefone;
        public PixEndereco endereco;
    }

    public static class PixEndereco {
        public String logradouro;
        public String numero;
        public String complemento;
        public String bairro;
        public String cidade;
        public String uf;
        public String cep;
    }
}