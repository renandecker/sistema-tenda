package br.com.sistematenda.fruteira.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fruteira_perda", indexes = {
    @Index(name = "idx_fruteira_perda_produto", columnList = "produto_id"),
    @Index(name = "idx_fruteira_perda_data", columnList = "data"),
    @Index(name = "idx_fruteira_perda_tipo", columnList = "tipo")
})
public class Perda extends BaseEntity {

    public enum TipoPerda {
        DETERIORACAO("Deterioração/Perecível"),
        AVARIA("Avaria/Quebra"),
        VENCIMENTO("Vencimento"),
        FURTO("Furto/Desvio"),
        DOACAO("Doação"),
        OUTROS("Outros");

        private final String descricao;
        TipoPerda(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    @NotNull
    public Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull
    public TipoPerda tipo;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal custoUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal;

    @Column(name = "data", nullable = false)
    @NotNull
    public LocalDate data;

    @Column(name = "hora", nullable = false)
    @NotNull
    public LocalDateTime hora;

    @Column(name = "lote", length = 50)
    @Size(max = 50)
    public String lote;

    @Column(name = "data_validade")
    public LocalDate dataValidade;

    @Column(name = "motivo", length = 500)
    @Size(max = 500)
    public String motivo;

    @Column(name = "responsavel", length = 100)
    @Size(max = 100)
    public String responsavel;

    @Column(name = "autorizado_por", length = 100)
    @Size(max = 100)
    public String autorizadoPor;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @Column(name = "baixa_estoque_realizada", nullable = false)
    public boolean baixaEstoqueRealizada = false;

    @Column(name = "data_baixa_estoque")
    public LocalDateTime dataBaixaEstoque;
}