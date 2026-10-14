package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.ItemComanda;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ItemComandaRepository implements PanacheRepositoryBase<ItemComanda, UUID> {

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
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query, sort, comanda);
        panacheQuery.page(pageable);
        
        List<ItemComanda> content = panacheQuery.list();
        long totalElements = count(query, comanda);
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public BigDecimal getTotalByComanda(Comanda comanda) {
        BigDecimal total = Panache.getEntityManager()
            .createQuery("select coalesce(sum(i.valorTotal), 0) from ItemComanda i "
                + "where i.comanda = :comanda and i.cancelado = false and i.deleted = false", BigDecimal.class)
            .setParameter("comanda", comanda)
            .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalByTipoEPeriodo(ItemComanda.TipoItem tipo,
                                             java.time.LocalDateTime inicio,
                                             java.time.LocalDateTime fim) {
        BigDecimal total = Panache.getEntityManager()
            .createQuery("select coalesce(sum(i.valorTotal), 0) from ItemComanda i "
                + "where i.tipo = :tipo and i.dataLancamento >= :inicio and i.dataLancamento <= :fim "
                + "and i.cancelado = false and i.deleted = false", BigDecimal.class)
            .setParameter("tipo", tipo)
            .setParameter("inicio", inicio)
            .setParameter("fim", fim)
            .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }
}