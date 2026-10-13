package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdv_movimento_caixa", indexes = {
    @Index(name = "idx_pdv_movimento_sessao", columnList = "sessao_caixa_id"),
    @Index(name = "idx_pdv_movimento_tipo", columnList = "tipo"),
    @Index(name = "idx_pdv_movimento_data", columnList = "data")
})
public class MovimentoCaixa extends BaseEntity {

    public enum TipoMovimento {
        SANGRIA("Sangria"),
        REFORCO("Reforço"),
        VENDA("Venda"),
        ESTORNO("Estorno"),
        AJUSTE("Ajuste");

        private final String descricao;
        TipoMovimento(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "sessao_caixa_id", nullable = false)
    @NotNull
    public SessaoCaixa sessaoCaixa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull
    public TipoMovimento tipo;

    @Column(name = "valor", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal valor;

    @Column(name = "descricao", length = 500)
    @Size(max = 500)
    public String descricao;

    @Column(name = "operador", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String operador;

    @Column(name = "data", nullable = false)
    @NotNull
    public LocalDateTime data;

    @Column(name = "venda_id")
    public UUID vendaId;
}