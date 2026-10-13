package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.FormaPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdv_pagamento", indexes = {
    @Index(name = "idx_pdv_pagamento_venda", columnList = "venda_id"),
    @Index(name = "idx_pdv_pagamento_forma", columnList = "forma_pagamento"),
    @Index(name = "idx_pdv_pagamento_nsu", columnList = "nsu")
})
public class Pagamento extends BaseEntity {

    public enum StatusPagamento {
        PENDENTE("Pendente"),
        AUTORIZADO("Autorizado"),
        NEGADO("Negado"),
        CANCELADO("Cancelado"),
        ESTORNADO("Estornado");

        private final String descricao;
        StatusPagamento(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    @NotNull
    public Venda venda;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    @NotNull
    public FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "valor", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    public BigDecimal valor;

    @Column(name = "valor_taxa", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorTaxa;

    @Column(name = "valor_liquido", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal valorLiquido;

    @Column(name = "nsu", length = 50)
    @Size(max = 50)
    public String nsu;

    @Column(name = "codigo_autorizacao", length = 50)
    @Size(max = 50)
    public String codigoAutorizacao;

    @Column(name = "bandeira_cartao", length = 30)
    @Size(max = 30)
    public String bandeiraCartao;

    @Column(name = "ultimos_digitos_cartao", length = 4)
    @Size(max = 4)
    public String ultimosDigitosCartao;

    @Column(name = "parcelas")
    public Integer parcelas = 1;

    @Column(name = "pix_tx_id", length = 100)
    @Size(max = 100)
    public String pixTxId;

    @Column(name = "pix_qr_code", columnDefinition = "TEXT")
    public String pixQrCode;

    @Column(name = "vale_numero", length = 50)
    @Size(max = 50)
    public String valeNumero;

    @Column(name = "vale_validade")
    public LocalDateTime valeValidade;

    @Column(name = "data_pagamento", nullable = false)
    @NotNull
    public LocalDateTime dataPagamento;

    @Column(name = "data_autorizacao")
    public LocalDateTime dataAutorizacao;

    @Column(name = "mensagem_retorno", length = 500)
    @Size(max = 500)
    public String mensagemRetorno;

    @Column(name = "comprovante", columnDefinition = "TEXT")
    public String comprovante;

    @PrePersist
    protected void onCreate() {
        if (dataPagamento == null) {
            dataPagamento = LocalDateTime.now();
        }
        if (valorLiquido == null) {
            valorLiquido = valor.subtract(valorTaxa != null ? valorTaxa : BigDecimal.ZERO);
        }
    }
}