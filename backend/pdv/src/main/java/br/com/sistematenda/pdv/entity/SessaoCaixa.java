package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdv_sessao_caixa", indexes = {
    @Index(name = "idx_pdv_sessao_pdv", columnList = "pdv_id"),
    @Index(name = "idx_pdv_sessao_data", columnList = "data_abertura"),
    @Index(name = "idx_pdv_sessao_status", columnList = "status")
})
public class SessaoCaixa extends BaseEntity {

    public enum StatusSessao {
        ABERTA("Aberta"),
        FECHADA("Fechada"),
        SANGRIA("Sangria"),
        REFORCO("Reforço");

        private final String descricao;
        StatusSessao(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "pdv_id", nullable = false)
    @NotNull
    public Pdv pdv;

    @Column(name = "operador_abertura", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String operadorAbertura;

    @Column(name = "operador_fechamento", length = 100)
    @Size(max = 100)
    public String operadorFechamento;

    @Column(name = "valor_inicial", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorInicial = BigDecimal.ZERO;

    @Column(name = "valor_final_sistema", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorFinalSistema;

    @Column(name = "valor_final_contado", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorFinalContado;

    @Column(name = "diferenca", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal diferenca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusSessao status = StatusSessao.ABERTA;

    @Column(name = "data_abertura", nullable = false)
    @NotNull
    public LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    public LocalDateTime dataFechamento;

    @Column(name = "observacoes_abertura", length = 1000)
    @Size(max = 1000)
    public String observacoesAbertura;

    @Column(name = "observacoes_fechamento", length = 1000)
    @Size(max = 1000)
    public String observacoesFechamento;

    @Column(name = "sangrias_total", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal sangriasTotal = BigDecimal.ZERO;

    @Column(name = "reforcos_total", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal reforcosTotal = BigDecimal.ZERO;

    @Column(name = "vendas_dinheiro", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal vendasDinheiro = BigDecimal.ZERO;

    @Column(name = "vendas_cartao", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal vendasCartao = BigDecimal.ZERO;

    @Column(name = "vendas_pix", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal vendasPix = BigDecimal.ZERO;

    @Column(name = "vendas_vale", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal vendasVale = BigDecimal.ZERO;

    @Column(name = "total_vendas", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal totalVendas = BigDecimal.ZERO;

    @Column(name = "numero_ultima_venda")
    public Long numeroUltimaVenda = 0L;

    @Column(name = "reducao_z", columnDefinition = "TEXT")
    public String reducaoZ;

    @Column(name = "status_sangria_aberta")
    public Boolean statusSangriaAberta = false;

    // Métodos auxiliares
    public BigDecimal getSaldoCaixa() {
        return valorFinalSistema != null ? valorFinalSistema : BigDecimal.ZERO;
    }

    public boolean isSangriaAberta() {
        return statusSangriaAberta;
    }

    public void abrirSangria() {
        this.statusSangriaAberta = true;
    }

    public void fecharSangria() {
        this.statusSangriaAberta = false;
    }
}