package br.com.sistematenda.fruteira.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fruteira.entity.EtiquetaBalanca;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class EtiquetaBalancaDTO extends BaseDTO {

    private String codigo;
    private ProdutoResumoDTO produto;
    private BigDecimal preco;
    private Integer validadeDias;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate dataValidade;

    private BigDecimal tara;
    private String textoLivre1;
    private String textoLivre2;
    private Boolean imprimirCodigoBarras;
    private Boolean imprimirQrCode;
    private String modeloEtiqueta;
    private Boolean ativa;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime ultimaImpressao;

    private Integer quantidadeImpressa;

    @Data
    public static class ProdutoResumoDTO {
        private String id;
        private String codigo;
        private String nome;
        private String unidadeMedida;
    }
}