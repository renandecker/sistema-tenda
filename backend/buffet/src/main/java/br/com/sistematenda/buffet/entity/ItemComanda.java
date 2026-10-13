package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buffet_item_comanda", indexes = {
    @Index(name = "idx_buffet_item_comanda_comanda", columnList = "comanda_id"),
    @Index(name = "idx_buffet_item_comanda_produto", columnList = "produto_id"),
    @Index(name = "idx_buffet_item_comanda_tipo", columnList = "tipo")
})
public class ItemComanda extends BaseEntity {

    public enum TipoItem {
        PRATO_BUFFET("Prato do Buffet"),
        BEBIDA("Bebida"),
        SOBREMESA("Sobremesa"),
        ADICIONAL("Adicional"),
        PRATO_FEITO("Prato Feito"),
        OUTROS("Outros");

        private final String descricao;
        TipoItem(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    @NotNull
    public Comanda comanda;

    @Column(name = "produto_id")
    public UUID produtoId;

    @Column(name = "codigo_produto", length = 50)
    @Size(max = 50)
    public String codigoProduto;

    @Column(name = "nome_produto", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String nomeProduto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull
    public TipoItem tipo;

    @Column(name = "quantidade", nullable = false, precision = 10, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal quantidade = BigDecimal.ONE;

    @Column(name = "peso_bruto", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal pesoBruto;

    @Column(name = "tara", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal tara;

    @Column(name = "peso_liquido", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal pesoLiquido;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal precoUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @Column(name = "lancado_por", length = 100)
    @Size(max = 100)
    public String lancadoPor;

    @Column(name = "data_lancamento", nullable = false)
    @NotNull
    public LocalDateTime dataLancamento;

    @Column(name = "cancelado", nullable = false)
    public boolean cancelado = false;

    @Column(name = "motivo_cancelamento", length = 500)
    @Size(max = 500)
    public String motivoCancelamento;

    @Column(name = "cancelado_por", length = 100)
    @Size(max = 100)
    public String canceladoPor;

    @Column(name = "data_cancelamento")
    public LocalDateTime dataCancelamento;

    @PrePersist
    @PreUpdate
    protected void calculateValues() {
        if (pesoBruto != null && tara != null) {
            pesoLiquido = pesoBruto.subtract(tara);
            if (pesoLiquido.compareTo(BigDecimal.ZERO) < 0) {
                pesoLiquido = BigDecimal.ZERO;
            }
        }
        valorTotal = precoUnitario.multiply(quantidade);
    }
}