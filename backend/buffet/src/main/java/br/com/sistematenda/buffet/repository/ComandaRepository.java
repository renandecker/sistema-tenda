package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.StatusComanda;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ComandaRepository implements PanacheRepository<Comanda> {

    public Optional<Comanda> findByNumero(String numero) {
        return find("numero = ?1 and deleted = false", numero).firstResultOptional();
    }

    public Optional<Comanda> findByMesaAtiva(Mesa mesa) {
        return find("mesa = ?1 and status in ('ABERTA', 'EM_USO') and deleted = false", mesa).firstResultOptional();
    }

    public List<Comanda> findByStatus(StatusComanda status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<Comanda> findAbertas() {
        return find("status in ('ABERTA', 'EM_USO') and deleted = false").list();
    }

    public List<Comanda> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataAbertura >= ?1 and dataAbertura <= ?2 and deleted = false", inicio, fim).list();
    }

    public List<Comanda> findByCliente(String nomeCliente) {
        return find("lower(clienteNome) like lower(?1) and deleted = false", "%" + nomeCliente + "%").list();
    }

    public PageResponse<Comanda> findPaginated(int page, int size, String sortBy, String sortDirection,
                                               StatusComanda status, LocalDate dataInicio, LocalDate dataFim,
                                               UUID mesaId) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

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
        if (mesaId != null) {
            query.append(" and mesa.id = ?").append(params.size() + 1);
            params.add(mesaId);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataAbertura", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Comanda> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public BigDecimal getTotalVendasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataFechamento >= ?1 and dataFechamento <= ?2 and status in ('FECHADA', 'PAGA') and deleted = false", 
                inicio, fim)
            .project(java.math.BigDecimal.class, "sum(valorTotal)")
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public long countAbertas() {
        return count("status in ('ABERTA', 'EM_USO') and deleted = false");
    }
}