package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.ClienteBuffetDTO;
import br.com.sistematenda.buffet.entity.ClienteBuffet;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.VisitaCliente;
import br.com.sistematenda.buffet.repository.ClienteBuffetRepository;
import br.com.sistematenda.buffet.repository.VisitaClienteRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ClienteBuffetService {

    @Inject
    ClienteBuffetRepository repository;

    @Inject
    VisitaClienteRepository visitaRepository;

    public List<ClienteBuffetDTO> findAll() {
        return repository.findAllAtivos().stream().map(this::toDTO).toList();
    }

    public ClienteBuffetDTO findById(UUID id) {
        ClienteBuffet c = repository.findById(id);
        if (c == null || c.deleted) throw new NotFoundException("Cliente", id);
        return toDTO(c);
    }

    @Transactional
    public ClienteBuffetDTO update(UUID id, ClienteBuffetDTO dto) {
        ClienteBuffet c = repository.findById(id);
        if (c == null || c.deleted) throw new NotFoundException("Cliente", id);
        if (dto.getNome() != null && !dto.getNome().isBlank()) c.nome = dto.getNome();
        if (dto.getTelefone() != null) c.telefone = dto.getTelefone();
        if (dto.getObservacoes() != null) c.observacoes = dto.getObservacoes();
        repository.persist(c);
        return toDTO(c);
    }

    @Transactional
    public ClienteBuffetDTO upsertByCpf(String nome, String cpf, String telefone) {
        if (cpf == null || cpf.isBlank()) {
            throw new ValidationException("CPF e obrigatorio");
        }
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.isBlank()) throw new ValidationException("CPF invalido");
        ClienteBuffet cliente = repository.findByCpf(cpfLimpo).orElse(null);
        if (cliente == null) {
            cliente = new ClienteBuffet();
            cliente.nome = (nome == null || nome.isBlank()) ? "Consumidor" : nome;
            cliente.cpf = cpfLimpo;
            cliente.telefone = telefone;
            cliente.totalVisitas = 0;
            cliente.totalConsumido = BigDecimal.ZERO;
            cliente.primeiraVisita = LocalDateTime.now();
            cliente.ultimaVisita = LocalDateTime.now();
            repository.persist(cliente);
        } else {
            if (nome != null && !nome.isBlank()) cliente.nome = nome;
            if (telefone != null && !telefone.isBlank()) cliente.telefone = telefone;
            repository.persist(cliente);
        }
        return toDTO(cliente);
    }

    @Transactional
    public void registrarVisita(UUID clienteId, UUID comandaId, LocalDateTime dataVisita) {
        ClienteBuffet cliente = repository.findById(clienteId);
        if (cliente == null || cliente.deleted) return;
        VisitaCliente visita = visitaRepository.findByComanda(comandaId);
        if (visita == null) {
            visita = new VisitaCliente();
            visita.cliente = cliente;
            visita.comanda = null;
            try {
                if (comandaId != null) {
                    Comanda c = Comanda.findById(comandaId);
                    visita.comanda = c;
                }
            } catch (Exception ignored) {}
            visita.dataVisita = dataVisita != null ? dataVisita : LocalDateTime.now();
            visita.valorConsumido = BigDecimal.ZERO;
            visitaRepository.persist(visita);
        }
        cliente.totalVisitas = (cliente.totalVisitas == null ? 0 : cliente.totalVisitas) + (visita == null ? 0 : 0);
        // avoid double counting: only increment if new visit
        // We'll handle increment in upsert path instead; here do nothing extra
        cliente.ultimaVisita = LocalDateTime.now();
        if (cliente.primeiraVisita == null) cliente.primeiraVisita = LocalDateTime.now();
        repository.persist(cliente);
    }

    // Simplified: increment visit counters when comanda created
    @Transactional
    public ClienteBuffetDTO registrarVisitaPorComanda(String nome, String cpf, String telefone, UUID comandaId, LocalDateTime dataVisita) {
        ClienteBuffetDTO dto = upsertByCpf(nome, cpf, telefone);
        ClienteBuffet cliente = repository.findById(dto.getId());
        // create visit if not exists
        VisitaCliente visita = (comandaId != null) ? visitaRepository.findByComanda(comandaId) : null;
        boolean novaVisita = (visita == null);
        if (visita == null) {
            visita = new VisitaCliente();
            visita.cliente = cliente;
            if (comandaId != null) {
                try { visita.comanda = Comanda.findById(comandaId); } catch (Exception ignored) {}
            }
            visita.dataVisita = dataVisita != null ? dataVisita : LocalDateTime.now();
            visita.valorConsumido = BigDecimal.ZERO;
            visitaRepository.persist(visita);
        }
        if (novaVisita) {
            cliente.totalVisitas = (cliente.totalVisitas == null ? 0 : cliente.totalVisitas) + 1;
            cliente.ultimaVisita = visita.dataVisita;
            if (cliente.primeiraVisita == null) cliente.primeiraVisita = visita.dataVisita;
            repository.persist(cliente);
        }
        return toDTO(cliente);
    }

    @Transactional
    public void atualizarConsumoComanda(UUID comandaId, BigDecimal valorTotal) {
        VisitaCliente visita = visitaRepository.findByComanda(comandaId);
        if (visita == null) return;
        BigDecimal anterior = visita.valorConsumido != null ? visita.valorConsumido : BigDecimal.ZERO;
        visita.valorConsumido = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        visitaRepository.persist(visita);
        ClienteBuffet cliente = visita.cliente;
        if (cliente != null) {
            BigDecimal total = cliente.totalConsumido != null ? cliente.totalConsumido : BigDecimal.ZERO;
            total = total.subtract(anterior).add(visita.valorConsumido);
            cliente.totalConsumido = total;
            cliente.ultimaVisita = LocalDateTime.now();
            repository.persist(cliente);
        }
    }

    public List<ClienteBuffetDTO> rankingFrequentes(int limit) {
        return repository.findTopFrequentes(limit).stream().map(this::toDTO).toList();
    }

    public List<ClienteBuffetDTO> rankingConsumo(int limit) {
        return repository.findTopConsumidores(limit).stream().map(this::toDTO).toList();
    }

    public long totalClientes() {
        return repository.count("deleted = false");
    }

    public BigDecimal totalConsumidoGeral() {
        return repository.findAllAtivos().stream()
            .map(c -> c.totalConsumido != null ? c.totalConsumido : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ClienteBuffetDTO toDTO(ClienteBuffet c) {
        ClienteBuffetDTO dto = new ClienteBuffetDTO();
        dto.setId(c.id);
        dto.setNome(c.nome);
        dto.setCpf(c.cpf);
        dto.setTelefone(c.telefone);
        dto.setTotalVisitas(c.totalVisitas);
        dto.setTotalConsumido(c.totalConsumido);
        dto.setPrimeiraVisita(c.primeiraVisita);
        dto.setUltimaVisita(c.ultimaVisita);
        dto.setObservacoes(c.observacoes);
        return dto;
    }
}
