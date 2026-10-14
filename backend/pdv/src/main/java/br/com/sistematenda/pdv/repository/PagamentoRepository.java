package br.com.sistematenda.pdv.repository;

import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.common.enums.FormaPagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PagamentoRepository implements PanacheRepositoryBase<Pagamento, UUID> {

    public List<Pagamento> findByVenda(Venda venda) {
        return find("venda = ?1 and deleted = false", venda).list();
    }

    public List<Pagamento> findByFormaPagamento(FormaPagamento forma) {
        return find("formaPagamento = ?1 and deleted = false", forma).list();
    }

    public List<Pagamento> findByNSU(String nsu) {
        return find("nsu = ?1 and deleted = false", nsu).list();
    }

    public List<Pagamento> findByStatus(Pagamento.StatusPagamento status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public java.math.BigDecimal getTotalByVenda(Venda venda) {
        return find("select sum(valor) from Pagamento where venda = ?1 and status = 'AUTORIZADO' and deleted = false", venda)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalByFormaPeriodo(FormaPagamento forma,
                                                        java.time.LocalDateTime inicio,
                                                        java.time.LocalDateTime fim) {
        return find("select sum(valor) from Pagamento where formaPagamento = ?1 and dataPagamento >= ?2 and dataPagamento <= ?3 and status = 'AUTORIZADO' and deleted = false", 
                forma, inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal getTotalTaxasPeriodo(java.time.LocalDateTime inicio,
                                                      java.time.LocalDateTime fim) {
        return find("select sum(valorTaxa) from Pagamento where dataPagamento >= ?1 and dataPagamento <= ?2 and status = 'AUTORIZADO' and deleted = false", 
                inicio, fim)
            .project(java.math.BigDecimal.class)
            .firstResultOptional()
            .orElse(java.math.BigDecimal.ZERO);
    }

    public Optional<Pagamento> findByPixTxId(String pixTxId) {
        return find("pixTxId = ?1 and deleted = false", pixTxId).firstResultOptional();
    }

    public List<Pagamento> listarPendentesPix() {
        return find("formaPagamento = 'PIX' and status = 'PENDENTE' and deleted = false and pixTxId is not null").list();
    }
}