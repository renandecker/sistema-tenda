package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.pdv.entity.ItemVenda;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ItemVendaRepository implements PanacheRepositoryBase<ItemVenda, UUID> {

    public List<ItemVenda> findByVenda(Venda venda) {
        return find("venda = ?1 and cancelado = false and deleted = false", venda).list();
    }

    public List<ItemVenda> findByVendaIncluindoCancelados(Venda venda) {
        return find("venda = ?1 and deleted = false", venda).list();
    }

    public List<ItemVenda> findByOrigem(ItemVenda.OrigemItem origem) {
        return find("origem = ?1 and cancelado = false and deleted = false", origem).list();
    }

    public PageResponse<ItemVenda> findByVendaPaginated(Venda venda, int page, int size,
                                                         String sortBy, String sortDirection) {
        String query = "venda = ?1 and cancelado = false and deleted = false";
        Sort sort = Sort.by(sortBy != null ? sortBy : "id", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query, sort, venda);
        panacheQuery.page(pageable);
        
        List<ItemVenda> content = panacheQuery.list();
        long totalElements = count(query, venda);
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public java.math.BigDecimal getTotalByVenda(Venda venda) {
        return find("select sum(valorTotal) from ItemVenda where venda = ?1 and cancelado = false and deleted = false", venda)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalByOrigemPeriodo(ItemVenda.OrigemItem origem,
                                                         java.time.LocalDateTime inicio,
                                                         java.time.LocalDateTime fim) {
        return find("select sum(valorTotal) from ItemVenda where origem = ?1 and venda.data >= ?2 and venda.data <= ?3 and cancelado = false and deleted = false", 
                origem, inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }
}