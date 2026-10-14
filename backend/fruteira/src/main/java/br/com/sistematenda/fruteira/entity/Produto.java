package br.com.sistematenda.fruteira.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.CstIcms;
import br.com.sistematenda.common.enums.CstPisCofins;
import br.com.sistematenda.common.enums.TipoProduto;
import br.com.sistematenda.common.enums.UnidadeMedida;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "fruteira_produto", indexes = {
    @Index(name = "idx_fruteira_produto_codigo", columnList = "codigo"),
    @Index(name = "idx_fruteira_produto_codigo_barras", columnList = "codigo_barras"),
    @Index(name = "idx_fruteira_produto_nome", columnList = "nome"),
    @Index(name = "idx_fruteira_produto_tipo", columnList = "tipo"),
    @Index(name = "idx_fruteira_produto_ativo", columnList = "ativo")
})
@Check(constraints = "preco_venda >= 0")
@Check(constraints = "custo_medio >= 0")
@Check(constraints = "estoque_atual >= 0")
public class Produto extends BaseEntity {

    @Column(name = "codigo", unique = true, nullable = false, length = 50)
    @NotBlank
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
    @Column(name = "tipo", nullable = false, length = 30)
    @NotNull
    public TipoProduto tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 10)
    @NotNull
    public UnidadeMedida unidadeMedida;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal precoVenda = BigDecimal.ZERO;

    @Column(name = "preco_atacado", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal precoAtacado;

    @Column(name = "quantidade_minima_atacado")
    public Integer quantidadeMinimaAtacado;

    @Column(name = "custo_medio", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal custoMedio = BigDecimal.ZERO;

    @Column(name = "custo_ultimo", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoUltimo;

    @Column(name = "margem_desejada", precision = 5, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal margemDesejada;

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

    @Column(name = "peso_liquido_padrao", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal pesoLiquidoPadrao;

    @Column(name = "tara_padrao", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal taraPadrao;

    @Column(name = "dias_validade_alerta")
    public Integer diasValidadeAlerta = 3;

    @Column(name = "controla_validade", nullable = false)
    public boolean controlaValidade = false;

    @Column(name = "controla_lote", nullable = false)
    public boolean controlaLote = false;

    @Column(name = "permite_fracao", nullable = false)
    public boolean permiteFracao = true;

    @Column(name = "ativo", nullable = false)
    public boolean ativo = true;

    @Column(name = "pesavel_balanca", nullable = false)
    public boolean pesavelBalanca = false;

    @Column(name = "codigo_balanca", length = 20)
    @Size(max = 20)
    public String codigoBalanca;

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