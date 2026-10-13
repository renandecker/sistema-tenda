package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.ReceitaDTO;
import br.com.sistematenda.buffet.entity.Receita;
import br.com.sistematenda.buffet.entity.ReceitaIngrediente;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.mapper.ReceitaMapper;
import br.com.sistematenda.buffet.repository.ReceitaRepository;
import br.com.sistematenda.buffet.repository.ProdutoBuffetRepository;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.common.exception.BusinessException;
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
public class ReceitaService extends BaseCrudServiceImpl<Receita, ReceitaDTO, UUID> {

    @Inject
    ReceitaRepository repository;

    @Inject
    ReceitaMapper mapper;

    @Inject
    ProdutoBuffetRepository produtoBuffetRepository;

    @Inject
    ProdutoService produtoFruteiraService;

    @Override
    protected ReceitaRepository getRepository() {
        return repository;
    }

    @Override
    protected ReceitaMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Receita> getEntityClass() {
        return Receita.class;
    }

    public ReceitaDTO findByProduto(UUID produtoId) {
        ProdutoBuffet produto = produtoBuffetRepository.findById(produtoId);
        if (produto == null) {
            throw new NotFoundException("Produto", produtoId);
        }
        return repository.findByProduto(produto)
            .map(mapper::toDTO)
            .orElse(null);
    }

    public List<ReceitaDTO> findAtivas() {
        return repository.findAtivas().stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<ReceitaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return repository.findPaginated(page, size, sortBy, sortDirection, null);
    }

