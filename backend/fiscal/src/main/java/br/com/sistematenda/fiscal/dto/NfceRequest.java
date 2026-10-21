package br.com.sistematenda.fiscal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class NfceRequest {
    private UUID vendaId;
    private String clienteNome;
    private String clienteDocumento;
    private BigDecimal valorTotal;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal acrescimo;

    // Informações da operação
    private String naturezaOperacao;
    private String codigoCfop;
    private String codigoNcm;
    private String cstIcms;
    private String cstPis;
    private String cstCofins;
    private String meioPagamento;
}
