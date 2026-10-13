package br.com.sistematenda.common.dto;

import br.com.sistematenda.common.entity.Cliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClienteDTO extends BaseDTO {

    private String nome;
    private String email;
    private String telefone;
    private String cpfCnpj;
    private TipoPessoa tipoPessoa; // FISICA or JURIDICA
    private String tipoCliente; // COMUM, VIP, ATACADISTA
    private StatusCliente status; // ATIVO, INATIVO, Bloqueado
    private BigDecimal limiteCredito;
    private Integer pontosFidelidade;
    private LocalDate dataNascimento;
    private String rgIe;
    private String inscricaoEstadual;
    private String endereco;
    private String numero;
    private String bairro;
    private String cidade;
    private String cep;
    private String nomeFantasia;
    private String observacoes;
    private String contatoComercial;
    private String emailComercial;

    @Data
    public enum TipoPessoa {
        FISICA("Física"),
        JURIDICA("Jurídica");

        private final String descricao;
        TipoPessoa(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Data
    public enum StatusCliente {
        ATIVO("Ativo"),
        INATIVO("Inativo"),
        BLOQUEADO("Bloqueado");

        private final String descricao;
        StatusCliente(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    // Helper methods
    public boolean isFisica() {
        return this.tipoPessoa != null && this.tipoPessoa.name().equals("FISICA");
    }

    public boolean isJuridica() {
        return this.tipoPessoa != null && this.tipoPessoa.name().equals("JURIDICA");
    }

    public boolean isAtivo() {
        return this.status != null && this.status.name().equals("ATIVO");
    }

    public boolean isInativo() {
        return this.status != null && this.status.name().equals("INATIVO");
    }
}