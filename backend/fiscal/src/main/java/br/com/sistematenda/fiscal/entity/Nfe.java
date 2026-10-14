package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fiscal_nfe", indexes = {
    @Index(name = "idx_fiscal_nfe_chave", columnList = "chave_acesso", unique = true),
    @Index(name = "idx_fiscal_nfe_numero", columnList = "numero"),
    @Index(name = "idx_fiscal_nfe_status", columnList = "status"),
    @Index(name = "idx_fiscal_nfe_data_emissao", columnList = "data_emissao"),
    @Index(name = "idx_fiscal_nfe_venda", columnList = "venda_id"),
    @Index(name = "idx_fiscal_nfe_produtor_rural", columnList = "eh_produtor_rural")
})
public class Nfe extends BaseEntity {

    public enum StatusNfe {
        PENDENTE("Pendente"),
        EM_PROCESSAMENTO("Em Processamento"),
        AUTORIZADA("Autorizada"),
        REJEITADA("Rejeitada"),
        CANCELADA("Cancelada"),
        DENEGADA("Denegada"),
        INUTILIZADA("Inutilizada"),
        // Status específico para produtor rural
        PRODUTOR_RURAL("Produtor Rural");

        private final String descricao;
        StatusNfe(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum ModeloDocumento {
        NFE("55", "NF-e"),
        NFCE("65", "NFC-e"),
        SAT("59", "SAT/CFe"),
        // Documento do produtor rural
        DANFE_PRODUTOR_RURAL("89", "DANFE Produtor Rural");

        private final String codigo;
        private final String descricao;
        ModeloDocumento(String codigo, String descricao) { 
            this.codigo = codigo; 
            this.descricao = descricao; 
        }
        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "chave_acesso", unique = true, length = 50)
    @NotNull
    @Size(max = 50)
    public String chaveAcesso;

    @Column(name = "modelo", length = 2)
    @Size(max = 2)
    public String modelo = "65"; // NFC-e padrão

    @Column(name = "serie", nullable = false)
    @NotNull
    public Integer serie;

    @Column(name = "numero", nullable = false)
    @NotNull
    public Long numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusNfe status = StatusNfe.PENDENTE;

    @Column(name = "natureza_operacao", length = 60)
    @Size(max = 60)
    public String naturezaOperacao = "VENDA";

    @Column(name = "data_emissao", nullable = false)
    @NotNull
    public LocalDateTime dataEmissao;

    @Column(name = "data_saida_entrada")
    public LocalDateTime dataSaidaEntrada;

    @Column(name = "cliente_nome", length = 200)
    @Size(max = 200)
    public String clienteNome;

    @Column(name = "cliente_documento", length = 20)
    @Size(max = 20)
    public String clienteDocumento;

    @Column(name = "valor_total", precision = 15, scale = 4)
    public BigDecimal valorTotal;

    @Column(name = "valor_icms", precision = 15, scale = 4)
    public BigDecimal valorIcms;

    @Column(name = "valor_ipi", precision = 15, scale = 4)
    public BigDecimal valorIpi;

    @Column(name = "valor_pis", precision = 15, scale = 4)
    public BigDecimal valorPis;

    @Column(name = "valor_cofins", precision = 15, scale = 4)
    public BigDecimal valorCofins;

    @Column(name = "protocolo_autorizacao", length = 50)
    @Size(max = 50)
    public String protocoloAutorizacao;

    @Column(name = "digito_validador")
    public Integer digitoValidador;

    @Column(name = "xml_original", columnDefinition = "TEXT")
    public String xmlOriginal;

    @Column(name = "xml_autorizado", columnDefinition = "TEXT")
    public String xmlAutorizado;

    @Column(name = "xml_cancelamento", columnDefinition = "TEXT")
    public String xmlCancelamento;

    @Column(name = "motivo_rejeicao", length = 500)
    @Size(max = 500)
    public String motivoRejeicao;

    @Column(name = "tentativas_envio")
    public Integer tentativasEnvio = 0;

    @Column(name = "ultima_tentativa")
    public LocalDateTime ultimaTentativa;

    @Column(name = "danfe", columnDefinition = "BYTEA")
    public byte[] danfe;

    @Column(name = "venda_id")
    public UUID vendaId;

    @Column(name = "ambiente", length = 20)
    @Size(max = 20)
    public String ambiente = "HOMOLOGACAO";

    // --- Campos para Produtor Rural ---
    
    @Column(name = "eh_produtor_rural")
    public Boolean ehProdutoRural = false;

    @Column(name = "cnpj_produtor", length = 18)
    @Size(max = 18)
    public String cnpjProdutoRural;

    @Column(name = "nome_produtor", length = 200)
    @Size(max = 200)
    public String nomeProdutoRural;

    @Column(name = "rural_ie", length = 20)
    @Size(max = 20)
    public String inscricaoEstadualRural;

    @Column(name = "modalidade_acre", length = 2)
    @Size(max = 2)
    public String modalidadeAcre = "0"; // 0 - Normal, 1 - Substituição Tributária

    @Column(name = "cfop_produtor", length = 4)
    @Size(max = 4)
    public String cfopProdutoRural = "5102"; // CFOP para produção própria

    @Column(name = "obs_produtor_rural", columnDefinition = "TEXT")
    public String observacoesProdutoRural;

    @Column(name = "tipo_producao", length = 50)
    @Size(max = 50)
    public String tipoProducao; // EX: "AGRICOLA", "PECUARIA", "APICULTURA", "OUTROS"

    @Column(name = "grade_producao", length = 100)
    @Size(max = 100)
    public String gradeProducao; // Ex: "Soja", "Milho", "Gado", etc.

    @Column(name = "area_producao", precision = 15, scale = 4)
    public BigDecimal areaProducao;

    @Column(name = "volume_producao", precision = 15, scale = 4)
    public BigDecimal volumeProducao;

    @Column(name = "data_colheita")
    public LocalDate dataColheita;
}