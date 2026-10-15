package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.Dre;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class DreDTO extends BaseDTO {

    private Dre.UnidadeNegocio unidadeNegocio;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Dre.StatusDre status;
    private BigDecimal receitaBruta;
    private BigDecimal receitaFruteira;
    private BigDecimal receitaBuffet;
    private BigDecimal receitaOutros;
    private BigDecimal devolucoes;
    private BigDecimal descontosConcedidos;
    private BigDecimal impostosVendas;
    private BigDecimal receitaLiquida;
    private BigDecimal cmv;
    private BigDecimal cmvFruteira;
    private BigDecimal cmvBuffet;
    private BigDecimal lucroBruto;
    private BigDecimal margemBrutaPercentual;
    private BigDecimal despesasVendas;
    private BigDecimal despesasAdministrativas;
    private BigDecimal despesasFinanceiras;
    private BigDecimal despesasOperacionaisTotal;
    private BigDecimal lucroOperacional;
    private BigDecimal receitasNaoOperacionais;
    private BigDecimal despesasNaoOperacionais;
    private BigDecimal resultadoNaoOperacional;
    private BigDecimal resultadoAntesIrCsll;
    private BigDecimal irpj;
    private BigDecimal csll;
    private BigDecimal lucroLiquido;
    private BigDecimal margemLiquidaPercentual;
    private String observacoes;
    private Integer mesesCalculados;
    private LocalDateTime ultimaAtualizacao;

    public static DreDTO fromEntity(Dre entity) {
        if (entity == null) return null;
        DreDTO dto = new DreDTO();
        dto.setId(entity.id);
        dto.setCreatedAt(entity.createdAt);
        dto.setUpdatedAt(entity.updatedAt);
        dto.unidadeNegocio = entity.unidadeNegocio;
        dto.dataInicio = entity.dataInicio;
        dto.dataFim = entity.dataFim;
        dto.status = entity.status;
        dto.receitaBruta = entity.receitaBruta;
        dto.receitaFruteira = entity.receitaFruteira;
        dto.receitaBuffet = entity.receitaBuffet;
        dto.receitaOutros = entity.receitaOutros;
        dto.devolucoes = entity.devolucoes;
        dto.descontosConcedidos = entity.descontosConcedidos;
        dto.impostosVendas = entity.impostosVendas;
        dto.receitaLiquida = entity.receitaLiquida;
        dto.cmv = entity.cmv;
        dto.cmvFruteira = entity.cmvFruteira;
        dto.cmvBuffet = entity.cmvBuffet;
        dto.lucroBruto = entity.lucroBruto;
        dto.margemBrutaPercentual = entity.margemBrutaPercentual;
        dto.despesasVendas = entity.despesasVendas;
        dto.despesasAdministrativas = entity.despesasAdministrativas;
        dto.despesasFinanceiras = entity.despesasFinanceiras;
        dto.despesasOperacionaisTotal = entity.despesasOperacionaisTotal;
        dto.lucroOperacional = entity.lucroOperacional;
        dto.receitasNaoOperacionais = entity.receitasNaoOperacionais;
        dto.despesasNaoOperacionais = entity.despesasNaoOperacionais;
        dto.resultadoNaoOperacional = entity.resultadoNaoOperacional;
        dto.resultadoAntesIrCsll = entity.resultadoAntesIrCsll;
        dto.irpj = entity.irpj;
        dto.csll = entity.csll;
        dto.lucroLiquido = entity.lucroLiquido;
        dto.margemLiquidaPercentual = entity.margemLiquidaPercentual;
        dto.observacoes = entity.observacoes;
        dto.mesesCalculados = entity.mesesCalculados;
        dto.ultimaAtualizacao = entity.ultimaAtualizacao;
        return dto;
    }
}