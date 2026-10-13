package br.com.sistematenda.common.repository;

import br.com.sistematenda.common.entity.Cliente;
import br.com.sistematenda.common.dto.ClienteDTO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public Optional<Cliente> findByEmail(String email) {
        return find("email = ?1 and deleted = false", email).firstResultOptional();
    }

    public Optional<Cliente> findByCpfCnpj(String cpfCnpj) {
        return find("cpfCnpj = ?1 and deleted = false", cpfCnpj).firstResultOptional();
    }

    public List<Cliente> findByTipo(TipoPessoa tipo) {
        return find("tipoPessoa = ?1 and deleted = false", tipo).list();
    }

    public List<Cliente> findAtivos() {
        return find("status = 'ATIVO' and deleted = false").list();
    }

    public List<Cliente> findByNome(String nome) {
        return find("lower(nome) like lower(?1) and deleted = false", "%" + nome + "%").list();
    }

    public PageResponse<ClienteDTO> findPaginated(int page, int size, String sortBy, String sortDirection,
                                                  TipoPessoa tipo, String nome, Boolean ativo) {
        StringBuilder query = new StringBuilder("deleted = false");
        var params = new java.util.ArrayList<Object>();

        if (tipo != null) {
            query.append(" and tipoPessoa = ?").append(params.size() + 1);
            params.add(tipo);
        }
        if (nome != null && !nome.isBlank()) {
            query.append(" and lower(nome) like lower(?").append(params.size() + 1).append(")");
            params.add("%" + nome + "%");
        }
        if (ativo != null) {
            query.append(" and status = ?").append(params.size() + 1);
            params.add(ativo ? "ATIVO" : "INATIVO");
        }

        Sort sort = Sort.by(sortBy != null ? sortBy : "nome", 
            "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        
        Page pageable = Page.of(page, size, sort);
        
        var panacheQuery = find(query.toString(), sort, params.toArray());
        panacheQuery.page(pageable);
        
        List<Cliente> content = panacheQuery.list();
        long totalElements = count(query.toString(), params.toArray());
        
        // Converter para DTOs
        java.util.List<ClienteDTO> dtoList = content.stream()
            .map(c -> {
                ClienteDTO dto = new ClienteDTO();
                dto.id = c.id.toString();
                dto.nome = c.nome;
                dto.email = c.email;
                dto.telefone = c.telefone;
                dto.cpfCnpj = c.cpfCnpj;
                dto.tipoPessoa = c.tipoPessoa;
                dto.tipoCliente = c.tipoCliente;
                dto.status = c.status;
                dto.pontosFidelidade = c.pontosFidelidade;
                dto.limiteCredito = c.limiteCredito;
                return dto;
            })
            .toList();
        
        return new PageResponse<>(
            dtoList,
            totalElements,
            page,
            size
        );
    }

    public void toggleStatus(UUID id) {
        Cliente cliente = findById(id);
        if (cliente != null) {
            cliente.status = cliente.status.equals("ATIVO") ? "INATIVO" : "ATIVO";
            persist(cliente);
        }
    }
}