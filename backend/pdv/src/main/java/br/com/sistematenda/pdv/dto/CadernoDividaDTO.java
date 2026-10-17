package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.CadernoDivida;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class CadernoDividaDTO extends BaseDTO {

    private UUID clienteId;
    private String clienteNome;
    private String clienteDocumento;
    private String clienteTelefone;
    private UUID vendaId;
    private String numeroVenda;
    private String descricao;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private BigDecimal saldoDevedor;
    private CadernoDivida.StatusDivida status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataLancamento;

    private LocalDate dataVencimento;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataQuitacao;

    private String operador;
    private UUID pdvId;
    private String observacoes;
    private Boolean vencida;
}
