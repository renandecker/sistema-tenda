package br.com.sistematenda.fiscal.service;

import br.com.sistematenda.fiscal.dto.CompraDTO;
import br.com.sistematenda.fiscal.entity.Compra;
import br.com.sistematenda.fiscal.mapper.CompraMapper;
import br.com.sistematenda.fiscal.repository.CompraRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import br.com.sistematenda.common.service.FornecedorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CompraService extends BaseCrudServiceImpl<Compra, CompraDTO, UUID> {

    @Inject
    CompraRepository repository;

    @Inject
    CompraMapper mapper;

    @Inject
    FornecedorService fornecedorService;

    @Override
    protected CompraRepository getRepository() {
        return repository;
    }

    protected CompraMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Compra> getEntityClass() {
        return Compra.class;
    }

    public CompraDTO findByNumero(String numero) {
        return repository.findByNumero(numero)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Compra", numero));
    }

    public List<CompraDTO> findByFornecedor(UUID fornecedorId) {
        fornecedorService.findById(fornecedorId);
        return repository.findByFornecedor(fornecedorId).stream()
            .map(mapper::toDTO).toList();
    }

    public List<CompraDTO> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.findByPeriodo(dataInicio, dataFim).stream()
            .map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<CompraDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return repository.findPaginated(page, size, sortBy, sortDirection, null, null, null, null);
    }

    public PageResponse<CompraDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                         CompraDTO.StatusCompra status, LocalDate dataInicio, LocalDate dataFim,
                                         UUID fornecedorId) {
        Compra.StatusCompra entityStatus = status != null
            ? Compra.StatusCompra.valueOf(status.name())
            : null;
        return repository.findPaginated(page, size, sortBy, sortDirection, entityStatus, dataInicio, dataFim, fornecedorId);
    }

    public BigDecimal getTotalComprasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalComprasPeriodo(dataInicio, dataFim);
    }

    public BigDecimal getTotalCeasaPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalCeasaPeriodo(dataInicio, dataFim);
    }

    @Override
    @Transactional
    public CompraDTO create(@Valid CompraDTO dto) {
        validateCreate(dto);
        return super.create(dto);
    }

    @Override
    @Transactional
    public CompraDTO update(UUID id, @Valid CompraDTO dto) {
        validateUpdate(id, dto);
        return super.update(id, dto);
    }

    private void validateCreate(CompraDTO dto) {
        if (dto.getFornecedor() == null || dto.getFornecedor().isBlank()) {
            throw new ValidationException("Fornecedor é obrigatório");
        }
        if (dto.getDataCompra() == null) {
            throw new ValidationException("Data da compra é obrigatória");
        }
        if (dto.getValorTotal() == null || dto.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valor total deve ser maior que zero");
        }
    }

    private void validateUpdate(UUID id, CompraDTO dto) {
        // Validations for update
    }
}