    public PageResponse<ReceitaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                            Boolean ativa) {
        return repository.findPaginated(page, size, sortBy, sortDirection, ativa);
    }

    @Override
    @Transactional
    public ReceitaDTO create(@Valid ReceitaDTO dto) {
        validateCreate(dto);
        
        Receita entity = mapper.toEntity(dto);
        entity.produto = produtoBuffetRepository.findById(dto.getProduto().getId());
        if (entity.produto == null) {
            throw new NotFoundException("Produto", dto.getProduto().getId());
        }
        
        entity.versao = 1;
        entity.ativa = true;
        
        // Calcular custo total
        recalcularCustos(entity, dto.getIngredientes());
        
        getRepository().persist(entity);
        
        // Salvar ingredientes
        if (dto.getIngredientes() != null) {
            for (ReceitaDTO.ReceitaIngredienteDTO ingDto : dto.getIngredientes()) {
                ReceitaIngrediente ingrediente = new ReceitaIngrediente();
                ingrediente.receita = entity;
                ingrediente.produto = produtoFruteiraService.getRepository().findById(
                    java.util.UUID.fromString(ingDto.getProduto().getId()));
                ingrediente.quantidade = ingDto.getQuantidade();
                ingrediente.unidade = ingDto.getUnidade();
                ingrediente.obrigatorio = ingDto.getObrigatorio() != null ? ingDto.getObrigatorio() : true;
                ingrediente.ordem = ingDto.getOrdem();
                ingrediente.observacoes = ingDto.getObservacoes();
                getRepository().getEntityManager().persist(ingrediente);
            }
        }
        
        // Atualizar preço sugerido no produto
        if (entity.precoSugerido != null && entity.precoSugerido.compareTo(BigDecimal.ZERO) > 0) {
            entity.produto.precoVenda = entity.precoSugerido;
            produtoBuffetRepository.persist(entity.produto);
        }
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ReceitaDTO atualizarReceita(UUID receitaId, ReceitaDTO dto) {
        Receita entity = getRepository().findById(receitaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Receita", receitaId);
        }
        
        // Criar nova versão
        entity.versao++;
        entity.ativa = false;
        getRepository().persist(entity);
        
        // Criar nova receita baseada na anterior
        Receita novaReceita = new Receita();
        novaReceita.produto = entity.produto;
        novaReceita.rendimento = dto.getRendimento() != null ? dto.getRendimento() : entity.rendimento;
        novaReceita.unidadeRendimento = dto.getUnidadeRendimento() != null ? dto.getUnidadeRendimento() : entity.unidadeRendimento;
        novaReceita.tempoPreparoMinutos = dto.getTempoPreparoMinutos();
        novaReceita.tempoCozimentoMinutos = dto.getTempoCozimentoMinutos();
        novaReceita.dificuldade = dto.getDificuldade();
        novaReceita.instrucoes = dto.getInstrucoes();
        novaReceita.margemDesejada = dto.getMargemDesejada();
        novaReceita.observacoes = dto.getObservacoes();
        novaReceita.versao = entity.versao;
        novaReceita.ativa = true;
        
        recalcularCustos(novaReceita, dto.getIngredientes());
        
        getRepository().persist(novaReceita);
        
        // Salvar ingredientes
        if (dto.getIngredientes() != null) {
            for (ReceitaDTO.ReceitaIngredienteDTO ingDto : dto.getIngredientes()) {
                ReceitaIngrediente ingrediente = new ReceitaIngrediente();
                ingrediente.receita = novaReceita;
                ingrediente.produto = produtoFruteiraService.getRepository().findById(
                    java.util.UUID.fromString(ingDto.getProduto().getId()));
                ingrediente.quantidade = ingDto.getQuantidade();
                ingrediente.unidade = ingDto.getUnidade();
                ingrediente.obrigatorio = ingDto.getObrigatorio() != null ? ingDto.getObrigatorio() : true;
                ingrediente.ordem = ingDto.getOrdem();
                ingrediente.observacoes = ingDto.getObservacoes();
                getRepository().getEntityManager().persist(ingrediente);
            }
        }
        
        // Atualizar preço no produto
        if (novaReceita.precoSugerido != null && novaReceita.precoSugerido.compareTo(BigDecimal.ZERO) > 0) {
            novaReceita.produto.precoVenda = novaReceita.precoSugerido;
            produtoBuffetRepository.persist(novaReceita.produto);
        }
        
        return mapper.toDTO(novaReceita);
    }

    private void recalcularCustos(Receita receita, List<ReceitaDTO.ReceitaIngredienteDTO> ingredientesDto) {
        BigDecimal custoTotal = BigDecimal.ZERO;
        
        if (ingredientesDto != null) {
            for (ReceitaDTO.ReceitaIngredienteDTO ingDto : ingredientesDto) {
                Produto produtoFruteira = produtoFruteiraService.getRepository().findById(
                    java.util.UUID.fromString(ingDto.getProduto().getId()));
                if (produtoFruteira != null) {
                    BigDecimal custoUnitario = produtoFruteira.getCustoMedio();
                    BigDecimal custoIngrediente = ingDto.getQuantidade().multiply(custoUnitario);
                    custoTotal = custoTotal.add(custoIngrediente);
                }
            }
        }
        
        receita.custoTotal = custoTotal;
        
        // Calcular custo por unidade de rendimento
        if (receita.rendimento != null && receita.rendimento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal custoPorUnidade = custoTotal.divide(receita.rendimento, 4, BigDecimal.ROUND_HALF_UP);
            
            // Aplicar margem desejada
            if (receita.margemDesejada != null && receita.margemDesejada.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal fatorMargem = BigDecimal.ONE.add(receita.margemDesejada.divide(new BigDecimal("100")));
                receita.precoSugerido = custoPorUnidade.multiply(fatorMargem).setScale(4, BigDecimal.ROUND_HALF_UP);
            } else {
                receita.precoSugerido = custoPorUnidade;
            }
        }
    }

    private void validateCreate(ReceitaDTO dto) {
        if (dto.getProduto() == null || dto.getProduto().getId() == null) {
            throw new ValidationException("Produto é obrigatório");
        }
        if (dto.getRendimento() == null || dto.getRendimento().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Rendimento deve ser maior que zero");
        }
        if (dto.getUnidadeRendimento() == null) {
            throw new ValidationException("Unidade de rendimento é obrigatória");
        }
        
        // Verificar se já existe receita ativa para este produto
        ProdutoBuffet produto = produtoBuffetRepository.findById(dto.getProduto().getId());
        if (produto != null) {
            repository.findByProduto(produto).ifPresent(r -> {
                if (r.ativa) {
                    throw new ValidationException("Já existe uma receita ativa para este produto");
                }
            });
        }
    }
}