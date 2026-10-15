package br.com.sistematenda.estoque.dto;

import br.com.sistematenda.estoque.entity.AlertaEstoque;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AlertaEstoqueDTO {

    private UUID id;
    private UUID produtoFruteiraId;
    private UUID produtoBuffetId;
    private AlertaEstoque.TipoAlerta tipo;
    private AlertaEstoque.StatusAlerta status;
    private AlertaEstoque.Prioridade prioridade;
    private String mensagem;
    private BigDecimal valorReferencia;
    private BigDecimal valorAtual;
    private LocalDate dataVencimento;
    private Integer diasParaVencimento;
    private LocalDateTime dataAlerta;
    private LocalDateTime dataVisualizacao;
    private LocalDateTime dataResolucao;
    private String visualizadoPor;
    private String resolvidoPor;
    private String observacoesResolucao;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProdutoFruteiraId() { return produtoFruteiraId; }
    public void setProdutoFruteiraId(UUID produtoFruteiraId) { this.produtoFruteiraId = produtoFruteiraId; }

    public UUID getProdutoBuffetId() { return produtoBuffetId; }
    public void setProdutoBuffetId(UUID produtoBuffetId) { this.produtoBuffetId = produtoBuffetId; }

    public AlertaEstoque.TipoAlerta getTipo() { return tipo; }
    public void setTipo(AlertaEstoque.TipoAlerta tipo) { this.tipo = tipo; }

    public AlertaEstoque.StatusAlerta getStatus() { return status; }
    public void setStatus(AlertaEstoque.StatusAlerta status) { this.status = status; }

    public AlertaEstoque.Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(AlertaEstoque.Prioridade prioridade) { this.prioridade = prioridade; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public BigDecimal getValorReferencia() { return valorReferencia; }
    public void setValorReferencia(BigDecimal valorReferencia) { this.valorReferencia = valorReferencia; }

    public BigDecimal getValorAtual() { return valorAtual; }
    public void setValorAtual(BigDecimal valorAtual) { this.valorAtual = valorAtual; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public Integer getDiasParaVencimento() { return diasParaVencimento; }
    public void setDiasParaVencimento(Integer diasParaVencimento) { this.diasParaVencimento = diasParaVencimento; }

    public LocalDateTime getDataAlerta() { return dataAlerta; }
    public void setDataAlerta(LocalDateTime dataAlerta) { this.dataAlerta = dataAlerta; }

    public LocalDateTime getDataVisualizacao() { return dataVisualizacao; }
    public void setDataVisualizacao(LocalDateTime dataVisualizacao) { this.dataVisualizacao = dataVisualizacao; }

    public LocalDateTime getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(LocalDateTime dataResolucao) { this.dataResolucao = dataResolucao; }

    public String getVisualizadoPor() { return visualizadoPor; }
    public void setVisualizadoPor(String visualizadoPor) { this.visualizadoPor = visualizadoPor; }

    public String getResolvidoPor() { return resolvidoPor; }
    public void setResolvidoPor(String resolvidoPor) { this.resolvidoPor = resolvidoPor; }

    public String getObservacoesResolucao() { return observacoesResolucao; }
    public void setObservacoesResolucao(String observacoesResolucao) { this.observacoesResolucao = observacoesResolucao; }
}