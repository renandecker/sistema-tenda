package br.com.sistematenda.fiscal.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.fiscal.entity.ConciliacaoCartao;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ConciliacaoCartaoRepository implements PanacheRepositoryBase<ConciliacaoCartao, UUID> {

    public PageResponse<ConciliacaoCartao> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                         ConciliacaoCartao.StatusConciliacao status,
                                                         String adquirente,
                                                         String bandeira,
                                                         String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (status != null) {
            query.append(" AND status = :status");
            params.put("status", status);
        }
        if (adquirente != null && !adquirente.isEmpty()) {
            query.append(" AND adquirente = :adquirente");
            params.put("adquirente", adquirente);
        }
        if (bandeira != null && !bandeira.isEmpty()) {
            query.append(" AND bandeira = :bandeira");
            params.put("bandeira", bandeira);
        }
        if (dataInicio != null && !dataInicio.isEmpty()) {
            query.append(" AND dataVenda >= :dataInicio");
            params.put("dataInicio", LocalDate.parse(dataInicio));
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            query.append(" AND dataVenda <= :dataFim");
            params.put("dataFim", LocalDate.parse(dataFim));
        }

        return doFindPaginated(query.toString(), buildSort(sortBy, sortDirection), params, page, size);
    }

    private PageResponse<ConciliacaoCartao> doFindPaginated(String query, Sort sort, Map<String, Object> params,
                                                            int page, int size) {
        List<ConciliacaoCartao> content = find(query, sort, params).page(Page.of(page, size)).list();
        long totalElements = count(query, params);
        return PageResponse.of(content, totalElements, page, size);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        String column = (sortBy != null && !sortBy.isBlank()) ? sortBy : "dataVenda";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;
        return Sort.by(column, direction);
    }

    public ConciliacaoCartao findByNsu(String nsu) {
        return find("nsu = ?1 and deleted = false", nsu).firstResult();
    }
}