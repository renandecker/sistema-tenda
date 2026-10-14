package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fiscal_item_compra", indexes = {
    @Index(name = "idx_fiscal_item_compra_compra", columnList = "compra_id"),
    @Index(name = "idx_fiscal_item_compra_produto", columnList = "produto_id")
})
public class ItemCompra extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    public Compra compra;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    public br.com.sistematenda.fruteira.entity.Produto produtoFruteira;

    @ManyToOne
    @JoinColumn(name = "produto_buffet_id")
    public br.com.sistematenda.buffet.entity.ProdutoBuffet produtoBuffet;

    @Column(name = "codigo_produto", length = 50)
    @Size(max = 50)
    public String codigoProduto;

    @Column(name = "nome_produto", length = 200)
    @Size(max = 200)
    public String nomeProduto;

    @Column(name = "quantidade", precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal quantidade;

    @Column(name = "unidade", length = 10)
    @Size(max = 10)
    public String unidade;

    @Column(name = "valor_unitario", precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal;

    @Column(name = "codigo_ncm", length = 8)
    @Size(min = 8, max = 8)
    public String codigoNcm;

    @Column(name = "cst_icms", length = 3)
    @Size(max = 3)
    public String cstIcms;

    @Column(name = "cfop", length = 4)
    @Size(max = 4)
    public String cfop;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @Column(name = "preco_referencia_ceasa", precision = 15, scale = 4)
    public BigDecimal precoReferenciaCeasa;

    @Column(name = "data_cotacao_ceasa")
    public LocalDate dataCotacaoCeasa;
}