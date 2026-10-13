package br.com.sistematenda.fruteira.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fruteira.entity.Perda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PerdaDTO extends BaseDTO {

    private ProdutoResumoDTO produto;
    private Perda.TipoPerda tipo;
    private BigDecimal quantidade;
    private BigDecimal custoUnitario;
    private BigDecimal valorTotal;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate data;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime hora;

    private String lote;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dataValidade;

    private String motivo;
    private String responsavel;
    private String autorizadoPor;
    private String observacoes;
    private Boolean baixaEstoqueRealizada;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime dataBaixaEstoque;

    @Data
    public static class ProdutoResumoDTO {
        private String id;
        private String codigo;
        private String nome;
        private String unidadeMedida;
    }
}