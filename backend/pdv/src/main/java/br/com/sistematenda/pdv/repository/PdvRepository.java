package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PdvRepository implements PanacheRepositoryBase<Pdv, UUID> {

    public Optional<Pdv> findByCodigo(String codigo) {
        return find("codigo = ?1 and deleted = false", codigo).firstResultOptional();
    }

    public List<Pdv> findByStatus(Pdv.StatusPdv status) {
        return find("status = ?1 and ativo = true and deleted = false", status).list();
    }

    public List<Pdv> findAtivos() {
        return find("ativo = true and deleted = false").list();
    }

    public List<Pdv> findLivres() {
        return find("status = 'LIVRE' and ativo = true and deleted = false").list();
    }

    public PageResponse<Pdv> findPaginated(int page, int size, String sortBy, String sortDirection,
                                           Pdv.StatusPdv status, Boolean ativo) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (ativo != null) {
            query.append(" and ativo = ?").append(params.size() + 1);
            params.add(ativo);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "codigo", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Pdv> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public void atualizarStatus(UUID pdvId, Pdv.StatusPdv status) {
        Pdv pdv = findById(pdvId);
        if (pdv != null) {
            pdv.status = status;
            persist(pdv);
        }
    }
}