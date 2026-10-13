package br.com.sistematenda.fruteira.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class TabelaPrecoDTO extends BaseDTO {

    private ProdutoResumoDTO produto;
    private BigDecimal precoVenda;
    private BigDecimal precoAtacado;
    private Integer quantidadeMinimaAtacado;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dataInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dataFim;

    private Boolean ativa;
    private String origem;
    private String observacoes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime aplicadaEm;

    private String aplicadaPor;

    @Data
    public static class ProdutoResumoDTO {
        private String id;
        private String codigo;
        private String nome;
    }
}