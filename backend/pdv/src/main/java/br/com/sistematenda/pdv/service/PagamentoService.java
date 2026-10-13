package br.com.sistematenda.pdv.service;

import br.com.sistematenda.pdv.dto.PagamentoDTO;
import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.pdv.mapper.PagamentoMapper;
import br.com.sistematenda.pdv.repository.PagamentoRepository;
import br.com.sistematenda.pdv.repository.VendaRepository;
import br.com.sistematenda.common.enums.FormaPagamento;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PagamentoService extends BaseCrudServiceImpl<Pagamento, PagamentoDTO, UUID> {

    @Inject
    PagamentoRepository repository;

    @Inject
    PagamentoMapper mapper;

    @Inject
    VendaRepository vendaRepository;

    @Override
    protected PagamentoRepository getRepository() {
        return repository;
    }

    @Override
    protected PagamentoMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Pagamento> getEntityClass() {
        return Pagamento.class;
    }

    public List<PagamentoDTO> findByVenda(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null) {
            throw new NotFoundException("Venda", vendaId);
        }
        return repository.findByVenda(venda).stream().map(mapper::toDTO).toList();
    }

    public BigDecimal getTotalPago(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null) {
            throw new NotFoundException("Venda", vendaId);
        }
        return repository.getTotalByVenda(venda);
    }

    @Transactional
    public VendaDTO processarPagamento(UUID vendaId, VendaService.PagamentoRequest request) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        BigDecimal saldoDevedor = venda.valorTotal.subtract(getTotalPago(vendaId));
        if (request.valor.compareTo(saldoDevedor) > 0) {
            throw new ValidationException("Valor do pagamento excede o saldo devedor: " + saldoDevedor);
        }
        
        Pagamento entity = new Pagamento();
        entity.venda = venda;
        entity.formaPagamento = request.formaPagamento;
        entity.valor = request.valor;
        entity.parcelas = request.parcelas != null ? request.parcelas : 1;
        entity.dataPagamento = LocalDateTime.now();
        
        // Processar baseado na forma de pagamento
        switch (request.formaPagamento) {
            case DINHEIRO -> processarDinheiro(entity);
            case CARTAO_CREDITO, CARTAO_DEBITO -> processarCartao(entity, request);
            case PIX -> processarPix(entity, request);
            case VALE_ALIMENTACAO, VALE_REFEICAO, VALE_CULTURA -> processarVale(entity, request);
            default -> {
                entity.status = Pagamento.StatusPagamento.AUTORIZADO;
                entity.dataAutorizacao = LocalDateTime.now();
            }
        }
        
        entity.valorLiquido = entity.valor.subtract(entity.valorTaxa != null ? entity.valorTaxa : BigDecimal.ZERO);
        getRepository().persist(entity);
        
        // Atualizar venda
        BigDecimal totalPago = getTotalPago(vendaId);
        venda.valorPago = totalPago;
        if (totalPago.compareTo(venda.valorTotal) >= 0) {
            venda.status = br.com.sistematenda.common.enums.StatusVenda.PAGA;
            venda.dataFinalizacao = LocalDateTime.now();
        }
        vendaRepository.persist(venda);
        
        return new VendaService().mapper.toDTO(venda);
    }

    private void processarDinheiro(Pagamento entity) {
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        // Para dinheiro, não há taxa
        entity.valorTaxa = BigDecimal.ZERO;
    }

    private void processarCartao(Pagamento entity, VendaService.PagamentoRequest request) {
        // Simular integração TEF
        // Em produção, aqui faria a chamada para o TEF
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        entity.nsu = request.nsu != null ? request.nsu : gerarNSU();
        entity.codigoAutorizacao = request.codigoAutorizacao != null ? request.codigoAutorizacao : gerarCodigoAutorizacao();
        entity.bandeiraCartao = request.bandeiraCartao;
        entity.ultimosDigitosCartao = request.ultimosDigitosCartao;
        
        // Calcular taxa (exemplo: 2.5% para crédito, 1.5% para débito)
        BigDecimal taxaPercentual = entity.formaPagamento == FormaPagamento.CARTAO_CREDITO 
            ? new BigDecimal("0.025") 
            : new BigDecimal("0.015");
        entity.valorTaxa = entity.valor.multiply(taxaPercentual).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private void processarPix(Pagamento entity, VendaService.PagamentoRequest request) {
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        entity.pixTxId = request.pixTxId != null ? request.pixTxId : gerarPixTxId();
        entity.pixQrCode = request.pixQrCode;
        // Pix geralmente não tem taxa para o comerciante
        entity.valorTaxa = BigDecimal.ZERO;
    }

    private void processarVale(Pagamento entity, VendaService.PagamentoRequest request) {
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        entity.valeNumero = request.valeNumero;
        entity.valeValidade = request.valeValidade;
        // Vales podem ter taxa dependendo da operadora
        entity.valorTaxa = BigDecimal.ZERO;
    }

    @Transactional
    public void estornarPagamentos(UUID vendaId, String motivo) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        List<Pagamento> pagamentos = repository.findByVenda(venda);
        for (Pagamento pag : pagamentos) {
            if (pag.status == Pagamento.StatusPagamento.AUTORIZADO) {
                pag.status = Pagamento.StatusPagamento.ESTORNADO;
                pag.mensagemRetorno = "ESTORNO: " + motivo;
                getRepository().persist(pag);
            }
        }
    }

    private String gerarNSU() {
        return "NSU" + System.currentTimeMillis();
    }

    private String gerarCodigoAutorizacao() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private String gerarPixTxId() {
        return "PIX" + System.currentTimeMillis();
    }
}