package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.util.ChaveAcessoUtils;
import br.com.sistematenda.fiscal.entity.Nfe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ApplicationScoped
public class NfeEmissor {

    private static final Logger LOG = Logger.getLogger(NfeEmissor.class);
    private static final DateTimeFormatter HORA_EVENTO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Inject
    FiscalConfig fiscalConfig;

    @Inject
    NfeXmlBuilder xmlBuilder;

    @Inject
    NfeXmlSigner xmlSigner;

    @Inject
    SefazClient sefazClient;

    @Inject
    CertificadoDigital certificadoDigital;

    @Transactional
    public Nfe emitir(Nfe nfe) {
        if (nfe == null) {
            throw new BusinessException("NFE_INVALIDA", "Nota fiscal nao informada para emissao");
        }
        if (!NfeConstantes.MODELO_NFE.equals(nfe.modelo) && !NfeConstantes.MODELO_NFCE.equals(nfe.modelo)) {
            throw new BusinessException("MODELO_INVALIDO",
                "Modelo de documento fiscal nao suportado para emissao: " + nfe.modelo);
        }
        if (!certificadoDigital.configurado()) {
            throw new BusinessException("CERTIFICADO_NAO_CONFIGURADO",
                "Configure o certificado digital A1 em fiscal.certificado-path");
        }

        nfe.tentativasEnvio = (nfe.tentativasEnvio != null ? nfe.tentativasEnvio : 0) + 1;
        nfe.ultimaTentativa = LocalDateTime.now();
        nfe.status = Nfe.StatusNfe.EM_PROCESSAMENTO;

        SefazEndpoints.Endpoints endpoints = SefazEndpoints.resolver(fiscalConfig, nfe.modelo);

        String xml = xmlBuilder.build(nfe);
        String xmlAssinado = xmlSigner.assinarNfe(xml);
        nfe.xmlOriginal = xmlAssinado;
        nfe.versaoXml = NfeConstantes.VERSAO_NFE;

        String loteId = String.format(Locale.ROOT, "%015d", nfe.numero);
        try {
            String requisicao = SefazRequestBuilder.autorizacao(xmlAssinado, loteId);
            SefazResponse resposta = sefazClient.enviar(
                endpoints.autorizacao(), NfeConstantes.SERVICO_AUTORIZACAO, requisicao, xmlAssinado);

            if (resposta.loteRecebido()) {
                nfe.reciboLote = resposta.recibo;
                resposta = aguardarProcessamento(endpoints, nfe, xmlAssinado);
            }

            aplicarRespostaAutorizacao(nfe, resposta);
            return nfe;
        } catch (BusinessException e) {
            nfe.status = Nfe.StatusNfe.REJEITADA;
            nfe.codigoStatus = e.getErrorCode();
            nfe.motivoRejeicao = limitar(e.getMessage(), 500);
            LOG.warnf("Falha ao emitir %s %s/%s: %s", nfe.modelo, nfe.serie, nfe.numero, e.getMessage());
            return nfe;
        }
    }

    @Transactional
    public Nfe consultar(Nfe nfe) {
        if (nfe == null || nfe.chaveAcesso == null) {
            throw new BusinessException("NFE_NAO_ENCONTRADA", "Nota fiscal sem chave de acesso para consulta");
        }
        SefazEndpoints.Endpoints endpoints = SefazEndpoints.resolver(fiscalConfig, nfe.modelo);
        String requisicao = SefazRequestBuilder.consulta(nfe.chaveAcesso, ambiente(nfe));
        SefazResponse resposta = sefazClient.enviar(
            endpoints.consulta(), NfeConstantes.SERVICO_CONSULTA, requisicao, nfe.xmlOriginal);

        nfe.codigoStatus = resposta.cStat;
        if (resposta.cancelamento) {
            nfe.status = Nfe.StatusNfe.CANCELADA;
            nfe.dataCancelamento = nfe.dataCancelamento != null ? nfe.dataCancelamento : LocalDateTime.now();
        } else if (resposta.autorizada()) {
            nfe.status = Nfe.StatusNfe.AUTORIZADA;
            nfe.protocoloAutorizacao = resposta.protocolo;
            nfe.dataAutorizacao = nfe.dataAutorizacao != null ? nfe.dataAutorizacao : LocalDateTime.now();
            nfe.xmlAutorizado = resposta.nfeProcXml != null ? resposta.nfeProcXml : nfe.xmlAutorizado;
            nfe.motivoRejeicao = null;
        } else if (resposta.denegada()) {
            nfe.status = Nfe.StatusNfe.DENEGADA;
            nfe.motivoRejeicao = limitar(resposta.descricao(), 500);
        }
        return nfe;
    }

