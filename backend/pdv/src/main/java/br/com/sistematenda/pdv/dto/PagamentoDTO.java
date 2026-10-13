package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.common.enums.FormaPagamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PagamentoDTO extends BaseDTO {

    private VendaResumoDTO venda;
    private FormaPagamento formaPagamento;
    private String status;
    private BigDecimal valor;
    private BigDecimal valorTaxa;
    private BigDecimal valorLiquido;
    private String nsu;
    private String codigoAutorizacao;
    private String bandeiraCartao;
    private String ultimosDigitosCartao;
    private Integer parcelas;
    private String pixTxId;
    private String pixQrCode;
    private String valeNumero;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime valeValidade;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataPagamento;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAutorizacao;

    private String mensagemRetorno;
    private String comprovante;

    @Data
    public static class VendaResumoDTO {
        private String id;
        private String numero;
        private String clienteNome;
    }
}