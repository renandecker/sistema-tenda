package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.fruteira.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "buffet_receita_ingrediente", indexes = {
    @Index(name = "idx_buffet_receita_ingrediente_receita", columnList = "receita_id"),
    @Index(name = "idx_buffet_receita_ingrediente_produto", columnList = "produto_id")
})
public class ReceitaIngrediente extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "receita_id", nullable = false)
    @NotNull
    public Receita receita;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    @NotNull
    public Produto produto;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade", nullable = false, length = 10)
    @NotNull
    public UnidadeMedida unidade;

    @Column(name = "custo_unitario", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoUnitario;

    @Column(name = "custo_total", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoTotal;

    @Column(name = "obrigatorio", nullable = false)
    public boolean obrigatorio = true;

    @Column(name = "ordem")
    public Integer ordem;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @PrePersist
    @PreUpdate
    protected void calculateCost() {
        if (custoUnitario == null) {
            custoUnitario = produto.custoMedio;
        }
        custoTotal = quantidade.multiply(custoUnitario);
    }
}