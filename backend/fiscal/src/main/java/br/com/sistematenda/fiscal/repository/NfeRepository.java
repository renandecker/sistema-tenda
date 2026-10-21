package br.com.sistematenda.fiscal.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.fiscal.entity.Nfe;
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
public class NfeRepository implements PanacheRepositoryBase<Nfe, UUID> {

    public PageResponse<Nfe> findPaginated(int page, int size, String sortBy, String sortDirection,
                                            Nfe.StatusNfe status, Nfe.ModeloDocumento modelo,
                                            String dataInicio, String dataFim, UUID vendaId) {
        StringBuilder query = new StringBuilder("deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (status != null) {
            query.append(" AND status = :status");
            params.put("status", status);
        }
        if (modelo != null) {
            query.append(" AND modelo = :modelo");
            params.put("modelo", modelo.getCodigo());
        }
        if (vendaId != null) {
            query.append(" AND vendaId = :vendaId");
            params.put("vendaId", vendaId);
        }
        if (dataInicio != null && !dataInicio.isEmpty()) {
            query.append(" AND dataEmissao >= :dataInicio");
            params.put("dataInicio", LocalDate.parse(dataInicio).atStartOfDay());
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            query.append(" AND dataEmissao <= :dataFim");
            params.put("dataFim", LocalDate.parse(dataFim).atTime(23, 59, 59));
        }

        return doFindPaginated(query.toString(), buildSort(sortBy, sortDirection), params, page, size);
    }

    public PageResponse<Nfe> findPaginated(int page, int size, String sortBy, String sortDirection,
                                            String status, String modelo,
                                            String busca, String dataInicio, String dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (status != null && !status.isEmpty()) {
            query.append(" AND status = :status");
            params.put("status", Nfe.StatusNfe.valueOf(status));
        }
        if (modelo != null && !modelo.isEmpty()) {
            query.append(" AND modelo = :modelo");
            params.put("modelo", modelo);
        }
        if (busca != null && !busca.isEmpty()) {
            query.append(" AND (chaveAcesso LIKE :busca OR clienteNome LIKE :busca OR clienteDocumento LIKE :busca)");
            params.put("busca", "%" + busca + "%");
        }
        if (dataInicio != null && !dataInicio.isEmpty()) {
            query.append(" AND dataEmissao >= :dataInicio");
            params.put("dataInicio", LocalDate.parse(dataInicio).atStartOfDay());
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            query.append(" AND dataEmissao <= :dataFim");
            params.put("dataFim", LocalDate.parse(dataFim).atTime(23, 59, 59));
        }

        return doFindPaginated(query.toString(), buildSort(sortBy, sortDirection), params, page, size);
    }

    private PageResponse<Nfe> doFindPaginated(String query, Sort sort, Map<String, Object> params,
                                              int page, int size) {
        List<Nfe> content = find(query, sort, params).page(Page.of(page, size)).list();
        long totalElements = count(query, params);
        return PageResponse.of(content, totalElements, page, size);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        String column = "dataEmissao";
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy) {
                case "dataEmissao":
                case "status":
                case "modelo":
                case "serie":
                case "numero":
                case "chaveAcesso":
                case "clienteNome":
                case "cnpjProdutor":
                case "ambiente":
                case "cfopProdutoRural":
                case "naturezaOperacao":
                case "dataSaidaEntrada":
                case "dataColheita":
                case "volumeProducao":
                case "areaProducao":
                case "tipoProducao":
                case "gradeProducao":
                case "ehProdutoRural":
                case "modalidadeAcre":
                case "ruralIe":
                case "meioPagamento":
                case "codigoNcm":
                case "cstIcms":
                case "cstPis":
                case "cstCofins":
                case "valorIcms":
                case "valorPis":
                case "valorCofins":
                case "valorIpi":
                case "total":
                case "deleted":
                case "version":
                case "vendaId":
                    column = sortBy;
                    break;
                default:
                    column = "dataEmissao";
            }
        }
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;
        return Sort.by(column, direction);
    }

    public Nfe findByChaveAcesso(String chaveAcesso) {
        return find("chaveAcesso = ?1 and deleted = false", chaveAcesso).firstResult();
    }

    public Nfe findByNumeroESerie(Long numero, Integer serie) {
        return find("numero = ?1 and serie = ?2 and deleted = false", numero, serie).firstResult();
    }

    public Long getProximoNumero(Integer serie) {
        Object max = find("select max(numero) from Nfe where serie = ?1 and deleted = false", serie)
                .firstResult();
        return (max != null ? ((Number) max).longValue() : 0L) + 1;
    }
}
