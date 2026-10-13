package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.common.enums.StatusVenda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VendaDTO extends BaseDTO {

    private String numero;
    private ComandaResumoDTO comanda;
    private String clienteNome;
    private String clienteDocumento;
    private StatusVenda status;
    private BigDecimal subtotalFruteira;
    private BigDecimal subtotalBuffet;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private BigDecimal troco;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFinalizacao;

    private String observacoes;
    private String operador;
    private UUID pdvId;
    private String numeroNfce;
    private String chaveNfce;
    private List<ItemVendaResumoDTO> itens;
    private List<PagamentoResumoDTO> pagamentos;

    public BigDecimal getSaldoDevedor() {
        return valorTotal != null ? valorTotal.subtract(valorPago != null ? valorPago : BigDecimal.ZERO) : BigDecimal.ZERO;
    }

    @Data
    public static class ComandaResumoDTO {
        private String id;
        private String numero;
        private String clienteNome;
    }

    @Data
    public static class ItemVendaResumoDTO {
        private String id;
        private String codigoProduto;
        private String nomeProduto;
        private OrigemItem origem;
        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal valorTotal;
    }

    @Data
    public static class PagamentoResumoDTO {
        private String id;
        private FormaPagamento formaPagamento;
        private BigDecimal valor;
        private String status;
    }
}