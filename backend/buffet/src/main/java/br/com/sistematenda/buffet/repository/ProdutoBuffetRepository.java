package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProdutoBuffetRepository implements PanacheRepository<ProdutoBuffet> {

    public Optional<ProdutoBuffet> findByCodigo(String codigo) {
        return find("codigo = ?1 and deleted = false", codigo).firstResultOptional();
    }

    public Optional<ProdutoBuffet> findByCodigoBarras(String codigoBarras) {
        return find("codigoBarras = ?1 and deleted = false", codigoBarras).firstResultOptional();
    }

    public List<ProdutoBuffet> findByCategoria(ProdutoBuffet.Categoria categoria) {
        return find("categoria = ?1 and ativo = true and deleted = false", categoria).list();
    }

    public List<ProdutoBuffet> findByUnidadeMedida(UnidadeMedida unidadeMedida) {
        return find("unidadeMedida = ?1 and ativo = true and deleted = false", unidadeMedida).list();
    }

    public List<ProdutoBuffet> findAtivos() {
        return find("ativo = true and deleted = false").list();
    }

    public List<ProdutoBuffet> findDisponiveisBalcao() {
        return find("disponivelBalcao = true and ativo = true and deleted = false").list();
    }

    public List<ProdutoBuffet> findEstoqueBaixo() {
        return find("estoqueAtual <= estoqueMinimo and estoqueMinimo > 0 and controlaEstoque = true and ativo = true and deleted = false").list();
    }

    public List<ProdutoBuffet> search(String termo) {
        return find(
            "(lower(nome) like lower(?1) or lower(codigo) like lower(?1) or lower(codigoBarras) like lower(?1)) and deleted = false",
            "%" + termo + "%"
        ).list();
    }

    public PageResponse<ProdutoBuffet> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                     ProdutoBuffet.Categoria categoria, UnidadeMedida unidadeMedida,
                                                     Boolean ativo, Boolean disponivelBalcao) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (categoria != null) {
            query.append(" and categoria = ?").append(params.size() + 1);
            params.add(categoria);
        }
        if (unidadeMedida != null) {
            query.append(" and unidadeMedida = ?").append(params.size() + 1);
            params.add(unidadeMedida);
        }
        if (ativo != null) {
            query.append(" and ativo = ?").append(params.size() + 1);
            params.add(ativo);
        }
        if (disponivelBalcao != null) {
            query.append(" and disponivelBalcao = ?").append(params.size() + 1);
            params.add(disponivelBalcao);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "nome", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<ProdutoBuffet> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public void updateEstoque(UUID produtoId, java.math.BigDecimal quantidade) {
        ProdutoBuffet produto = findById(produtoId);
        if (produto != null && produto.controlaEstoque) {
            produto.estoqueAtual = produto.estoqueAtual.add(quantidade);
            if (produto.estoqueAtual.compareTo(java.math.BigDecimal.ZERO) < 0) {
                produto.estoqueAtual = java.math.BigDecimal.ZERO;
            }
            persist(produto);
        }
    }
}