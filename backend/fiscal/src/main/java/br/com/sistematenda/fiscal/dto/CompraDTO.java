package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.Compra;
import br.com.sistematenda.common.entity.Fornecedor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompraDTO extends BaseDTO {

    private String numeroNota;
    private Integer serieNota;
    private LocalDate dataCompra;
    private String fornecedor;
    private BigDecimal valorTotal;
    private BigDecimal valorIcms;
    private BigDecimal valorIpi;
    private BigDecimal valorPis;
    private BigDecimal valorCofins;
    private BigDecimal desconto;
    private BigDecimal frete;
    private BigDecimal outrasDespesas;
    private StatusCompra status;
    private String condicaoPagamento;
    private String nomeFornecedor;
    private String cnpjFornecedor;
    private String observacoes;
    private List<ItemCompraDTO> itens;

    public enum StatusCompra {
        ORCAMENTO("Orçamento"),
        CONFIRMADA("Confirmada"),
        RECEBIDA("Recebida"),
        CANCELADA("Cancelada");

        private final String descricao;
        StatusCompra(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Data
    public static class ItemCompraDTO {
        private String codigoProduto;
        private String nomeProduto;
        private BigDecimal quantidade;
        private String unidade;
        private BigDecimal valorUnitario;
        private BigDecimal valorTotal;
        private String codigoNcm;
        private String cstIcms;
        private String cfop;
        private BigDecimal precoReferenciaCeasa;
        private LocalDate dataCotacaoCeasa;
        private String observacoes;
    }
}