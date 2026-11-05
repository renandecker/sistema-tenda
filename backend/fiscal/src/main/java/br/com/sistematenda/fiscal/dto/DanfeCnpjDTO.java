package br.com.sistematenda.fiscal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agrupamento de DANFEs por CNPJ do destinatario.
 */
@Data
public class DanfeCnpjDTO {

    private String cnpj;
    /** CNPJ, CPF ou DOC, conforme o tamanho do documento agrupado. */
    private String tipoDocumento;
    private String razaoSocial;
    private long totalDanfes;
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ultimaEmissao;

    /** Indica se o cadastro do CNPJ veio da tabela de clientes. */
    private boolean clienteCadastrado;
}
