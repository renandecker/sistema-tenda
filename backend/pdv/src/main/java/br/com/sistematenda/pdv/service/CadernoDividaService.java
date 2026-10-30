package br.com.sistematenda.pdv.service;

import br.com.sistematenda.common.entity.Cliente;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.repository.ClienteRepository;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.pdv.dto.CadernoClienteResumoDTO;
import br.com.sistematenda.pdv.dto.CadernoDividaDTO;
import br.com.sistematenda.pdv.dto.CadernoTotalDTO;
import br.com.sistematenda.pdv.dto.LancamentoCadernoRequest;
import br.com.sistematenda.pdv.dto.SituacaoClienteDTO;
import br.com.sistematenda.pdv.entity.CadernoDivida;
import br.com.sistematenda.pdv.repository.CadernoDividaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CadernoDividaService {

    @Inject
    CadernoDividaRepository repository;

    @Inject
    ClienteRepository clienteRepository;

    @Transactional
    public CadernoDividaDTO lancar(LancamentoCadernoRequest request) {
        if (request == null) {
            throw new ValidationException("Dados do lançamento são obrigatórios");
        }
        if (request.getValor() == null || request.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Informe um valor de dívida válido");
        }

        CadernoDivida divida = new CadernoDivida();
        divida.valorTotal = request.getValor();
        divida.valorPago = BigDecimal.ZERO;
        divida.descricao = request.getDescricao();
        divida.dataVencimento = request.getDataVencimento();
        divida.operador = request.getOperador();
        divida.pdvId = request.getPdvId();
        divida.observacoes = request.getObservacoes();
        divida.vendaId = request.getVendaId();
        divida.numeroVenda = request.getNumeroVenda();
        divida.clienteNome = request.getClienteNome();
        divida.clienteDocumento = request.getClienteDocumento();
        divida.clienteTelefone = request.getClienteTelefone();

        if (request.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.getClienteId());
            if (cliente == null || cliente.deleted) {
                throw new NotFoundException("Cliente", request.getClienteId());
            }
            divida.cliente = cliente;
            divida.clienteNome = cliente.nome;
            divida.clienteDocumento = cliente.cpfCnpj;
            divida.clienteTelefone = cliente.telefone;
        }

        if (divida.clienteNome == null || divida.clienteNome.isBlank()) {
            throw new ValidationException("Informe o cliente ou selecione um cliente cadastrado");
        }

        divida.dataLancamento = LocalDateTime.now();
        divida.status = CadernoDivida.StatusDivida.ABERTA;

        repository.persist(divida);
        return toDTO(divida);
    }

    @Transactional
    public CadernoDividaDTO registrarPagamento(UUID id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Informe um valor de pagamento válido");
        }

        CadernoDivida divida = buscar(id);
        if (divida.status == CadernoDivida.StatusDivida.CANCELADA) {
            throw new ValidationException("Lançamento cancelado não pode receber pagamento");
        }

        BigDecimal pago = divida.valorPago != null ? divida.valorPago : BigDecimal.ZERO;
        pago = pago.add(valor);
        if (pago.compareTo(divida.valorTotal) > 0) {
            pago = divida.valorTotal;
        }
        divida.valorPago = pago;
        divida.atualizarStatus();

        repository.persist(divida);
        return toDTO(divida);
    }

    @Transactional
    public CadernoDividaDTO cancelar(UUID id, String motivo) {
        CadernoDivida divida = buscar(id);
        divida.status = CadernoDivida.StatusDivida.CANCELADA;
        if (motivo != null && !motivo.isBlank()) {
            divida.observacoes = (divida.observacoes != null ? divida.observacoes + "\n" : "")
                + "CANCELADO: " + motivo;
        }
        repository.persist(divida);
        return toDTO(divida);
    }

    @Transactional
    public void excluir(UUID id) {
        CadernoDivida divida = buscar(id);
        divida.deleted = true;
        repository.persist(divida);
    }

    public CadernoDividaDTO buscarDTO(UUID id) {
        return toDTO(buscar(id));
    }

    private CadernoDivida buscar(UUID id) {
        CadernoDivida divida = repository.findById(id);
        if (divida == null || divida.deleted) {
            throw new NotFoundException("Dívida do caderno", id);
        }
        return divida;
    }

    public PageResponse<CadernoClienteResumoDTO> listarClientesComDivida(int page, int size) {
        List<CadernoClienteResumoDTO> resultado = new ArrayList<>();
        for (Object[] row : repository.resumoClientesComDivida()) {
            CadernoClienteResumoDTO dto = new CadernoClienteResumoDTO();
            dto.setClienteId((UUID) row[0]);
            dto.setClienteNome((String) row[1]);
            dto.setClienteDocumento((String) row[2]);
            dto.setClienteTelefone((String) row[3]);
            dto.setTotalDivida(toBigDecimal(row[4]));
            dto.setQuantidadeLancamentos(row[5] != null ? ((Number) row[5]).longValue() : 0L);
            dto.setUltimaData((LocalDateTime) row[6]);
            dto.setPossuiAtraso(row[7] != null && ((Number) row[7]).intValue() > 0);
            resultado.add(dto);
        }
        int from = Math.min(page * size, resultado.size());
        int to = Math.min(from + size, resultado.size());
        return PageResponse.of(resultado.subList(from, to), resultado.size(), page, size);
    }

    public PageResponse<CadernoDividaDTO> listarTodos(String filtro, boolean somenteEmAberto, int page, int size) {
        PageResponse<CadernoDivida> paged = repository.findTodosPaginated(filtro, somenteEmAberto, page, size);
        return PageResponse.of(
                paged.getContent().stream().map(this::toDTO).toList(),
                paged.getTotalElements(),
                paged.getCurrentPage(),
                paged.getPageSize());
    }

    public SituacaoClienteDTO situacaoCliente(UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId);
        if (cliente == null || cliente.deleted) {
            throw new NotFoundException("Cliente", clienteId);
        }
        SituacaoClienteDTO dto = montarSituacao(repository.findByClienteId(clienteId));
        dto.setClienteId(cliente.id);
        dto.setClienteNome(cliente.nome);
        dto.setClienteDocumento(cliente.cpfCnpj);
        dto.setClienteTelefone(cliente.telefone);
        return dto;
    }

    public SituacaoClienteDTO situacaoPorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new ValidationException("Informe o documento do cliente");
        }
        String limpo = documento.replaceAll("[^0-9]", "");
        return montarSituacao(repository.findByDocumento(limpo));
    }

    private SituacaoClienteDTO montarSituacao(List<CadernoDivida> todas) {
        SituacaoClienteDTO dto = new SituacaoClienteDTO();
        List<CadernoDividaDTO> abertas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean atraso = false;

        for (CadernoDivida divida : todas) {
            if (divida.isEmAberto()) {
                abertas.add(toDTO(divida));
                total = total.add(divida.getSaldoDevedor());
                atraso = atraso || divida.isVencida();
            }
        }

        dto.setTotalDivida(total);
        dto.setQuantidadeLancamentos((long) abertas.size());
        dto.setPossuiAtraso(atraso);
        dto.setDividas(abertas);

        if (dto.getClienteId() == null && !abertas.isEmpty()) {
            CadernoDivida primeira = todas.get(0);
            dto.setClienteId(primeira.cliente != null ? primeira.cliente.id : null);
            dto.setClienteNome(primeira.clienteNome);
            dto.setClienteDocumento(primeira.clienteDocumento);
            dto.setClienteTelefone(primeira.clienteTelefone);
        }
        return dto;
    }

    public CadernoTotalDTO totais() {
        CadernoTotalDTO dto = new CadernoTotalDTO();
        dto.setTotalEmAberto(repository.getTotalEmAberto());
        dto.setTotalRecebido(repository.getTotalRecebido());
        dto.setQuantidadeLancamentosEmAberto(repository.countEmAberto());
        dto.setQuantidadeClientesDevedores(repository.countClientesDevedores());
        dto.setQuantidadeLancamentosEmAtraso(repository.countEmAtraso());
        return dto;
    }

    private CadernoDividaDTO toDTO(CadernoDivida divida) {
        CadernoDividaDTO dto = new CadernoDividaDTO();
        dto.setId(divida.id);
        dto.setClienteId(divida.cliente != null ? divida.cliente.id : null);
        dto.setClienteNome(divida.clienteNome);
        dto.setClienteDocumento(divida.clienteDocumento);
        dto.setClienteTelefone(divida.clienteTelefone);
        dto.setVendaId(divida.vendaId);
        dto.setNumeroVenda(divida.numeroVenda);
        dto.setDescricao(divida.descricao);
        dto.setValorTotal(divida.valorTotal);
        dto.setValorPago(divida.valorPago);
        dto.setSaldoDevedor(divida.getSaldoDevedor());
        dto.setStatus(divida.status);
        dto.setDataLancamento(divida.dataLancamento);
        dto.setDataVencimento(divida.dataVencimento);
        dto.setDataQuitacao(divida.dataQuitacao);
        dto.setOperador(divida.operador);
        dto.setPdvId(divida.pdvId);
        dto.setObservacoes(divida.observacoes);
        dto.setVencida(divida.isVencida());
        return dto;
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(valor.toString());
    }
}
