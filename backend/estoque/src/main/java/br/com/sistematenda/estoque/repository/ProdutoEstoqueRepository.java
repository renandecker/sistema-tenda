package br.com.sistematenda.estoque.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.estoque.entity.ProdutoEstoque;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProdutoEstoqueRepository implements PanacheRepositoryBase<ProdutoEstoque, UUID> {

    public Optional<ProdutoEstoque> findByCodigo(String codigo) {
        return find("codigo = ?1 and deleted = false", codigo).firstResultOptional();
    }

    public Optional<ProdutoEstoque> findByCodigoBarras(String codigoBarras) {
        return find("codigoBarras = ?1 and deleted = false", codigoBarras).firstResultOptional();
    }

    public List<ProdutoEstoque> findByCategoria(ProdutoEstoque.Categoria categoria) {
        return find("categoria = ?1 and ativo = true and deleted = false", categoria).list();
    }

    public List<ProdutoEstoque> findByUnidadeMedida(UnidadeMedida unidadeMedida) {
        return find("unidadeMedida = ?1 and ativo = true and deleted = false", unidadeMedida).list();
    }

    public List<ProdutoEstoque> findAtivos() {
        return find("ativo = true and deleted = false").list();
    }

    public List<ProdutoEstoque> findEstoqueBaixo() {
        return find("estoqueAtual <= estoqueMinimo and estoqueMinimo > 0 and ativo = true and deleted = false").list();
    }

    public List<ProdutoEstoque> search(String termo) {
        return find(
            "(lower(nome) like lower(?1) or lower(codigo) like lower(?1) or lower(codigoBarras) like lower(?1)) and deleted = false",
            "%" + termo + "%"
        ).list();
    }

    public PageResponse<ProdutoEstoque> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                      ProdutoEstoque.Categoria categoria, UnidadeMedida unidadeMedida,
                                                      Boolean ativo) {
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

        Sort sort = Sort.by(sortBy != null ? sortBy : "nome",
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        Page pageable = Page.of(page, size);

        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);

        List<ProdutoEstoque> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());

        return PageResponse.of(content, totalElements, page, size);
    }

    public void updateEstoque(UUID produtoId, BigDecimal quantidade) {
        ProdutoEstoque produto = findById(produtoId);
        if (produto != null) {
            produto.estoqueAtual = produto.estoqueAtual.add(quantidade);
            if (produto.estoqueAtual.compareTo(BigDecimal.ZERO) < 0) {
                produto.estoqueAtual = BigDecimal.ZERO;
            }
            persist(produto);
        }
    }
}
