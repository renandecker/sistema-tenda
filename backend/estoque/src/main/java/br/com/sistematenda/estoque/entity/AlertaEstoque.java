package br.com.sistematenda.estoque.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "estoque_alerta", indexes = {
    @Index(name = "idx_estoque_alerta_produto_fruteira", columnList = "produto_fruteira_id"),
    @Index(name = "idx_estoque_alerta_produto_buffet", columnList = "produto_buffet_id"),
    @Index(name = "idx_estoque_alerta_tipo", columnList = "tipo"),
    @Index(name = "idx_estoque_alerta_status", columnList = "status"),
    @Index(name = "idx_estoque_alerta_data", columnList = "data_alerta")
})
public class AlertaEstoque extends BaseEntity {

    public enum TipoAlerta {
        ESTOQUE_MINIMO("Estoque Mínimo"),
        ESTOQUE_MAXIMO("Estoque Máximo"),
        VENCIMENTO_PROXIMO("Vencimento Próximo"),
        VENCIDO("Vencido"),
        SEM_MOVIMENTACAO("Sem Movimentação"),
        TEMPERATURA("Temperatura Fora da Faixa");

        private final String descricao;
        TipoAlerta(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum StatusAlerta {
        ATIVO("Ativo"),
        VISUALIZADO("Visualizado"),
        RESOLVIDO("Resolvido"),
        IGNORADO("Ignorado");

        private final String descricao;
        StatusAlerta(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum Prioridade {
        BAIXA("Baixa"),
        MEDIA("Média"),
        ALTA("Alta"),
        CRITICA("Crítica");

        private final String descricao;
        Prioridade(String descricao) { this.descricao = descricao; }
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
    public TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusAlerta status = StatusAlerta.ATIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", length = 20)
    public Prioridade prioridade = Prioridade.MEDIA;

    @Column(name = "mensagem", length = 500)
    public String mensagem;

    @Column(name = "valor_referencia", precision = 15, scale = 4)
    public java.math.BigDecimal valorReferencia;

    @Column(name = "valor_atual", precision = 15, scale = 4)
    public java.math.BigDecimal valorAtual;

    @Column(name = "data_vencimento")
    public LocalDate dataVencimento;

    @Column(name = "dias_para_vencimento")
    public Integer diasParaVencimento;

    @Column(name = "data_alerta", nullable = false)
    @NotNull
    public LocalDateTime dataAlerta;

    @Column(name = "data_visualizacao")
    public LocalDateTime dataVisualizacao;

    @Column(name = "data_resolucao")
    public LocalDateTime dataResolucao;

    @Column(name = "visualizado_por", length = 100)
    @Size(max = 100)
    public String visualizadoPor;

    @Column(name = "resolvido_por", length = 100)
    @Size(max = 100)
    public String resolvidoPor;

    @Column(name = "observacoes_resolucao", length = 500)
    @Size(max = 500)
    public String observacoesResolucao;

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
}