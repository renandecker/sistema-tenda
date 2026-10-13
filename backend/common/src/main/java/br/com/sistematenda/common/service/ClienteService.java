package br.com.sistematenda.common.service;

import br.com.sistematenda.common.dto.ClienteDTO;
import br.com.sistematenda.common.entity.Cliente;
import br.com.sistematenda.common.mapper.ClienteMapper;
import br.com.sistematenda.common.repository.ClienteRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ClienteService extends BaseCrudServiceImpl<Cliente, ClienteDTO, UUID> {

    @Inject
    ClienteRepository repository;

    @Inject
    ClienteMapper mapper;

    @Override
    protected ClienteRepository getRepository() {
        return repository;
    }

    @Override
    protected ClienteMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Cliente> getEntityClass() {
        return Cliente.class;
    }

    public ClienteDTO findByEmail(String email) {
        return repository.findByEmail(email)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Cliente", email));
    }

    public ClienteDTO findByCpfCnpj(String cpfCnpj) {
        return repository.findByCpfCnpj(cpfCnpj)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Cliente com CPF/CNPJ", cpfCnpj));
    }

    public List<ClienteDTO> findByTipo(TipoPessoa tipo) {
        return repository.findByTipo(tipo).stream().map(mapper::toDTO).toList();
    }

    public List<ClienteDTO> findAtivos() {
        return repository.findAtivos().stream().map(mapper::toDTO).toList();
    }

    public List<ClienteDTO> search(String nome) {
        return repository.findByNome(nome).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<ClienteDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return repository.findPaginated(page, size, sortBy, sortDirection, null, null, null);
    }

    public PageResponse<ClienteDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                          TipoPessoa tipo, String nome, Boolean ativo) {
        return repository.findPaginated(page, size, sortBy, sortDirection, tipo, nome, ativo);
    }

    @Override
    @Transactional
    public ClienteDTO create(@Valid ClienteDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Override
    @Transactional
    public ClienteDTO update(UUID id, @Valid ClienteDTO dto) {
        validateUpdate(id, dto);
        return super.update(id, dto);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        repository.toggleStatus(id);
    }

    private void validateCreate(ClienteDTO dto) {
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Já existe um cliente com este email: " + dto.getEmail());
        }
        if (repository.findByCpfCnpj(dto.getCpfCnpj()).isPresent()) {
            throw new ValidationException("Já existe um cliente com este CPF/CNPJ: " + dto.getCpfCnpj());
        }
        
        // Validações específicas por tipo de pessoa
        if (dto.isFisica()) {
            if (dto.getDataNascimento() == null) {
                throw new ValidationException("Data de nascimento é obrigatória para pessoa física");
            }
            if (dto.getRgIe() == null || dto.getRgIe().isBlank()) {
                throw new ValidationException("RG é obrigatório para pessoa física");
            }
        } else if (dto.isJuridica()) {
            if (dto.getInscricaoEstadual() == null || dto.getInscricaoEstadual().isBlank()) {
                throw new ValidationException("Inscrição estadual é obrigatória para pessoa jurídica");
            }
            if (dto.getNomeFantasia() == null || dto.getNomeFantasia().isBlank()) {
                throw new ValidationException("Nome fantasia é obrigatório para pessoa jurídica");
            }
        }
    }

    private void validateUpdate(UUID id, ClienteDTO dto) {
        repository.findByEmail(dto.getEmail()).ifPresent(cliente -> {
            if (!cliente.id.equals(id)) {
                throw new ValidationException("Já existe um cliente com este email: " + dto.getEmail());
            }
        });
        repository.findByCpfCnpj(dto.getCpfCnpj()).ifPresent(cliente -> {
            if (!cliente.id.equals(id)) {
                throw new ValidationException("Já existe um cliente com este CPF/CNPJ: " + dto.getCpfCnpj());
            }
        });
    }
}