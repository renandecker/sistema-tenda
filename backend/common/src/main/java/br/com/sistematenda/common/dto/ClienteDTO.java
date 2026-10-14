package br.com.sistematenda.common.dto;

import br.com.sistematenda.common.entity.Cliente;
import br.com.sistematenda.common.enums.TipoPessoa;
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
    private String status; // ATIVO, INATIVO, BLOQUEADO
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

    // Helper methods
    public boolean isFisica() {
        return this.tipoPessoa == TipoPessoa.FISICA;
    }

    public boolean isJuridica() {
        return this.tipoPessoa == TipoPessoa.JURIDICA;
    }

    public boolean isAtivo() {
        return "ATIVO".equals(this.status);
    }

    public boolean isInativo() {
        return "INATIVO".equals(this.status);
    }
}
