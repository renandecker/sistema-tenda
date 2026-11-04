package br.com.sistematenda.estoque.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.estoque.entity.ProdutoEstoque;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProdutoEstoqueDTO extends BaseDTO {

    private String codigo;
    private String codigoBarras;
    private String nome;
    private String descricao;
    private ProdutoEstoque.Categoria categoria;
    private UnidadeMedida unidadeMedida;
    private BigDecimal custoMedio;
    private BigDecimal estoqueAtual;
    private BigDecimal estoqueMinimo;
    private BigDecimal estoqueMaximo;
    private Boolean ativo;
    private String observacoes;
}
