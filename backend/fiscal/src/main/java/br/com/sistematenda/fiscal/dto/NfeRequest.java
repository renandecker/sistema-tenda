package br.com.sistematenda.fiscal.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class NfeRequest {
    private UUID vendaId;
    private String naturezaOperacao;
    private String informacoesComplementares;
    private String informacoesFisco;

    private String clienteNome;
    private String clienteDocumento;
    private String clienteIe;
    private String clienteLogradouro;
    private String clienteNumero;
    private String clienteComplemento;
    private String clienteBairro;
    private Integer clienteCodigoMunicipio;
    private String clienteMunicipio;
    private String clienteUf;
    private String clienteCep;
    private String clienteEmail;

    private BigDecimal valorTotal;
    private BigDecimal desconto;
    private BigDecimal acrescimo;
    private BigDecimal valorTroco;

    private Integer modalidadeFrete;
    private String transportadoraNome;
    private String transportadoraDocumento;
    private String transportadoraIe;
    private String transportadoraEndereco;
    private String transportadoraMunicipio;
    private String transportadoraUf;
    private BigDecimal volumeQuantidade;
    private String volumeEspecie;
    private BigDecimal volumePesoLiquido;
    private BigDecimal volumePesoBruto;

    private Integer finalidadeNfe;
    private Integer tipoOperacao;
    private Integer consumidorFinal;
    private Integer presencaComprador;
    private Integer tipoEmissao;

    private List<NfceRequest.ItemNfceRequest> itens = new ArrayList<>();
    private List<NfceRequest.PagamentoNfceRequest> pagamentos = new ArrayList<>();
}
