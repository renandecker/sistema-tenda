package br.com.sistematenda.fruteira.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.common.enums.CstIcms;
import br.com.sistematenda.common.enums.CstPisCofins;
import br.com.sistematenda.common.enums.TipoProduto;
import br.com.sistematenda.common.enums.UnidadeMedida;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProdutoDTO extends BaseDTO {

    private String codigo;
    private String codigoBarras;
    private String nome;
    private String descricao;
    private TipoProduto tipo;
    private UnidadeMedida unidadeMedida;
    private BigDecimal precoVenda;
    private BigDecimal precoAtacado;
    private Integer quantidadeMinimaAtacado;
    private BigDecimal custoMedio;
    private BigDecimal custoUltimo;
    private BigDecimal margemDesejada;
    private BigDecimal estoqueAtual;
    private BigDecimal estoqueMinimo;
    private BigDecimal estoqueMaximo;
    private BigDecimal pesoLiquidoPadrao;
    private BigDecimal taraPadrao;
    private Integer diasValidadeAlerta;
    private Boolean controlaValidade;
    private Boolean controlaLote;
    private Boolean permiteFracao;
    private Boolean ativo;
    private Boolean pesavelBalanca;
    private String codigoBalanca;
    private CstIcms cstIcms;
    private CstPisCofins cstPis;
    private CstPisCofins cstCofins;
    private String ncm;
    private String cest;
    private Integer origemMercadoria;
    private String observacoes;
    private String imagemUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private java.time.LocalDateTime updatedAt;
}