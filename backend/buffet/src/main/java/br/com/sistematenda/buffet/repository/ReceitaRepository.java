package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.Receita;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ReceitaRepository implements PanacheRepositoryBase<Receita, UUID> {

    public Optional<Receita> findByProduto(ProdutoBuffet produto) {
        return find("produto = ?1 and deleted = false", produto).firstResultOptional();
    }

    public List<Receita> findAtivas() {
        return find("ativa = true and deleted = false").list();
    }

    public PageResponse<Receita> findPaginated(int page, int size, String sortBy, String sortDirection,
                                               Boolean ativa) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (ativa != null) {
            query.append(" and ativa = ?").append(params.size() + 1);
            params.add(ativa);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "produto.nome", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(size, page);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Receita> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }
}