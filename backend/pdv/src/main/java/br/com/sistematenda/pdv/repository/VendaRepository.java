package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.StatusVenda;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VendaRepository implements PanacheRepositoryBase<Venda, UUID> {

    public Optional<Venda> findByNumero(String numero) {
        return find("numero = ?1 and deleted = false", numero).firstResultOptional();
    }

    public Optional<Venda> findByComanda(Comanda comanda) {
        return find("comanda = ?1 and deleted = false", comanda).firstResultOptional();
    }

    public List<Venda> findByStatus(StatusVenda status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<Venda> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("data >= ?1 and data <= ?2 and deleted = false", inicio, fim).list();
    }

    public List<Venda> findByPdv(UUID pdvId) {
        return find("pdvId = ?1 and deleted = false", pdvId).list();
    }

    public List<Venda> findByCliente(String documento) {
        return find("clienteDocumento = ?1 and deleted = false", documento).list();
    }

    public PageResponse<Venda> findPaginated(int page, int size, String sortBy, String sortDirection,
                                             StatusVenda status, LocalDate dataInicio, LocalDate dataFim,
                                             UUID pdvId, String clienteDocumento) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (dataInicio != null) {
            query.append(" and data >= ?").append(params.size() + 1);
            params.add(dataInicio.atStartOfDay());
        }
        if (dataFim != null) {
            query.append(" and data <= ?").append(params.size() + 1);
            params.add(dataFim.atTime(23, 59, 59));
        }
        if (pdvId != null) {
            query.append(" and pdvId = ?").append(params.size() + 1);
            params.add(pdvId);
        }
        if (clienteDocumento != null) {
            query.append(" and clienteDocumento = ?").append(params.size() + 1);
            params.add(clienteDocumento);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "data", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Venda> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public java.math.BigDecimal getTotalVendasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("select sum(valorTotal) from Venda where data >= ?1 and data <= ?2 and status in ('FINALIZADA', 'PAGA') and deleted = false", 
                inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalFruteiraPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("select sum(subtotalFruteira) from Venda where data >= ?1 and data <= ?2 and status in ('FINALIZADA', 'PAGA') and deleted = false", 
                inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalBuffetPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("select sum(subtotalBuffet) from Venda where data >= ?1 and data <= ?2 and status in ('FINALIZADA', 'PAGA') and deleted = false", 
                inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public long countByStatus(StatusVenda status) {
        return count("status = ?1 and deleted = false", status);
    }

    public String getProximoNumero(UUID pdvId) {
        // Numero e unico globalmente (idx_pdv_venda_numero), portanto o sequencial
        // e calculado sobre todas as vendas para evitar colisao entre PDVs.
        String ultimoNumero = find("select max(numero) from Venda where deleted = false")
            .project(String.class)
            .firstResultOptional()
            .orElse(null);
        long sequencia = 0L;
        if (ultimoNumero != null) {
            try {
                sequencia = Long.parseLong(ultimoNumero.trim());
            } catch (NumberFormatException ignored) {
                sequencia = 0L;
            }
        }
        return String.format("%06d", sequencia + 1);
    }
}