package br.com.sistematenda.fruteira.service;

import br.com.sistematenda.fruteira.dto.ProdutoDTO;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.repository.ProdutoRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.TipoProduto;
import br.com.sistematenda.common.enums.UnidadeMedida;
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
public class ProdutoService extends BaseCrudServiceImpl<Produto, ProdutoDTO, UUID> {

    @Inject
    ProdutoRepository repository;

    @Inject
    Event<StockMovementEvent> stockMovementEvent;

    @Override
    public ProdutoRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<Produto> getEntityClass() {
        return Produto.class;
    }

    @Override
    protected String getEntidadeNome() {
        return "Produto";
    }

    public Produto buscarEntidade(UUID id) {
        return repository.findById(id);
    }

    public ProdutoDTO findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto", codigo));
    }

    public ProdutoDTO findByCodigoBarras(String codigoBarras) {
        return repository.findByCodigoBarras(codigoBarras)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto com código de barras", codigoBarras));
    }

    public ProdutoDTO findByCodigoBalanca(String codigoBalanca) {
        return repository.findByCodigoBalanca(codigoBalanca)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto com código de balança", codigoBalanca));
    }

    public List<ProdutoDTO> findByTipo(TipoProduto tipo) {
        return repository.findByTipo(tipo).stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoDTO> findByUnidadeMedida(UnidadeMedida unidadeMedida) {
        return repository.findByUnidadeMedida(unidadeMedida).stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoDTO> findAtivos() {
        return repository.findByAtivoTrue().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoDTO> findEstoqueBaixo() {
        return repository.findByEstoqueBaixo().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoDTO> findPesaveisBalanca() {
        return repository.findPesaveisBalanca().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoDTO> search(String termo) {
        return repository.search(termo).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<ProdutoDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null));
    }

    public PageResponse<ProdutoDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                            TipoProduto tipo, UnidadeMedida unidadeMedida, Boolean ativo) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, tipo, unidadeMedida, ativo));
    }

    @Override
    @Transactional
    public ProdutoDTO create(@Valid ProdutoDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Override
    protected void antesDePersistir(Produto entity) {
        if (entity.codigo == null || entity.codigo.isBlank()) {
            entity.codigo = gerarCodigoUnico();
        }
    }

    private String gerarCodigoUnico() {
        long sequencia = repository.count() + 1;
        String codigo;
        do {
            codigo = String.format("P%06d", sequencia++);
        } while (repository.findByCodigo(codigo).isPresent());
        return codigo;
    }

    @Override
    @Transactional
    public ProdutoDTO update(UUID id, @Valid ProdutoDTO dto) {
        validateUpdate(id, dto);
        return super.update(id, dto);
    }

    @Transactional
    public ProdutoDTO atualizarEstoque(UUID produtoId, BigDecimal quantidade) {
        ProdutoDTO produto = findById(produtoId);
        BigDecimal novoEstoque = produto.getEstoqueAtual().add(quantidade);
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", 
                "Estoque insuficiente. Atual: " + produto.getEstoqueAtual() + ", Tentativa: " + quantidade);
        }
        repository.updateEstoque(produtoId, quantidade);
        
        // Fire stock movement event
        fireStockMovementEvent(produtoId, quantidade, produto.getCustoMedio());
        
        return findById(produtoId);
    }

    @Transactional
    public ProdutoDTO atualizarPrecoVenda(UUID produtoId, BigDecimal novoPreco) {
        if (novoPreco.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Preço de venda não pode ser negativo");
        }
        repository.updatePrecoVenda(produtoId, novoPreco);
        return findById(produtoId);
    }

    private void fireStockMovementEvent(UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario) {
        StockMovementEvent event = new StockMovementEvent();
        event.setProdutoFruteiraId(produtoId);
        event.setTipo(quantidade.compareTo(BigDecimal.ZERO) > 0 
            ? StockMovementEvent.MovementType.ENTRADA_AJUSTE 
            : StockMovementEvent.MovementType.SAIDA_AJUSTE);
        event.setOrigem(StockMovementEvent.OriginDest.AJUSTE);
        event.setDestino(StockMovementEvent.OriginDest.FRUTEIRA);
        event.setQuantidade(quantidade.abs());
        event.setCustoUnitario(custoUnitario);
        event.setData(LocalDateTime.now());
        event.setResponsavel("SISTEMA");
        event.setObservacoes("Ajuste de estoque via atualização direta");
        
        stockMovementEvent.fire(event);
    }

    private void validateCreate(ProdutoDTO dto) {
        if (dto.getCodigoBarras() != null && repository.findByCodigoBarras(dto.getCodigoBarras()).isPresent()) {
            throw new ValidationException("Já existe um produto com este código de barras: " + dto.getCodigoBarras());
        }
        if (dto.getCodigoBalanca() != null && repository.findByCodigoBalanca(dto.getCodigoBalanca()).isPresent()) {
            throw new ValidationException("Já existe um produto com este código de balança: " + dto.getCodigoBalanca());
        }
    }

    private void validateUpdate(UUID id, ProdutoDTO dto) {
        if (dto.getCodigoBarras() != null) {
            repository.findByCodigoBarras(dto.getCodigoBarras()).ifPresent(p -> {
                if (!p.id.equals(id)) {
                    throw new ValidationException("Já existe um produto com este código de barras: " + dto.getCodigoBarras());
                }
            });
        }
        if (dto.getCodigoBalanca() != null) {
            repository.findByCodigoBalanca(dto.getCodigoBalanca()).ifPresent(p -> {
                if (!p.id.equals(id)) {
                    throw new ValidationException("Já existe um produto com este código de balança: " + dto.getCodigoBalanca());
                }
            });
        }
    }
}
