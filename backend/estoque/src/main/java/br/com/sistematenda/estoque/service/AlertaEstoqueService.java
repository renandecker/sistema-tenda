package br.com.sistematenda.estoque.service;

import br.com.sistematenda.estoque.dto.AlertaEstoqueDTO;
import br.com.sistematenda.estoque.entity.AlertaEstoque;
import br.com.sistematenda.estoque.repository.AlertaEstoqueRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AlertaEstoqueService extends BaseCrudServiceImpl<AlertaEstoque, AlertaEstoqueDTO, UUID> {

    @Inject
    AlertaEstoqueRepository repository;

    @Override
    public AlertaEstoqueRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<AlertaEstoque> getEntityClass() {
        return AlertaEstoque.class;
    }

    public PageResponse<AlertaEstoqueDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                  String tipo, String status, UUID produto) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipo = ?").append(params.size() + 1);
            params.add(tipo);
        }
        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (produto != null) {
            query.append(" and produtoFruteira.id = ?").append(params.size() + 1);
            params.add(produto);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataAlerta",
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        Page pageable = Page.of(page, size);

        var panacheQuery = repository.find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);

        List<AlertaEstoque> content = panacheQuery.list();
        long totalElements = repository.count(query.toString(), params.toArray());

        return toDtoPage(PageResponse.of(content, totalElements, page, size));
    }
}