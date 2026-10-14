package br.com.sistematenda.pdv.service;

import br.com.sistematenda.pdv.dto.PagamentoDTO;
import br.com.sistematenda.pdv.dto.VendaDTO;
import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.pdv.mapper.PagamentoMapper;
import br.com.sistematenda.pdv.mapper.VendaMapper;
import br.com.sistematenda.pdv.pix.PixService;
import br.com.sistematenda.pdv.repository.PagamentoRepository;
import br.com.sistematenda.pdv.repository.VendaRepository;
import br.com.sistematenda.pdv.tef.TefService;
import br.com.sistematenda.pdv.tef.TefIniciarTransacaoRequest;
import br.com.sistematenda.pdv.tef.TefTransacaoResponse;
import br.com.sistematenda.pdv.tef.TipoTransacaoTef;
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

    @Inject
    VendaMapper vendaMapper;

    @Inject
    PixService pixService;

    @Inject
    TefService tefService;

    @Override
    protected PagamentoRepository getRepository() {
        return repository;
    }

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
            case CARTAO_CREDITO, CARTAO_DEBITO -> {
                if (request.tefRequestId != null && !request.tefRequestId.isBlank()) {
                    processarCartaoTef(entity, request);
                } else {
                    processarCartao(entity, request);
                }
            }
            case PIX -> processarPix(entity, request, vendaId);
            case VALE_ALIMENTACAO, VALE_REFEICAO, VALE_CULTURA -> {
                if (request.tefRequestId != null && !request.tefRequestId.isBlank()) {
                    processarValeTef(entity, request);
                } else {
                    processarVale(entity, request);
                }
            }
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
        
        return vendaMapper.toDTO(venda);
    }

    private void processarDinheiro(Pagamento entity) {
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        entity.valorTaxa = BigDecimal.ZERO;
    }

    private void processarCartao(Pagamento entity, VendaService.PagamentoRequest request) {
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        entity.nsu = request.nsu != null ? request.nsu : gerarNSU();
        entity.codigoAutorizacao = request.codigoAutorizacao != null ? request.codigoAutorizacao : gerarCodigoAutorizacao();
        entity.bandeiraCartao = request.bandeiraCartao;
        entity.ultimosDigitosCartao = request.ultimosDigitosCartao;
        
        BigDecimal taxaPercentual = entity.formaPagamento == FormaPagamento.CARTAO_CREDITO 
            ? new BigDecimal("0.025") 
            : new BigDecimal("0.015");
        entity.valorTaxa = entity.valor.multiply(taxaPercentual).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private void processarPix(Pagamento entity, VendaService.PagamentoRequest request, UUID vendaId) {
        if (request.pixTxId != null && !request.pixTxId.isEmpty()) {
            entity.status = Pagamento.StatusPagamento.AUTORIZADO;
            entity.dataAutorizacao = LocalDateTime.now();
            entity.pixTxId = request.pixTxId;
            entity.pixQrCode = request.pixQrCode;
            entity.valorTaxa = BigDecimal.ZERO;
        } else {
            entity.status = Pagamento.StatusPagamento.PENDENTE;
            entity.pixTxId = pixService.gerarTxid(vendaId);
            entity.pixQrCode = null;
            entity.valorTaxa = BigDecimal.ZERO;
        }
    }

    private void processarVale(Pagamento entity, VendaService.PagamentoRequest request) {
        entity.status = Pagamento.StatusPagamento.AUTORIZADO;
        entity.dataAutorizacao = LocalDateTime.now();
        entity.valeNumero = request.valeNumero;
        entity.valeValidade = request.valeValidade;
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

    private void processarCartaoTef(Pagamento entity, VendaService.PagamentoRequest request) {
        try {
            TefTransacaoResponse tefResponse = tefService.confirmarTransacao(request.tefRequestId);
            
            if (!tefResponse.sucesso) {
                entity.status = Pagamento.StatusPagamento.NEGADO;
                entity.mensagemRetorno = tefResponse.mensagem;
                throw new BusinessException("TEF_NEGADO", tefResponse.mensagem != null ? tefResponse.mensagem : "Transação negada pelo TEF");
            }

            entity.status = Pagamento.StatusPagamento.AUTORIZADO;
            entity.dataAutorizacao = LocalDateTime.now();
            entity.nsu = tefResponse.nsu;
            entity.codigoAutorizacao = tefResponse.codigoAutorizacao;
            entity.bandeiraCartao = request.bandeiraCartao;
            entity.ultimosDigitosCartao = request.ultimosDigitosCartao;
            entity.comprovante = tefResponse.comprovanteEstabelecimento;
            entity.mensagemRetorno = "TEF: " + tefResponse.mensagem;

            BigDecimal taxaPercentual = entity.formaPagamento == FormaPagamento.CARTAO_CREDITO 
                ? new BigDecimal("0.025") 
                : new BigDecimal("0.015");
            entity.valorTaxa = entity.valor.multiply(taxaPercentual).setScale(4, BigDecimal.ROUND_HALF_UP);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("TEF_ERROR", "Erro ao processar confirmação TEF: " + e.getMessage());
        }
    }

    private void processarValeTef(Pagamento entity, VendaService.PagamentoRequest request) {
        try {
            TefTransacaoResponse tefResponse = tefService.confirmarTransacao(request.tefRequestId);
            
            if (!tefResponse.sucesso) {
                entity.status = Pagamento.StatusPagamento.NEGADO;
                entity.mensagemRetorno = tefResponse.mensagem;
                throw new BusinessException("TEF_NEGADO", tefResponse.mensagem != null ? tefResponse.mensagem : "Transação negada pelo TEF");
            }

            entity.status = Pagamento.StatusPagamento.AUTORIZADO;
            entity.dataAutorizacao = LocalDateTime.now();
            entity.nsu = tefResponse.nsu;
            entity.codigoAutorizacao = tefResponse.codigoAutorizacao;
            entity.bandeiraCartao = request.bandeiraCartao;
            entity.valeNumero = request.valeNumero;
            entity.valeValidade = request.valeValidade;
            entity.comprovante = tefResponse.comprovanteEstabelecimento;
            entity.mensagemRetorno = "TEF: " + tefResponse.mensagem;
            entity.valorTaxa = BigDecimal.ZERO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("TEF_ERROR", "Erro ao processar confirmação TEF: " + e.getMessage());
        }
    }

    @Transactional
    public TefTransacaoResponse iniciarTransacaoTef(UUID vendaId, VendaService.PagamentoRequest request) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }

        BigDecimal saldoDevedor = venda.valorTotal.subtract(getTotalPago(vendaId));
        if (request.valor.compareTo(saldoDevedor) > 0) {
            throw new ValidationException("Valor do pagamento excede o saldo devedor: " + saldoDevedor);
        }

        TefIniciarTransacaoRequest tefRequest = new TefIniciarTransacaoRequest();
        tefRequest.valor = request.valor;
        tefRequest.tipoTransacao = TipoTransacaoTef.fromFormaPagamento(request.formaPagamento.name());
        tefRequest.parcelas = request.parcelas != null ? request.parcelas : 1;
        tefRequest.bandeira = request.bandeiraCartao;
        tefRequest.documento = venda.numero;
        tefRequest.cpfCnpjConsumidor = request.cpfConsumidor;

        return tefService.iniciarTransacao(tefRequest);
    }

    @Transactional
    public void cancelarTransacaoTef(String tefRequestId) {
        tefService.desfazerTransacao(tefRequestId);
    }

    public String reimprimirComprovanteTef(String tefRequestId, String via) {
        return tefService.reimprimirComprovante(tefRequestId, via);
    }
}