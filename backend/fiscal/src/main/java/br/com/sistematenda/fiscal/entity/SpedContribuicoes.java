package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "fiscal_sped_contribuicoes", indexes = {
    @Index(name = "idx_fiscal_sped_contrib_periodo", columnList = "data_inicio, data_fim"),
    @Index(name = "idx_fiscal_sped_contrib_status", columnList = "status")
})
public class SpedContribuicoes extends BaseEntity {

    public enum StatusSped {
        GERANDO("Gerando"),
        GERADO("Gerado"),
        VALIDADO("Validado"),
        TRANSMITIDO("Transmitido"),
        ERRO("Erro");

        private final String descricao;
        StatusSped(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "data_inicio", nullable = false)
    @NotNull
    public LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    @NotNull
    public LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusSped status = StatusSped.GERANDO;

    @Column(name = "versao_leiaute", length = 10)
    @Size(max = 10)
    public String versaoLeiaute = "014";

    @Column(name = "indicador_movimento", length = 1)
    public String indicadorMovimento = "0";

    @Column(name = "arquivo_gerado", columnDefinition = "TEXT")
    public String arquivoGerado;

    @Column(name = "arquivo_validado", columnDefinition = "TEXT")
    public String arquivoValidado;

    @Column(name = "protocolo_envio", length = 50)
    @Size(max = 50)
    public String protocoloEnvio;

    @Column(name = "data_geracao")
    public java.time.LocalDateTime dataGeracao;

    @Column(name = "data_validacao")
    public java.time.LocalDateTime dataValidacao;

    @Column(name = "data_transmissao")
    public java.time.LocalDateTime dataTransmissao;

    @Column(name = "erros_validacao", columnDefinition = "TEXT")
    public String errosValidacao;

    @Column(name = "total_registros")
    public Long totalRegistros = 0L;

    @Column(name = "tamanho_arquivo")
    public Long tamanhoArquivo = 0L;
}