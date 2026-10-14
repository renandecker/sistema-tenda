package br.com.sistematenda.estoque.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "estoque_item_inventario", indexes = {
    @Index(name = "idx_estoque_item_inventario_inventario", columnList = "inventario_id"),
    @Index(name = "idx_estoque_item_inventario_produto_fruteira", columnList = "produto_fruteira_id"),
    @Index(name = "idx_estoque_item_inventario_produto_buffet", columnList = "produto_buffet_id")
})
public class ItemInventario extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    public Inventario inventario;

    @ManyToOne
    @JoinColumn(name = "produto_fruteira_id")
    public Produto produtoFruteira;

    @ManyToOne
    @JoinColumn(name = "produto_buffet_id")
    public ProdutoBuffet produtoBuffet;

    @Column(name = "quantidade_sistema", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal quantidadeSistema = BigDecimal.ZERO;

    @Column(name = "quantidade_contada", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal quantidadeContada;

    @Column(name = "quantidade_divergencia", precision = 15, scale = 4)
    public BigDecimal quantidadeDivergencia;

    @Column(name = "custo_unitario", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoUnitario;

    @Column(name = "valor_divergencia", precision = 15, scale = 4)
    public BigDecimal valorDivergencia;

    @Column(name = "contado", nullable = false)
    public boolean contado = false;

    @Column(name = "data_contagem")
    public java.time.LocalDateTime dataContagem;

    @Column(name = "contado_por", length = 100)
    @Size(max = 100)
    public String contadoPor;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @PrePersist
    @PreUpdate
    protected void calculateDivergencia() {
        if (quantidadeContada != null && quantidadeSistema != null) {
            quantidadeDivergencia = quantidadeContada.subtract(quantidadeSistema);
            if (custoUnitario != null) {
                valorDivergencia = quantidadeDivergencia.multiply(custoUnitario);
            }
        }
    }

    public UUID getProdutoId() {
        if (produtoFruteira != null) return produtoFruteira.id;
        if (produtoBuffet != null) return produtoBuffet.id;
        return null;
    }
}