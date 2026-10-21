package br.com.sistematenda.fiscal.service;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.fiscal.dto.NfceRequest;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.repository.NfeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        nfe.naturezaOperacao = request.getNaturezaOperacao() != null ? request.getNaturezaOperacao() : "VENDA";
        nfe.dataEmissao = LocalDateTime.now();
        nfe.dataSaidaEntrada = LocalDateTime.now();
        nfe.clienteNome = request.getClienteNome();
        nfe.clienteDocumento = request.getClienteDocumento();
        nfe.valorTotal = request.getValorTotal();
        nfe.valorIcms = calcularIcms(request.getValorTotal(), request.getCstIcms());
        nfe.codigoCfop = request.getCodigoCfop();
        nfe.codigoNcm = request.getCodigoNcm();
        nfe.cstIcms = request.getCstIcms();
        nfe.cstPis = request.getCstPis();
        nfe.cstCofins = request.getCstCofins();
        nfe.meioPagamento = request.getMeioPagamento();
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

    private BigDecimal calcularIcms(BigDecimal valorTotal, String cstIcms) {
        if (valorTotal == null) {
            return BigDecimal.ZERO;
        }
        if (cstIcms == null || cstIcms.isEmpty()) {
            return valorTotal.multiply(new BigDecimal("0.18"));
        }
        switch (cstIcms) {
            case "00": case "40": case "41": case "50": case "51": case "60":
            case "61": case "70": case "77": case "90": case "010": case "020":
                return BigDecimal.ZERO;
            case "10": case "11": case "12": case "13": case "14": case "15":
            case "16": case "17": case "18": case "19": case "20": case "21":
            case "22": case "23": case "24": case "25": case "26": case "27":
            case "28": case "29": case "59": case "79": case "99":
                return valorTotal.multiply(new BigDecimal("0.18"));
            default:
                return valorTotal.multiply(new BigDecimal("0.18"));
        }
    }

    private String gerarChaveAcesso(Integer serie, long numero) {
        StringBuilder sb = new StringBuilder();
        // Formato da chave de acesso NFC-e: UF(2) + Ano(4) + Mês(2) + Modelo(2) + Série(3) + NF(9) + DV(1) + CNPJ(14) = 44
        java.time.LocalDate hoje = LocalDate.now();
        sb.append("35"); // UF placeholder (ex: 35 = RS)
        sb.append(hoje.getYear()); // Ano (4 dígitos)
        sb.append(String.format("%02d", hoje.getMonthValue())); // Mês (2 dígitos)
        sb.append("65"); // Modelo NFC-e
        sb.append(String.format("%03d", serie != null ? serie : 1)); // Série (3 dígitos)
        sb.append(String.format("%09d", numero)); // Número da NF (9 dígitos)
        // DV (1 dígito) + CNPJ (14 dígitos) = 15 dígitos restantes
        // Em stub, preencher com zeros
        while (sb.length() < 44) {
            sb.append("0");
        }
        return sb.substring(0, 44);
    }

    private String buildXmlStub(Nfe nfe) {
        return "<nfe><chave>" + nfe.chaveAcesso + "</chave><numero>" + nfe.numero + "</numero></nfe>";
    }
}
