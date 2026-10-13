package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.pdv.entity.Pdv;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SessaoCaixaDTO extends BaseDTO {

    private PdvResumoDTO pdv;
    private String operadorAbertura;
    private String operadorFechamento;
    private BigDecimal valorInicial;
    private BigDecimal valorFinalSistema;
    private BigDecimal valorFinalContado;
    private BigDecimal diferenca;
    private SessaoCaixa.StatusSessao status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAbertura;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFechamento;

    private String observacoesAbertura;
    private String observacoesFechamento;
    private BigDecimal sangriasTotal;
    private BigDecimal reforcosTotal;
    private BigDecimal vendasDinheiro;
    private BigDecimal vendasCartao;
    private BigDecimal vendasPix;
    private BigDecimal vendasVale;
    private BigDecimal totalVendas;
    private Long numeroUltimaVenda;
    private String reducaoZ;

    @Data
    public static class PdvResumoDTO {
        private String id;
        private String codigo;
        private String nome;
    }
}