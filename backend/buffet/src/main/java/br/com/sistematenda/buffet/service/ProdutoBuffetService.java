package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.ProdutoBuffetDTO;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.mapper.ProdutoBuffetMapper;
import br.com.sistematenda.buffet.repository.ProdutoBuffetRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProdutoBuffetService extends BaseCrudServiceImpl<ProdutoBuffet, ProdutoBuffetDTO, UUID> {

    @Inject
    ProdutoBuffetRepository repository;

    @Override
    public ProdutoBuffetRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<ProdutoBuffet> getEntityClass() {
        return ProdutoBuffet.class;
    }

    @Override
    protected String getEntidadeNome() {
        return getEntityClass().getSimpleName();
    }

    public ProdutoBuffetDTO findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto", codigo));
    }

    public ProdutoBuffetDTO findByCodigoBarras(String codigoBarras) {
        return repository.findByCodigoBarras(codigoBarras)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Produto com código de barras", codigoBarras));
    }

    public List<ProdutoBuffetDTO> findByCategoria(ProdutoBuffet.Categoria categoria) {
        return repository.findByCategoria(categoria).stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoBuffetDTO> findByUnidadeMedida(UnidadeMedida unidadeMedida) {
        return repository.findByUnidadeMedida(unidadeMedida).stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoBuffetDTO> findAtivos() {
        return repository.findAtivos().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoBuffetDTO> findDisponiveisBalcao() {
        return repository.findDisponiveisBalcao().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoBuffetDTO> findEstoqueBaixo() {
        return repository.findEstoqueBaixo().stream().map(mapper::toDTO).toList();
    }

    public List<ProdutoBuffetDTO> search(String termo) {
        return repository.search(termo).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<ProdutoBuffetDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null, null));
    }

    public PageResponse<ProdutoBuffetDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                  ProdutoBuffet.Categoria categoria, UnidadeMedida unidadeMedida,
                                                  Boolean ativo, Boolean disponivelBalcao) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, categoria, unidadeMedida, ativo, disponivelBalcao));
    }

    @Override
    @Transactional
    public ProdutoBuffetDTO create(@Valid ProdutoBuffetDTO dto) {
        // Produtos do buffet são itens de venda/consumo e não controlam estoque.
        // O estoque de matéria-prima é controlado pelo cadastro de produtos de estoque.
        dto.setControlaEstoque(false);
        validateCreate(dto);
        return super.create(dto);
    }

    @Override
    protected void antesDePersistir(ProdutoBuffet entity) {
        entity.controlaEstoque = false;
        if (entity.codigo == null || entity.codigo.isBlank()) {
            entity.codigo = gerarCodigoUnico();
        }
    }

    private String gerarCodigoUnico() {
        long sequencia = repository.count() + 1;
        String codigo;
        do {
            codigo = String.format("B%06d", sequencia++);
        } while (repository.findByCodigo(codigo).isPresent());
        return codigo;
    }

    @Override
    @Transactional
    public ProdutoBuffetDTO update(UUID id, @Valid ProdutoBuffetDTO dto) {
        dto.setControlaEstoque(false);
        validateUpdate(id, dto);
        return super.update(id, dto);
    }

    @Transactional
    public ProdutoBuffetDTO atualizarEstoque(UUID produtoId, BigDecimal quantidade) {
        // Produtos do buffet não controlam estoque. Utilize o cadastro de produtos de estoque.
        throw new ValidationException("Produtos do buffet não controlam estoque. Utilize o cadastro de produtos de estoque.");
    }

    private void validateCreate(ProdutoBuffetDTO dto) {
        if (dto.getCodigoBarras() != null && repository.findByCodigoBarras(dto.getCodigoBarras()).isPresent()) {
            throw new ValidationException("Já existe um produto com este código de barras: " + dto.getCodigoBarras());
        }
    }

    private void validateUpdate(UUID id, ProdutoBuffetDTO dto) {
        if (dto.getCodigoBarras() != null) {
            repository.findByCodigoBarras(dto.getCodigoBarras()).ifPresent(p -> {
                if (!p.id.equals(id)) {
                    throw new ValidationException("Já existe um produto com este código de barras: " + dto.getCodigoBarras());
                }
            });
        }
    }
}