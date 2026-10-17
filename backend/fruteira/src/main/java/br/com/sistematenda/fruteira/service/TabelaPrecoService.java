package br.com.sistematenda.fruteira.service;

import br.com.sistematenda.fruteira.dto.TabelaPrecoDTO;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.entity.TabelaPreco;
import br.com.sistematenda.fruteira.repository.TabelaPrecoRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TabelaPrecoService extends BaseCrudServiceImpl<TabelaPreco, TabelaPrecoDTO, UUID> {

    @Inject
    TabelaPrecoRepository repository;

    @Inject
    ProdutoService produtoService;

    @Inject
    EntityManager entityManager;

    @Override
    protected TabelaPrecoRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<TabelaPreco> getEntityClass() {
        return TabelaPreco.class;
    }

    @Override
    protected String getEntidadeNome() {
        return "TabelaPreco";
    }

    public List<TabelaPrecoDTO> findByProduto(UUID produtoId) {
        Produto produto = new Produto();
        produto.id = produtoId;
        return repository.findByProduto(produto).stream().map(mapper::toDTO).toList();
    }

    public TabelaPrecoDTO findVigenteByProduto(UUID produtoId, LocalDate data) {
        Produto produto = new Produto();
        produto.id = produtoId;
        return repository.findVigenteByProduto(produto, data)
            .map(mapper::toDTO)
            .orElse(null);
    }

    public List<TabelaPrecoDTO> findVigentes(LocalDate data) {
        return repository.findVigentes(data).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<TabelaPrecoDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null));
    }

    public PageResponse<TabelaPrecoDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                Boolean ativa, LocalDate dataInicio, LocalDate dataFim) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, ativa, dataInicio, dataFim));
    }

    @Override
    @Transactional
    public TabelaPrecoDTO create(@Valid TabelaPrecoDTO dto) {
        validateCreate(dto);
        
        TabelaPreco entity = mapper.toEntity(dto);
        entity.produto = entityManager.getReference(Produto.class, UUID.fromString(dto.getProduto().getId()));
        entity.aplicadaEm = LocalDateTime.now();
        entity.aplicadaPor = "SISTEMA"; // TODO: pegar usuário logado
        
        // Desativar tabelas antigas do mesmo produto
        repository.desativarTabelasAntigas(entity.produto, entity.dataInicio);
        
        // Atualizar preço no produto se a tabela estiver vigente
        LocalDate hoje = LocalDate.now();
        if (entity.ativa && entity.dataInicio.isBefore(hoje.plusDays(1)) 
                && (entity.dataFim == null || entity.dataFim.isAfter(hoje.minusDays(1)))) {
            produtoService.atualizarPrecoVenda(entity.produto.id, entity.precoVenda);
        }
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public TabelaPrecoDTO aplicarTabelaVigente(UUID produtoId) {
        Produto produto = new Produto();
        produto.id = produtoId;
        LocalDate hoje = LocalDate.now();
        
        return repository.findVigenteByProduto(produto, hoje)
            .map(tabela -> {
                produtoService.atualizarPrecoVenda(produtoId, tabela.precoVenda);
                return mapper.toDTO(tabela);
            })
            .orElse(null);
    }

    @Transactional
    public void aplicarTodasVigentes() {
        LocalDate hoje = LocalDate.now();
        List<TabelaPreco> vigentes = repository.findVigentes(hoje);
        
        for (TabelaPreco tabela : vigentes) {
            if (tabela.ativa) {
                produtoService.atualizarPrecoVenda(tabela.produto.id, tabela.precoVenda);
                tabela.aplicadaEm = LocalDateTime.now();
                tabela.aplicadaPor = "BATCH_APLICACAO";
                getRepository().persist(tabela);
            }
        }
    }

    private void validateCreate(TabelaPrecoDTO dto) {
        if (dto.getProduto() == null || dto.getProduto().getId() == null) {
            throw new ValidationException("Produto é obrigatório");
        }
        if (dto.getPrecoVenda() == null || dto.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Preço de venda não pode ser negativo");
        }
        if (dto.getDataInicio() == null) {
            throw new ValidationException("Data de início é obrigatória");
        }
        if (dto.getDataFim() != null && dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new ValidationException("Data de fim não pode ser anterior à data de início");
        }
    }
}