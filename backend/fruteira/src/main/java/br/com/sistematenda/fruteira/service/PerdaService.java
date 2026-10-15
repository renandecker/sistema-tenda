package br.com.sistematenda.fruteira.service;

import br.com.sistematenda.fruteira.dto.PerdaDTO;
import br.com.sistematenda.fruteira.dto.ProdutoDTO;
import br.com.sistematenda.fruteira.entity.Perda;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.repository.PerdaRepository;
import br.com.sistematenda.common.dto.PageResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PerdaService extends BaseCrudServiceImpl<Perda, PerdaDTO, UUID> {

    @Inject
    PerdaRepository repository;

    @Inject
    ProdutoService produtoService;

    @Inject
    Event<StockMovementEvent> stockMovementEvent;

    @Override
    protected PerdaRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<Perda> getEntityClass() {
        return Perda.class;
    }

    public List<PerdaDTO> findByProduto(UUID produtoId) {
        Produto produto = new Produto();
        produto.id = produtoId;
        return repository.findByProduto(produto).stream().map(mapper::toDTO).toList();
    }

    public List<PerdaDTO> findByTipo(Perda.TipoPerda tipo) {
        return repository.findByTipo(tipo).stream().map(mapper::toDTO).toList();
    }

    public List<PerdaDTO> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.findByPeriodo(dataInicio, dataFim).stream().map(mapper::toDTO).toList();
    }

    public List<PerdaDTO> findByPeriodoETipo(LocalDate dataInicio, LocalDate dataFim, Perda.TipoPerda tipo) {
        return repository.findByPeriodoETipo(dataInicio, dataFim, tipo).stream().map(mapper::toDTO).toList();
    }

    public BigDecimal getTotalPerdasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalPerdasPeriodo(dataInicio, dataFim);
    }

    public BigDecimal getTotalPerdasPorTipoPeriodo(Perda.TipoPerda tipo, LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalPerdasPorTipoPeriodo(tipo, dataInicio, dataFim);
    }

    public List<PerdaDTO> findPendentesBaixaEstoque() {
        return repository.findPendentesBaixaEstoque().stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<PerdaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null));
    }

    public PageResponse<PerdaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                          Perda.TipoPerda tipo, LocalDate dataInicio, LocalDate dataFim) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, tipo, dataInicio, dataFim));
    }

    @Override
    @Transactional
    public PerdaDTO create(@Valid PerdaDTO dto) {
        validateCreate(dto);
        
        Perda entity = mapper.toEntity(dto);
        entity.produto = new Produto();
        entity.produto.id = UUID.fromString(dto.getProduto().getId());
        
        entity.valorTotal = entity.custoUnitario.multiply(entity.quantidade);
        entity.hora = LocalDateTime.now();
        entity.baixaEstoqueRealizada = false;
        
        getRepository().persist(entity);
        
        // Baixa automática do estoque via event
        ProdutoDTO produto = produtoService.findById(entity.produto.id);
        fireStockMovementEventPerda(entity.produto.id, entity.quantidade, produto.getCustoMedio(), 
                                    entity.getTipo() != null ? entity.getTipo().name() : "PERDA", 
                                    entity.getMotivo(), entity.getResponsavel());
        
        entity.baixaEstoqueRealizada = true;
        entity.dataBaixaEstoque = LocalDateTime.now();
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public PerdaDTO registrarPerdaRapida(UUID produtoId, Perda.TipoPerda tipo, BigDecimal quantidade, 
                                          String motivo, String responsavel) {
        ProdutoDTO produto = produtoService.findById(produtoId);
        
        if (quantidade.compareTo(produto.getEstoqueAtual()) > 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", 
                "Quantidade de perda maior que estoque atual. Estoque: " + produto.getEstoqueAtual());
        }
        
        PerdaDTO dto = new PerdaDTO();
        dto.setProduto(new PerdaDTO.ProdutoResumoDTO());
        dto.getProduto().setId(produtoId.toString());
        dto.setTipo(tipo);
        dto.setQuantidade(quantidade);
        dto.setCustoUnitario(produto.getCustoMedio());
        dto.setMotivo(motivo);
        dto.setResponsavel(responsavel);
        dto.setData(LocalDate.now());
        
        return create(dto);
    }

    private void fireStockMovementEventPerda(UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario,
                                              String tipoPerda, String motivo, String responsavel) {
        StockMovementEvent event = new StockMovementEvent();
        event.setProdutoFruteiraId(produtoId);
        event.setTipo(StockMovementEvent.MovementType.SAIDA_PERDA);
        event.setOrigem(StockMovementEvent.OriginDest.FRITEIRA);
        event.setDestino(StockMovementEvent.OriginDest.PERDA);
        event.setQuantidade(quantidade);
        event.setCustoUnitario(custoUnitario);
        event.setData(LocalDateTime.now());
        event.setResponsavel(responsavel != null ? responsavel : "SISTEMA");
        event.setObservacoes("Perda: " + tipoPerda + (motivo != null ? " - " + motivo : ""));
        
        stockMovementEvent.fire(event);
    }

    private void validateCreate(PerdaDTO dto) {
        if (dto.getProduto() == null || dto.getProduto().getId() == null) {
            throw new ValidationException("Produto é obrigatório");
        }
        if (dto.getTipo() == null) {
            throw new ValidationException("Tipo de perda é obrigatório");
        }
        if (dto.getQuantidade() == null || dto.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantidade deve ser maior que zero");
        }
        if (dto.getCustoUnitario() == null || dto.getCustoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Custo unitário não pode ser negativo");
        }
    }
}