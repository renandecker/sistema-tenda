package br.com.sistematenda.common.entity;

import br.com.sistematenda.common.enums.TipoPessoa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "cadastro_clientes", indexes = {
    @Index(name = "idx_cliente_email", columnList = "email", unique = true),
    @Index(name = "idx_cliente_tipo", columnList = "tipo_pessoa"),
    @Index(name = "idx_cliente_ativo", columnList = "status")
})
public class Cliente extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String nome;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    @NotNull
    @Size(max = 100)
    @Email
    public String email;

    @Column(name = "telefone", length = 20)
    @Size(max = 20)
    public String telefone;

    @Column(name = "cpf_cnpj", nullable = false, length = 18)
    @NotNull
    @Size(max = 18)
    public String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", length = 20)
    @NotNull
    public TipoPessoa tipoPessoa = TipoPessoa.FISICA;

    @Column(name = "rg_ie", length = 20)
    @Size(max = 20)
    public String rgIe;

    @Column(name = "inscricao_estadual", length = 20)
    @Size(max = 20)
    public String inscricaoEstadual;

    @Column(name = "endereco", length = 500)
    @Size(max = 500)
    public String endereco;

    @Column(name = "numero", length = 10)
    @Size(max = 10)
    public String numero;

    @Column(name = "bairro", length = 100)
    @Size(max = 100)
    public String bairro;

    @Column(name = "cidade", length = 100)
    @Size(max = 100)
    public String cidade;

    @Column(name = "cep", length = 9)
    @Size(max = 9)
    public String cep;

    @Column(name = "data_nascimento")
    public LocalDate dataNascimento;

    @Column(name = "rg")
    @Size(max = 20)
    public String rg;

    @Column(name = "orgao_emissor", length = 50)
    @Size(max = 50)
    public String orgaoEmissor;

    @Column(name = "nome_fantasia", length = 200)
    @Size(max = 200)
    public String nomeFantasia;

    @Column(name = "status", length = 20)
    @Size(max = 20)
    public String status = "ATIVO";

    @Column(name = "limite_credito", precision = 15, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "pontos_fidelidade", precision = 15, scale = 0)
    @DecimalMin("0")
    public Integer pontosFidelidade = 0;

    @Column(name = "ultima_compra")
    public LocalDate ultimaCompra;

    @Column(name = "tipo_cliente", length = 50)
    @Size(max = 50)
    public String tipoCliente = "COMUM"; // COMUM, VIP, ATACADISTA

    @Column(name = "observacoes", columnDefinition = "TEXT")
    public String observacoes;

    @Column(name = "contato_comercial", length = 100)
    @Size(max = 100)
    public String contatoComercial;

    @Column(name = "email_comercial", length = 100)
    @Size(max = 100)
    public String emailComercial;

    @PrePersist
    @PreUpdate
    protected void validarCPF_CNPJ() {
        // Validação simplificada - em produção usaria uma biblioteca específica
        if (cpfCnpj != null && cpfCnpj.length() >= 11) {
            // Remover caracteres não numéricos
            String nums = cpfCnpj.replaceAll("[^0-9]", "");
            
            if (nums.length() == 11) {
                // Validar CPF
                if (isValidCPF(nums)) {
                    // CPF válido
                }
            } else if (nums.length() == 14) {
                // Validar CNPJ
                if (isValidCNPJ(nums)) {
                    // CNPJ válido
                }
            }
        }
    }

    private boolean isValidCPF(String cpf) {
        // Validação básica de CPF
        if (cpf.length() != 11) return false;
        
        // Todos os dígitos iguais são inválidos
        String primeiroDigito = cpf.substring(0, 1);
        boolean todosIguais = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != primeiroDigito.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;
        
        // Calcular dígitos verificadores
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Integer.parseInt(cpf.substring(i, i + 1)) * (10 - i);
        }
        int resto = soma % 11;
        int digitoVerificador1 = (resto < 2) ? 0 : 11 - resto;
        
        if (Integer.parseInt(cpf.substring(9, 10)) != digitoVerificador1) return false;
        
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Integer.parseInt(cpf.substring(i, i + 1)) * (11 - i);
        }
        resto = soma % 11;
        int digitoVerificador2 = (resto < 2) ? 0 : 11 - resto;
        
        return Integer.parseInt(cpf.substring(10, 11)) == digitoVerificador2;
    }

    private boolean isValidCNPJ(String cnpj) {
        if (cnpj.length() != 14) return false;
        
        // Todos os dígitos iguais são inválidos
        String primeiroDigito = cnpj.substring(0, 1);
        boolean todosIguais = true;
        for (int i = 1; i < cnpj.length(); i++) {
            if (cnpj.charAt(i) != primeiroDigito.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;
        
        // Primeiro dígito verificador
        int pesos1[] = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Integer.parseInt(cnpj.substring(i, i + 1)) * pesos1[i];
        }
        int resto = soma % 11;
        int digitoVerificador1 = (resto < 2) ? 0 : 11 - resto;
        
        if (Integer.parseInt(cnpj.substring(12, 13)) != digitoVerificador1) return false;
        
        // Segundo dígito verificador
        int pesos2[] = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Integer.parseInt(cnpj.substring(i, i + 1)) * pesos2[i];
        }
        resto = soma % 11;
        int digitoVerificador2 = (resto < 2) ? 0 : 11 - resto;
        
        return Integer.parseInt(cnpj.substring(13, 14)) == digitoVerificador2;
    }
}