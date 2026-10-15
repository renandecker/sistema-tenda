package br.com.sistematenda.fiscal.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.fiscal.entity.AtivoImobilizado;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class AtivoImobilizadoRepository implements PanacheRepositoryBase<AtivoImobilizado, UUID> {

    public PageResponse<AtivoImobilizado> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                        AtivoImobilizado.StatusAtivo status,
                                                        AtivoImobilizado.TipoAtivo tipo,
                                                        AtivoImobilizado.UnidadeNegocio unidadeNegocio,
                                                        String busca) {
        StringBuilder query = new StringBuilder("deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (status != null) {
            query.append(" AND status = :status");
            params.put("status", status);
        }
        if (tipo != null) {
            query.append(" AND tipo = :tipo");
            params.put("tipo", tipo);
        }
        if (unidadeNegocio != null) {
            query.append(" AND unidadeNegocio = :unidadeNegocio");
            params.put("unidadeNegocio", unidadeNegocio);
        }
        if (busca != null && !busca.isEmpty()) {
            query.append(" AND (codigo LIKE :busca OR descricao LIKE :busca OR fornecedor LIKE :busca)");
            params.put("busca", "%" + busca + "%");
        }

        return doFindPaginated(query.toString(), buildSort(sortBy, sortDirection), params, page, size);
    }

    private PageResponse<AtivoImobilizado> doFindPaginated(String query, Sort sort, Map<String, Object> params,
                                                           int page, int size) {
        List<AtivoImobilizado> content = find(query, sort, params).page(Page.of(page, size)).list();
        long totalElements = count(query, params);
        return PageResponse.of(content, totalElements, page, size);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        String column = (sortBy != null && !sortBy.isBlank()) ? sortBy : "dataAquisicao";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;
        return Sort.by(column, direction);
    }

    public AtivoImobilizado findByCodigo(String codigo) {
        return find("codigo = ?1 and deleted = false", codigo).firstResult();
    }
}