    @Transactional
    public Nfe cancelar(Nfe nfe, String justificativa) {
        if (nfe == null || nfe.chaveAcesso == null) {
            throw new BusinessException("NFE_NAO_ENCONTRADA", "Nota fiscal nao encontrada para cancelamento");
        }
        if (nfe.status != Nfe.StatusNfe.AUTORIZADA) {
            throw new BusinessException("NFE_NAO_CANCELAVEL", "Somente nota autorizada pode ser cancelada");
        }
        if (justificativa == null || justificativa.trim().length() < 15) {
            throw new BusinessException("JUSTIFICATIVA_INVALIDA", "A justificativa deve ter no minimo 15 caracteres");
        }
        if (nfe.protocoloAutorizacao == null || nfe.protocoloAutorizacao.isBlank()) {
            throw new BusinessException("PROTOCOLO_NAO_ENCONTRADO",
                "Protocolo de autorizacao nao encontrado para cancelamento");
        }

        SefazEndpoints.Endpoints endpoints = SefazEndpoints.resolver(fiscalConfig, nfe.modelo);
        String evento = xmlSigner.assinarEvento(montarEventoCancelamento(nfe, justificativa));
        String requisicao = SefazRequestBuilder.evento(evento);
        SefazResponse resposta = sefazClient.enviar(
            endpoints.evento(), NfeConstantes.SERVICO_EVENTO, requisicao, null);

        if (!resposta.eventoHomologado()) {
            throw new BusinessException(resposta.cStat, "Cancelamento rejeitado pela SEFAZ: " + resposta.descricao());
        }

        nfe.status = Nfe.StatusNfe.CANCELADA;
        nfe.codigoStatus = resposta.cStat;
        nfe.protocoloCancelamento = resposta.protocolo;
        nfe.dataCancelamento = LocalDateTime.now();
        nfe.xmlCancelamento = evento;
        return nfe;
    }

    @Transactional
    public SefazResponse inutilizar(String justificativa, int numeroInicial, int numeroFinal,
                                    String modelo, int serie) {
        if (justificativa == null || justificativa.trim().length() < 15) {
            throw new BusinessException("JUSTIFICATIVA_INVALIDA", "A justificativa deve ter no minimo 15 caracteres");
        }
        if (numeroInicial <= 0 || numeroFinal < numeroInicial) {
            throw new BusinessException("NUMERACAO_INVALIDA",
                "A numeracao inicial deve ser positiva e menor ou igual a final");
        }
        if (!NfeConstantes.MODELO_NFE.equals(modelo) && !NfeConstantes.MODELO_NFCE.equals(modelo)) {
            throw new BusinessException("MODELO_INVALIDO", "Modelo invalido para inutilizacao: " + modelo);
        }
        if (serie < 0 || serie > 999) {
            throw new BusinessException("SERIE_INVALIDA", "Serie deve estar entre 0 e 999");
        }
        if (!certificadoDigital.configurado()) {
            throw new BusinessException("CERTIFICADO_NAO_CONFIGURADO",
                "Configure o certificado digital A1 em fiscal.certificado-path");
        }

        SefazEndpoints.Endpoints endpoints = SefazEndpoints.resolver(fiscalConfig, modelo);
        String xml = xmlSigner.assinarInutilizacao(
            montarInutilizacao(justificativa, numeroInicial, numeroFinal, modelo, serie));
        String requisicao = SefazRequestBuilder.inutilizacao(xml);
        SefazResponse resposta = sefazClient.enviar(
            endpoints.inutilizacao(), NfeConstantes.SERVICO_INUTILIZACAO, requisicao, null);

        if (!NfeConstantes.STATUS_INUTILIZACAO_HOMOLOGADA.equals(resposta.cStat)) {
            throw new BusinessException(resposta.cStat,
                "Inutilizacao rejeitada pela SEFAZ: " + resposta.descricao());
        }
        return resposta;
    }

    public SefazResponse statusServico(String modelo) {
        SefazEndpoints.Endpoints endpoints = SefazEndpoints.resolver(fiscalConfig, modelo);
        String requisicao = SefazRequestBuilder.status(fiscalConfig.ambiente(), fiscalConfig.uf());
        return sefazClient.enviar(endpoints.status(), NfeConstantes.SERVICO_STATUS, requisicao, null);
    }

