package br.com.sistematenda.estoque.service;

import br.com.sistematenda.estoque.dto.InventarioDTO;
import br.com.sistematenda.estoque.entity.Inventario;
import br.com.sistematenda.estoque.repository.InventarioRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InventarioService extends BaseCrudServiceImpl<Inventario, InventarioDTO, UUID> {

    @Inject
    InventarioRepository repository;

    @Override
    public InventarioRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<Inventario> getEntityClass() {
        return Inventario.class;
    }

    public PageResponse<InventarioDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                               String status, String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (dataInicio != null) {
            query.append(" and dataInicio >= ?").append(params.size() + 1);
            params.add(dataInicio);
        }
        if (dataFim != null) {
            query.append(" and dataInicio <= ?").append(params.size() + 1);
            params.add(dataFim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataInicio",
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        Page pageable = Page.of(page, size);

        var panacheQuery = repository.find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);

        List<Inventario> content = panacheQuery.list();
        long totalElements = repository.count(query.toString(), params.toArray());

        return toDtoPage(PageResponse.of(content, totalElements, page, size));
    }
}