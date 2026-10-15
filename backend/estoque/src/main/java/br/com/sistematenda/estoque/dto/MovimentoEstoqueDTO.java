package br.com.sistematenda.estoque.dto;

import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.estoque.entity.MovimentoEstoque.TipoMovimento;
import br.com.sistematenda.estoque.entity.MovimentoEstoque.OrigemDestino;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MovimentoEstoqueDTO {

    private UUID id;
    private UUID produtoFruteiraId;
    private UUID produtoBuffetId;
    private TipoMovimento tipo;
    private OrigemDestino origem;
    private OrigemDestino destino;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal quantidade;
    private BigDecimal custoUnitario;
    private BigDecimal valorTotal;
    private BigDecimal estoqueAnterior;
    private BigDecimal estoquePosterior;
    private String lote;
    private LocalDateTime dataValidade;
    private String documentoReferencia;
    private String numeroDocumento;
    private String observacoes;
    private String motivoDefeito;
    private String tipoDefeito;
    private String loteDefeituoso;
    private BigDecimal quantidadeRecuperavel;
    private String responsavel;
    private LocalDateTime data;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProdutoFruteiraId() { return produtoFruteiraId; }
    public void setProdutoFruteiraId(UUID produtoFruteiraId) { this.produtoFruteiraId = produtoFruteiraId; }

    public UUID getProdutoBuffetId() { return produtoBuffetId; }
    public void setProdutoBuffetId(UUID produtoBuffetId) { this.produtoBuffetId = produtoBuffetId; }

    public TipoMovimento getTipo() { return tipo; }
    public void setTipo(TipoMovimento tipo) { this.tipo = tipo; }

    public OrigemDestino getOrigem() { return origem; }
    public void setOrigem(OrigemDestino origem) { this.origem = origem; }

    public OrigemDestino getDestino() { return destino; }
    public void setDestino(OrigemDestino destino) { this.destino = destino; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public void setCustoUnitario(BigDecimal custoUnitario) { this.custoUnitario = custoUnitario; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getEstoqueAnterior() { return estoqueAnterior; }
    public void setEstoqueAnterior(BigDecimal estoqueAnterior) { this.estoqueAnterior = estoqueAnterior; }

    public BigDecimal getEstoquePosterior() { return estoquePosterior; }
    public void setEstoquePosterior(BigDecimal estoquePosterior) { this.estoquePosterior = estoquePosterior; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public LocalDateTime getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDateTime dataValidade) { this.dataValidade = dataValidade; }

    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getMotivoDefeito() { return motivoDefeito; }
    public void setMotivoDefeito(String motivoDefeito) { this.motivoDefeito = motivoDefeito; }

    public String getTipoDefeito() { return tipoDefeito; }
    public void setTipoDefeito(String tipoDefeito) { this.tipoDefeito = tipoDefeito; }

    public String getLoteDefeituoso() { return loteDefeituoso; }
    public void setLoteDefeituoso(String loteDefeituoso) { this.loteDefeituoso = loteDefeituoso; }

    public BigDecimal getQuantidadeRecuperavel() { return quantidadeRecuperavel; }
    public void setQuantidadeRecuperavel(BigDecimal quantidadeRecuperavel) { this.quantidadeRecuperavel = quantidadeRecuperavel; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
}