package br.com.sistematenda.fiscal.repository;

import br.com.sistematenda.fiscal.entity.Compra;
import br.com.sistematenda.fiscal.entity.ItemCompra;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CompraRepository implements PanacheRepository<Compra> {

    public List<Compra> findByFornecedor(Fornecedor fornecedor) {
        return find("fornecedor = ?1 and deleted = false", fornecedor).list();
    }

    public List<Compra> findByStatus(StatusCompra status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<Compra> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataCompra >= ?1 and dataCompra <= ?2 and deleted = false", inicio, fim).list();
    }

    public PageResponse<CompraDTO> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                 StatusCompra status, LocalDate dataInicio, LocalDate dataFim,
                                                 Fornecedor fornecedor) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (dataInicio != null) {
            query.append(" and dataCompra >= ?").append(params.size() + 1);
            params.add(dataInicio.atStartOfDay());
        }
        if (dataFim != null) {
            query.append(" and dataCompra <= ?").append(params.size() + 1);
            params.add(dataFim.atTime(23, 59, 59));
        }
        if (fornecedor != null) {
            query.append(" and fornecedor.id = ?").append(params.size() + 1);
            params.add(fornecedor.id);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataCompra", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Compra> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        // Converter para DTOs
        java.util.List<CompraDTO> dtoList = content.stream()
            .map(c -> {
                CompraDTO dto = new CompraDTO();
                dto.id = c.id.toString();
                dto.numeroNota = c.numeroNota;
                serieNota = c.serieNota;
                dto.dataCompra = c.dataCompra;
                dto.fornecedor = c.fornecedor != null ? c.fornecedor.nome : "";
                dto.valorTotal = c.valorTotal;
                dto.status = c.status;
                dto.condicaoPagamento = c.condicaoPagamento;
                return dto;
            })
            .toList();
        
        return new PageResponse<>(
            dtoList,
            totalElements,
            page,
            size
        );
    }

    public java.math.BigDecimal getTotalComprasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataCompra >= ?1 and dataCompra <= ?2 and status = 'RECEBIDA' and deleted = false", inicio, fim)
            .project(java.math.BigDecimal.class, "sum(valorTotal)")
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalCeasaPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataCompra >= ?1 and dataCompra <= ?2 and status = 'RECEBIDA' and deleted = false ", inicio, fim)
            .project(java.math.BigDecimal.class, "sum(preco_referencia_ceasa * quantidade)")
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }
}