package br.com.sistematenda.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "cadastro_fornecedores", indexes = {
    @Index(name = "idx_fornecedor_email", columnList = "email", unique = true),
    @Index(name = "idx_fornecedor_cnpj", columnList = "cnpj_cpf"),
    @Index(name = "idx_fornecedor_ativo", columnList = "ativo")
})
public class Fornecedor extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String nome;

    @Column(name = "razao_social", length = 200)
    @Size(max = 200)
    public String razaoSocial;

    @Column(name = "email", length = 100, unique = true)
    @Size(max = 100)
    public String email;

    @Column(name = "telefone", length = 20)
    @Size(max = 20)
    public String telefone;

    @Column(name = "cnpj_cpf", nullable = false, length = 18)
    @NotNull
    @Size(max = 18)
    public String cnpjCpf;

    @Column(name = "inscricao_estadual", length = 20)
    @Size(max = 20)
    public String inscricaoEstadual;

    @Column(name = "inscricao_municipal", length = 20)
    @Size(max = 20)
    public String inscricaoMunicipal;

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

    @Column(name = "contato", length = 100)
    @Size(max = 100)
    public String contato;

    @Column(name = "email_contato", length = 100)
    @Size(max = 100)
    public String emailContato;

    @Column(name = "telefone_contato", length = 20)
    @Size(max = 20)
    public String telefoneContato;

    @Column(name = "tipo", length = 50)
    @Size(max = 50)
    public String tipo; // EX: Distribuidor, Manufacturer, Atacadista

    @Column(name = "segmento_mercado", length = 100)
    @Size(max = 100)
    public String segmentoMercado;

    @Column(name = "prazo_entrega_dias")
    @DecimalMin("0")
    public Integer prazoEntregaDias = 30;

    @Column(name = "status", length = 20)
    @Size(max = 20)
    public String status = "ATIVO";

    @Column(name = "observacoes", columnDefinition = "TEXT")
    public String observacoes;

    @Column(name = "limite_compra", precision = 15, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal limiteCompra = BigDecimal.valueOf(10000);

    @Column(name = "total_compras", precision = 15, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal totalCompras = BigDecimal.ZERO;

    @Column(name = "ultima_compra")
    public LocalDate ultimaCompra;

    @Column(name = "codigo_ncm", length = 8)
    @Size(min = 8, max = 8)
    public String codigoNcm; // Tabela NCM do produto

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_icms", length = 3)
    public CstIcms cstIcms;

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_pis", length = 2)
    public CstPisCofins cstPis;

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_cofins", length = 2)
    public CstPisCofins cstCofins;

    @Column(name = "situacao_tributaria", length = 50)
    @Size(max = 50)
    public String situacaoTributaria; // Simples Nacional, Lucro Presumido, Lucro Real

    @Column(name = "aliquota_icms", precision = 5, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal aliquotaIcms = BigDecimal.ZERO;

    @Column(name = "aliquota_pis", precision = 5, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal aliquotaPis = BigDecimal.ZERO;

    @Column(name = "aliquota_cofins", precision = 5, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal aliquotaCofins = BigDecimal.ZERO;

    @Column(name = "percentual_desconto", precision = 5, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal percentualDesconto = BigDecimal.ZERO;

    @Column(name = "condicoes_pagamento", length = 100)
    @Size(max = 100)
    public String condicoesPagamento; // Ex: "30 dias", "Na entrega", etc.

    @Column(name = "status_entrega", length = 30)
    @Size(max = 30)
    public String statusEntrega = "PENDENTE"; // PENDENTE, ENTREGUE, ATRASADO

    @Column(name = "pontos_parceria", precision = 15, scale = 0)
    @DecimalMin("0")
    public Integer pontosParceria = 0;

    @PrePersist
    @PreUpdate
    void validarCnpjCpf() {
        if (cnpjCpf != null && cnpjCpf.length() >= 11) {
            String nums = cnpjCpf.replaceAll("[^0-9]", "");
            if (nums.length() == 11) {
                // CPF
                if (!isValidCPF(nums)) {
                    throw new RuntimeException("CPF inválido");
                }
            } else if (nums.length() == 14) {
                // CNPJ
                if (!isValidCNPJ(nums)) {
                    throw new RuntimeException("CNPJ inválido");
                }
            } else {
                throw new RuntimeException("CPF/CNPJ deve ter 11 ou 14 dígitos");
            }
        }
    }

    private boolean isValidCPF(String cpf) {
        if (cpf.length() != 11) return false;
        String primeiroDigito = cpf.substring(0, 1);
        boolean todosIguais = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != primeiroDigito.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;
        
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
        
        String primeiroDigito = cnpj.substring(0, 1);
        boolean todosIguais = true;
        for (int i = 1; i < cnpj.length(); i++) {
            if (cnpj.charAt(i) != primeiroDigito.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;
        
        int pesos1[] = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Integer.parseInt(cnpj.substring(i, i + 1)) * pesos1[i];
        }
        int resto = soma % 11;
        int digitoVerificador1 = (resto < 2) ? 0 : 11 - resto;
        
        if (Integer.parseInt(cnpj.substring(12, 13)) != digitoVerificador1) return false;
        
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