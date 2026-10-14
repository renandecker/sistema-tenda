package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.fruteira.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "fiscal_item_nota_entrada", indexes = {
    @Index(name = "idx_fiscal_item_nota_entrada_nota", columnList = "nota_entrada_id"),
    @Index(name = "idx_fiscal_item_nota_entrada_produto", columnList = "produto_id")
})
public class ItemNotaEntrada extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "nota_entrada_id", nullable = false)
    public NotaFiscalEntrada notaEntrada;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    public Produto produto;

    @Column(name = "codigo_produto", length = 50)
    @Size(max = 50)
    public String codigoProduto;

    @Column(name = "nome_produto", length = 200)
    @Size(max = 200)
    public String nomeProduto;

    @Column(name = "ncm", length = 8)
    @Size(min = 8, max = 8)
    public String ncm;

    @Column(name = "cest", length = 7)
    @Size(min = 7, max = 7)
    public String cest;

    @Column(name = "cfop", length = 4)
    @Size(max = 4)
    public String cfop;

    @Column(name = "quantidade", precision = 15, scale = 4)
    @DecimalMin("0.0001")
    public BigDecimal quantidade;

    @Column(name = "unidade", length = 10)
    @Size(max = 10)
    public String unidade;

    @Column(name = "valor_unitario", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 15, scale = 4)
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

    @Column(name = "cst_icms", length = 3)
    @Size(max = 3)
    public String cstIcms;

    @Column(name = "cst_pis", length = 2)
    @Size(max = 2)
    public String cstPis;

    @Column(name = "cst_cofins", length = 2)
    @Size(max = 2)
    public String cstCofins;

    @Column(name = "origem_mercadoria")
    public Integer origemMercadoria;

    @Column(name = "processado", nullable = false)
    public boolean processado = false;
}