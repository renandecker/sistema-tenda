package br.com.sistematenda.buffet.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.ItemComanda;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.common.enums.ModalidadeBuffet;
import br.com.sistematenda.common.enums.StatusComanda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ComandaDTO extends BaseDTO {

    private String numero;
    private String clienteNome;
    private String clienteDocumento;
    private String clienteTelefone;
    private ModalidadeBuffet modalidade;
    private StatusComanda status;
    private MesaResumoDTO mesa;
    private BigDecimal valorFixo;
    private BigDecimal precoKg;
    private BigDecimal taraPadrao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAbertura;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFechamento;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataPagamento;

    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private String observacoes;
    private String abertaPor;
    private String fechadaPor;
    private String canceladaPor;
    private String motivoCancelamento;
    private List<ItemComandaResumoDTO> itens;

    public BigDecimal getSaldoDevedor() {
        return valorTotal != null ? valorTotal.subtract(valorPago != null ? valorPago : BigDecimal.ZERO) : BigDecimal.ZERO;
    }

    public boolean isFechada() {
        return status == StatusComanda.FECHADA || status == StatusComanda.PAGA;
    }

    public boolean isPaga() {
        return status == StatusComanda.PAGA;
    }

    @Data
    public static class MesaResumoDTO {
        private String id;
        private String numero;
        private String nome;
    }

    @Data
    public static class ItemComandaResumoDTO {
        private String id;
        private String nomeProduto;
        private ItemComanda.TipoItem tipo;
        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal valorTotal;
    }
}