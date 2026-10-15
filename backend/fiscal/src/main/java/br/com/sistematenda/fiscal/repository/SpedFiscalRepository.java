package br.com.sistematenda.fiscal.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.fiscal.entity.SpedFiscal;
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
public class SpedFiscalRepository implements PanacheRepositoryBase<SpedFiscal, UUID> {

    public PageResponse<SpedFiscal> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                  SpedFiscal.StatusSped status,
                                                  String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (status != null) {
            query.append(" AND status = :status");
            params.put("status", status);
        }
        if (dataInicio != null && !dataInicio.isEmpty()) {
            query.append(" AND dataInicio >= :dataInicio");
            params.put("dataInicio", LocalDate.parse(dataInicio));
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            query.append(" AND dataFim <= :dataFim");
            params.put("dataFim", LocalDate.parse(dataFim));
        }

        return doFindPaginated(query.toString(), buildSort(sortBy, sortDirection), params, page, size);
    }

    private PageResponse<SpedFiscal> doFindPaginated(String query, Sort sort, Map<String, Object> params,
                                                     int page, int size) {
        List<SpedFiscal> content = find(query, sort, params).page(Page.of(page, size)).list();
        long totalElements = count(query, params);
        return PageResponse.of(content, totalElements, page, size);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        String column = (sortBy != null && !sortBy.isBlank()) ? sortBy : "dataInicio";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;
        return Sort.by(column, direction);
    }
}