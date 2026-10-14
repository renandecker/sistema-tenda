package br.com.sistematenda.common.pix;

import br.com.sistematenda.common.enums.PixStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PixChargeResponse {

    public String txid;
    public String locationId;
    public String qrCode;
    public String qrCodeBase64;
    public String pixCopiaECola;
    public String status;
    public BigDecimal valor;
    public String chavePix;
    public LocalDateTime criadoEm;
    public LocalDateTime expiracao;
    public LocalDateTime atualizadoEm;
    public PixPagador pagador;
    public PixInfoAdicional[] infoAdicionais;

    public PixStatus getStatusEnum() {
        if (status == null) return PixStatus.ATIVA;
        try {
            return PixStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (status.toUpperCase()) {
                case "ACTIVE" -> PixStatus.ATIVA;
                case "CONCLUDED", "COMPLETED", "PAID" -> PixStatus.CONCLUIDA;
                case "REMOVED_BY_RECEIVER", "CANCELLED" -> PixStatus.REMOVIDA_PELO_USUARIO_RECEBEDOR;
                case "EXPIRED" -> PixStatus.EXPIRADA;
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

    public static class PixInfoAdicional {
        public String nome;
        public String valor;
    }
}