package br.com.sistematenda.fiscal.repository;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.fiscal.dto.CompraDTO;
import br.com.sistematenda.fiscal.entity.Compra;
import br.com.sistematenda.fiscal.entity.Compra.StatusCompra;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CompraRepository implements PanacheRepositoryBase<Compra, UUID> {

    public List<Compra> findByFornecedor(UUID fornecedorId) {
        return find("fornecedor.id = ?1 and deleted = false", fornecedorId).list();
    }

    public List<Compra> findByStatus(StatusCompra status) {
        return find("status = ?1 and deleted = false", status).list();
    }

    public List<Compra> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("dataCompra >= ?1 and dataCompra <= ?2 and deleted = false", inicio, fim).list();
    }

    public Optional<Compra> findByNumero(String numero) {
        return find("numeroNota = ?1 and deleted = false", numero).firstResultOptional();
    }

    public PageResponse<CompraDTO> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                 StatusCompra status, LocalDate dataInicio, LocalDate dataFim,
                                                 UUID fornecedorId) {
        StringBuilder query = new StringBuilder("deleted = false");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }
        if (dataInicio != null) {
            query.append(" and dataCompra >= ?").append(params.size() + 1);
            params.add(dataInicio.atStartOfDay());
        }
        if (dataFim != null) {
            query.append(" and dataCompra <= ?").append(params.size() + 1);
            params.add(dataFim.atTime(23, 59, 59));
        }
        if (fornecedorId != null) {
            query.append(" and fornecedor.id = ?").append(params.size() + 1);
            params.add(fornecedorId);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "dataCompra",
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        Page pageable = Page.of(page, size);

        List<Compra> content = find(query.toString(), sort, params.toArray()).page(pageable).list();
        long totalElements = count(query.toString(), params.toArray());

        List<CompraDTO> dtoList = content.stream().map(this::toDTO).toList();

        return PageResponse.of(dtoList, totalElements, page, size);
    }

    private CompraDTO toDTO(Compra c) {
        CompraDTO dto = new CompraDTO();
        dto.setId(c.id);
        dto.setNumeroNota(c.numeroNota);
        dto.setSerieNota(c.serieNota);
        dto.setDataCompra(c.dataCompra);
        dto.setFornecedor(c.fornecedor != null ? c.fornecedor.nome : null);
        dto.setValorTotal(c.valorTotal);
        dto.setValorIcms(c.valorIcms);
        dto.setValorIpi(c.valorIpi);
        dto.setValorPis(c.valorPis);
        dto.setValorCofins(c.valorCofins);
        dto.setDesconto(c.desconto);
        dto.setFrete(c.frete);
        dto.setOutrasDespesas(c.outrasDespesas);
        dto.setStatus(c.status != null ? CompraDTO.StatusCompra.valueOf(c.status.name()) : null);
        dto.setCondicaoPagamento(c.condicaoPagamento);
        dto.setObservacoes(c.observacoes);
        dto.setNomeFornecedor(c.nomeFornecedor);
        dto.setCnpjFornecedor(c.cnpjFornecedor);
        return dto;
    }

    public BigDecimal getTotalComprasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("select sum(valorTotal) from Compra where dataCompra >= ?1 and dataCompra <= ?2 and status = 'RECEBIDA' and deleted = false", inicio, fim)
            .project(BigDecimal.class)
            .firstResultOptional()
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalCeasaPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return find("select sum(valorTotal) from Compra where dataCompra >= ?1 and dataCompra <= ?2 and status = 'RECEBIDA' and deleted = false", inicio, fim)
            .project(BigDecimal.class)
            .firstResultOptional()
            .orElse(BigDecimal.ZERO);
    }
}
