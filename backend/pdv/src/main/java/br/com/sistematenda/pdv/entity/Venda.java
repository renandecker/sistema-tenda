package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.FormaPagamento;
import br.com.sistematenda.common.enums.StatusVenda;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.fruteira.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pdv_venda", indexes = {
    @Index(name = "idx_pdv_venda_numero", columnList = "numero", unique = true),
    @Index(name = "idx_pdv_venda_status", columnList = "status"),
    @Index(name = "idx_pdv_venda_data", columnList = "data"),
    @Index(name = "idx_pdv_venda_cliente", columnList = "cliente_documento"),
    @Index(name = "idx_pdv_venda_comanda", columnList = "comanda_id"),
    @Index(name = "idx_pdv_venda_pdv", columnList = "pdv_id")
})
public class Venda extends BaseEntity {

    @Column(name = "numero", unique = true, nullable = false, length = 30)
    @NotNull
    @Size(max = 30)
    public String numero;

    @ManyToOne
    @JoinColumn(name = "comanda_id")
    public Comanda comanda;

    @Column(name = "cliente_nome", length = 200)
    @Size(max = 200)
    public String clienteNome;

    @Column(name = "cliente_documento", length = 20)
    @Size(max = 20)
    public String clienteDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusVenda status = StatusVenda.ABERTA;

    @Column(name = "subtotal_fruteira", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal subtotalFruteira = BigDecimal.ZERO;

    @Column(name = "subtotal_buffet", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal subtotalBuffet = BigDecimal.ZERO;

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

    @Column(name = "troco", precision = 15, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal troco = BigDecimal.ZERO;

    @Column(name = "data", nullable = false)
    @NotNull
    public LocalDateTime data;

    @Column(name = "data_finalizacao")
    public LocalDateTime dataFinalizacao;

    @Column(name = "observacoes", length = 1000)
    @Size(max = 1000)
    public String observacoes;

    @Column(name = "operador", length = 100)
    @Size(max = 100)
    public String operador;

    @Column(name = "pdv_id")
    public UUID pdvId;

    @Column(name = "numero_nfce", length = 20)
    @Size(max = 20)
    public String numeroNfce;

    @Column(name = "chave_nfce", length = 50)
    @Size(max = 50)
    public String chaveNfce;

    @Column(name = "xml_nfce", columnDefinition = "TEXT")
    public String xmlNfce;

    @Column(name = "danfe_nfce", columnDefinition = "BYTEA")
    public byte[] danfeNfce;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemVenda> itens;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Pagamento> pagamentos;

    public BigDecimal getSaldoDevedor() {
        return valorTotal.subtract(valorPago != null ? valorPago : BigDecimal.ZERO);
    }

    public boolean isFinalizada() {
        return status == StatusVenda.FINALIZADA || status == StatusVenda.PAGA;
    }

    public boolean isPaga() {
        return valorPago.compareTo(valorTotal) >= 0;
    }
}