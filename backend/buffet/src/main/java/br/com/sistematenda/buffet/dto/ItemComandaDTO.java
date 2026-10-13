package br.com.sistematenda.buffet.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.buffet.entity.ItemComanda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemComandaDTO extends BaseDTO {

    private ComandaResumoDTO comanda;
    private String produtoId;
    private String codigoProduto;
    private String nomeProduto;
    private ItemComanda.TipoItem tipo;
    private BigDecimal quantidade;
    private BigDecimal pesoBruto;
    private BigDecimal tara;
    private BigDecimal pesoLiquido;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;
    private String observacoes;
    private String lancadoPor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataLancamento;

    private Boolean cancelado;
    private String motivoCancelamento;
    private String canceladoPor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCancelamento;

    @Data
    public static class ComandaResumoDTO {
        private String id;
        private String numero;
        private String clienteNome;
    }
}