package br.com.sistematenda.pdv.service;

import br.com.sistematenda.pdv.dto.PdvDTO;
import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.pdv.mapper.PdvMapper;
import br.com.sistematenda.pdv.repository.PdvRepository;
import br.com.sistematenda.common.dto.PageResponse;
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
public class PdvService extends BaseCrudServiceImpl<Pdv, PdvDTO, UUID> {

    @Inject
    PdvRepository repository;

    @Inject
    PdvMapper mapper;

    @Override
    protected PdvRepository getRepository() {
        return repository;
    }

    protected PdvMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Pdv> getEntityClass() {
        return Pdv.class;
    }

    @Override
    protected String getEntidadeNome() {
        return getEntityClass().getSimpleName();
    }

    public PdvDTO findByCodigo(String codigo) {
        return repository.findByCodigo(codigo)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("PDV", codigo));
    }

    public List<PdvDTO> findLivres() {
        return repository.findLivres().stream().map(mapper::toDTO).toList();
    }

    public List<PdvDTO> findAtivos() {
        return repository.findAtivos().stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<PdvDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null));
    }

    public PageResponse<PdvDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                        Pdv.StatusPdv status, Boolean ativo) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, status, ativo));
    }

    @Override
    @Transactional
    public PdvDTO create(@Valid PdvDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Transactional
    public PdvDTO atualizarStatus(UUID pdvId, Pdv.StatusPdv status) {
        repository.atualizarStatus(pdvId, status);
        return findById(pdvId);
    }

    private void validateCreate(PdvDTO dto) {
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            throw new ValidationException("Código do PDV é obrigatório");
        }
        if (repository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new ValidationException("Já existe um PDV com este código: " + dto.getCodigo());
        }
    }
}