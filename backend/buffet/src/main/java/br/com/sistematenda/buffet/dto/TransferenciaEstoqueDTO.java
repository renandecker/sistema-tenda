package br.com.sistematenda.buffet.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.buffet.entity.TransferenciaEstoque;
import br.com.sistematenda.common.enums.OrigemProduto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransferenciaEstoqueDTO extends BaseDTO {

    private ProdutoFruteiraResumoDTO produto;
    private BigDecimal quantidade;
    private BigDecimal custoUnitario;
    private BigDecimal valorTotal;
    private OrigemProduto origem;
    private OrigemProduto destino;
    private TransferenciaEstoque.StatusTransferencia status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataConclusao;

    private String solicitadoPor;
    private String aprovadoPor;
    private String recebidoPor;
    private String observacoes;
    private String motivoRejeicao;

    @Data
    public static class ProdutoFruteiraResumoDTO {
        private String id;
        private String codigo;
        private String nome;
        private String unidadeMedida;
    }
}