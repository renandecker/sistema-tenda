package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.ConciliacaoCartao;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConciliacaoCartaoDTO extends BaseDTO {

    private LocalDate dataVenda;
    private LocalDate dataRecebimento;
    private String adquirente;
    private String bandeira;
    private String nsu;
    private String codigoAutorizacao;
    private BigDecimal valorBruto;
    private BigDecimal taxaAdesao;
    private BigDecimal taxaMdr;
    private BigDecimal valorTaxa;
    private BigDecimal valorLiquido;
    private BigDecimal valorRecebido;
    private BigDecimal diferenca;
    private ConciliacaoCartao.StatusConciliacao status;
    private UUID vendaId;
    private String numeroVenda;
    private String observacoes;
    private LocalDateTime dataConciliacao;
    private String conciliadoPor;

    public static ConciliacaoCartaoDTO fromEntity(ConciliacaoCartao entity) {
        if (entity == null) return null;
        ConciliacaoCartaoDTO dto = new ConciliacaoCartaoDTO();
        dto.setId(entity.id);
        dto.setCreatedAt(entity.createdAt);
        dto.setUpdatedAt(entity.updatedAt);
        dto.dataVenda = entity.dataVenda;
        dto.dataRecebimento = entity.dataRecebimento;
        dto.adquirente = entity.adquirente;
        dto.bandeira = entity.bandeira;
        dto.nsu = entity.nsu;
        dto.codigoAutorizacao = entity.codigoAutorizacao;
        dto.valorBruto = entity.valorBruto;
        dto.taxaAdesao = entity.taxaAdesao;
        dto.taxaMdr = entity.taxaMdr;
        dto.valorTaxa = entity.valorTaxa;
        dto.valorLiquido = entity.valorLiquido;
        dto.valorRecebido = entity.valorRecebido;
        dto.diferenca = entity.diferenca;
        dto.status = entity.status;
        dto.vendaId = entity.vendaId;
        dto.numeroVenda = entity.numeroVenda;
        dto.observacoes = entity.observacoes;
        dto.dataConciliacao = entity.dataConciliacao;
        dto.conciliadoPor = entity.conciliadoPor;
        return dto;
    }
}