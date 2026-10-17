package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.entity.Cliente;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pdv_caderno_divida", indexes = {
    @Index(name = "idx_caderno_cliente", columnList = "cliente_id"),
    @Index(name = "idx_caderno_status", columnList = "status"),
    @Index(name = "idx_caderno_documento", columnList = "cliente_documento"),
    @Index(name = "idx_caderno_data", columnList = "data_lancamento")
})
public class CadernoDivida extends BaseEntity {

    public enum StatusDivida {
        ABERTA("Aberta"),
        PARCIAL("Parcialmente paga"),
        QUITADA("Quitada"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusDivida(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    public Cliente cliente;

    @Column(name = "cliente_nome", nullable = false, length = 200)
    @NotNull
    @Size(max = 200)
    public String clienteNome;

    @Column(name = "cliente_documento", length = 20)
    @Size(max = 20)
    public String clienteDocumento;

    @Column(name = "cliente_telefone", length = 20)
    @Size(max = 20)
    public String clienteTelefone;

    @Column(name = "venda_id")
    public UUID vendaId;

    @Column(name = "numero_venda", length = 30)
    @Size(max = 30)
    public String numeroVenda;

    @Column(name = "descricao", length = 500)
    @Size(max = 500)
    public String descricao;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    @DecimalMin("0.0")
    public BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusDivida status = StatusDivida.ABERTA;

    @Column(name = "data_lancamento", nullable = false)
    public LocalDateTime dataLancamento;

    @Column(name = "data_vencimento")
    public LocalDate dataVencimento;

    @Column(name = "data_quitacao")
    public LocalDateTime dataQuitacao;

    @Column(name = "operador", length = 100)
    @Size(max = 100)
    public String operador;

    @Column(name = "pdv_id")
    public UUID pdvId;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @PrePersist
    protected void onPersist() {
        if (dataLancamento == null) {
            dataLancamento = LocalDateTime.now();
        }
        if (valorTotal == null) {
            valorTotal = BigDecimal.ZERO;
        }
        if (valorPago == null) {
            valorPago = BigDecimal.ZERO;
        }
        atualizarStatus();
    }

    public BigDecimal getSaldoDevedor() {
        BigDecimal pago = valorPago != null ? valorPago : BigDecimal.ZERO;
        return valorTotal.subtract(pago);
    }

    public boolean isEmAberto() {
        return status == StatusDivida.ABERTA || status == StatusDivida.PARCIAL;
    }

    public boolean isVencida() {
        return isEmAberto()
            && dataVencimento != null
            && dataVencimento.isBefore(LocalDate.now());
    }

    public void atualizarStatus() {
        if (status == StatusDivida.CANCELADA) {
            return;
        }
        BigDecimal pago = valorPago != null ? valorPago : BigDecimal.ZERO;
        BigDecimal total = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        if (pago.compareTo(BigDecimal.ZERO) <= 0) {
            status = StatusDivida.ABERTA;
        } else if (pago.compareTo(total) >= 0) {
            status = StatusDivida.QUITADA;
            if (dataQuitacao == null) {
                dataQuitacao = LocalDateTime.now();
            }
        } else {
            status = StatusDivida.PARCIAL;
        }
    }
}
