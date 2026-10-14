package br.com.sistematenda.fruteira.repository;

import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.TipoProduto;
import br.com.sistematenda.common.enums.UnidadeMedida;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepositoryBase<Produto, UUID> {

    public Optional<Produto> findByCodigo(String codigo) {
        return find("codigo = ?1 and deleted = false", codigo).firstResultOptional();
    }

    public Optional<Produto> findByCodigoBarras(String codigoBarras) {
        return find("codigoBarras = ?1 and deleted = false", codigoBarras).firstResultOptional();
    }

    public Optional<Produto> findByCodigoBalanca(String codigoBalanca) {
        return find("codigoBalanca = ?1 and deleted = false", codigoBalanca).firstResultOptional();
    }

    public List<Produto> findByTipo(TipoProduto tipo) {
        return find("tipo = ?1 and ativo = true and deleted = false", tipo).list();
    }

    public List<Produto> findByUnidadeMedida(UnidadeMedida unidadeMedida) {
        return find("unidadeMedida = ?1 and ativo = true and deleted = false", unidadeMedida).list();
    }

    public List<Produto> findByAtivoTrue() {
        return find("ativo = true and deleted = false").list();
    }

    public List<Produto> findByEstoqueBaixo() {
        return find("estoqueAtual <= estoqueMinimo and estoqueMinimo > 0 and ativo = true and deleted = false").list();
    }

    public List<Produto> findPesaveisBalanca() {
        return find("pesavelBalanca = true and ativo = true and deleted = false").list();
    }

    public List<Produto> search(String termo) {
        return find(
            "(lower(nome) like lower(?1) or lower(codigo) like lower(?1) or lower(codigoBarras) like lower(?1)) and deleted = false",
            "%" + termo + "%"
        ).list();
    }

    public PageResponse<Produto> findPaginated(int page, int size, String sortBy, String sortDirection,
                                               TipoProduto tipo, UnidadeMedida unidadeMedida, Boolean ativo) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipo = ?").append(params.size() + 1);
            params.add(tipo);
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
        
        List<Produto> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return PageResponse.of(content, totalElements, page, size);
    }

    public void updateEstoque(UUID produtoId, BigDecimal quantidade) {
        Produto produto = findById(produtoId);
        if (produto != null) {
            produto.estoqueAtual = produto.estoqueAtual.add(quantidade);
            if (produto.estoqueAtual.compareTo(BigDecimal.ZERO) < 0) {
                produto.estoqueAtual = BigDecimal.ZERO;
            }
            persist(produto);
        }
    }

    public void updatePrecoVenda(UUID produtoId, BigDecimal novoPreco) {
        Produto produto = findById(produtoId);
        if (produto != null) {
            produto.precoVenda = novoPreco;
            persist(produto);
        }
    }
}