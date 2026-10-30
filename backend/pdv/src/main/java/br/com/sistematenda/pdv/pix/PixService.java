package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.enums.PixStatus;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.common.pix.PixException;
import br.com.sistematenda.common.pix.PixProviderService;
import br.com.sistematenda.common.pix.PixWebhookNotification;
import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.pdv.repository.PagamentoRepository;
import br.com.sistematenda.pdv.repository.VendaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PixService {

    private static final Logger LOG = Logger.getLogger(PixService.class);

    @Inject
    PixProviderFactory providerFactory;

    @Inject
    VendaRepository vendaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Transactional
    public PixChargeResponse gerarPixParaVenda(UUID vendaId, BigDecimal valor, String txid) {
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessException("PIX_VALOR_INVALIDO", "Informe um valor positivo para gerar o PIX");
        }

        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }

        BigDecimal saldoDevedor = venda.valorTotal.subtract(getTotalPago(vendaId));
        if (saldoDevedor.signum() <= 0) {
            throw new BusinessException("PIX_VENDA_QUITADA", "Esta venda já está totalmente paga");
        }
        if (valor.compareTo(saldoDevedor) > 0) {
            throw new BusinessException("Valor do pagamento excede o saldo devedor: " + saldoDevedor);
        }

        String finalTxid = txid != null && !txid.isEmpty() ? txid : gerarTxid(vendaId);

        PixProviderService provider = resolveProvider();

        PixChargeRequest request = new PixChargeRequest();
        request.valor = valor;
        request.txid = finalTxid;
        request.expiracaoSegundos = 3600;
        request.solicitacaoPagador = "Pagamento Venda #" + venda.numero;

        if (venda.clienteDocumento != null || venda.clienteNome != null) {
            request.pagador = new PixChargeRequest.PixPagador();
            request.pagador.nome = venda.clienteNome;
            request.pagador.cpf = venda.clienteDocumento;
        }

        try {
            PixChargeResponse response = provider.createCharge(request);

            Pagamento pagamento = new Pagamento();
            pagamento.venda = venda;
            pagamento.formaPagamento = br.com.sistematenda.common.enums.FormaPagamento.PIX;
            pagamento.valor = valor;
            pagamento.status = Pagamento.StatusPagamento.PENDENTE;
            pagamento.pixTxId = response.txid;
            pagamento.pixQrCode = response.pixCopiaECola;
            pagamento.dataPagamento = LocalDateTime.now();
            pagamento.valorTaxa = BigDecimal.ZERO;
            pagamento.valorLiquido = valor;
            
            pagamentoRepository.persist(pagamento);

            LOG.info("PIX gerado para venda " + vendaId + ": " + response.txid);
            return response;

        } catch (PixException e) {
            LOG.error("Erro ao gerar PIX para venda " + vendaId, e);
            throw new BusinessException("Erro ao gerar PIX: " + e.getMessage());
        }
    }

    @Transactional
    public void processarWebhook(PixWebhookNotification notification) {
        LOG.info("Processando webhook PIX: " + notification.txid + " - Status: " + notification.status);

        Optional<Pagamento> pagamentoOpt = pagamentoRepository.findByPixTxId(notification.txid);
        if (pagamentoOpt.isEmpty()) {
            LOG.warn("Pagamento não encontrado para txid: " + notification.txid);
            return;
        }

        aplicarStatus(pagamentoOpt.get(), notification.getStatusEnum(), notification.endToEndId, "webhook");
    }

    private void aplicarStatus(Pagamento pagamento, PixStatus pixStatus, String endToEndId, String origem) {
        if (pagamento.status == Pagamento.StatusPagamento.AUTORIZADO) {
            LOG.info("Pagamento já processado: " + pagamento.pixTxId);
            return;
        }

        switch (pixStatus) {
            case CONCLUIDA -> {
                pagamento.status = Pagamento.StatusPagamento.AUTORIZADO;
                pagamento.dataAutorizacao = LocalDateTime.now();
                if (endToEndId != null) {
                    pagamento.codigoAutorizacao = endToEndId;
                }
                pagamento.mensagemRetorno = "PIX confirmado via " + origem;
                atualizarVenda(pagamento.venda.id);
            }
            case EXPIRADA, REMOVIDA_PELO_USUARIO_RECEBEDOR -> {
                pagamento.status = Pagamento.StatusPagamento.CANCELADO;
                pagamento.mensagemRetorno = "PIX " + pixStatus.getDescricao();
            }
            default -> {
                LOG.info("Status PIX não final: " + pixStatus);
                return;
            }
        }

        pagamentoRepository.persist(pagamento);
    }

    @Transactional
    public PixChargeResponse consultarStatus(String txid) {
        PixProviderService provider = resolveProvider();
        PixChargeResponse response;
        try {
            response = provider.getCharge(txid);
        } catch (PixException e) {
            LOG.error("Erro ao consultar status PIX: " + txid, e);
            throw new BusinessException("Erro ao consultar status: " + e.getMessage());
        }

        // Mantém o pagamento/venda sincronizados mesmo quando a confirmação
        // chega apenas pelo polling (sem webhook do PSP).
        if (response.getStatusEnum().isFinal()) {
            pagamentoRepository.findByPixTxId(txid)
                    .ifPresent(pagamento -> aplicarStatus(
                            pagamento, response.getStatusEnum(), null, "consulta de status"));
        }

        return response;
    }

    @Transactional
    public void cancelarPix(String txid) {
        PixProviderService provider = resolveProvider();
        try {
            provider.cancelCharge(txid);
            
            Optional<Pagamento> pagamentoOpt = pagamentoRepository.findByPixTxId(txid);
            pagamentoOpt.ifPresent(pagamento -> {
                pagamento.status = Pagamento.StatusPagamento.CANCELADO;
                pagamento.mensagemRetorno = "PIX cancelado pelo usuário";
                pagamentoRepository.persist(pagamento);
                
                atualizarVenda(pagamento.venda.id);
            });
            
        } catch (PixException e) {
            LOG.error("Erro ao cancelar PIX: " + txid, e);
            throw new BusinessException("Erro ao cancelar PIX: " + e.getMessage());
        }
    }

    private PixProviderService resolveProvider() {
        try {
            return providerFactory.getProviderOrDefault();
        } catch (IllegalStateException e) {
            LOG.warn("PIX indisponível: " + e.getMessage());
            throw new BusinessException("PIX_NAO_CONFIGURADO",
                    "PIX não configurado: " + e.getMessage());
        }
    }

    public String gerarTxid(UUID vendaId) {
        return "TEND" + vendaId.toString().replace("-", "").substring(0, 26).toUpperCase();
    }

    private BigDecimal getTotalPago(UUID vendaId) {
        return pagamentoRepository.getTotalByVenda(vendaRepository.findById(vendaId));
    }

    private void atualizarVenda(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda != null) {
            BigDecimal totalPago = getTotalPago(vendaId);
            venda.valorPago = totalPago;
            if (totalPago.compareTo(venda.valorTotal) >= 0) {
                venda.status = br.com.sistematenda.common.enums.StatusVenda.PAGA;
                venda.dataFinalizacao = LocalDateTime.now();
            }
            vendaRepository.persist(venda);
        }
    }

    public List<Pagamento> listarPendentes() {
        return pagamentoRepository.listarPendentesPix();
    }

    @Transactional
    public void verificarPendentes() {
        List<Pagamento> pendentes = listarPendentes();
        for (Pagamento pag : pendentes) {
            if (pag.pixTxId != null) {
                try {
                    PixChargeResponse status = consultarStatus(pag.pixTxId);
                    if (status.getStatusEnum() == PixStatus.CONCLUIDA && pag.status != Pagamento.StatusPagamento.AUTORIZADO) {
                        PagamentoWebhookNotification notification = new PagamentoWebhookNotification();
                        notification.txid = pag.pixTxId;
                        notification.endToEndId = "MANUAL";
                        notification.status = "CONCLUIDA";
                        notification.valor = pag.valor;
                        processarWebhook(notification);
                    }
                } catch (Exception e) {
                    LOG.warn("Erro ao verificar PIX pendente " + pag.pixTxId + ": " + e.getMessage());
                }
            }
        }
    }

    private static class PagamentoWebhookNotification extends PixWebhookNotification {}
}