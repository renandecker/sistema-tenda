package br.com.sistematenda.fiscal.service;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.fiscal.dto.NfceRequest;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.repository.NfeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * NFC-e emission service.
 *
 * The SEFAZ/webservice integration is currently stubbed so the application can
 * run without a digital certificate or the external NFe library. Replace the
 * body of {@link #emitirNfce(NfceRequest)} with a real implementation when the
 * fiscal integration is enabled.
 */
@ApplicationScoped
public class NfceService {

    @Inject
    FiscalConfig fiscalConfig;

    @Inject
    NfeRepository nfeRepository;

    private final AtomicLong contador = new AtomicLong();

    public Nfe emitirNfce(NfceRequest request) {
        if (!fiscalConfig.nfce().enabled()) {
            throw new BusinessException("NFCE_DISABLED", "Emissão de NFC-e não está habilitada");
        }

        long numero = getProximoNumeroNfce();

        Nfe nfe = new Nfe();
        nfe.modelo = Nfe.ModeloDocumento.NFCE.getCodigo();
        nfe.serie = fiscalConfig.serieNfce();
        nfe.numero = numero;
        nfe.status = Nfe.StatusNfe.AUTORIZADA;
        nfe.naturezaOperacao = "VENDA";
        nfe.dataEmissao = LocalDateTime.now();
        nfe.dataSaidaEntrada = LocalDateTime.now();
        nfe.clienteNome = request.getClienteNome();
        nfe.clienteDocumento = request.getClienteDocumento();
        nfe.valorTotal = request.getValorTotal();
        nfe.valorIcms = calcularIcms(request.getValorTotal());
        nfe.ambiente = fiscalConfig.ambiente();
        nfe.vendaId = request.getVendaId();
        nfe.chaveAcesso = gerarChaveAcesso(nfe.serie, numero);
        nfe.protocoloAutorizacao = "STUB" + numero;
        nfe.xmlOriginal = buildXmlStub(nfe);
        nfe.xmlAutorizado = nfe.xmlOriginal;

        nfeRepository.persist(nfe);
        return nfe;
    }

    public void cancelarNfce(Nfe nfe, String justificativa) {
        nfe.status = Nfe.StatusNfe.CANCELADA;
        nfe.xmlCancelamento = "<cancelamento justificativa=\"" + justificativa + "\"/>";
        nfeRepository.persist(nfe);
    }

    public void inutilizarNumeracao(String justificativa, int numeroInicial, int numeroFinal) {
        // Stub: integração real com a SEFAZ desabilitada
    }

    public Nfe consultarStatus(Nfe nfe) {
        return nfe;
    }

    private long getProximoNumeroNfce() {
        int inicial = fiscalConfig.numeroNfceInicial() != null ? fiscalConfig.numeroNfceInicial() : 1;
        return inicial + contador.getAndIncrement();
    }

    private BigDecimal calcularIcms(BigDecimal valorTotal) {
        if (valorTotal == null) {
            return BigDecimal.ZERO;
        }
        return valorTotal.multiply(new BigDecimal("0.18"));
    }

    private String gerarChaveAcesso(Integer serie, long numero) {
        String base = "35" + System.currentTimeMillis() + "0000000000000000000000";
        return base.substring(0, 44);
    }

    private String buildXmlStub(Nfe nfe) {
        return "<nfe><chave>" + nfe.chaveAcesso + "</chave><numero>" + nfe.numero + "</numero></nfe>";
    }
}
