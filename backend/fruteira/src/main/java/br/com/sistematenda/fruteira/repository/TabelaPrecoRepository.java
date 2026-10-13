package br.com.sistematenda.fruteira.repository;

import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.entity.TabelaPreco;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TabelaPrecoRepository implements PanacheRepository<TabelaPreco> {

    public List<TabelaPreco> findByProduto(Produto produto) {
        return find("produto = ?1 and deleted = false order by dataInicio desc", produto).list();
    }

    public Optional<TabelaPreco> findVigenteByProduto(Produto produto, LocalDate data) {
        return find("produto = ?1 and ativa = true and dataInicio <= ?2 and (dataFim is null or dataFim >= ?2) and deleted = false", 
                produto, data)
            .firstResultOptional();
    }

    public List<TabelaPreco> findVigentes(LocalDate data) {
        return find("ativa = true and dataInicio <= ?1 and (dataFim is null or dataFim >= ?1) and deleted = false", data).list();
    }

    public List<TabelaPreco> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return find("dataInicio >= ?1 and dataInicio <= ?2 and deleted = false", dataInicio, dataFim).list();
    }

    public PageResponse<TabelaPreco> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                   Boolean ativa, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (ativa != null) {
            query.append(" and ativa = ?").append(params.size() + 1);
            params.add(ativa);
        }
        if (dataInicio != null) {
            query.append(" and dataInicio >= ?").append(params.size() + 1);
            params.add(dataInicio);
        }
        if (dataFim != null) {
            query.append(" and dataInicio <= ?").append(params.size() + 1);
            params.add(dataFim);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataInicio", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<TabelaPreco> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public void desativarTabelasAntigas(Produto produto, LocalDate dataInicioNova) {
        find("produto = ?1 and ativa = true and dataInicio < ?2 and deleted = false", produto, dataInicioNova)
            .list()
            .forEach(t -> {
                t.ativa = false;
                persist(t);
            });
    }
}