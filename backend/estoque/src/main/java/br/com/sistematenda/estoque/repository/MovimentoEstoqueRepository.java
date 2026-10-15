package br.com.sistematenda.estoque.repository;

import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MovimentoEstoqueRepository implements PanacheRepositoryBase<MovimentoEstoque, UUID> {

    public List<MovimentoEstoque> findByProdutoFruteira(Produto produto) {
        return find("produtoFruteira = ?1 and deleted = false", produto).list();
    }

    public List<MovimentoEstoque> findByProdutoBuffet(ProdutoBuffet produto) {
        return find("produtoBuffet = ?1 and deleted = false", produto).list();
    }

    public List<MovimentoEstoque> findByTipo(MovimentoEstoque.TipoMovimento tipo) {
        return find("tipo = ?1 and deleted = false", tipo).list();
    }

    public List<MovimentoEstoque> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return find("data >= ?1 and data <= ?2 and deleted = false", inicio, fim).list();
    }

    public List<MovimentoEstoque> findByOrigemDestino(MovimentoEstoque.OrigemDestino origem, 
                                                       MovimentoEstoque.OrigemDestino destino) {
        return find("origem = ?1 and destino = ?2 and deleted = false", origem, destino).list();
    }

    public PageResponse<MovimentoEstoque> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                          MovimentoEstoque.TipoMovimento tipo,
                                                          UUID produtoFruteiraId, UUID produtoBuffetId,
                                                          LocalDateTime inicio, LocalDateTime fim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipo = ?").append(params.size() + 1);
            params.add(tipo);
        }
        if (produtoFruteiraId != null) {
            query.append(" and produtoFruteira.id = ?").append(params.size() + 1);
            params.add(produtoFruteiraId);
        }
        if (produtoBuffetId != null) {
            query.append(" and produtoBuffet.id = ?").append(params.size() + 1);
            params.add(produtoBuffetId);
        }
        if (inicio != null) {
            query.append(" and data >= ?").append(params.size() + 1);
            params.add(inicio);
        }
        if (fim != null) {
            query.append(" and data <= ?").append(params.size() + 1);
            params.add(fim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "data", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<MovimentoEstoque> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public java.math.BigDecimal getTotalEntradasPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return io.quarkus.hibernate.orm.panache.Panache.getEntityManager()
            .createQuery("select coalesce(sum(m.valorTotal),0) from MovimentoEstoque m where m.data >= ?1 and m.data <= ?2 and m.tipo like 'ENTRADA%' and m.deleted = false", java.math.BigDecimal.class)
            .setParameter(1, inicio)
            .setParameter(2, fim)
            .getSingleResult();
    }

    public java.math.BigDecimal getTotalSaidasPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return io.quarkus.hibernate.orm.panache.Panache.getEntityManager()
            .createQuery("select coalesce(sum(m.valorTotal),0) from MovimentoEstoque m where m.data >= ?1 and m.data <= ?2 and m.tipo like 'SAIDA%' and m.deleted = false", java.math.BigDecimal.class)
            .setParameter(1, inicio)
            .setParameter(2, fim)
            .getSingleResult();
    }
}