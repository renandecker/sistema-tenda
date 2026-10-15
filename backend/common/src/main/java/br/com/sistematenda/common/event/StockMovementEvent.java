package br.com.sistematenda.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovementEvent {

    public enum MovementType {
        ENTRADA_COMPRA,
        ENTRADA_TRANSFERENCIA,
        ENTRADA_AJUSTE,
        ENTRADA_DEVOLUCAO,
        SAIDA_VENDA,
        SAIDA_TRANSFERENCIA,
        SAIDA_PERDA,
        SAIDA_AJUSTE,
        SAIDA_CONSUMO_INTERNO
    }

    public enum OriginDest {
        FRUTEIRA,
        BUFFET,
        FORNECEDOR,
        CLIENTE,
        PERDA,
        AJUSTE,
        OUTROS
    }

    private UUID produtoFruteiraId;
    private UUID produtoBuffetId;
    private MovementType tipo;
    private OriginDest origem;
    private OriginDest destino;
    private BigDecimal quantidade;
    private BigDecimal custoUnitario;
    private String lote;
    private LocalDateTime dataValidade;
    private String documentoReferencia;
    private String numeroDocumento;
    private String observacoes;
    private String responsavel;
    private LocalDateTime data;

    public StockMovementEvent() {
        this.data = LocalDateTime.now();
    }

    public UUID getProdutoFruteiraId() { return produtoFruteiraId; }
    public void setProdutoFruteiraId(UUID produtoFruteiraId) { this.produtoFruteiraId = produtoFruteiraId; }

    public UUID getProdutoBuffetId() { return produtoBuffetId; }
    public void setProdutoBuffetId(UUID produtoBuffetId) { this.produtoBuffetId = produtoBuffetId; }

    public MovementType getTipo() { return tipo; }
    public void setTipo(MovementType tipo) { this.tipo = tipo; }

    public OriginDest getOrigem() { return origem; }
    public void setOrigem(OriginDest origem) { this.origem = origem; }

    public OriginDest getDestino() { return destino; }
    public void setDestino(OriginDest destino) { this.destino = destino; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public void setCustoUnitario(BigDecimal custoUnitario) { this.custoUnitario = custoUnitario; }

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

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
}