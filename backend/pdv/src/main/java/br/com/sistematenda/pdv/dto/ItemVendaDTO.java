package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.ItemVenda;
import br.com.sistematenda.pdv.entity.Venda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemVendaDTO extends BaseDTO {

    private VendaResumoDTO venda;
    private String produtoFruteiraId;
    private String produtoBuffetId;
    private ItemVenda.OrigemItem origem;
    private String codigoProduto;
    private String nomeProduto;
    private ItemVenda.TipoItem tipo;
    private BigDecimal quantidade;
    private BigDecimal pesoBruto;
    private BigDecimal tara;
    private BigDecimal pesoLiquido;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;
    private String observacoes;
    private Boolean cancelado;

    @Data
    public static class VendaResumoDTO {
        private String id;
        private String numero;
        private String clienteNome;
    }
}