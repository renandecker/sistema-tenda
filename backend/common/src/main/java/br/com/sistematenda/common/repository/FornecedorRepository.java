package br.com.sistematenda.common.repository;

import br.com.sistematenda.common.entity.Fornecedor;
import br.com.sistematenda.common.dto.FornecedorDTO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FornecedorRepository implements PanacheRepository<Fornecedor> {

    public Optional<Fornecedor> findByCnpjCpf(String cnpjCpf) {
        return find("cnpj_cpf = ?1 and deleted = false", cnpjCpf).firstResultOptional();
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
        var params = new java.util.ArrayList<Object>();

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
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Fornecedor> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        return new PageResponse<>(
            content.stream().map(f -> {
                FornecedorDTO dto = new FornecedorDTO();
                dto.id = f.id.toString();
                dto.nome = f.nome;
                dto.razaoSocial = f.razaoSocial;
                dto.email = f.email;
                dto.telefone = f.telefone;
                dto.cnpjCpf = f.cnpjCpf;
                dto.tipo = f.tipo;
                dto.segmentoMercado = f.segmentoMercado;
                dto.status = f.status;
                dto.codigoNcm = f.codigoNcm;
                dto.cstIcms = f.cstIcms;
                dto.aliquotaIcms = f.aliquotaIcms;
                dto.prazoEntregaDias = f.prazoEntregaDias;
                dto.condicoesPagamento = f.condicoesPagamento;
                return dto;
            }).toList(),
            totalElements,
            page,
            size
        );
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