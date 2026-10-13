package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.OrigemProduto;
import br.com.sistematenda.fruteira.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buffet_transferencia_estoque", indexes = {
    @Index(name = "idx_buffet_transferencia_produto", columnList = "produto_id"),
    @Index(name = "idx_buffet_transferencia_data", columnList = "data"),
    @Index(name = "idx_buffet_transferencia_status", columnList = "status")
})
public class TransferenciaEstoque extends BaseEntity {

    public enum StatusTransferencia {
        PENDENTE("Pendente"),
        APROVADA("Aprovada"),
        REJEITADA("Rejeitada"),
        CONCLUIDA("Concluída"),
        CANCELADA("Cancelada");

        private final String descricao;
        StatusTransferencia(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    @NotNull
    public Produto produto;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal custoUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 20)
    @NotNull
    public OrigemProduto origem = OrigemProduto.FRUTEIRA;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino", nullable = false, length = 20)
    @NotNull
    public OrigemProduto destino = OrigemProduto.BUFFET;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusTransferencia status = StatusTransferencia.PENDENTE;

    @Column(name = "data", nullable = false)
    @NotNull
    public LocalDateTime data;

    @Column(name = "data_conclusao")
    public LocalDateTime dataConclusao;

    @Column(name = "solicitado_por", length = 100)
    @Size(max = 100)
    public String solicitadoPor;

    @Column(name = "aprovado_por", length = 100)
    @Size(max = 100)
    public String aprovadoPor;

    @Column(name = "recebido_por", length = 100)
    @Size(max = 100)
    public String recebidoPor;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @Column(name = "motivo_rejeicao", length = 500)
    @Size(max = 500)
    public String motivoRejeicao;

    @PrePersist
    @PreUpdate
    protected void calculateValues() {
        valorTotal = quantidade.multiply(custoUnitario);
    }
}