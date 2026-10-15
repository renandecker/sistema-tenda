package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.AtivoImobilizado;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AtivoImobilizadoDTO extends BaseDTO {

    private String codigo;
    private String descricao;
    private AtivoImobilizado.TipoAtivo tipo;
    private AtivoImobilizado.UnidadeNegocio unidadeNegocio;
    private AtivoImobilizado.StatusAtivo status;
    private LocalDate dataAquisicao;
    private BigDecimal valorAquisicao;
    private Integer vidaUtilMeses;
    private String metodoDepreciacao;
    private BigDecimal taxaDepreciacaoAnual;
    private BigDecimal valorResidual;
    private BigDecimal depreciacaoAcumulada;
    private BigDecimal valorLiquido;
    private LocalDate dataInicioDepreciacao;
    private LocalDate dataFimDepreciacao;
    private LocalDate ultimaDepreciacao;
    private String fornecedor;
    private String notaFiscal;
    private String localizacao;
    private String responsavel;
    private String observacoes;
    private LocalDate dataBaixa;
    private String motivoBaixa;
    private BigDecimal valorBaixa;

    public static AtivoImobilizadoDTO fromEntity(AtivoImobilizado entity) {
        if (entity == null) return null;
        AtivoImobilizadoDTO dto = new AtivoImobilizadoDTO();
        dto.setId(entity.id);
        dto.setCreatedAt(entity.createdAt);
        dto.setUpdatedAt(entity.updatedAt);
        dto.codigo = entity.codigo;
        dto.descricao = entity.descricao;
        dto.tipo = entity.tipo;
        dto.unidadeNegocio = entity.unidadeNegocio;
        dto.status = entity.status;
        dto.dataAquisicao = entity.dataAquisicao;
        dto.valorAquisicao = entity.valorAquisicao;
        dto.vidaUtilMeses = entity.vidaUtilMeses;
        dto.metodoDepreciacao = entity.metodoDepreciacao;
        dto.taxaDepreciacaoAnual = entity.taxaDepreciacaoAnual;
        dto.valorResidual = entity.valorResidual;
        dto.depreciacaoAcumulada = entity.depreciacaoAcumulada;
        dto.valorLiquido = entity.valorLiquido;
        dto.dataInicioDepreciacao = entity.dataInicioDepreciacao;
        dto.dataFimDepreciacao = entity.dataFimDepreciacao;
        dto.ultimaDepreciacao = entity.ultimaDepreciacao;
        dto.fornecedor = entity.fornecedor;
        dto.notaFiscal = entity.notaFiscal;
        dto.localizacao = entity.localizacao;
        dto.responsavel = entity.responsavel;
        dto.observacoes = entity.observacoes;
        dto.dataBaixa = entity.dataBaixa;
        dto.motivoBaixa = entity.motivoBaixa;
        dto.valorBaixa = entity.valorBaixa;
        return dto;
    }
}