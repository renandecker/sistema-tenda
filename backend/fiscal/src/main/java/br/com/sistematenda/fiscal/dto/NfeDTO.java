package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.Nfe;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class NfeDTO extends BaseDTO {

    private String chaveAcesso;
    private String modelo;
    private Integer serie;
    private Long numero;
    private String status;
    private String naturezaOperacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataEmissao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataSaidaEntrada;

    private String clienteNome;
    private String clienteDocumento;
    private BigDecimal valorTotal;
    private BigDecimal valorIcms;
    private BigDecimal valorIpi;
    private BigDecimal valorPis;
    private BigDecimal valorCofins;
    private String protocoloAutorizacao;
    private Integer digitoValidador;
    private String xmlOriginal;
    private String xmlAutorizado;
    private String xmlCancelamento;
    private String motivoRejeicao;
    private Integer tentativasEnvio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ultimaTentativa;

    private String ambiente;
    private Boolean ehProdutoRural;
    private String cnpjProdutoRural;
    private String nomeProdutoRural;
    private UUID vendaId;

    private String informacoesFisco;
    private String informacoesComplementares;
    private String destinatarioIe;
    private String destinatarioLogradouro;
    private String destinatarioNumero;
    private String destinatarioComplemento;
    private String destinatarioBairro;
    private Integer destinatarioCodigoMunicipio;
    private String destinatarioMunicipio;
    private String destinatarioUf;
    private String destinatarioCep;
    private String destinatarioEmail;
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

    private List<ItemNfeDTO> itens = new ArrayList<>();
    private List<PagamentoNfeDTO> pagamentos = new ArrayList<>();

    public static NfeDTO fromEntity(Nfe entity) {
        NfeDTO dto = new NfeDTO();
        dto.setId(entity.id);
        dto.setCreatedAt(entity.createdAt);
        dto.setUpdatedAt(entity.updatedAt);
        dto.chaveAcesso = entity.chaveAcesso;
        dto.modelo = entity.modelo;
        dto.serie = entity.serie;
        dto.numero = entity.numero;
        dto.status = entity.status != null ? entity.status.name() : null;
        dto.naturezaOperacao = entity.naturezaOperacao;
        dto.dataEmissao = entity.dataEmissao;
        dto.dataSaidaEntrada = entity.dataSaidaEntrada;
        dto.clienteNome = entity.clienteNome;
        dto.clienteDocumento = entity.clienteDocumento;
        dto.valorTotal = entity.valorTotal;
        dto.valorIcms = entity.valorIcms;
        dto.valorIpi = entity.valorIpi;
        dto.valorPis = entity.valorPis;
        dto.valorCofins = entity.valorCofins;
        dto.protocoloAutorizacao = entity.protocoloAutorizacao;
        dto.digitoValidador = entity.digitoValidador;
        dto.xmlOriginal = entity.xmlOriginal;
        dto.xmlAutorizado = entity.xmlAutorizado;
        dto.xmlCancelamento = entity.xmlCancelamento;
        dto.motivoRejeicao = entity.motivoRejeicao;
        dto.tentativasEnvio = entity.tentativasEnvio;
        dto.ultimaTentativa = entity.ultimaTentativa;
        dto.ambiente = entity.ambiente;
        dto.ehProdutoRural = entity.ehProdutoRural;
        dto.cnpjProdutoRural = entity.cnpjProdutoRural;
        dto.nomeProdutoRural = entity.nomeProdutoRural;
        dto.vendaId = entity.vendaId;
        dto.informacoesFisco = entity.informacoesFisco;
        dto.informacoesComplementares = entity.informacoesComplementares;
        dto.destinatarioIe = entity.destinatarioIe;
        dto.destinatarioLogradouro = entity.destinatarioLogradouro;
        dto.destinatarioNumero = entity.destinatarioNumero;
        dto.destinatarioComplemento = entity.destinatarioComplemento;
        dto.destinatarioBairro = entity.destinatarioBairro;
        dto.destinatarioCodigoMunicipio = entity.destinatarioCodigoMunicipio;
        dto.destinatarioMunicipio = entity.destinatarioMunicipio;
        dto.destinatarioUf = entity.destinatarioUf;
        dto.destinatarioCep = entity.destinatarioCep;
        dto.destinatarioEmail = entity.destinatarioEmail;
        dto.modalidadeFrete = entity.modalidadeFrete;
        dto.transportadoraNome = entity.transportadoraNome;
        dto.transportadoraDocumento = entity.transportadoraDocumento;
        dto.transportadoraIe = entity.transportadoraIe;
        dto.transportadoraEndereco = entity.transportadoraEndereco;
        dto.transportadoraMunicipio = entity.transportadoraMunicipio;
        dto.transportadoraUf = entity.transportadoraUf;
        dto.volumeQuantidade = entity.volumeQuantidade;
        dto.volumeEspecie = entity.volumeEspecie;
        dto.volumePesoLiquido = entity.volumePesoLiquido;
        dto.volumePesoBruto = entity.volumePesoBruto;
        dto.finalidadeNfe = entity.finalidadeNfe;
        dto.tipoOperacao = entity.tipoOperacao;
        dto.consumidorFinal = entity.consumidorFinal;
        dto.presencaComprador = entity.presencaComprador;
        dto.tipoEmissao = entity.tipoEmissao;
        return dto;
    }

    public static NfeDTO fromEntityDetalhado(Nfe entity) {
        NfeDTO dto = fromEntity(entity);
        if (entity.itens != null) {
            dto.itens = entity.itens.stream().map(ItemNfeDTO::fromEntity).toList();
        }
        if (entity.pagamentos != null) {
            dto.pagamentos = entity.pagamentos.stream().map(PagamentoNfeDTO::fromEntity).toList();
        }
        return dto;
    }

    @Data
    public static class ItemNfeDTO {
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
        private BigDecimal valorIcms;
        private BigDecimal aliquotaIcms;
        private BigDecimal valorPis;
        private BigDecimal valorCofins;

        public static ItemNfeDTO fromEntity(br.com.sistematenda.fiscal.entity.NfeItem entity) {
            ItemNfeDTO dto = new ItemNfeDTO();
            dto.numeroItem = entity.numeroItem;
            dto.codigoProduto = entity.codigoProduto;
            dto.descricao = entity.descricao;
            dto.ncm = entity.ncm;
            dto.cfop = entity.cfop;
            dto.cstIcms = entity.cstIcms;
            dto.cstPis = entity.cstPis;
            dto.cstCofins = entity.cstCofins;
            dto.unidadeComercial = entity.unidadeComercial;
            dto.quantidade = entity.quantidade;
            dto.valorUnitario = entity.valorUnitario;
            dto.valorTotal = entity.valorTotal;
            dto.valorDesconto = entity.valorDesconto;
            dto.valorIcms = entity.valorIcms;
            dto.aliquotaIcms = entity.aliquotaIcms;
            dto.valorPis = entity.valorPis;
            dto.valorCofins = entity.valorCofins;
            return dto;
        }
    }

    @Data
    public static class PagamentoNfeDTO {
        private String formaPagamento;
        private String descricao;
        private BigDecimal valor;

        public static PagamentoNfeDTO fromEntity(br.com.sistematenda.fiscal.entity.NfePagamento entity) {
            PagamentoNfeDTO dto = new PagamentoNfeDTO();
            dto.formaPagamento = entity.formaPagamento;
            dto.descricao = entity.descricao;
            dto.valor = entity.valor;
            return dto;
        }
    }
}