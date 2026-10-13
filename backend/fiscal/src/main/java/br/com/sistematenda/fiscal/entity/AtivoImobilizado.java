package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fiscal_ativo_imobilizado", indexes = {
    @Index(name = "idx_fiscal_ativo_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_fiscal_ativo_status", columnList = "status"),
    @Index(name = "idx_fiscal_ativo_unidade", columnList = "unidade_negocio")
})
public class AtivoImobilizado extends BaseEntity {

    public enum StatusAtivo {
        ATIVO("Ativo"),
        BAIXADO("Baixado"),
        VENDIDO("Vendido"),
        DOADO("Doado"),
        PERDIDO("Perdido"),
        EM_MANUTENCAO("Em Manutenção");

        private final String descricao;
        StatusAtivo(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum TipoAtivo {
        MAQUINA_EQUIPAMENTO("Máquinas e Equipamentos"),
        MOVEIS_UTENSILIOS("Móveis e Utensílios"),
        VEICULOS("Veículos"),
        EQUIPAMENTOS_INFORMATICA("Equipamentos de Informática"),
        CAMARAS_FRIAS("Câmaras Frigoríficas"),
        BALANCAS("Balanças"),
        EQUIPAMENTOS_COZINHA("Equipamentos de Cozinha"),
        INSTALACOES("Instalações"),
        BENFEITORIAS("Benfeitorias"),
        OUTROS("Outros");

        private final String descricao;
        TipoAtivo(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum UnidadeNegocio {
        FRUTEIRA("Fruteira"),
        BUFFET("Buffet"),
        ADMINISTRATIVO("Administrativo"),
        COMUM("Comum");

        private final String descricao;
        UnidadeNegocio(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "codigo", unique = true, nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    public String codigo;

    @Column(name = "descricao", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    @NotNull
    public TipoAtivo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_negocio", nullable = false, length = 20)
    @NotNull
    public UnidadeNegocio unidadeNegocio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusAtivo status = StatusAtivo.ATIVO;

    @Column(name = "data_aquisicao", nullable = false)
    @NotNull
    public LocalDate dataAquisicao;

    @Column(name = "valor_aquisicao", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorAquisicao;

    @Column(name = "vida_util_meses", nullable = false)
    @NotNull
    public Integer vidaUtilMeses;

    @Column(name = "metodo_depreciacao", length = 20)
    @Size(max = 20)
    public String metodoDepreciacao = "LINEAR";

    @Column(name = "taxa_depreciacao_anual", precision = 5, scale = 2)
    public BigDecimal taxaDepreciacaoAnual;

    @Column(name = "valor_residual", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorResidual = BigDecimal.ZERO;

    @Column(name = "depreciacao_acumulada", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal depreciacaoAcumulada = BigDecimal.ZERO;

    @Column(name = "valor_liquido", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorLiquido;

    @Column(name = "data_inicio_depreciacao")
    public LocalDate dataInicioDepreciacao;

    @Column(name = "data_fim_depreciacao")
    public LocalDate dataFimDepreciacao;

    @Column(name = "ultima_depreciacao")
    public LocalDate ultimaDepreciacao;

    @Column(name = "fornecedor", length = 200)
    @Size(max = 200)
    public String fornecedor;

    @Column(name = "nota_fiscal", length = 50)
    @Size(max = 50)
    public String notaFiscal;

    @Column(name = "localizacao", length = 200)
    @Size(max = 200)
    public String localizacao;

    @Column(name = "responsavel", length = 100)
    @Size(max = 100)
    public String responsavel;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    public String observacoes;

    @Column(name = "data_baixa")
    public LocalDate dataBaixa;

    @Column(name = "motivo_baixa", length = 500)
    @Size(max = 500)
    public String motivoBaixa;

    @Column(name = "valor_baixa", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorBaixa;

    @PrePersist
    @PreUpdate
    protected void calculateValues() {
        if (taxaDepreciacaoAnual == null && vidaUtilMeses != null && vidaUtilMeses > 0) {
            taxaDepreciacaoAnual = new BigDecimal("100").divide(new BigDecimal(vidaUtilMeses / 12), 4, BigDecimal.ROUND_HALF_UP);
        }
        valorLiquido = valorAquisicao.subtract(depreciacaoAcumulada);
    }
}