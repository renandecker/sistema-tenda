package br.com.sistematenda.estoque.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "estoque_movimento", indexes = {
    @Index(name = "idx_estoque_movimento_produto_fruteira", columnList = "produto_fruteira_id"),
    @Index(name = "idx_estoque_movimento_produto_buffet", columnList = "produto_buffet_id"),
    @Index(name = "idx_estoque_movimento_tipo", columnList = "tipo"),
    @Index(name = "idx_estoque_movimento_data", columnList = "data"),
    @Index(name = "idx_estoque_movimento_origem_destino", columnList = "origem, destino")
})
public class MovimentoEstoque extends BaseEntity {

    public enum TipoMovimento {
        ENTRADA_COMPRA("Entrada por Compra"),
        ENTRADA_TRANSFERENCIA("Entrada por Transferência"),
        ENTRADA_AJUSTE("Entrada por Ajuste"),
        ENTRADA_DEVOLUCAO("Entrada por Devolução"),
        SAIDA_VENDA("Saída por Venda"),
        SAIDA_TRANSFERENCIA("Saída por Transferência"),
        SAIDA_PERDA("Saída por Perda"),
        SAIDA_AJUSTE("Saída por Ajuste"),
        SAIDA_CONSUMO_INTERNO("Saída por Consumo Interno"),
        SAIDA_DEFEITO_AVARIA("Saída por Defeito - Avaria/Quebra"),
        SAIDA_DEFEITO_DETERIORACAO("Saída por Defeito - Deterioração/Perecível"),
        SAIDA_DEFEITO_VENCIMENTO("Saída por Defeito - Vencimento"),
        SAIDA_DEFEITO_QUALIDADE("Saída por Defeito - Qualidade/Reprovação"),
        SAIDA_DEFEITO_OUTROS("Saída por Defeito - Outros"),
        ENTRADA_DEFEITO_REPROCESSO("Entrada por Defeito - Reprocessamento"),
        INVENTARIO("Inventário");

        private final String descricao;
        TipoMovimento(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }

        public boolean isEntrada() {
            return this.name().startsWith("ENTRADA");
        }

        public boolean isSaida() {
            return this.name().startsWith("SAIDA");
        }

        public boolean isDefeito() {
            return this.name().contains("DEFEITO");
        }
    }

    public enum OrigemDestino {
        FRUTEIRA("Fruteira"),
        BUFFET("Buffet"),
        FORNECEDOR("Fornecedor"),
        CLIENTE("Cliente"),
        PERDA("Perda/Descarte"),
        AJUSTE("Ajuste"),
        DEFEITO("Defeito/Qualidade"),
        REPROCESSO("Reprocessamento"),
        OUTROS("Outros");

        private final String descricao;
        OrigemDestino(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne
    @JoinColumn(name = "produto_fruteira_id")
    public Produto produtoFruteira;

    @ManyToOne
    @JoinColumn(name = "produto_buffet_id")
    public ProdutoBuffet produtoBuffet;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    @NotNull
    public TipoMovimento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", length = 20)
    public OrigemDestino origem;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino", length = 20)
    public OrigemDestino destino;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal quantidade;

    @Column(name = "custo_unitario", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoUnitario;

    @Column(name = "valor_total", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorTotal;

    @Column(name = "estoque_anterior", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal estoqueAnterior;

    @Column(name = "estoque_posterior", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal estoquePosterior;

    @Column(name = "lote", length = 50)
    @Size(max = 50)
    public String lote;

    @Column(name = "data_validade")
    public LocalDateTime dataValidade;

    @Column(name = "documento_referencia", length = 100)
    @Size(max = 100)
    public String documentoReferencia;

    @Column(name = "numero_documento", length = 50)
    @Size(max = 50)
    public String numeroDocumento;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @Column(name = "motivo_defeito", length = 500)
    @Size(max = 500)
    public String motivoDefeito;

    @Column(name = "tipo_defeito", length = 50)
    @Size(max = 50)
    public String tipoDefeito;

    @Column(name = "lote_defeituoso", length = 50)
    @Size(max = 50)
    public String loteDefeituoso;

    @Column(name = "quantidade_recuperavel", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal quantidadeRecuperavel;

    @Column(name = "responsavel", length = 100)
    @Size(max = 100)
    public String responsavel;

    @Column(name = "data", nullable = false)
    @NotNull
    public LocalDateTime data;

    @PrePersist
    @PreUpdate
    protected void calculateValues() {
        if (custoUnitario != null) {
            valorTotal = quantidade.multiply(custoUnitario);
        }
    }

    public UUID getProdutoId() {
        if (produtoFruteira != null) return produtoFruteira.id;
        if (produtoBuffet != null) return produtoBuffet.id;
        return null;
    }

    public String getProdutoNome() {
        if (produtoFruteira != null) return produtoFruteira.nome;
        if (produtoBuffet != null) return produtoBuffet.nome;
        return null;
    }

    public String getProdutoCodigo() {
        if (produtoFruteira != null) return produtoFruteira.codigo;
        if (produtoBuffet != null) return produtoBuffet.codigo;
        return null;
    }
}