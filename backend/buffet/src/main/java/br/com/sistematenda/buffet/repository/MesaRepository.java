package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MesaRepository implements PanacheRepositoryBase<Mesa, UUID> {

    public Optional<Mesa> findByNumero(String numero) {
        return find("numero = ?1 and deleted = false", numero).firstResultOptional();
    }

    public List<Mesa> findByStatus(Mesa.StatusMesa status) {
        return find("status = ?1 and ativa = true and deleted = false", status).list();
    }

    public List<Mesa> findDisponiveis() {
        return find("status = 'LIVRE' and ativa = true and deleted = false").list();
    }

    public List<Mesa> findByArea(String area) {
        return find("area = ?1 and ativa = true and deleted = false", area).list();
    }

    public List<Mesa> findAtivas() {
        return find("ativa = true and deleted = false order by numero").list();
    }

    public PageResponse<Mesa> findPaginated(int page, int size, String sortBy, String sortDirection,
                                            Mesa.StatusMesa status, Boolean ativa, String area) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (ativa != null) {
            query.append(" and ativa = ?").append(params.size() + 1);
            params.add(ativa);
        }
        if (area != null) {
            query.append(" and area = ?").append(params.size() + 1);
            params.add(area);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "numero", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Mesa> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public void atualizarStatus(UUID mesaId, Mesa.StatusMesa status) {
        Mesa mesa = findById(mesaId);
        if (mesa != null) {
            mesa.status = status;
            persist(mesa);
        }
    }
}