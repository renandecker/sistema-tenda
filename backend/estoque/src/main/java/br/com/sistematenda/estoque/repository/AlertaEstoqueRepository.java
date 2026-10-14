package br.com.sistematenda.estoque.repository;

import br.com.sistematenda.estoque.entity.AlertaEstoque;
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
public class AlertaEstoqueRepository implements PanacheRepositoryBase<AlertaEstoque, UUID> {

    public List<AlertaEstoque> findByTipo(AlertaEstoque.TipoAlerta tipo) {
        return find("tipo = ?1 and deleted = false", tipo).list();
    }

    public List<AlertaEstoque> findByStatus(AlertaEstoque.StatusAlerta status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<AlertaEstoque> findAtivos() {
        return find("status = 'ATIVO' and deleted = false").list();
    }

    public List<AlertaEstoque> findByProdutoFruteira(Produto produto) {
        return find("produtoFruteira = ?1 and deleted = false", produto).list();
    }

    public List<AlertaEstoque> findByProdutoBuffet(ProdutoBuffet produto) {
        return find("produtoBuffet = ?1 and deleted = false", produto).list();
    }

    public List<AlertaEstoque> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return find("dataAlerta >= ?1 and dataAlerta <= ?2 and deleted = false", inicio, fim).list();
    }

    public PageResponse<AlertaEstoque> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                      AlertaEstoque.TipoAlerta tipo,
                                                      AlertaEstoque.StatusAlerta status,
                                                      UUID produtoFruteiraId, UUID produtoBuffetId,
                                                      LocalDateTime inicio, LocalDateTime fim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipo = ?").append(params.size() + 1);
            params.add(tipo);
        }
        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
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
            query.append(" and dataAlerta >= ?").append(params.size() + 1);
            params.add(inicio);
        }
        if (fim != null) {
            query.append(" and dataAlerta <= ?").append(params.size() + 1);
            params.add(fim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataAlerta", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<AlertaEstoque> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public long countAtivos() {
        return count("status = 'ATIVO' and deleted = false");
    }

    public long countCriticos() {
        return count("prioridade = 'CRITICA' and status = 'ATIVO' and deleted = false");
    }
}