package br.com.sistematenda.fiscal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dados de um DANFE vinculado a um CNPJ.
 */
@Data
public class DanfeArquivoDTO {

    private UUID id;
    private String chaveAcesso;
    private String modelo;
    private Integer serie;
    private Long numero;
    private String status;
    private String cnpj;
    private String tipoDocumento;
    private String razaoSocial;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataEmissao;

    private BigDecimal valorTotal;
    private String nomeArquivo;
    private boolean arquivoFisico;
}
