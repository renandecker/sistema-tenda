package br.com.sistematenda.fruteira.service;

import br.com.sistematenda.fruteira.dto.EtiquetaBalancaDTO;
import br.com.sistematenda.fruteira.dto.ProdutoDTO;
import br.com.sistematenda.fruteira.entity.EtiquetaBalanca;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.repository.EtiquetaBalancaRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EtiquetaBalancaService extends BaseCrudServiceImpl<EtiquetaBalanca, EtiquetaBalancaDTO, UUID> {

    @Inject
    EtiquetaBalancaRepository repository;

    @Inject
    ProdutoService produtoService;

    @Override
    protected EtiquetaBalancaRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<EtiquetaBalanca> getEntityClass() {
        return EtiquetaBalanca.class;
    }

    @Override
    protected String getEntidadeNome() {
        return "EtiquetaBalanca";
    }

    public EtiquetaBalancaDTO findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Etiqueta de balança", codigo));
    }

    public List<EtiquetaBalancaDTO> findByProduto(UUID produtoId) {
        Produto produto = new Produto();
        produto.id = produtoId;
        return repository.findByProduto(produto).stream().map(mapper::toDTO).toList();
    }

    public List<EtiquetaBalancaDTO> findAtivas() {
        return repository.findAtivas().stream().map(mapper::toDTO).toList();
    }

    public List<EtiquetaBalancaDTO> findAtivasPorProduto(UUID produtoId) {
        Produto produto = new Produto();
        produto.id = produtoId;
        return repository.findAtivasPorProduto(produto).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<EtiquetaBalancaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null));
    }

    public PageResponse<EtiquetaBalancaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                    Boolean ativa) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, ativa));
    }

    @Override
    @Transactional
    public EtiquetaBalancaDTO create(@Valid EtiquetaBalancaDTO dto) {
        validateCreate(dto);
        
        EtiquetaBalanca entity = mapper.toEntity(dto);
        entity.produto = new Produto();
        entity.produto.id = UUID.fromString(dto.getProduto().getId());
        
        // Sincronizar preço com produto se não informado
        if (entity.preco == null || entity.preco.compareTo(BigDecimal.ZERO) == 0) {
            ProdutoDTO produtoDTO = produtoService.findById(entity.produto.id);
            entity.preco = produtoDTO.getPrecoVenda();
        }
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public EtiquetaBalancaDTO gerarParaProduto(UUID produtoId) {
        ProdutoDTO produto = produtoService.findById(produtoId);
        
        if (!produto.getPesavelBalanca()) {
            throw new ValidationException("Produto não está configurado para pesagem em balança");
        }
        
        // Gerar código único para balança (máx 6 dígitos para Toledo)
        String codigoBalanca = String.format("%06d", produtoId.hashCode() & 0xFFFFFF);
        
        EtiquetaBalancaDTO dto = new EtiquetaBalancaDTO();
        dto.setCodigo(codigoBalanca);
        dto.setProduto(new EtiquetaBalancaDTO.ProdutoResumoDTO());
        dto.getProduto().setId(produtoId.toString());
        dto.setPreco(produto.getPrecoVenda());
        dto.setImprimirCodigoBarras(true);
        dto.setImprimirQrCode(false);
        dto.setModeloEtiqueta("PADRAO");
        dto.setAtiva(true);
        
        return create(dto);
    }

    @Transactional
    public EtiquetaBalancaDTO imprimirEtiqueta(String codigo) {
        EtiquetaBalancaDTO etiqueta = findByCodigo(codigo);
        repository.incrementarImpressao(codigo);
        return etiqueta;
    }

    private void validateCreate(EtiquetaBalancaDTO dto) {
        if (dto.getProduto() == null || dto.getProduto().getId() == null) {
            throw new ValidationException("Produto é obrigatório");
        }
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            throw new ValidationException("Código da etiqueta é obrigatório");
        }
        if (repository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new ValidationException("Já existe uma etiqueta com este código: " + dto.getCodigo());
        }
    }
}