package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.MesaDTO;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.buffet.mapper.MesaMapper;
import br.com.sistematenda.buffet.repository.MesaRepository;
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
public class MesaService extends BaseCrudServiceImpl<Mesa, MesaDTO, UUID> {

    @Inject
    MesaRepository repository;

    @Override
    protected MesaRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<Mesa> getEntityClass() {
        return Mesa.class;
    }

    @Override
    protected String getEntidadeNome() {
        return getEntityClass().getSimpleName();
    }

    public MesaDTO findByNumero(String numero) {
        return repository.findByNumero(numero)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Mesa", numero));
    }

    public List<MesaDTO> findByStatus(Mesa.StatusMesa status) {
        return repository.findByStatus(status).stream().map(mapper::toDTO).toList();
    }

    public List<MesaDTO> findDisponiveis() {
        return repository.findDisponiveis().stream().map(mapper::toDTO).toList();
    }

    public List<MesaDTO> findByArea(String area) {
        return repository.findByArea(area).stream().map(mapper::toDTO).toList();
    }

    public List<MesaDTO> findAtivas() {
        return repository.findAtivas().stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<MesaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null));
    }

    public PageResponse<MesaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                         Mesa.StatusMesa status, Boolean ativa, String area) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, status, ativa, area));
    }

    @Override
    @Transactional
    public MesaDTO create(@Valid MesaDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Transactional
    public MesaDTO atualizarStatus(UUID mesaId, Mesa.StatusMesa status) {
        repository.atualizarStatus(mesaId, status);
        return findById(mesaId);
    }

    @Transactional
    public MesaDTO ocuparMesa(UUID mesaId) {
        MesaDTO mesa = findById(mesaId);
        if (!mesa.isDisponivel()) {
            throw new ValidationException("Mesa não está disponível para ocupação");
        }
        return atualizarStatus(mesaId, Mesa.StatusMesa.OCUPADA);
    }

    @Transactional
    public MesaDTO liberarMesa(UUID mesaId) {
        return atualizarStatus(mesaId, Mesa.StatusMesa.LIVRE);
    }

    @Transactional
    public MesaDTO reservarMesa(UUID mesaId) {
        MesaDTO mesa = findById(mesaId);
        if (!mesa.isDisponivel()) {
            throw new ValidationException("Mesa não está disponível para reserva");
        }
        return atualizarStatus(mesaId, Mesa.StatusMesa.RESERVADA);
    }

    private void validateCreate(MesaDTO dto) {
        if (dto.getNumero() == null || dto.getNumero().isBlank()) {
            throw new ValidationException("Número da mesa é obrigatório");
        }
        if (repository.findByNumero(dto.getNumero()).isPresent()) {
            throw new ValidationException("Já existe uma mesa com este número: " + dto.getNumero());
        }
    }
}