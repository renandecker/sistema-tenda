package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.fruteira.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "buffet_receita", indexes = {
    @Index(name = "idx_buffet_receita_produto", columnList = "produto_id"),
    @Index(name = "idx_buffet_receita_ativa", columnList = "ativa")
})
public class Receita extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false, unique = true)
    @NotNull
    public ProdutoBuffet produto;

    @Column(name = "rendimento", nullable = false, precision = 10, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal rendimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_rendimento", nullable = false, length = 10)
    @NotNull
    public UnidadeMedida unidadeRendimento;

    @Column(name = "tempo_preparo_minutos")
    public Integer tempoPreparoMinutos;

    @Column(name = "tempo_cozimento_minutos")
    public Integer tempoCozimentoMinutos;

    @Column(name = "dificuldade", length = 20)
    @Size(max = 20)
    public String dificuldade;

    @Column(name = "instrucoes", length = 5000)
    @Size(max = 5000)
    public String instrucoes;

    @Column(name = "custo_total", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal custoTotal;

    @Column(name = "margem_desejada", precision = 5, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal margemDesejada;

    @Column(name = "preco_sugerido", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal precoSugerido;

    @Column(name = "ativa", nullable = false)
    public boolean ativa = true;

    @Column(name = "versao", nullable = false)
    public Integer versao = 1;

    @Column(name = "observacoes", length = 2000)
    @Size(max = 2000)
    public String observacoes;

    @OneToMany(mappedBy = "receita", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ReceitaIngrediente> ingredientes;
}