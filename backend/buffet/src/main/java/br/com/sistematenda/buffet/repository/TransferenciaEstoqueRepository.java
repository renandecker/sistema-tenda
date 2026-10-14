package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.TransferenciaEstoque;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransferenciaEstoqueRepository implements PanacheRepositoryBase<TransferenciaEstoque, UUID> {

    public List<TransferenciaEstoque> findByStatus(TransferenciaEstoque.StatusTransferencia status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<TransferenciaEstoque> findByProduto(UUID produtoId) {
        return find("produto.id = ?1 and deleted = false", produtoId).list();
    }

    public List<TransferenciaEstoque> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return find("data >= ?1 and data <= ?2 and deleted = false", inicio, fim).list();
    }

    public List<TransferenciaEstoque> findPendentes() {
        return find("status = 'PENDENTE' and deleted = false").list();
    }

    public PageResponse<TransferenciaEstoque> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                             TransferenciaEstoque.StatusTransferencia status,
                                                             UUID produtoId, LocalDateTime inicio, LocalDateTime fim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (produtoId != null) {
            query.append(" and produto.id = ?").append(params.size() + 1);
            params.add(produtoId);
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
        
        List<TransferenciaEstoque> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public BigDecimal getTotalTransferidoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal total = Panache.getEntityManager()
            .createQuery("select coalesce(sum(t.valorTotal), 0) from TransferenciaEstoque t "
                + "where t.data >= :inicio and t.data <= :fim "
                + "and t.status = 'CONCLUIDA' and t.deleted = false", BigDecimal.class)
            .setParameter("inicio", inicio)
            .setParameter("fim", fim)
            .getSingleResult();
        return total != null ? total : BigDecimal.ZERO;
    }
}