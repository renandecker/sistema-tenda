package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.ComandaDTO;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.buffet.mapper.ComandaMapper;
import br.com.sistematenda.buffet.repository.ComandaRepository;
import br.com.sistematenda.buffet.repository.MesaRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.StatusComanda;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ComandaService extends BaseCrudServiceImpl<Comanda, ComandaDTO, UUID> {

    @Inject
    ComandaRepository repository;

    @Inject
    ComandaMapper mapper;

    @Inject
    MesaRepository mesaRepository;

    @Inject
    ItemComandaService itemComandaService;

    @Override
    protected ComandaRepository getRepository() {
        return repository;
    }

    @Override
    protected ComandaMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Comanda> getEntityClass() {
        return Comanda.class;
    }

    public ComandaDTO findByNumero(String numero) {
        return repository.findByNumero(numero)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Comanda", numero));
    }

    public List<ComandaDTO> findAbertas() {
        return repository.findAbertas().stream().map(mapper::toDTO).toList();
    }

    public List<ComandaDTO> findByStatus(StatusComanda status) {
        return repository.findByStatus(status).stream().map(mapper::toDTO).toList();
    }

    public List<ComandaDTO> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.findByPeriodo(dataInicio, dataFim).stream().map(mapper::toDTO).toList();
    }

    public List<ComandaDTO> findByCliente(String nomeCliente) {
        return repository.findByCliente(nomeCliente).stream().map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<ComandaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return repository.findPaginated(page, size, sortBy, sortDirection, null, null, null, null);
    }

    public PageResponse<ComandaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                            StatusComanda status, LocalDate dataInicio, LocalDate dataFim,
                                            UUID mesaId) {
        return repository.findPaginated(page, size, sortBy, sortDirection, status, dataInicio, dataFim, mesaId);
    }

    @Override
    @Transactional
    public ComandaDTO create(@Valid ComandaDTO dto) {
        validateCreate(dto);
        
        Comanda entity = mapper.toEntity(dto);
        entity.numero = generateNumero();
        entity.dataAbertura = LocalDateTime.now();
        entity.status = StatusComanda.ABERTA;
        entity.subtotal = BigDecimal.ZERO;
        entity.desconto = BigDecimal.ZERO;
        entity.acrescimo = BigDecimal.ZERO;
        entity.valorTotal = BigDecimal.ZERO;
        entity.valorPago = BigDecimal.ZERO;
        entity.abertaPor = "SISTEMA"; // TODO: usuário logado
        
        if (dto.getMesa() != null && dto.getMesa().getId() != null) {
            Mesa mesa = mesaRepository.findById(dto.getMesa().getId());
            if (mesa == null) {
                throw new NotFoundException("Mesa", dto.getMesa().getId());
            }
            if (!mesa.isDisponivel()) {
                throw new BusinessException("MESA_INDISPONIVEL", "Mesa não está disponível");
            }
            entity.mesa = mesa;
            mesa.status = Mesa.StatusMesa.OCUPADA;
            mesaRepository.getRepository().persist(mesa);
        }
        
        // Definir preços baseado na modalidade
        if (entity.modalidade == null) {
            entity.modalidade = Comanda.ModalidadeBuffet.POR_PESO;
        }
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public ComandaDTO fecharComanda(UUID comandaId) {
        ComandaDTO comanda = findById(comandaId);
        
        if (comanda.isFechada()) {
            throw new BusinessException("COMANDA_JA_FECHADA", "Comanda já está fechada");
        }
        
        // Calcular totais
        BigDecimal subtotal = itemComandaService.getTotalByComanda(comandaId);
        comanda.setSubtotal(subtotal);
        comanda.setValorTotal(subtotal
            .subtract(comanda.getDesconto() != null ? comanda.getDesconto() : BigDecimal.ZERO)
            .add(comanda.getAcrescimo() != null ? comanda.getAcrescimo() : BigDecimal.ZERO));
        
        Comanda entity = getRepository().findById(comandaId);
        entity.subtotal = comanda.getSubtotal();
        entity.valorTotal = comanda.getValorTotal();
        entity.status = StatusComanda.FECHADA;
        entity.dataFechamento = LocalDateTime.now();
        entity.fechadaPor = "SISTEMA"; // TODO: usuário logado
        getRepository().persist(entity);
        
        // Liberar mesa se houver
        if (entity.mesa != null) {
            entity.mesa.status = Mesa.StatusMesa.LIVRE;
            mesaRepository.getRepository().persist(entity.mesa);
        }
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ComandaDTO reabrirComanda(UUID comandaId) {
        Comanda entity = getRepository().findById(comandaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Comanda", comandaId);
        }
        
        if (!entity.isFechada()) {
            throw new BusinessException("COMANDA_NAO_FECHADA", "Comanda não está fechada");
        }
        
        entity.status = StatusComanda.ABERTA;
        entity.dataFechamento = null;
        entity.fechadaPor = null;
        
        if (entity.mesa != null) {
            entity.mesa.status = Mesa.StatusMesa.OCUPADA;
            mesaRepository.getRepository().persist(entity.mesa);
        }
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public ComandaDTO cancelarComanda(UUID comandaId, String motivo) {
        Comanda entity = getRepository().findById(comandaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Comanda", comandaId);
        }
        
        if (entity.isPaga()) {
            throw new BusinessException("COMANDA_PAGA", "Não é possível cancelar uma comanda já paga");
        }
        
        entity.status = StatusComanda.CANCELADA;
        entity.motivoCancelamento = motivo;
        entity.canceladaPor = "SISTEMA"; // TODO: usuário logado
        entity.dataFechamento = LocalDateTime.now();
        
        // Cancelar itens
        itemComandaService.cancelarItensComanda(comandaId, motivo);
        
        // Liberar mesa se houver
        if (entity.mesa != null) {
            entity.mesa.status = Mesa.StatusMesa.LIVRE;
            mesaRepository.getRepository().persist(entity.mesa);
        }
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public ComandaDTO aplicarDesconto(UUID comandaId, BigDecimal desconto) {
        ComandaDTO comanda = findById(comandaId);
        
        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Desconto não pode ser negativo");
        }
        
        if (desconto.compareTo(comanda.getSubtotal()) > 0) {
            throw new ValidationException("Desconto não pode ser maior que o subtotal");
        }
        
        Comanda entity = getRepository().findById(comandaId);
        entity.desconto = desconto;
        entity.valorTotal = entity.subtotal
            .subtract(desconto)
            .add(entity.acrescimo != null ? entity.acrescimo : BigDecimal.ZERO);
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ComandaDTO aplicarAcrescimo(UUID comandaId, BigDecimal acrescimo) {
        ComandaDTO comanda = findById(comandaId);
        
        if (acrescimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Acréscimo não pode ser negativo");
        }
        
        Comanda entity = getRepository().findById(comandaId);
        entity.acrescimo = acrescimo;
        entity.valorTotal = entity.subtotal
            .subtract(entity.desconto != null ? entity.desconto : BigDecimal.ZERO)
            .add(acrescimo);
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    public BigDecimal getTotalVendasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalVendasPeriodo(dataInicio, dataFim);
    }

    public long countAbertas() {
        return repository.countAbertas();
    }

    private String generateNumero() {
        String prefix = "COM";
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        long count = repository.count("dataAbertura >= ?1 and deleted = false", java.time.LocalDate.now().atStartOfDay()) + 1;
        return String.format("%s%s%04d", prefix, datePart, count);
    }

    private void validateCreate(ComandaDTO dto) {
        if (dto.getModalidade() == null) {
            throw new ValidationException("Modalidade da comanda é obrigatória");
        }
    }
}