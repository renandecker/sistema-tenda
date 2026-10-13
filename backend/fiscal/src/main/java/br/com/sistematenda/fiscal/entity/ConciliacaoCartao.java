package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fiscal_conciliacao_cartao", indexes = {
    @Index(name = "idx_fiscal_conciliacao_data", columnList = "data_venda"),
    @Index(name = "idx_fiscal_conciliacao_adquirente", columnList = "adquirente"),
    @Index(name = "idx_fiscal_conciliacao_status", columnList = "status"),
    @Index(name = "idx_fiscal_conciliacao_nsu", columnList = "nsu")
})
public class ConciliacaoCartao extends BaseEntity {

    public enum StatusConciliacao {
        PENDENTE("Pendente"),
        CONCILIADO("Conciliado"),
        DIVERGENTE("Divergente"),
        ESTORNADO("Estornado"),
        NAO_IDENTIFICADO("Não Identificado");

        private final String descricao;
        StatusConciliacao(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "data_venda", nullable = false)
    @NotNull
    public LocalDate dataVenda;

    @Column(name = "data_recebimento")
    public LocalDate dataRecebimento;

    @Column(name = "adquirente", length = 100)
    @Size(max = 100)
    public String adquirente;

    @Column(name = "bandeira", length = 30)
    @Size(max = 30)
    public String bandeira;

    @Column(name = "nsu", length = 50)
    @Size(max = 50)
    public String nsu;

    @Column(name = "codigo_autorizacao", length = 50)
    @Size(max = 50)
    public String codigoAutorizacao;

    @Column(name = "valor_bruto", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorBruto;

    @Column(name = "taxa_adesao", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal taxaAdesao;

    @Column(name = "taxa_mdr", precision = 5, scale = 4)
    public BigDecimal taxaMdr;

    @Column(name = "valor_taxa", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorTaxa;

    @Column(name = "valor_liquido", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorLiquido;

    @Column(name = "valor_recebido", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorRecebido;

    @Column(name = "diferenca", precision = 15, scale = 4)
    public BigDecimal diferenca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusConciliacao status = StatusConciliacao.PENDENTE;

    @Column(name = "venda_id")
    public UUID vendaId;

    @Column(name = "numero_venda", length = 30)
    @Size(max = 30)
    public String numeroVenda;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @Column(name = "data_conciliacao")
    public java.time.LocalDateTime dataConciliacao;

    @Column(name = "conciliado_por", length = 100)
    @Size(max = 100)
    public String conciliadoPor;

    @PrePersist
    @PreUpdate
    protected void calculateValues() {
        if (valorTaxa == null && taxaMdr != null) {
            valorTaxa = valorBruto.multiply(taxaMdr).setScale(4, BigDecimal.ROUND_HALF_UP);
        }
        if (valorLiquido == null) {
            valorLiquido = valorBruto.subtract(valorTaxa != null ? valorTaxa : BigDecimal.ZERO);
        }
        if (valorRecebido != null) {
            diferenca = valorRecebido.subtract(valorLiquido);
        }
    }
}