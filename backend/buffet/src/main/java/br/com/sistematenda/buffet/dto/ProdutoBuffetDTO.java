package br.com.sistematenda.buffet.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.common.enums.CstIcms;
import br.com.sistematenda.common.enums.CstPisCofins;
import br.com.sistematenda.common.enums.UnidadeMedida;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProdutoBuffetDTO extends BaseDTO {

    private String codigo;
    private String codigoBarras;
    private String nome;
    private String descricao;
    private ProdutoBuffet.Categoria categoria;
    private UnidadeMedida unidadeMedida;
    private BigDecimal precoVenda;
    private BigDecimal custoMedio;
    private Boolean controlaEstoque;
    private BigDecimal estoqueAtual;
    private BigDecimal estoqueMinimo;
    private Boolean permiteFracao;
    private Boolean ativo;
    private Boolean disponivelBalcao;
    private Integer tempoPreparoMinutos;
    private Integer calorias;
    private String alergenicos;
    private String informacoesNutricionais;
    private CstIcms cstIcms;
    private CstPisCofins cstPis;
    private CstPisCofins cstCofins;
    private String ncm;
    private String cest;
    private Integer origemMercadoria;
    private String observacoes;
    private String imagemUrl;
}