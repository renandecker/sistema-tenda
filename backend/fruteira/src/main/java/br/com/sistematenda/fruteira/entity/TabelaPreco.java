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
@Table(name = "fruteira_tabela_preco", indexes = {
    @Index(name = "idx_fruteira_tabela_preco_produto", columnList = "produto_id"),
    @Index(name = "idx_fruteira_tabela_preco_vigencia", columnList = "data_inicio, data_fim"),
    @Index(name = "idx_fruteira_tabela_preco_ativa", columnList = "ativa")
})
public class TabelaPreco extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    @NotNull
    public Produto produto;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal precoVenda;

    @Column(name = "preco_atacado", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal precoAtacado;

    @Column(name = "quantidade_minima_atacado")
    public Integer quantidadeMinimaAtacado;

    @Column(name = "data_inicio", nullable = false)
    @NotNull
    public LocalDate dataInicio;

    @Column(name = "data_fim")
    public LocalDate dataFim;

    @Column(name = "ativa", nullable = false)
    public boolean ativa = true;

    @Column(name = "origem", length = 50)
    @Size(max = 50)
    public String origem;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @Column(name = "aplicada_em")
    public LocalDateTime aplicadaEm;

    @Column(name = "aplicada_por", length = 100)
    @Size(max = 100)
    public String aplicadaPor;
}