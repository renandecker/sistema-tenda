package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.fruteira.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fiscal_nota_entrada", indexes = {
    @Index(name = "idx_fiscal_nota_entrada_chave", columnList = "chave_acesso", unique = true),
    @Index(name = "idx_fiscal_nota_entrada_fornecedor", columnList = "fornecedor_documento"),
    @Index(name = "idx_fiscal_nota_entrada_data", columnList = "data_emissao"),
    @Index(name = "idx_fiscal_nota_entrada_status", columnList = "status")
})
public class NotaFiscalEntrada extends BaseEntity {

    public enum StatusNotaEntrada {
        IMPORTADA("Importada"),
        PROCESSADA("Processada"),
        ERRO("Erro"),
        CANCELADA("Cancelada");

        private final String descricao;
        StatusNotaEntrada(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "chave_acesso", unique = true, length = 50)
    @NotNull
    @Size(max = 50)
    public String chaveAcesso;

    @Column(name = "modelo", length = 2)
    @Size(max = 2)
    public String modelo;

    @Column(name = "serie")
    public Integer serie;

    @Column(name = "numero")
    public Long numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    public StatusNotaEntrada status = StatusNotaEntrada.IMPORTADA;

    @Column(name = "fornecedor_nome", length = 200)
    @Size(max = 200)
    public String fornecedorNome;

    @Column(name = "fornecedor_documento", length = 20)
    @Size(max = 20)
    public String fornecedorDocumento;

    @Column(name = "fornecedor_ie", length = 20)
    @Size(max = 20)
    public String fornecedorIe;

    @Column(name = "data_emissao", nullable = false)
    @NotNull
    public LocalDateTime dataEmissao;

    @Column(name = "data_entrada")
    public LocalDateTime dataEntrada;

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

    @Column(name = "xml_original", columnDefinition = "TEXT")
    public String xmlOriginal;

    @Column(name = "protocolo_autorizacao", length = 50)
    @Size(max = 50)
    public String protocoloAutorizacao;

    @Column(name = "data_importacao")
    public LocalDateTime dataImportacao;

    @Column(name = "erro_importacao", length = 1000)
    @Size(max = 1000)
    public String erroImportacao;

    @Column(name = "processada", nullable = false)
    public boolean processada = false;

    @Column(name = "data_processamento")
    public LocalDateTime dataProcessamento;

    @OneToMany(mappedBy = "notaEntrada", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ItemNotaEntrada> itens;
}