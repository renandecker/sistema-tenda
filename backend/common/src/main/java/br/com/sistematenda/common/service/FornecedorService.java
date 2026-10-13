package br.com.sistematenda.common.service;

import br.com.sistematenda.common.dto.FornecedorDTO;
import br.com.sistematenda.common.entity.Fornecedor;
import br.com.sistematenda.common.mapper.FornecedorMapper;
import br.com.sistematenda.common.repository.FornecedorRepository;
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
public class FornecedorService extends BaseCrudServiceImpl<Fornecedor, FornecedorDTO, UUID> {

    @Inject
    FornecedorRepository repository;

    @Inject
    FornecedorMapper mapper;

    @Override
    protected FornecedorRepository getRepository() {
        return repository;
    }

    @Override
    protected FornecedorMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Fornecedor> getEntityClass() {
        return Fornecedor.class;
    }

    public FornecedorDTO findByCnpjCpf(String cnpjCpf) {
        return repository.findByCnpjCpf(cnpjCpf)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Fornecedor", cnpjCpf));
    }

    public FornecedorDTO findByEmail(String email) {
        return repository.findByEmail(email)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Fornecedor", email));
    }

    public List<FornecedorDTO> findAtivos() {
        return repository.findAtivos().stream().map(mapper::toDTO).toList();
    }

    public List<FornecedorDTO> findBySegmento(String segmento) {
        return repository.findBySegmento(segmento).stream().map(mapper::toDTO).toList();
    }

    public List<FornecedorDTO> search(String nome) {
        return repository.search(nome).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<FornecedorDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return repository.findPaginated(page, size, sortBy, sortDirection, null, null);
    }

    public PageResponse<FornecedorDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                 String segmento, String status) {
        return repository.findPaginated(page, size, sortBy, sortDirection, segmento, status);
    }

    @Override
    @Transactional
    public FornecedorDTO create(@Valid FornecedorDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Override
    @Transactional
    public FornecedorDTO update(UUID id, @Valid FornecedorDTO dto) {
        validateUpdate(id, dto);
        return super.update(id, dto);
    }

    private void validateCreate(FornecedorDTO dto) {
        if (repository.findByCnpjCpf(dto.getCnpjCpf()).isPresent()) {
            throw new ValidationException("Já existe um fornecedor com este CNPJ/CPF: " + dto.getCnpjCpf());
        }
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Já existe um fornecedor com este email: " + dto.getEmail());
        }
    }

    private void validateUpdate(UUID id, FornecedorDTO dto) {
        repository.findByCnpjCpf(dto.getCnpjCpf()).ifPresent(fornecedor -> {
            if (!fornecedor.id.equals(id)) {
                throw new ValidationException("Já existe um fornecedor com este CNPJ/CPF: " + dto.getCnpjCpf());
            }
        });
        repository.findByEmail(dto.getEmail()).ifPresent(fornecedor -> {
            if (!fornecedor.id.equals(id)) {
                throw new ValidationException("Já existe um fornecedor com este email: " + dto.getEmail());
            }
        });
    }
}