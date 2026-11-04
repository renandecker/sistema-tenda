package br.com.sistematenda.estoque.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.UnidadeMedida;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

/**
 * Produto de estoque utilizado no controle de matéria-prima e insumos.
 *
 * É separado dos produtos de buffet (que são apenas itens de venda) e dos
 * produtos da fruteira (hortifrúti). A movimentação de estoque trabalha com
 * este cadastro e com os produtos da fruteira.
 */
@Entity
@Table(name = "estoque_produto", indexes = {
    @Index(name = "idx_estoque_produto_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_estoque_produto_codigo_barras", columnList = "codigo_barras"),
    @Index(name = "idx_estoque_produto_nome", columnList = "nome"),
    @Index(name = "idx_estoque_produto_categoria", columnList = "categoria"),
    @Index(name = "idx_estoque_produto_ativo", columnList = "ativo")
})
@Check(constraints = "custo_medio >= 0")
@Check(constraints = "estoque_atual >= 0")
@Check(constraints = "estoque_minimo >= 0")
@Check(constraints = "estoque_maximo >= 0")
public class ProdutoEstoque extends BaseEntity {

    public enum Categoria {
        MATERIA_PRIMA("Matéria-prima"),
        EMBALAGEM("Embalagem"),
        DESCARTAVEL("Descartável"),
        LIMPEZA("Limpeza"),
        OUTROS("Outros");

        private final String descricao;
        Categoria(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "codigo", unique = true, nullable = false, length = 50, updatable = false)
    @Size(max = 50)
    public String codigo;

    @Column(name = "codigo_barras", unique = true, length = 100)
    @Size(max = 100)
    public String codigoBarras;

    @Column(name = "nome", nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    public String nome;

    @Column(name = "descricao", length = 1000)
    @Size(max = 1000)
    public String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 30)
    @NotNull
    public Categoria categoria = Categoria.MATERIA_PRIMA;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 10)
    @NotNull
    public UnidadeMedida unidadeMedida = UnidadeMedida.UN;

    @Column(name = "custo_medio", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal custoMedio = BigDecimal.ZERO;

    @Column(name = "estoque_atual", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal estoqueMinimo;

    @Column(name = "estoque_maximo", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal estoqueMaximo;

    @Column(name = "ativo", nullable = false)
    public boolean ativo = true;

    @Column(name = "observacoes", length = 2000)
    @Size(max = 2000)
    public String observacoes;
}
