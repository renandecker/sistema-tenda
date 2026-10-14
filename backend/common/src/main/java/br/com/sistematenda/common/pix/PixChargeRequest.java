package br.com.sistematenda.common.pix;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class PixChargeRequest {

    @NotNull
    @Positive
    public BigDecimal valor;

    public String chavePix;

    public String txid;

    public Integer expiracaoSegundos = 3600;

    public PixPagador pagador;

    public String solicitacaoPagador;

    public PixInfoAdicional[] infoAdicionais;

    public Map<String, String> metadados;

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