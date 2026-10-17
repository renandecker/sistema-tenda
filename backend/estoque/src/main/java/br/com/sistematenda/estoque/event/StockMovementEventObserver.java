package br.com.sistematenda.estoque.event;

import br.com.sistematenda.common.event.StockMovementEvent;
import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.estoque.repository.MovimentoEstoqueRepository;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.repository.ProdutoRepository;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.repository.ProdutoBuffetRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class StockMovementEventObserver {

    @Inject
    MovimentoEstoqueRepository movimentoRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    ProdutoBuffetRepository produtoBuffetRepository;

    @Inject
    EntityManager entityManager;

    @Transactional
    public void onStockMovement(@Observes StockMovementEvent event) {
        BigDecimal estoqueAnterior = BigDecimal.ZERO;
        BigDecimal estoquePosterior = BigDecimal.ZERO;

        // Get current stock and update it based on movement type
        if (event.getProdutoFruteiraId() != null) {
            Produto produto = produtoRepository.findById(event.getProdutoFruteiraId());
            if (produto != null) {
                estoqueAnterior = produto.estoqueAtual;
                
                boolean isEntrada = isEntrada(event.getTipo());
                
                if (isEntrada) {
                    estoquePosterior = estoqueAnterior.add(event.getQuantidade());
                } else {
                    estoquePosterior = estoqueAnterior.subtract(event.getQuantidade());
                    if (estoquePosterior.compareTo(BigDecimal.ZERO) < 0) {
                        estoquePosterior = BigDecimal.ZERO;
                    }
                }
                
                produto.estoqueAtual = estoquePosterior;
                produtoRepository.persist(produto);
            }
        } else if (event.getProdutoBuffetId() != null) {
            ProdutoBuffet produto = produtoBuffetRepository.findById(event.getProdutoBuffetId());
            if (produto != null && produto.controlaEstoque) {
                estoqueAnterior = produto.estoqueAtual;
                
                boolean isEntrada = isEntrada(event.getTipo());
                
                if (isEntrada) {
                    estoquePosterior = estoqueAnterior.add(event.getQuantidade());
                } else {
                    estoquePosterior = estoqueAnterior.subtract(event.getQuantidade());
                    if (estoquePosterior.compareTo(BigDecimal.ZERO) < 0) {
                        estoquePosterior = BigDecimal.ZERO;
                    }
                }
                
                produto.estoqueAtual = estoquePosterior;
                produtoBuffetRepository.persist(produto);
            }
        }

        // Create the movement record with stock tracking
        MovimentoEstoque entity = new MovimentoEstoque();

        if (event.getProdutoFruteiraId() != null) {
            entity.produtoFruteira = entityManager.getReference(Produto.class, event.getProdutoFruteiraId());
        }
        if (event.getProdutoBuffetId() != null) {
            entity.produtoBuffet = entityManager.getReference(ProdutoBuffet.class, event.getProdutoBuffetId());
        }
        
        entity.tipo = mapTipoMovimento(event.getTipo());
        entity.origem = mapOrigemDestino(event.getOrigem());
        entity.destino = mapOrigemDestino(event.getDestino());
        entity.quantidade = event.getQuantidade();
        entity.custoUnitario = event.getCustoUnitario();
        entity.valorTotal = event.getQuantidade() != null && event.getCustoUnitario() != null 
            ? event.getQuantidade().multiply(event.getCustoUnitario()) 
            : BigDecimal.ZERO;
        entity.estoqueAnterior = estoqueAnterior;
        entity.estoquePosterior = estoquePosterior;
        entity.lote = event.getLote();
        entity.dataValidade = event.getDataValidade();
        entity.documentoReferencia = event.getDocumentoReferencia();
        entity.numeroDocumento = event.getNumeroDocumento();
        entity.observacoes = event.getObservacoes();
        entity.responsavel = event.getResponsavel();
        entity.data = event.getData() != null ? event.getData() : LocalDateTime.now();
        
        movimentoRepository.persist(entity);
    }

    private boolean isEntrada(StockMovementEvent.MovementType tipo) {
        if (tipo == null) return false;
        return tipo.name().startsWith("ENTRADA");
    }

    private MovimentoEstoque.TipoMovimento mapTipoMovimento(StockMovementEvent.MovementType tipo) {
        if (tipo == null) return MovimentoEstoque.TipoMovimento.INVENTARIO;
        return switch (tipo) {
            case ENTRADA_COMPRA -> MovimentoEstoque.TipoMovimento.ENTRADA_COMPRA;
            case ENTRADA_TRANSFERENCIA -> MovimentoEstoque.TipoMovimento.ENTRADA_TRANSFERENCIA;
            case ENTRADA_AJUSTE -> MovimentoEstoque.TipoMovimento.ENTRADA_AJUSTE;
            case ENTRADA_DEVOLUCAO -> MovimentoEstoque.TipoMovimento.ENTRADA_DEVOLUCAO;
            case SAIDA_VENDA -> MovimentoEstoque.TipoMovimento.SAIDA_VENDA;
            case SAIDA_TRANSFERENCIA -> MovimentoEstoque.TipoMovimento.SAIDA_TRANSFERENCIA;
            case SAIDA_PERDA -> MovimentoEstoque.TipoMovimento.SAIDA_PERDA;
            case SAIDA_AJUSTE -> MovimentoEstoque.TipoMovimento.SAIDA_AJUSTE;
            case SAIDA_CONSUMO_INTERNO -> MovimentoEstoque.TipoMovimento.SAIDA_CONSUMO_INTERNO;
        };
    }

    private MovimentoEstoque.OrigemDestino mapOrigemDestino(StockMovementEvent.OriginDest origem) {
        if (origem == null) return MovimentoEstoque.OrigemDestino.OUTROS;
        return switch (origem) {
            case FRUTEIRA -> MovimentoEstoque.OrigemDestino.FRUTEIRA;
            case BUFFET -> MovimentoEstoque.OrigemDestino.BUFFET;
            case FORNECEDOR -> MovimentoEstoque.OrigemDestino.FORNECEDOR;
            case CLIENTE -> MovimentoEstoque.OrigemDestino.CLIENTE;
            case PERDA -> MovimentoEstoque.OrigemDestino.PERDA;
            case AJUSTE -> MovimentoEstoque.OrigemDestino.AJUSTE;
            case OUTROS -> MovimentoEstoque.OrigemDestino.OUTROS;
        };
    }
}