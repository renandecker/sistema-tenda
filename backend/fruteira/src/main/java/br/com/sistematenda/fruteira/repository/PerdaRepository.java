package br.com.sistematenda.fruteira.repository;

import br.com.sistematenda.fruteira.entity.Perda;
import br.com.sistematenda.fruteira.entity.Produto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PerdaRepository implements PanacheRepository<Perda> {

    public List<Perda> findByProduto(Produto produto) {
        return find("produto = ?1 and deleted = false", produto).list();
    }

    public List<Perda> findByTipo(Perda.TipoPerda tipo) {
        return find("tipo = ?1 and deleted = false", tipo).list();
    }

    public List<Perda> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return find("data >= ?1 and data <= ?2 and deleted = false", dataInicio, dataFim).list();
    }

    public List<Perda> findByPeriodoETipo(LocalDate dataInicio, LocalDate dataFim, Perda.TipoPerda tipo) {
        return find("data >= ?1 and data <= ?2 and tipo = ?3 and deleted = false", dataInicio, dataFim, tipo).list();
    }

    public BigDecimal getTotalPerdasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return find("data >= ?1 and data <= ?2 and deleted = false", dataInicio, dataFim)
            .project(BigDecimal.class, "sum(valorTotal)")
            .firstResultOptional()
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalPerdasPorTipoPeriodo(Perda.TipoPerda tipo, LocalDate dataInicio, LocalDate dataFim) {
        return find("tipo = ?1 and data >= ?2 and data <= ?3 and deleted = false", tipo, dataInicio, dataFim)
            .project(BigDecimal.class, "sum(valorTotal)")
            .firstResultOptional()
            .orElse(BigDecimal.ZERO);
    }

    public List<Perda> findPendentesBaixaEstoque() {
        return find("baixaEstoqueRealizada = false and deleted = false").list();
    }

    public PageResponse<Perda> findPaginated(int page, int size, String sortBy, String sortDirection,
                                             Perda.TipoPerda tipo, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipo = ?").append(params.size() + 1);
            params.add(tipo);
        }
        if (dataInicio != null) {
            query.append(" and data >= ?").append(params.size() + 1);
            params.add(dataInicio);
        }
        if (dataFim != null) {
            query.append(" and data <= ?").append(params.size() + 1);
            params.add(dataFim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "data", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Perda> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }
}