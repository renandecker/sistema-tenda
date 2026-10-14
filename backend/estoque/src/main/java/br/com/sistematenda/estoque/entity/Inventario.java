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
import java.util.List;

@Entity
@Table(name = "estoque_inventario", indexes = {
    @Index(name = "idx_estoque_inventario_data", columnList = "data_inicio"),
    @Index(name = "idx_estoque_inventario_status", columnList = "status")
})
public class Inventario extends BaseEntity {

    public enum StatusInventario {
        PLANEJADO("Planejado"),
        EM_ANDAMENTO("Em Andamento"),
        CONCLUIDO("Concluído"),
        CANCELADO("Cancelado");

        private final String descricao;
        StatusInventario(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "descricao", length = 200)
    @Size(max = 200)
    public String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusInventario status = StatusInventario.PLANEJADO;

    @Column(name = "data_inicio", nullable = false)
    @NotNull
    public LocalDateTime dataInicio;

    @Column(name = "data_fim")
    public LocalDateTime dataFim;

    @Column(name = "responsavel", length = 100)
    @Size(max = 100)
    public String responsavel;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @Column(name = "total_itens")
    public Integer totalItens = 0;

    @Column(name = "itens_contados")
    public Integer itensContados = 0;

    @Column(name = "itens_divergentes")
    public Integer itensDivergentes = 0;

    @Column(name = "valor_divergencia", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorDivergencia = BigDecimal.ZERO;

    @OneToMany(mappedBy = "inventario", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemInventario> itens;
}