package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.ItemComanda;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ItemComandaRepository implements PanacheRepository<ItemComanda> {

    public List<ItemComanda> findByComanda(Comanda comanda) {
        return find("comanda = ?1 and cancelado = false and deleted = false", comanda).list();
    }

    public List<ItemComanda> findByComandaIncluindoCancelados(Comanda comanda) {
        return find("comanda = ?1 and deleted = false", comanda).list();
    }

    public List<ItemComanda> findByTipo(ItemComanda.TipoItem tipo) {
        return find("tipo = ?1 and cancelado = false and deleted = false", tipo).list();
    }

    public List<ItemComanda> findByProdutoId(UUID produtoId) {
        return find("produtoId = ?1 and cancelado = false and deleted = false", produtoId).list();
    }

    public PageResponse<ItemComanda> findByComandaPaginated(Comanda comanda, int page, int size, 
                                                             String sortBy, String sortDirection) {
        String query = "comanda = ?1 and cancelado = false and deleted = false";
        Sort sort = Sort.by(sortBy != null ? sortBy : "dataLancamento", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query, sort, comanda);
        panacheQuery.page(pageable);
        
        List<ItemComanda> content = panacheQuery.list();
        long totalElements = count(query, comanda);
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public java.math.BigDecimal getTotalByComanda(Comanda comanda) {
        return find("comanda = ?1 and cancelado = false and deleted = false", comanda)
            .project(java.math.BigDecimal.class, "sum(valorTotal)")
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalByTipoEPeriodo(ItemComanda.TipoItem tipo, 
                                                        java.time.LocalDateTime inicio, 
                                                        java.time.LocalDateTime fim) {
        return find("tipo = ?1 and dataLancamento >= ?2 and dataLancamento <= ?3 and cancelado = false and deleted = false", 
                tipo, inicio, fim)
            .project(java.math.BigDecimal.class, "sum(valorTotal)")
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }
}