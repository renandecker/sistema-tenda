package br.com.sistematenda.fruteira.repository;

import br.com.sistematenda.fruteira.entity.EtiquetaBalanca;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.common.dto.PageResponse;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EtiquetaBalancaRepository implements PanacheRepositoryBase<EtiquetaBalanca, UUID> {

    public Optional<EtiquetaBalanca> findByCodigo(String codigo) {
        return find("codigo = ?1 and deleted = false", codigo).firstResultOptional();
    }

    public List<EtiquetaBalanca> findByProduto(Produto produto) {
        return find("produto = ?1 and deleted = false", produto).list();
    }

    public List<EtiquetaBalanca> findAtivas() {
        return find("ativa = true and deleted = false").list();
    }

    public List<EtiquetaBalanca> findAtivasPorProduto(Produto produto) {
        return find("produto = ?1 and ativa = true and deleted = false", produto).list();
    }

    public PageResponse<EtiquetaBalanca> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                       Boolean ativa) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (ativa != null) {
            query.append(" and ativa = ?").append(params.size() + 1);
            params.add(ativa);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "codigo", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<EtiquetaBalanca> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public void incrementarImpressao(String codigo) {
        EtiquetaBalanca etiqueta = findByCodigo(codigo).orElse(null);
        if (etiqueta != null) {
            etiqueta.quantidadeImpressa++;
            etiqueta.ultimaImpressao = java.time.LocalDateTime.now();
            persist(etiqueta);
        }
    }
}