package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.MovimentoCaixa;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.pdv.entity.Venda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MovimentoCaixaDTO extends BaseDTO {

    private SessaoCaixaResumoDTO sessaoCaixa;
    private MovimentoCaixa.TipoMovimento tipo;
    private BigDecimal valor;
    private String descricao;
    private String operador;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime data;

    private UUID vendaId;

    @Data
    public static class SessaoCaixaResumoDTO {
        private String id;
        private String pdvCodigo;
        private String operadorAbertura;
    }
}