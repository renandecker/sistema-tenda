package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "recibo_lote", length = 20)
    @Size(max = 20)
    public String reciboLote;

    @Column(name = "data_autorizacao")
    public LocalDateTime dataAutorizacao;

    @Column(name = "codigo_status", length = 10)
    @Size(max = 10)
    public String codigoStatus;

    @Column(name = "versao_xml", length = 10)
    @Size(max = 10)
    public String versaoXml;

    @Column(name = "protocolo_cancelamento", length = 50)
    @Size(max = 50)
    public String protocoloCancelamento;

    @Column(name = "data_cancelamento")
    public LocalDateTime dataCancelamento;

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

    @Column(name = "codigo_cfop", length = 4)
    @Size(max = 4)
    public String codigoCfop;

    @Column(name = "codigo_ncm", length = 20)
    @Size(max = 20)
    public String codigoNcm;

    @Column(name = "cst_icms", length = 3)
    @Size(max = 3)
    public String cstIcms;

    @Column(name = "cst_pis", length = 2)
    @Size(max = 2)
    public String cstPis;

    @Column(name = "cst_cofins", length = 2)
    @Size(max = 2)
    public String cstCofins;

    @Column(name = "meio_pagamento", length = 20)
    @Size(max = 20)
    public String meioPagamento;

    @Column(name = "valor_desconto", precision = 15, scale = 4)
    public BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_acrescimo", precision = 15, scale = 4)
    public BigDecimal valorAcrescimo = BigDecimal.ZERO;

    @Column(name = "valor_troco", precision = 15, scale = 4)
    public BigDecimal valorTroco = BigDecimal.ZERO;

    @Column(name = "informacoes_complementares", columnDefinition = "TEXT")
    public String informacoesComplementares;

    @Column(name = "informacoes_fisco", columnDefinition = "TEXT")
    public String informacoesFisco;

    // Destinatario completo (NF-e modelo 55)
    @Column(name = "destinatario_ie", length = 20)
    public String destinatarioIe;

    @Column(name = "destinatario_logradouro", length = 60)
    public String destinatarioLogradouro;

    @Column(name = "destinatario_numero", length = 60)
    public String destinatarioNumero;

    @Column(name = "destinatario_complemento", length = 60)
    public String destinatarioComplemento;

    @Column(name = "destinatario_bairro", length = 60)
    public String destinatarioBairro;

    @Column(name = "destinatario_codigo_municipio")
    public Integer destinatarioCodigoMunicipio;

    @Column(name = "destinatario_municipio", length = 60)
    public String destinatarioMunicipio;

    @Column(name = "destinatario_uf", length = 2)
    public String destinatarioUf;

    @Column(name = "destinatario_cep", length = 8)
    public String destinatarioCep;

    @Column(name = "destinatario_email", length = 60)
    public String destinatarioEmail;

    // Transporte
    @Column(name = "modalidade_frete")
    public Integer modalidadeFrete = 9;

    @Column(name = "transportadora_nome", length = 60)
    public String transportadoraNome;

    @Column(name = "transportadora_documento", length = 20)
    public String transportadoraDocumento;

    @Column(name = "transportadora_ie", length = 20)
    public String transportadoraIe;

    @Column(name = "transportadora_endereco", length = 60)
    public String transportadoraEndereco;

    @Column(name = "transportadora_municipio", length = 60)
    public String transportadoraMunicipio;

    @Column(name = "transportadora_uf", length = 2)
    public String transportadoraUf;

    @Column(name = "volume_quantidade", precision = 15, scale = 4)
    public BigDecimal volumeQuantidade;

    @Column(name = "volume_especie", length = 60)
    public String volumeEspecie;

    @Column(name = "volume_peso_liquido", precision = 15, scale = 3)
    public BigDecimal volumePesoLiquido;

    @Column(name = "volume_peso_bruto", precision = 15, scale = 3)
    public BigDecimal volumePesoBruto;

    @Column(name = "finalidade_nfe")
    public Integer finalidadeNfe = 1;

    @Column(name = "tipo_operacao")
    public Integer tipoOperacao = 1;

    @Column(name = "consumidor_final")
    public Integer consumidorFinal = 1;

    @Column(name = "presenca_comprador")
    public Integer presencaComprador = 1;

    @Column(name = "tipo_emissao")
    public Integer tipoEmissao = 1;

    @OneToMany(mappedBy = "nfe", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<NfeItem> itens = new ArrayList<>();

    @OneToMany(mappedBy = "nfe", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<NfePagamento> pagamentos = new ArrayList<>();

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