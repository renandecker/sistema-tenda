package br.com.sistematenda.estoque.service;

import br.com.sistematenda.estoque.dto.MovimentoEstoqueDTO;
import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.estoque.repository.MovimentoEstoqueRepository;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.repository.ProdutoRepository;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.repository.ProdutoBuffetRepository;
import br.com.sistematenda.buffet.service.ProdutoBuffetService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovimentoEstoqueService extends BaseCrudServiceImpl<MovimentoEstoque, MovimentoEstoqueDTO, UUID> {

    @Inject
    MovimentoEstoqueRepository repository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    ProdutoBuffetRepository produtoBuffetRepository;

    @Inject
    ProdutoService produtoService;

    @Inject
    ProdutoBuffetService produtoBuffetService;

    @Override
    public MovimentoEstoqueRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<MovimentoEstoque> getEntityClass() {
        return MovimentoEstoque.class;
    }

    public PageResponse<MovimentoEstoqueDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                     MovimentoEstoque.TipoMovimento tipo,
                                                     MovimentoEstoque.OrigemDestino origem,
                                                     MovimentoEstoque.OrigemDestino destino,
                                                     UUID produto,
                                                     String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipo = ?").append(params.size() + 1);
            params.add(tipo);
        }
        if (origem != null) {
            query.append(" and origem = ?").append(params.size() + 1);
            params.add(origem);
        }
        if (destino != null) {
            query.append(" and destino = ?").append(params.size() + 1);
            params.add(destino);
        }
        if (produto != null) {
            query.append(" and (produtoFruteira.id = ?").append(params.size() + 1)
                 .append(" or produtoBuffet.id = ?").append(params.size() + 2).append(")");
            params.add(produto);
            params.add(produto);
        }
        if (dataInicio != null) {
            query.append(" and data >= ?").append(params.size() + 1);
            params.add(dataInicio);
        }
        if (dataFim != null) {
            query.append(" and data <= ?").append(params.size() + 1);
            params.add(dataFim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "data",
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        Page pageable = Page.of(page, size);

        var panacheQuery = repository.find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);

        List<MovimentoEstoque> content = panacheQuery.list();
        long totalElements = repository.count(query.toString(), params.toArray());

        return toDtoPage(PageResponse.of(content, totalElements, page, size));
    }

    public PageResponse<MovimentoEstoqueDTO> findDefeitos(int page, int size, String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false and tipo like '%DEFEITO%'");
        var params = new java.util.ArrayList<Object>();

        if (dataInicio != null) {
            query.append(" and data >= ?").append(params.size() + 1);
            params.add(dataInicio);
        }
        if (dataFim != null) {
            query.append(" and data <= ?").append(params.size() + 1);
            params.add(dataFim);
        }

        Sort sort = Sort.by("data", Sort.Direction.Descending);
        Page pageable = Page.of(page, size);

        var panacheQuery = repository.find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);

        List<MovimentoEstoque> content = panacheQuery.list();
        long totalElements = repository.count(query.toString(), params.toArray());

        return toDtoPage(PageResponse.of(content, totalElements, page, size));
    }

    public PageResponse<MovimentoEstoqueDTO> findEntradas(int page, int size, String dataInicio, String dataFim) {
        return findByTipoPrefix("ENTRADA", page, size, dataInicio, dataFim);
    }

    public PageResponse<MovimentoEstoqueDTO> findSaidas(int page, int size, String dataInicio, String dataFim) {
        return findByTipoPrefix("SAIDA", page, size, dataInicio, dataFim);
    }

    private PageResponse<MovimentoEstoqueDTO> findByTipoPrefix(String prefix, int page, int size, String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false and tipo like ?1");
        var params = new java.util.ArrayList<Object>();
        params.add(prefix + "%");

        if (dataInicio != null) {
            query.append(" and data >= ?").append(params.size() + 1);
            params.add(dataInicio);
        }
        if (dataFim != null) {
            query.append(" and data <= ?").append(params.size() + 1);
            params.add(dataFim);
        }

        Sort sort = Sort.by("data", Sort.Direction.Descending);
        Page pageable = Page.of(page, size);

        var panacheQuery = repository.find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);

        List<MovimentoEstoque> content = panacheQuery.list();
        long totalElements = repository.count(query.toString(), params.toArray());

        return toDtoPage(PageResponse.of(content, totalElements, page, size));
    }

    public PageResponse<MovimentoEstoqueDTO> findEstoqueBaixo(int page, int size) {
        StringBuilder query = new StringBuilder("deleted = false and tipo like 'SAIDA_PER%'");
        var params = new java.util.ArrayList<Object>();
        Sort sort = Sort.by("data", Sort.Direction.Descending);
        Page pageable = Page.of(page, size);
        var panacheQuery = repository.find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        List<MovimentoEstoque> content = panacheQuery.list();
        long totalElements = repository.count(query.toString(), params.toArray());
        return toDtoPage(PageResponse.of(content, totalElements, page, size));
    }

    @Transactional
    public MovimentoEstoqueDTO createMovement(MovimentoEstoqueDTO dto) {
        BigDecimal estoqueAnterior = BigDecimal.ZERO;
        BigDecimal estoquePosterior = BigDecimal.ZERO;

        // Get current stock and update it based on movement type
        if (dto.getProdutoFruteiraId() != null) {
            Produto produto = produtoRepository.findById(dto.getProdutoFruteiraId());
            if (produto != null) {
                estoqueAnterior = produto.estoqueAtual;

                // Determine if it's an entry or exit
                boolean isEntrada = dto.getTipo() != null && dto.getTipo().isEntrada();

                if (isEntrada) {
                    estoquePosterior = estoqueAnterior.add(dto.getQuantidade());
                } else {
                    estoquePosterior = estoqueAnterior.subtract(dto.getQuantidade());
                    if (estoquePosterior.compareTo(BigDecimal.ZERO) < 0) {
                        estoquePosterior = BigDecimal.ZERO;
                    }
                }

                produto.estoqueAtual = estoquePosterior;
                produtoRepository.persist(produto);
            }
        } else if (dto.getProdutoBuffetId() != null) {
            ProdutoBuffet produto = produtoBuffetRepository.findById(dto.getProdutoBuffetId());
            if (produto != null && produto.controlaEstoque) {
                estoqueAnterior = produto.estoqueAtual;

                boolean isEntrada = dto.getTipo() != null && dto.getTipo().isEntrada();

                if (isEntrada) {
                    estoquePosterior = estoqueAnterior.add(dto.getQuantidade());
                } else {
                    estoquePosterior = estoqueAnterior.subtract(dto.getQuantidade());
                    if (estoquePosterior.compareTo(BigDecimal.ZERO) < 0) {
                        estoquePosterior = BigDecimal.ZERO;
                    }
                }

                produto.estoqueAtual = estoquePosterior;
                produtoBuffetRepository.persist(produto);
            }
        }

        // Create the movement record with stock tracking
        MovimentoEstoque entity = mapper.toEntity(dto);
        if (entity.id == null) {
            entity.id = generateId();
        }
        entity.estoqueAnterior = estoqueAnterior;
        entity.estoquePosterior = estoquePosterior;
        if (entity.data == null) {
            entity.data = LocalDateTime.now();
        }
        if (entity.valorTotal == null && entity.custoUnitario != null && entity.quantidade != null) {
            entity.valorTotal = entity.quantidade.multiply(entity.custoUnitario);
        }

        // For defect entries from reprocessing, add recoverable quantity to stock
        if (entity.tipo == MovimentoEstoque.TipoMovimento.ENTRADA_DEFEITO_REPROCESSO && entity.quantidadeRecuperavel != null) {
            // The recoverable quantity is already added via the normal entry logic above
            // This field is for tracking purposes
        }

        repository.persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public MovimentoEstoqueDTO registrarDefeito(UUID produtoFruteiraId, UUID produtoBuffetId,
                                                 MovimentoEstoque.TipoMovimento tipoDefeito,
                                                 BigDecimal quantidade, BigDecimal custoUnitario,
                                                 String motivo, String tipoDefeitoDesc,
                                                 String lote, String responsavel,
                                                 BigDecimal quantidadeRecuperavel) {
        MovimentoEstoqueDTO dto = new MovimentoEstoqueDTO();
        dto.setProdutoFruteiraId(produtoFruteiraId);
        dto.setProdutoBuffetId(produtoBuffetId);
        dto.setTipo(tipoDefeito);
        dto.setOrigem(MovimentoEstoque.OrigemDestino.DEFEITO);
        dto.setDestino(MovimentoEstoque.OrigemDestino.PERDA);
        dto.setQuantidade(quantidade);
        dto.setCustoUnitario(custoUnitario);
        dto.setMotivoDefeito(motivo);
        dto.setTipoDefeito(tipoDefeitoDesc);
        dto.setLoteDefeituoso(lote);
        dto.setQuantidadeRecuperavel(quantidadeRecuperavel);
        dto.setResponsavel(responsavel);
        dto.setData(LocalDateTime.now());
        return createMovement(dto);
    }

    @Transactional
    public MovimentoEstoqueDTO registrarEntradaCompra(UUID produtoFruteiraId, UUID produtoBuffetId,
                                                       BigDecimal quantidade, BigDecimal custoUnitario,
                                                       String documentoReferencia, String numeroDocumento,
                                                       String lote, LocalDateTime dataValidade,
                                                       String responsavel) {
        MovimentoEstoqueDTO dto = new MovimentoEstoqueDTO();
        dto.setProdutoFruteiraId(produtoFruteiraId);
        dto.setProdutoBuffetId(produtoBuffetId);
        dto.setTipo(MovimentoEstoque.TipoMovimento.ENTRADA_COMPRA);
        dto.setOrigem(MovimentoEstoque.OrigemDestino.FORNECEDOR);
        dto.setDestino(produtoFruteiraId != null ? MovimentoEstoque.OrigemDestino.FRUTEIRA : MovimentoEstoque.OrigemDestino.BUFFET);
        dto.setQuantidade(quantidade);
        dto.setCustoUnitario(custoUnitario);
        dto.setDocumentoReferencia(documentoReferencia);
        dto.setNumeroDocumento(numeroDocumento);
        dto.setLote(lote);
        dto.setDataValidade(dataValidade);
        dto.setResponsavel(responsavel);
        dto.setData(LocalDateTime.now());
        return createMovement(dto);
    }

    @Transactional
    public MovimentoEstoqueDTO registrarEntradaTransferencia(UUID produtoFruteiraId, UUID produtoBuffetId,
                                                              BigDecimal quantidade, BigDecimal custoUnitario,
                                                              MovimentoEstoque.OrigemDestino origem,
                                                              String responsavel) {
        MovimentoEstoqueDTO dto = new MovimentoEstoqueDTO();
        dto.setProdutoFruteiraId(produtoFruteiraId);
        dto.setProdutoBuffetId(produtoBuffetId);
        dto.setTipo(MovimentoEstoque.TipoMovimento.ENTRADA_TRANSFERENCIA);
        dto.setOrigem(origem);
        dto.setDestino(produtoFruteiraId != null ? MovimentoEstoque.OrigemDestino.FRUTEIRA : MovimentoEstoque.OrigemDestino.BUFFET);
        dto.setQuantidade(quantidade);
        dto.setCustoUnitario(custoUnitario);
        dto.setResponsavel(responsavel);
        dto.setData(LocalDateTime.now());
        return createMovement(dto);
    }

    @Transactional
    public MovimentoEstoqueDTO registrarSaidaVenda(UUID produtoFruteiraId, UUID produtoBuffetId,
                                                    BigDecimal quantidade, BigDecimal custoUnitario,
                                                    String responsavel) {
        MovimentoEstoqueDTO dto = new MovimentoEstoqueDTO();
        dto.setProdutoFruteiraId(produtoFruteiraId);
        dto.setProdutoBuffetId(produtoBuffetId);
        dto.setTipo(MovimentoEstoque.TipoMovimento.SAIDA_VENDA);
        dto.setOrigem(produtoFruteiraId != null ? MovimentoEstoque.OrigemDestino.FRUTEIRA : MovimentoEstoque.OrigemDestino.BUFFET);
        dto.setDestino(MovimentoEstoque.OrigemDestino.CLIENTE);
        dto.setQuantidade(quantidade);
        dto.setCustoUnitario(custoUnitario);
        dto.setResponsavel(responsavel);
        dto.setData(LocalDateTime.now());
        return createMovement(dto);
    }

    @Transactional
    public MovimentoEstoqueDTO registrarAjuste(UUID produtoFruteiraId, UUID produtoBuffetId,
                                                BigDecimal quantidade, BigDecimal custoUnitario,
                                                boolean isEntrada, String observacoes, String responsavel) {
        MovimentoEstoqueDTO dto = new MovimentoEstoqueDTO();
        dto.setProdutoFruteiraId(produtoFruteiraId);
        dto.setProdutoBuffetId(produtoBuffetId);
        dto.setTipo(isEntrada ? MovimentoEstoque.TipoMovimento.ENTRADA_AJUSTE : MovimentoEstoque.TipoMovimento.SAIDA_AJUSTE);
        dto.setOrigem(isEntrada ? MovimentoEstoque.OrigemDestino.AJUSTE : (produtoFruteiraId != null ? MovimentoEstoque.OrigemDestino.FRUTEIRA : MovimentoEstoque.OrigemDestino.BUFFET));
        dto.setDestino(isEntrada ? (produtoFruteiraId != null ? MovimentoEstoque.OrigemDestino.FRUTEIRA : MovimentoEstoque.OrigemDestino.BUFFET) : MovimentoEstoque.OrigemDestino.AJUSTE);
        dto.setQuantidade(quantidade);
        dto.setCustoUnitario(custoUnitario);
        dto.setObservacoes(observacoes);
        dto.setResponsavel(responsavel);
        dto.setData(LocalDateTime.now());
        return createMovement(dto);
    }
}