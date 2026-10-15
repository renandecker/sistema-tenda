package br.com.sistematenda.estoque.service;

import br.com.sistematenda.estoque.dto.MovimentoEstoqueDTO;
import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.estoque.repository.MovimentoEstoqueRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovimentoEstoqueService extends BaseCrudServiceImpl<MovimentoEstoque, MovimentoEstoqueDTO, UUID> {

    @Inject
    MovimentoEstoqueRepository repository;

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
            query.append(" and produtoFruteira.id = ?").append(params.size() + 1);
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
}