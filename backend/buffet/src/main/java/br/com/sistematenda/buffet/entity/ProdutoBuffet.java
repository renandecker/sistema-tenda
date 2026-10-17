package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.CstIcms;
import br.com.sistematenda.common.enums.CstPisCofins;
import br.com.sistematenda.common.enums.UnidadeMedida;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "buffet_produto", indexes = {
    @Index(name = "idx_buffet_produto_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_buffet_produto_codigo_barras", columnList = "codigo_barras"),
    @Index(name = "idx_buffet_produto_nome", columnList = "nome"),
    @Index(name = "idx_buffet_produto_categoria", columnList = "categoria"),
    @Index(name = "idx_buffet_produto_ativo", columnList = "ativo")
})
public class ProdutoBuffet extends BaseEntity {

    public enum Categoria {
        PRATO_QUENTE("Prato Quente"),
        PRATO_FRIO("Prato Frio"),
        SALADA("Salada"),
        GUARNICAO("Guarnicao"),
        SOBREMESA("Sobremesa"),
        BEBIDA("Bebida"),
        BEBIDA_ALCOOLICA("Bebida Alcoólica"),
        ADICIONAL("Adicional"),
        PRATO_FEITO("Prato Feito"),
        EXECUTIVO("Executivo"),
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
    public Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 10)
    @NotNull
    public UnidadeMedida unidadeMedida = UnidadeMedida.UN;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal precoVenda = BigDecimal.ZERO;

    @Column(name = "custo_medio", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoMedio;

    @Column(name = "controla_estoque", nullable = false)
    public boolean controlaEstoque = true;

    @Column(name = "estoque_atual", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal estoqueAtual = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal estoqueMinimo;

    @Column(name = "permite_fracao", nullable = false)
    public boolean permiteFracao = false;

    @Column(name = "ativo", nullable = false)
    public boolean ativo = true;

    @Column(name = "disponivel_balcao", nullable = false)
    public boolean disponivelBalcao = true;

    @Column(name = "tempo_preparo_minutos")
    public Integer tempoPreparoMinutos;

    @Column(name = "calorias")
    public Integer calorias;

    @Column(name = "alergenicos", length = 500)
    @Size(max = 500)
    public String alergenicos;

    @Column(name = "informacoes_nutricionais", length = 1000)
    @Size(max = 1000)
    public String informacoesNutricionais;

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_icms", length = 3)
    public CstIcms cstIcms;

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_pis", length = 2)
    public CstPisCofins cstPis;

    @Enumerated(EnumType.STRING)
    @Column(name = "cst_cofins", length = 2)
    public CstPisCofins cstCofins;

    @Column(name = "ncm", length = 8)
    @Size(min = 8, max = 8)
    public String ncm;

    @Column(name = "cest", length = 7)
    @Size(min = 7, max = 7)
    public String cest;

    @Column(name = "origem_mercadoria", length = 1)
    public Integer origemMercadoria = 0;

    @Column(name = "observacoes", length = 2000)
    @Size(max = 2000)
    public String observacoes;

    @Column(name = "imagem_url", length = 500)
    @Size(max = 500)
    public String imagemUrl;
}