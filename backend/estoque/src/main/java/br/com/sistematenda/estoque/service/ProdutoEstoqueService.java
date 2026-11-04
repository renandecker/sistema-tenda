package br.com.sistematenda.estoque.service;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.common.event.StockMovementEvent;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import br.com.sistematenda.estoque.dto.ProdutoEstoqueDTO;
import br.com.sistematenda.estoque.entity.ProdutoEstoque;
import br.com.sistematenda.estoque.repository.ProdutoEstoqueRepository;
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
public class ProdutoEstoqueService extends BaseCrudServiceImpl<ProdutoEstoque, ProdutoEstoqueDTO, UUID> {

    @Inject
    ProdutoEstoqueRepository repository;

    @Inject
    Event<StockMovementEvent> stockMovementEvent;

    @Override
    public ProdutoEstoqueRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<ProdutoEstoque> getEntityClass() {
        return ProdutoEstoque.class;
    }

    @Override
    protected String getEntidadeNome() {
        return "ProdutoEstoque";
    }

    public ProdutoEstoqueDTO findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto de estoque", codigo));
    }

    public ProdutoEstoqueDTO findByCodigoBarras(String codigoBarras) {
        return repository.findByCodigoBarras(codigoBarras)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto de estoque com código de barras", codigoBarras));
    }

    public List<ProdutoEstoqueDTO> findByCategoria(ProdutoEstoque.Categoria categoria) {
        return repository.findByCategoria(categoria).stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoEstoqueDTO> findByUnidadeMedida(UnidadeMedida unidadeMedida) {
        return repository.findByUnidadeMedida(unidadeMedida).stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoEstoqueDTO> findAtivos() {
        return repository.findAtivos().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoEstoqueDTO> findEstoqueBaixo() {
        return repository.findEstoqueBaixo().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoEstoqueDTO> search(String termo) {
        return repository.search(termo).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<ProdutoEstoqueDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null));
    }

    public PageResponse<ProdutoEstoqueDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                   ProdutoEstoque.Categoria categoria, UnidadeMedida unidadeMedida,
                                                   Boolean ativo) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, categoria, unidadeMedida, ativo));
    }

    @Override
    @Transactional
    public ProdutoEstoqueDTO create(@Valid ProdutoEstoqueDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Override
    protected void antesDePersistir(ProdutoEstoque entity) {
        if (entity.codigo == null || entity.codigo.isBlank()) {
            entity.codigo = gerarCodigoUnico();
        }
        if (entity.categoria == null) {
            entity.categoria = ProdutoEstoque.Categoria.MATERIA_PRIMA;
        }
        if (entity.estoqueAtual == null) {
            entity.estoqueAtual = BigDecimal.ZERO;
        }
        if (entity.custoMedio == null) {
            entity.custoMedio = BigDecimal.ZERO;
        }
    }

    private String gerarCodigoUnico() {
        long sequencia = repository.count() + 1;
        String codigo;
        do {
            codigo = String.format("MP%06d", sequencia++);
        } while (repository.findByCodigo(codigo).isPresent());
        return codigo;
    }

    @Override
    @Transactional
    public ProdutoEstoqueDTO update(UUID id, @Valid ProdutoEstoqueDTO dto) {
        validateUpdate(id, dto);
        return super.update(id, dto);
    }

    @Transactional
    public ProdutoEstoqueDTO atualizarEstoque(UUID produtoId, BigDecimal quantidade) {
        ProdutoEstoqueDTO produto = findById(produtoId);
        BigDecimal estoqueAtual = produto.getEstoqueAtual() != null ? produto.getEstoqueAtual() : BigDecimal.ZERO;
        BigDecimal novoEstoque = estoqueAtual.add(quantidade);
        if (novoEstoque.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE",
                "Estoque insuficiente. Atual: " + estoqueAtual + ", Tentativa: " + quantidade);
        }

        // O observador de movimentação é quem atualiza o saldo e registra o histórico.
        fireStockMovementEvent(produtoId, quantidade, produto.getCustoMedio());

        return findById(produtoId);
    }

    private void fireStockMovementEvent(UUID produtoId, BigDecimal quantidade, BigDecimal custoUnitario) {
        StockMovementEvent event = new StockMovementEvent();
        event.setProdutoEstoqueId(produtoId);
        event.setTipo(quantidade.compareTo(BigDecimal.ZERO) > 0
            ? StockMovementEvent.MovementType.ENTRADA_AJUSTE
            : StockMovementEvent.MovementType.SAIDA_AJUSTE);
        event.setOrigem(StockMovementEvent.OriginDest.ESTOQUE);
        event.setDestino(StockMovementEvent.OriginDest.ESTOQUE);
        event.setQuantidade(quantidade.abs());
        event.setCustoUnitario(custoUnitario);
        event.setData(LocalDateTime.now());
        event.setResponsavel("SISTEMA");
        event.setObservacoes("Ajuste de estoque via atualização direta");
        stockMovementEvent.fire(event);
    }

    private void validateCreate(ProdutoEstoqueDTO dto) {
        if (dto.getCodigoBarras() != null && repository.findByCodigoBarras(dto.getCodigoBarras()).isPresent()) {
            throw new ValidationException("Já existe um produto de estoque com este código de barras: " + dto.getCodigoBarras());
        }
    }

    private void validateUpdate(UUID id, ProdutoEstoqueDTO dto) {
        if (dto.getCodigoBarras() != null) {
            repository.findByCodigoBarras(dto.getCodigoBarras()).ifPresent(p -> {
                if (!p.id.equals(id)) {
                    throw new ValidationException("Já existe um produto de estoque com este código de barras: " + dto.getCodigoBarras());
                }
            });
        }
    }
}
