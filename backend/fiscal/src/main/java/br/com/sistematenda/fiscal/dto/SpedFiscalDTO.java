package br.com.sistematenda.fiscal.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.fiscal.entity.SpedFiscal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpedFiscalDTO extends BaseDTO {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private SpedFiscal.StatusSped status;
    private String versaoLeiaute;
    private String finalidade;
    private String perfilApresentacao;
    private String indicadorMovimento;
    private String arquivoGerado;
    private String arquivoValidado;
    private String protocoloEnvio;
    private LocalDateTime dataGeracao;
    private LocalDateTime dataValidacao;
    private LocalDateTime dataTransmissao;
    private String errosValidacao;
    private Long totalRegistros;
    private Long tamanhoArquivo;

    public static SpedFiscalDTO fromEntity(SpedFiscal entity) {
        if (entity == null) return null;
        SpedFiscalDTO dto = new SpedFiscalDTO();
        dto.setId(entity.id);
        dto.setCreatedAt(entity.createdAt);
        dto.setUpdatedAt(entity.updatedAt);
        dto.dataInicio = entity.dataInicio;
        dto.dataFim = entity.dataFim;
        dto.status = entity.status;
        dto.versaoLeiaute = entity.versaoLeiaute;
        dto.finalidade = entity.finalidade;
        dto.perfilApresentacao = entity.perfilApresentacao;
        dto.indicadorMovimento = entity.indicadorMovimento;
        dto.arquivoGerado = entity.arquivoGerado;
        dto.arquivoValidado = entity.arquivoValidado;
        dto.protocoloEnvio = entity.protocoloEnvio;
        dto.dataGeracao = entity.dataGeracao;
        dto.dataValidacao = entity.dataValidacao;
        dto.dataTransmissao = entity.dataTransmissao;
        dto.errosValidacao = entity.errosValidacao;
        dto.totalRegistros = entity.totalRegistros;
        dto.tamanhoArquivo = entity.tamanhoArquivo;
        return dto;
    }
}