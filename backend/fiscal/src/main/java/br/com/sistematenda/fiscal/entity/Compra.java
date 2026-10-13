package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "fiscal_compra", indexes = {
    @Index(name = "idx_fiscal_compra_fornecedor", columnList = "fornecedor_id"),
    @Index(name = "idx_fiscal_compra_data", columnList = "data_compra"),
    @Index(name = "idx_fiscal_compra_status", columnList = "status")
})
public class Compra extends BaseEntity {

    public enum StatusCompra {
        ORCAMENTO("Orçamento"),
        CONFIRMADA("Confirmada"),
        RECEBIDA("Recebida"),
        CANCELADA("Cancelada");

        private final String descricao;
        StatusCompra(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    public Fornecedor fornecedor;

    @Column(name = "numero_nota", length = 50)
    @Size(max = 50)
    public String numeroNota;

    @Column(name = "serie_nota")
    public Integer serieNota;

    @Column(name = "data_compra", nullable = false)
    @NotNull
    public LocalDate dataCompra;

    @Column(name = "data_recebimento")
    public LocalDate dataRecebimento;

    @Column(name = "valor_total", precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal;

    @Column(name = "valor_icms", precision = 15, scale = 4)
    public BigDecimal valorIcms;

    @Column(name = "valor_ipi", precision = 15, scale = 4)
    public BigDecimal valorIpi;

    @Column(name = "valor_pis", precision = 15, scale = 4)
    public BigDecimal valorPis;

    @Column(name = "valor_cofins", precision = 15, scale = 4)
    public BigDecimal valorCofins;

    @Column(name = "desconto", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal desconto;

    @Column(name = "frete", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal frete;

    @Column(name = "outras_despesas", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal outrasDespesas;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    public String observacoes;

    @Column(name = "status", length = 20)
    @Size(max = 20)
    public StatusCompra status = StatusCompra.ORCAMENTO;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemCompra> itens;

    @Column(name = "numero_os", length = 50)
    @Size(max = 50)
    public String numeroOs;

    @Column(name = "condicao_pagamento", length = 50)
    @Size(max = 50)
    public String condicaoPagamento; // Ex: "30 dias", "Na entrega", "À vista"

    @Column(name = "nome_fornecedor", length = 200)
    @Size(max = 200)
    public String nomeFornecedor;

    @Column(name = "cnpj_fornecedor", length = 18)
    @Size(max = 18)
    public String cnpjFornecedor;
}