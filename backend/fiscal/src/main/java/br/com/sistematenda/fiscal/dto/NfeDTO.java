package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.Nfe;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        return dto;
    }
}