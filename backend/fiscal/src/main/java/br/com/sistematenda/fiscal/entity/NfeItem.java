package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "fiscal_nfe_item", indexes = {
    @Index(name = "idx_fiscal_nfe_item_nfe", columnList = "nfe_id")
})
public class NfeItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "nfe_id", nullable = false)
    @NotNull
    public Nfe nfe;

    @Column(name = "numero_item", nullable = false)
    @NotNull
    public Integer numeroItem;

    @Column(name = "codigo_produto", length = 60)
    @Size(max = 60)
    public String codigoProduto;

    @Column(name = "descricao", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String descricao;

    @Column(name = "ncm", length = 8)
    @Size(max = 8)
    public String ncm;

    @Column(name = "cfop", nullable = false, length = 4)
    @NotNull
    @Size(max = 4)
    public String cfop;

    @Column(name = "cst_icms", length = 3)
    @Size(max = 3)
    public String cstIcms;

    @Column(name = "cst_pis", length = 2)
    @Size(max = 2)
    public String cstPis;

    @Column(name = "cst_cofins", length = 2)
    @Size(max = 2)
    public String cstCofins;

    @Column(name = "unidade_comercial", nullable = false, length = 6)
    @NotNull
    @Size(max = 6)
    public String unidadeComercial;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    @NotNull
    public BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 4)
    @NotNull
    public BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 4)
    @NotNull
    public BigDecimal valorTotal;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 4)
    public BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "origem_mercadoria", nullable = false)
    public Integer origemMercadoria = 0;

    @Column(name = "valor_icms", nullable = false, precision = 15, scale = 4)
    public BigDecimal valorIcms = BigDecimal.ZERO;

    @Column(name = "aliquota_icms", nullable = false, precision = 7, scale = 4)
    public BigDecimal aliquotaIcms = BigDecimal.ZERO;

    @Column(name = "valor_pis", nullable = false, precision = 15, scale = 4)
    public BigDecimal valorPis = BigDecimal.ZERO;

    @Column(name = "valor_cofins", nullable = false, precision = 15, scale = 4)
    public BigDecimal valorCofins = BigDecimal.ZERO;
}
