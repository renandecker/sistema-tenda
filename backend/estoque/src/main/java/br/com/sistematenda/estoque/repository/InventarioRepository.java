package br.com.sistematenda.estoque.repository;

import br.com.sistematenda.estoque.entity.Inventario;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class InventarioRepository implements PanacheRepository<Inventario> {

    public List<Inventario> findByStatus(Inventario.StatusInventario status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<Inventario> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return find("dataInicio >= ?1 and dataInicio <= ?2 and deleted = false", inicio, fim).list();
    }

    public PageResponse<Inventario> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                   Inventario.StatusInventario status,
                                                   LocalDateTime inicio, LocalDateTime fim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (inicio != null) {
            query.append(" and dataInicio >= ?").append(params.size() + 1);
            params.add(inicio);
        }
        if (fim != null) {
            query.append(" and dataInicio <= ?").append(params.size() + 1);
            params.add(fim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataInicio", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Inventario> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }
}