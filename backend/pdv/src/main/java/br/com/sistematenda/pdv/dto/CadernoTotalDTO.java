package br.com.sistematenda.pdv.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CadernoTotalDTO {

    private BigDecimal totalEmAberto;
    private BigDecimal totalRecebido;
    private Long quantidadeLancamentosEmAberto;
    private Long quantidadeClientesDevedores;
    private Long quantidadeLancamentosEmAtraso;
}
