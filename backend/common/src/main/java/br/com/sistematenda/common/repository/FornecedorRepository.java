package br.com.sistematenda.common.repository;

import br.com.sistematenda.common.dto.FornecedorDTO;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.entity.Fornecedor;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FornecedorRepository implements PanacheRepositoryBase<Fornecedor, UUID> {

    public Optional<Fornecedor> findByCnpjCpf(String cnpjCpf) {
        return find("cnpjCpf = ?1 and deleted = false", cnpjCpf).firstResultOptional();
    }

    public Optional<Fornecedor> findByEmail(String email) {
        return find("email = ?1 and deleted = false", email).firstResultOptional();
    }

    public List<Fornecedor> findAtivos() {
        return find("status = 'ATIVO' and deleted = false").list();
    }

    public List<Fornecedor> findBySegmento(String segmento) {
        return find("segmentoMercado = ?1 and status = 'ATIVO' and deleted = false", segmento).list();
    }

    public List<Fornecedor> search(String nome) {
        return find("lower(nome) like lower(?1) and deleted = false", "%" + nome + "%").list();
    }

    public PageResponse<FornecedorDTO> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                     String segmento, String status) {
        StringBuilder query = new StringBuilder("deleted = false");
        List<Object> params = new ArrayList<>();

        if (segmento != null && !segmento.isBlank()) {
            query.append(" and segmentoMercado = ?").append(params.size() + 1);
            params.add(segmento);
        }
        if (status != null && !status.isBlank()) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(status);
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "nome",
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);

        Page pageable = Page.of(page, size);

        List<Fornecedor> content = find(query.toString(), sort, params.toArray()).page(pageable).list();
        long totalElements = count(query.toString(), params.toArray());

        List<FornecedorDTO> dtoList = content.stream().map(this::toDTO).toList();

        return PageResponse.of(dtoList, totalElements, page, size);
    }

    private FornecedorDTO toDTO(Fornecedor f) {
        FornecedorDTO dto = new FornecedorDTO();
        dto.setId(f.id);
        dto.setNome(f.nome);
        dto.setRazaoSocial(f.razaoSocial);
        dto.setEmail(f.email);
        dto.setTelefone(f.telefone);
        dto.setCnpjCpf(f.cnpjCpf);
        dto.setTipo(f.tipo);
        dto.setSegmentoMercado(f.segmentoMercado);
        dto.setStatus(f.status);
        dto.setCodigoNcm(f.codigoNcm);
        dto.setCstIcms(f.cstIcms != null ? f.cstIcms.getCodigo() : null);
        dto.setAliquotaIcms(f.aliquotaIcms);
        dto.setPrazoEntregaDias(f.prazoEntregaDias);
        dto.setCondicoesPagamento(f.condicoesPagamento);
        dto.setSituacaoTributaria(f.situacaoTributaria);
        dto.setAliquotaPis(f.aliquotaPis);
        dto.setAliquotaCofins(f.aliquotaCofins);
        dto.setPercentualDesconto(f.percentualDesconto);
        dto.setSituacaoEntrega(f.statusEntrega);
        dto.setPontosParceria(f.pontosParceria);
        dto.setTotalCompras(f.totalCompras);
        dto.setUltimaCompra(f.ultimaCompra);
        return dto;
    }

    public void atualizarTotalCompras(UUID id, BigDecimal valor) {
        Fornecedor fornecedor = findById(id);
        if (fornecedor != null) {
            fornecedor.totalCompras = fornecedor.totalCompras.add(valor);
            fornecedor.ultimaCompra = java.time.LocalDate.now();
            persist(fornecedor);
        }
    }
}