    private SefazResponse aguardarProcessamento(SefazEndpoints.Endpoints endpoints, Nfe nfe, String xmlAssinado) {
        SefazResponse resposta = null;
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            dormir(2000);
            String requisicao = SefazRequestBuilder.retorno(nfe.reciboLote, ambiente(nfe));
            resposta = sefazClient.enviar(
                endpoints.retorno(), NfeConstantes.SERVICO_RETORNO, requisicao, xmlAssinado);
            if (!resposta.loteRecebido() && !resposta.emProcessamento()) {
                return resposta;
            }
        }
        return resposta;
    }

    private void aplicarRespostaAutorizacao(Nfe nfe, SefazResponse resposta) {
        nfe.codigoStatus = resposta.cStat;
        if (resposta.autorizada()) {
            nfe.status = Nfe.StatusNfe.AUTORIZADA;
            nfe.protocoloAutorizacao = resposta.protocolo;
            nfe.dataAutorizacao = LocalDateTime.now();
            nfe.xmlAutorizado = resposta.nfeProcXml != null ? resposta.nfeProcXml : nfe.xmlOriginal;
            nfe.motivoRejeicao = null;
        } else if (resposta.denegada()) {
            nfe.status = Nfe.StatusNfe.DENEGADA;
            nfe.motivoRejeicao = limitar(resposta.descricao(), 500);
        } else if (resposta.emProcessamento()) {
            nfe.status = Nfe.StatusNfe.EM_PROCESSAMENTO;
            nfe.reciboLote = resposta.recibo != null ? resposta.recibo : nfe.reciboLote;
            nfe.motivoRejeicao = limitar(resposta.descricao(), 500);
        } else {
            nfe.status = Nfe.StatusNfe.REJEITADA;
            nfe.motivoRejeicao = limitar(resposta.descricao(), 500);
        }
    }

    private String montarEventoCancelamento(Nfe nfe, String justificativa) {
        String cnpj = ChaveAcessoUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
        String id = "ID" + NfeConstantes.TP_EVENTO_CANCELAMENTO + nfe.chaveAcesso + "01";
        String dataHora = LocalDateTime.now().atZone(java.time.ZoneId.of("America/Sao_Paulo")).format(HORA_EVENTO);

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<evento xmlns=\"" + NfeConstantes.NS_NFE + "\" versao=\"" + NfeConstantes.VERSAO_EVENTO + "\">"
            + "<infEvento Id=\"" + id + "\">"
            + "<cOrgao>" + NfeConstantes.codigoUf(fiscalConfig.uf()) + "</cOrgao>"
            + "<tpAmb>" + NfeConstantes.ambienteCodigo(ambiente(nfe)) + "</tpAmb>"
            + "<CNPJ>" + cnpj + "</CNPJ>"
            + "<chNFe>" + nfe.chaveAcesso + "</chNFe>"
            + "<dhEvento>" + dataHora + "</dhEvento>"
            + "<tpEvento>" + NfeConstantes.TP_EVENTO_CANCELAMENTO + "</tpEvento>"
            + "<nSeqEvento>1</nSeqEvento>"
            + "<verEvento>" + NfeConstantes.VERSAO_EVENTO + "</verEvento>"
            + "<detEvento versao=\"" + NfeConstantes.VERSAO_EVENTO + "\">"
            + "<descEvento>Cancelamento</descEvento>"
            + "<nProt>" + nfe.protocoloAutorizacao + "</nProt>"
            + "<xJust>" + escape(justificativa.trim()) + "</xJust>"
            + "</detEvento>"
            + "</infEvento>"
            + "</evento>";
    }

    private String montarInutilizacao(String justificativa, int numeroInicial, int numeroFinal,
                                      String modelo, int serie) {
        String cnpj = ChaveAcessoUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
        String codigoUf = NfeConstantes.codigoUf(fiscalConfig.uf());
        String ano = String.format(Locale.ROOT, "%02d", Year.now().getValue() % 100);
        String id = "ID" + codigoUf + ano + cnpj + modelo
            + String.format(Locale.ROOT, "%03d", serie)
            + String.format(Locale.ROOT, "%09d", numeroInicial)
            + String.format(Locale.ROOT, "%09d", numeroFinal);

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<inutNFe xmlns=\"" + NfeConstantes.NS_NFE + "\" versao=\"" + NfeConstantes.VERSAO_INUT + "\">"
            + "<infInut Id=\"" + id + "\">"
            + "<tpAmb>" + NfeConstantes.ambienteCodigo(fiscalConfig.ambiente()) + "</tpAmb>"
            + "<xServ>INUTILIZAR</xServ>"
            + "<cUF>" + codigoUf + "</cUF>"
            + "<ano>" + ano + "</ano>"
            + "<CNPJ>" + cnpj + "</CNPJ>"
            + "<mod>" + modelo + "</mod>"
            + "<serie>" + serie + "</serie>"
            + "<nNFIni>" + numeroInicial + "</nNFIni>"
            + "<nNFFin>" + numeroFinal + "</nNFFin>"
            + "<xJust>" + escape(justificativa.trim()) + "</xJust>"
            + "</infInut>"
            + "</inutNFe>";
    }

    private String ambiente(Nfe nfe) {
        return nfe.ambiente != null && !nfe.ambiente.isBlank() ? nfe.ambiente : fiscalConfig.ambiente();
    }

    private void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("SEFAZ_COMUNICACAO", "Consulta do recibo da SEFAZ interrompida");
        }
    }

    private String limitar(String valor, int tamanho) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }

    private String escape(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
