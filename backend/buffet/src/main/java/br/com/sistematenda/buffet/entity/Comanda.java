package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.ModalidadeBuffet;
import br.com.sistematenda.common.enums.StatusComanda;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "buffet_comanda", indexes = {
    @Index(name = "idx_buffet_comanda_numero", columnList = "numero"),
    @Index(name = "idx_buffet_comanda_status", columnList = "status"),
    @Index(name = "idx_buffet_comanda_cliente", columnList = "cliente_nome"),
    @Index(name = "idx_buffet_comanda_data", columnList = "data_abertura"),
    @Index(name = "idx_buffet_comanda_mesa", columnList = "mesa_id")
})
public class Comanda extends BaseEntity {

    @Column(name = "numero", unique = true, nullable = false, length = 20)
    @NotNull
    @Size(max = 20)
    public String numero;

    @Column(name = "cliente_nome", length = 200)
    @Size(max = 200)
    public String clienteNome;

    @Column(name = "cliente_documento", length = 20)
    @Size(max = 20)
    public String clienteDocumento;

    @Column(name = "cliente_telefone", length = 20)
    @Size(max = 20)
    public String clienteTelefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", nullable = false, length = 20)
    @NotNull
    public ModalidadeBuffet modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusComanda status = StatusComanda.ABERTA;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    public Mesa mesa;

    @Column(name = "valor_fixo", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorFixo;

    @Column(name = "preco_kg", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal precoKg;

    @Column(name = "tara_padrao", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal taraPadrao = BigDecimal.ZERO;

    @Column(name = "data_abertura", nullable = false)
    @NotNull
    public LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    public LocalDateTime dataFechamento;

    @Column(name = "data_pagamento")
    public LocalDateTime dataPagamento;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "desconto", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "acrescimo", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal acrescimo = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "valor_pago", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @Column(name = "aberta_por", length = 100)
    @Size(max = 100)
    public String abertaPor;

    @Column(name = "fechada_por", length = 100)
    @Size(max = 100)
    public String fechadaPor;

    @Column(name = "cancelada_por", length = 100)
    @Size(max = 100)
    public String canceladaPor;

    @Column(name = "motivo_cancelamento", length = 500)
    @Size(max = 500)
    public String motivoCancelamento;

    public BigDecimal getSaldoDevedor() {
        return valorTotal.subtract(valorPago);
    }

    public boolean isFechada() {
        return status == StatusComanda.FECHADA || status == StatusComanda.PAGA;
    }

    public boolean isPaga() {
        return status == StatusComanda.PAGA;
    }

    public boolean isAberta() {
        return status == StatusComanda.ABERTA || status == StatusComanda.EM_USO;
    }
}