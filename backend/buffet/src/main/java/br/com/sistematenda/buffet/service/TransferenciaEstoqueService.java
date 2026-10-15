package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.TransferenciaEstoqueDTO;
import br.com.sistematenda.fruteira.dto.ProdutoDTO;
import br.com.sistematenda.buffet.entity.TransferenciaEstoque;
import br.com.sistematenda.buffet.mapper.TransferenciaEstoqueMapper;
import br.com.sistematenda.buffet.repository.TransferenciaEstoqueRepository;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.OrigemProduto;
import br.com.sistematenda.common.event.StockMovementEvent;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransferenciaEstoqueService extends BaseCrudServiceImpl<TransferenciaEstoque, TransferenciaEstoqueDTO, UUID> {

    @Inject
    TransferenciaEstoqueRepository repository;

    @Inject
    ProdutoService produtoService;

    @Inject
    Event<StockMovementEvent> stockMovementEvent;

    @Override
    protected TransferenciaEstoqueRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<TransferenciaEstoque> getEntityClass() {
        return TransferenciaEstoque.class;
    }

    public List<TransferenciaEstoqueDTO> findByStatus(TransferenciaEstoque.StatusTransferencia status) {
        return repository.findByStatus(status).stream().map(mapper::toDTO).toList();
    }

    public List<TransferenciaEstoqueDTO> findByProduto(UUID produtoId) {
        return repository.findByProduto(produtoId).stream().map(mapper::toDTO).toList();
    }

    public List<TransferenciaEstoqueDTO> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByPeriodo(inicio, fim).stream().map(mapper::toDTO).toList();
    }

    public List<TransferenciaEstoqueDTO> findPendentes() {
        return repository.findPendentes().stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<TransferenciaEstoqueDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null, null));
    }

    public PageResponse<TransferenciaEstoqueDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                         TransferenciaEstoque.StatusTransferencia status,
                                                         UUID produtoId, LocalDateTime inicio, LocalDateTime fim) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, status, produtoId, inicio, fim));
    }

    @Override
    @Transactional
    public TransferenciaEstoqueDTO create(@Valid TransferenciaEstoqueDTO dto) {
        validateCreate(dto);
        
        TransferenciaEstoque entity = mapper.toEntity(dto);
        entity.data = LocalDateTime.now();
        entity.status = TransferenciaEstoque.StatusTransferencia.PENDENTE;
        entity.solicitadoPor = "SISTEMA"; // TODO: usuário logado
        
        // Buscar custo do produto na fruteira
        ProdutoDTO produtoFruteira = produtoService.findById(UUID.fromString(dto.getProduto().getId()));
        entity.custoUnitario = produtoFruteira.getCustoMedio();
        entity.valorTotal = entity.quantidade.multiply(entity.custoUnitario);
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public TransferenciaEstoqueDTO aprovar(UUID transferenciaId, String aprovadoPor) {
        TransferenciaEstoque entity = getRepository().findById(transferenciaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Transferência", transferenciaId);
        }
        
        if (entity.status != TransferenciaEstoque.StatusTransferencia.PENDENTE) {
            throw new BusinessException("STATUS_INVALIDO", "Apenas transferências pendentes podem ser aprovadas");
        }
        
        // Verificar estoque na fruteira
        ProdutoDTO produtoFruteira = produtoService.findById(entity.produto.id);
        if (produtoFruteira.getEstoqueAtual().compareTo(entity.quantidade) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", 
                "Estoque insuficiente na fruteira. Disponível: " + produtoFruteira.getEstoqueAtual());
        }
        
        entity.status = TransferenciaEstoque.StatusTransferencia.APROVADA;
        entity.aprovadoPor = aprovadoPor;
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public TransferenciaEstoqueDTO rejeitar(UUID transferenciaId, String motivo, String rejeitadoPor) {
        TransferenciaEstoque entity = getRepository().findById(transferenciaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Transferência", transferenciaId);
        }
        
        if (entity.status != TransferenciaEstoque.StatusTransferencia.PENDENTE) {
            throw new BusinessException("STATUS_INVALIDO", "Apenas transferências pendentes podem ser rejeitadas");
        }
        
        entity.status = TransferenciaEstoque.StatusTransferencia.REJEITADA;
        entity.motivoRejeicao = motivo;
        entity.aprovadoPor = rejeitadoPor;
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public TransferenciaEstoqueDTO concluir(UUID transferenciaId, String recebidoPor) {
        TransferenciaEstoque entity = getRepository().findById(transferenciaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Transferência", transferenciaId);
        }
        
        if (entity.status != TransferenciaEstoque.StatusTransferencia.APROVADA) {
            throw new BusinessException("STATUS_INVALIDO", "Apenas transferências aprovadas podem ser concluídas");
        }
        
        // Get custo unitario
        ProdutoDTO produtoFruteira = produtoService.findById(entity.produto.id);
        BigDecimal custoUnitario = produtoFruteira.getCustoMedio();
        
        // Fire SAIDA movement from origin (fruteira)
        fireStockMovementEventSaida(entity, custoUnitario, recebidoPor);
        
        // Fire ENTRADA movement to destination (buffet/estoque)
        fireStockMovementEventEntrada(entity, custoUnitario, recebidoPor);
        
        entity.status = TransferenciaEstoque.StatusTransferencia.CONCLUIDA;
        entity.dataConclusao = LocalDateTime.now();
        entity.recebidoPor = recebidoPor;
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    private void fireStockMovementEventSaida(TransferenciaEstoque entity, BigDecimal custoUnitario, String responsavel) {
        StockMovementEvent.OriginDest origem = mapOrigemDestino(entity.origem);
        StockMovementEvent.OriginDest destino = mapOrigemDestino(entity.destino);
        
        StockMovementEvent event = new StockMovementEvent();
        event.setProdutoFruteiraId(entity.produto.id);
        event.setTipo(StockMovementEvent.MovementType.SAIDA_TRANSFERENCIA);
        event.setOrigem(origem);
        event.setDestino(destino);
        event.setQuantidade(entity.quantidade);
        event.setCustoUnitario(custoUnitario);
        event.setData(LocalDateTime.now());
        event.setResponsavel(responsavel);
        event.setObservacoes("Transferência para " + entity.destino.name() + " - Transferência ID: " + entity.id);
        event.setDocumentoReferencia(entity.id.toString());
        
        stockMovementEvent.fire(event);
    }

    private void fireStockMovementEventEntrada(TransferenciaEstoque entity, BigDecimal custoUnitario, String responsavel) {
        StockMovementEvent.OriginDest origem = mapOrigemDestino(entity.origem);
        StockMovementEvent.OriginDest destino = mapOrigemDestino(entity.destino);
        
        StockMovementEvent event = new StockMovementEvent();
        
        // Check if destination is buffet (has ProdutoBuffet)
        if (entity.destino == OrigemProduto.BUFFET) {
            // TODO: Find corresponding ProdutoBuffet ID - for now use fruteira ID
            event.setProdutoFruteiraId(entity.produto.id);
        } else {
            event.setProdutoFruteiraId(entity.produto.id);
        }
        
        event.setTipo(StockMovementEvent.MovementType.ENTRADA_TRANSFERENCIA);
        event.setOrigem(origem);
        event.setDestino(destino);
        event.setQuantidade(entity.quantidade);
        event.setCustoUnitario(custoUnitario);
        event.setData(LocalDateTime.now());
        event.setResponsavel(responsavel);
        event.setObservacoes("Transferência de " + entity.origem.name() + " - Transferência ID: " + entity.id);
        event.setDocumentoReferencia(entity.id.toString());
        
        stockMovementEvent.fire(event);
    }

    private StockMovementEvent.OriginDest mapOrigemDestino(OrigemProduto origem) {
        return switch (origem) {
            case FRUTEIRA -> StockMovementEvent.OriginDest.FRUTEIRA;
            case BUFFET -> StockMovementEvent.OriginDest.BUFFET;
            case FORNECEDOR -> StockMovementEvent.OriginDest.FORNECEDOR;
            case TRANSFERENCIA -> StockMovementEvent.OriginDest.OUTROS;
            default -> StockMovementEvent.OriginDest.OUTROS;
        };
    }

    @Transactional
    public TransferenciaEstoqueDTO cancelar(UUID transferenciaId, String motivo) {
        TransferenciaEstoque entity = getRepository().findById(transferenciaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Transferência", transferenciaId);
        }
        
        if (entity.status == TransferenciaEstoque.StatusTransferencia.CONCLUIDA) {
            throw new BusinessException("STATUS_INVALIDO", "Não é possível cancelar transferência concluída");
        }
        
        entity.status = TransferenciaEstoque.StatusTransferencia.CANCELADA;
        entity.motivoRejeicao = motivo;
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    public BigDecimal getTotalTransferidoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.getTotalTransferidoPeriodo(inicio, fim);
    }

    private void validateCreate(TransferenciaEstoqueDTO dto) {
        if (dto.getProduto() == null || dto.getProduto().getId() == null) {
            throw new ValidationException("Produto é obrigatório");
        }
        if (dto.getQuantidade() == null || dto.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantidade deve ser maior que zero");
        }
        if (dto.getOrigem() == null) {
            throw new ValidationException("Origem é obrigatória");
        }
        if (dto.getDestino() == null) {
            throw new ValidationException("Destino é obrigatório");
        }
        if (dto.getOrigem() == dto.getDestino()) {
            throw new ValidationException("Origem e destino devem ser diferentes");
        }
    }
}
