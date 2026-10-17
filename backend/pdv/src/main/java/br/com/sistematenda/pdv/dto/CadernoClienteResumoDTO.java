package br.com.sistematenda.pdv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CadernoClienteResumoDTO {

    private UUID clienteId;
    private String clienteNome;
    private String clienteDocumento;
    private String clienteTelefone;
    private BigDecimal totalDivida;
    private Long quantidadeLancamentos;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ultimaData;

    private Boolean possuiAtraso;
}
