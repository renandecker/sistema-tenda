package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.pdv.entity.CadernoDivida;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CadernoDividaRepository implements PanacheRepositoryBase<CadernoDivida, UUID> {

    private static final List<CadernoDivida.StatusDivida> STATUS_ABERTOS =
        List.of(CadernoDivida.StatusDivida.ABERTA, CadernoDivida.StatusDivida.PARCIAL);

    public List<CadernoDivida> findByClienteId(UUID clienteId) {
        return find("cliente.id = ?1 and deleted = false order by dataLancamento desc", clienteId).list();
    }

    public List<CadernoDivida> findEmAbertoByClienteId(UUID clienteId) {
        return find("cliente.id = ?1 and deleted = false and status in (?2, ?3) order by dataLancamento desc",
            clienteId, CadernoDivida.StatusDivida.ABERTA, CadernoDivida.StatusDivida.PARCIAL).list();
    }

    public List<CadernoDivida> findByDocumento(String documento) {
        return find("clienteDocumento = ?1 and deleted = false order by dataLancamento desc", documento).list();
    }

    public List<CadernoDivida> findEmAbertoByDocumento(String documento) {
        return find("clienteDocumento = ?1 and deleted = false and status in (?2, ?3) order by dataLancamento desc",
            documento, CadernoDivida.StatusDivida.ABERTA, CadernoDivida.StatusDivida.PARCIAL).list();
    }

    public List<CadernoDivida> findEmAberto() {
        return find("deleted = false and status in (?1, ?2) order by dataLancamento desc",
            CadernoDivida.StatusDivida.ABERTA, CadernoDivida.StatusDivida.PARCIAL).list();
    }

    public List<CadernoDivida> findTodos(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return find("deleted = false order by dataLancamento desc").list();
        }
        String termo = "%" + filtro.toLowerCase() + "%";
        return find("deleted = false and (lower(clienteNome) like ?1 or clienteDocumento like ?1 or numeroVenda like ?1) "
            + "order by dataLancamento desc", termo).list();
    }

    public PageResponse<CadernoDivida> findTodosPaginated(String filtro, boolean somenteEmAberto, int page, int size) {
        StringBuilder where = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();
        if (filtro != null && !filtro.isBlank()) {
            String termo = "%" + filtro.toLowerCase() + "%";
            where.append(" and (lower(clienteNome) like ?").append(params.size() + 1)
                .append(" or clienteDocumento like ?").append(params.size() + 1)
                .append(" or numeroVenda like ?").append(params.size() + 1).append(")");
            params.add(termo);
        }
        if (somenteEmAberto) {
            where.append(" and status in (?").append(params.size() + 1)
                .append(", ?").append(params.size() + 2).append(")");
            params.add(CadernoDivida.StatusDivida.ABERTA);
            params.add(CadernoDivida.StatusDivida.PARCIAL);
        }
        long total = count(where.toString(), params.toArray());
        var panacheQuery = find(where + " order by dataLancamento desc", params.toArray());
        return PageResponse.of(panacheQuery.page(page, size).list(), total, page, size);
    }

    public BigDecimal getTotalEmAbertoCliente(UUID clienteId) {
        return getEntityManager()
            .createQuery("select coalesce(sum(d.valorTotal - d.valorPago), 0) from CadernoDivida d "
                + "where d.deleted = false and d.status in :status and d.cliente.id = :clienteId", BigDecimal.class)
            .setParameter("status", STATUS_ABERTOS)
            .setParameter("clienteId", clienteId)
            .getSingleResult();
    }

    public List<Object[]> resumoClientesComDivida() {
        return getEntityManager()
            .createQuery("select d.cliente.id, d.clienteNome, max(d.clienteDocumento), max(d.clienteTelefone), "
                + "sum(d.valorTotal - d.valorPago), count(d), max(d.dataLancamento), "
                + "max(case when d.dataVencimento is not null and d.dataVencimento < current_date then 1 else 0 end) "
                + "from CadernoDivida d "
                + "where d.deleted = false and d.status in :status "
                + "group by d.cliente.id, d.clienteNome "
                + "order by d.clienteNome", Object[].class)
            .setParameter("status", STATUS_ABERTOS)
            .getResultList();
    }

    public BigDecimal getTotalEmAberto() {
        return getEntityManager()
            .createQuery("select coalesce(sum(d.valorTotal - d.valorPago), 0) from CadernoDivida d "
                + "where d.deleted = false and d.status in :status", BigDecimal.class)
            .setParameter("status", STATUS_ABERTOS)
            .getSingleResult();
    }

    public BigDecimal getTotalRecebido() {
        return getEntityManager()
            .createQuery("select coalesce(sum(d.valorPago), 0) from CadernoDivida d where d.deleted = false "
                + "and d.status <> :cancelada", BigDecimal.class)
            .setParameter("cancelada", CadernoDivida.StatusDivida.CANCELADA)
            .getSingleResult();
    }

    public long countEmAberto() {
        return count("deleted = false and status in (?1, ?2)",
            CadernoDivida.StatusDivida.ABERTA, CadernoDivida.StatusDivida.PARCIAL);
    }

    public long countEmAtraso() {
        return getEntityManager()
            .createQuery("select count(d) from CadernoDivida d where d.deleted = false and d.status in :status "
                + "and d.dataVencimento is not null and d.dataVencimento < current_date", Long.class)
            .setParameter("status", STATUS_ABERTOS)
            .getSingleResult();
    }

    public long countClientesDevedores() {
        return resumoClientesComDivida().size();
    }
}
