package br.com.sistematenda.pdv.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class SituacaoClienteDTO {

    private UUID clienteId;
    private String clienteNome;
    private String clienteDocumento;
    private String clienteTelefone;
    private BigDecimal totalDivida;
    private Long quantidadeLancamentos;
    private Boolean possuiAtraso;
    private List<CadernoDividaDTO> dividas;
}
