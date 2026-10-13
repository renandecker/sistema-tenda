package br.com.sistematenda.buffet.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.buffet.entity.Receita;
import br.com.sistematenda.common.enums.UnidadeMedida;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReceitaDTO extends BaseDTO {

    private ProdutoBuffetResumoDTO produto;
    private BigDecimal rendimento;
    private UnidadeMedida unidadeRendimento;
    private Integer tempoPreparoMinutos;
    private Integer tempoCozimentoMinutos;
    private String dificuldade;
    private String instrucoes;
    private BigDecimal custoTotal;
    private BigDecimal margemDesejada;
    private BigDecimal precoSugerido;
    private Boolean ativa;
    private Integer versao;
    private String observacoes;
    private List<ReceitaIngredienteDTO> ingredientes;

    @Data
    public static class ProdutoBuffetResumoDTO {
        private String id;
        private String codigo;
        private String nome;
    }

    @Data
    public static class ReceitaIngredienteDTO {
        private String id;
        private ProdutoFruteiraResumoDTO produto;
        private BigDecimal quantidade;
        private UnidadeMedida unidade;
        private BigDecimal custoUnitario;
        private BigDecimal custoTotal;
        private Boolean obrigatorio;
        private Integer ordem;
        private String observacoes;

        @Data
        public static class ProdutoFruteiraResumoDTO {
            private String id;
            private String codigo;
            private String nome;
            private String unidadeMedida;
        }
    }
}