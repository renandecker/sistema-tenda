package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pdv_item_venda", indexes = {
    @Index(name = "idx_pdv_item_venda_venda", columnList = "venda_id"),
    @Index(name = "idx_pdv_item_venda_produto_fruteira", columnList = "produto_fruteira_id"),
    @Index(name = "idx_pdv_item_venda_produto_buffet", columnList = "produto_buffet_id"),
    @Index(name = "idx_pdv_item_venda_origem", columnList = "origem")
})
public class ItemVenda extends BaseEntity {

    public enum OrigemItem {
        FRUTEIRA("Fruteira"),
        BUFFET("Buffet");

        private final String descricao;
        OrigemItem(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum TipoItem {
        PRODUTO("Produto"),
        PRATO_BUFFET("Prato Buffet"),
        BEBIDA("Bebida"),
        SOBREMESA("Sobremesa"),
        ADICIONAL("Adicional"),
        PRATO_FEITO("Prato Feito"),
        OUTROS("Outros"),
        SERVICO("Serviço");

        private final String descricao;
        TipoItem(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    @NotNull
    public Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_fruteira_id")
    public Produto produtoFruteira;

    @ManyToOne
    @JoinColumn(name = "produto_buffet_id")
    public ProdutoBuffet produtoBuffet;

    @Column(name = "origem", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    public OrigemItem origem;

    @Column(name = "codigo_produto", nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    public String codigoProduto;

    @Column(name = "nome_produto", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String nomeProduto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    public TipoItem tipo;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
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

    @Column(name = "cancelado", nullable = false)
    public boolean cancelado = false;

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