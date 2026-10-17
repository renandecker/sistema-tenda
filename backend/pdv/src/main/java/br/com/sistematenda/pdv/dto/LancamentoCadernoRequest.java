package br.com.sistematenda.pdv.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LancamentoCadernoRequest {

    private UUID clienteId;
    private String clienteNome;
    private String clienteDocumento;
    private String clienteTelefone;
    private UUID vendaId;
    private String numeroVenda;
    private String descricao;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private String operador;
    private UUID pdvId;
    private String observacoes;
}
