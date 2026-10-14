package br.com.sistematenda.common.repository;

import br.com.sistematenda.common.dto.ClienteDTO;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.entity.Cliente;
import br.com.sistematenda.common.enums.TipoPessoa;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ClienteRepository implements PanacheRepositoryBase<Cliente, UUID> {

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
        List<Object> params = new ArrayList<>();

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

        Page pageable = Page.of(page, size);

        List<Cliente> content = find(query.toString(), sort, params.toArray()).page(pageable).list();
        long totalElements = count(query.toString(), params.toArray());

        List<ClienteDTO> dtoList = content.stream()
            .map(this::toDTO)
            .toList();

        return PageResponse.of(dtoList, totalElements, page, size);
    }

    private ClienteDTO toDTO(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.id);
        dto.setNome(c.nome);
        dto.setEmail(c.email);
        dto.setTelefone(c.telefone);
        dto.setCpfCnpj(c.cpfCnpj);
        dto.setTipoPessoa(c.tipoPessoa);
        dto.setTipoCliente(c.tipoCliente);
        dto.setStatus(c.status);
        dto.setPontosFidelidade(c.pontosFidelidade);
        dto.setLimiteCredito(c.limiteCredito);
        dto.setDataNascimento(c.dataNascimento);
        dto.setRgIe(c.rgIe);
        dto.setInscricaoEstadual(c.inscricaoEstadual);
        dto.setNomeFantasia(c.nomeFantasia);
        dto.setObservacoes(c.observacoes);
        dto.setContatoComercial(c.contatoComercial);
        dto.setEmailComercial(c.emailComercial);
        return dto;
    }

    public void toggleStatus(UUID id) {
        Cliente cliente = findById(id);
        if (cliente != null) {
            cliente.status = cliente.status.equals("ATIVO") ? "INATIVO" : "ATIVO";
            persist(cliente);
        }
    }
}
