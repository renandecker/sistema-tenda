package br.com.sistematenda.fiscal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    private BigDecimal valorTroco;

    private String naturezaOperacao;
    private String codigoCfop;
    private String codigoNcm;
    private String cstIcms;
    private String cstPis;
    private String cstCofins;
    private String meioPagamento;
    private String informacoesComplementares;

    private List<ItemNfceRequest> itens = new ArrayList<>();
    private List<PagamentoNfceRequest> pagamentos = new ArrayList<>();

    @Data
    public static class ItemNfceRequest {
        private Integer numeroItem;
        private String codigoProduto;
        private String descricao;
        private String ncm;
        private String cfop;
        private String cstIcms;
        private String cstPis;
        private String cstCofins;
        private String unidadeComercial;
        private BigDecimal quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal valorTotal;
        private BigDecimal valorDesconto;
        private Integer origemMercadoria;
    }

    @Data
    public static class PagamentoNfceRequest {
        private String formaPagamento;
        private String descricao;
        private BigDecimal valor;
    }
}
