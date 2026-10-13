package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fiscal_dre", indexes = {
    @Index(name = "idx_fiscal_dre_periodo", columnList = "data_inicio, data_fim"),
    @Index(name = "idx_fiscal_dre_unidade", columnList = "unidade_negocio"),
    @Index(name = "idx_fiscal_dre_status", columnList = "status")
})
public class Dre extends BaseEntity {

    public enum UnidadeNegocio {
        FRUTEIRA("Fruteira"),
        BUFFET("Buffet"),
        CONSOLIDADO("Consolidado"),
        // Empresarial
        EMPRESARIAL("Empresarial");

        private final String descricao;
        UnidadeNegocio(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum StatusDre {
        GERANDO("Gerando"),
        GERADO("Gerado"),
        APROVADO("Aprovado");

        private final String descricao;
        StatusDre(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_negocio", nullable = false, length = 20)
    @NotNull
    public UnidadeNegocio unidadeNegocio;

    @Column(name = "data_inicio", nullable = false)
    @NotNull
    public LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    @NotNull
    public LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusDre status = StatusDre.GERANDO;

    // Receitas
    @Column(name = "receita_bruta", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal receitaBruta = BigDecimal.ZERO;

    @Column(name = "receita_fruteira", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal receitaFruteira = BigDecimal.ZERO;

    @Column(name = "receita_buffet", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal receitaBuffet = BigDecimal.ZERO;

    @Column(name = "receita_outros", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal receitaOutros = BigDecimal.ZERO;

    // Deduções
    @Column(name = "devolucoes", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal devolucoes = BigDecimal.ZERO;

    @Column(name = "descontos_concedidos", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal descontosConcedidos = BigDecimal.ZERO;

    @Column(name = "impostos_vendas", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal impostosVendas = BigDecimal.ZERO;

    @Column(name = "receita_liquida", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal receitaLiquida = BigDecimal.ZERO;

    // CMV (Custo das Mercadorias Vendidas)
    @Column(name = "cmv", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal cmv = BigDecimal.ZERO;

    @Column(name = "cmv_fruteira", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal cmvFruteira = BigDecimal.ZERO;

    @Column(name = "cmv_buffet", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal cmvBuffet = BigDecimal.ZERO;

    // Lucro Bruto
    @Column(name = "lucro_bruto", precision = 15, scale = 4)
    public BigDecimal lucroBruto = BigDecimal.ZERO;

    @Column(name = "margem_bruta_percentual", precision = 5, scale = 2)
    public BigDecimal margemBrutaPercentual = BigDecimal.ZERO;

    // Despesas Operacionais
    @Column(name = "despesas_vendas", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal despesasVendas = BigDecimal.ZERO;

    @Column(name = "despesas_administrativas", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal despesasAdministrativas = BigDecimal.ZERO;

    @Column(name = "despesas_financeiras", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal despesasFinanceiras = BigDecimal.ZERO;

    @Column(name = "despesas_operacionais_total", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal despesasOperacionaisTotal = BigDecimal.ZERO;

    // Lucro Operacional
    @Column(name = "lucro_operacional", precision = 15, scale = 4)
    public BigDecimal lucroOperacional = BigDecimal.ZERO;

    // Resultado Não Operacional
    @Column(name = "receitas_nao_operacionais", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal receitasNaoOperacionais = BigDecimal.ZERO;

    @Column(name = "despesas_nao_operacionais", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal despesasNaoOperacionais = BigDecimal.ZERO;

    @Column(name = "resultado_nao_operacional", precision = 15, scale = 4)
    public BigDecimal resultadoNaoOperacional = BigDecimal.ZERO;

    // Resultado Antes do IR/CSLL
    @Column(name = "resultado_antes_ir_csll", precision = 15, scale = 4)
    public BigDecimal resultadoAntesIrCsll = BigDecimal.ZERO;

    // IR/CSLL
    @Column(name = "irpj", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal irpj = BigDecimal.ZERO;

    @Column(name = "csll", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal csll = BigDecimal.ZERO;

    // Lucro Líquido
    @Column(name = "lucro_liquido", precision = 15, scale = 4)
    public BigDecimal lucroLiquido = BigDecimal.ZERO;

    @Column(name = "margem_liquida_percentual", precision = 5, scale = 2)
    public BigDecimal margemLiquidaPercentual = BigDecimal.ZERO;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    public String observacoes;

    // Metadados de controle
    @Column(name = "meses_calculados")
    public Integer mesesCalculados = 0;

    @Column(name = "ultima_atualizacao")
    public java.time.LocalDateTime ultimaAtualizacao;
}