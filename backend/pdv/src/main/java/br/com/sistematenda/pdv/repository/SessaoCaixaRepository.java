package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.common.dto.PageResponse;
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
public class SessaoCaixaRepository implements PanacheRepositoryBase<SessaoCaixa, UUID> {

    public Optional<SessaoCaixa> findAbertaByPdv(Pdv pdv) {
        return find("pdv = ?1 and status = 'ABERTA' and deleted = false", pdv).firstResultOptional();
    }

    public List<SessaoCaixa> findByPdv(Pdv pdv) {
        return find("pdv = ?1 and deleted = false order by dataAbertura desc", pdv).list();
    }

    public List<SessaoCaixa> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataAbertura >= ?1 and dataAbertura <= ?2 and deleted = false", inicio, fim).list();
    }

    public List<SessaoCaixa> findByStatus(SessaoCaixa.StatusSessao status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public PageResponse<SessaoCaixa> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                   UUID pdvId, SessaoCaixa.StatusSessao status,
                                                   LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (pdvId != null) {
            query.append(" and pdv.id = ?").append(params.size() + 1);
            params.add(pdvId);
        }
        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (dataInicio != null) {
            query.append(" and dataAbertura >= ?").append(params.size() + 1);
            params.add(dataInicio.atStartOfDay());
        }
        if (dataFim != null) {
            query.append(" and dataAbertura <= ?").append(params.size() + 1);
            params.add(dataFim.atTime(23, 59, 59));
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataAbertura", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<SessaoCaixa> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public java.math.BigDecimal getTotalVendasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("select sum(totalVendas) from SessaoCaixa where dataAbertura >= ?1 and dataAbertura <= ?2 and status = 'FECHADA' and deleted = false", 
                inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }
}