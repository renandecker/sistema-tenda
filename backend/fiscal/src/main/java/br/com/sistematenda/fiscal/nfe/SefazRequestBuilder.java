package br.com.sistematenda.fiscal.nfe;

import java.util.Map;

public final class SefazRequestBuilder {

    private static final Map<String, String> NAMESPACES = Map.of(
        NfeConstantes.SERVICO_AUTORIZACAO, "http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4",
        NfeConstantes.SERVICO_RETORNO, "http://www.portalfiscal.inf.br/nfe/wsdl/NFeRetAutorizacao4",
        NfeConstantes.SERVICO_CONSULTA, "http://www.portalfiscal.inf.br/nfe/wsdl/NFeConsultaProtocolo4",
        NfeConstantes.SERVICO_EVENTO, "http://www.portalfiscal.inf.br/nfe/wsdl/NFeRecepcaoEvento4",
        NfeConstantes.SERVICO_INUTILIZACAO, "http://www.portalfiscal.inf.br/nfe/wsdl/NFeInutilizacao4",
        NfeConstantes.SERVICO_STATUS, "http://www.portalfiscal.inf.br/nfe/wsdl/NFeStatusServico4"
    );

    private static final Map<String, String> ACOES = Map.of(
        NfeConstantes.SERVICO_AUTORIZACAO, "nfeAutorizacaoLote",
        NfeConstantes.SERVICO_RETORNO, "nfeRetAutorizacaoLote",
        NfeConstantes.SERVICO_CONSULTA, "nfeConsultaNF",
        NfeConstantes.SERVICO_EVENTO, "nfeRecepcaoEvento",
        NfeConstantes.SERVICO_INUTILIZACAO, "nfeInutilizacaoNF",
        NfeConstantes.SERVICO_STATUS, "nfeStatusServicoNF"
    );

    private SefazRequestBuilder() {
    }

    public static String autorizacao(String nfeAssinada, String loteId) {
        String dados = "<enviNFe xmlns=\"" + NfeConstantes.NS_NFE + "\" versao=\"" + NfeConstantes.VERSAO_NFE + "\">"
            + "<idLote>" + escape(loteId) + "</idLote>"
            + "<indSinc>1</indSinc>"
            + XmlUtils.removerDeclaracao(nfeAssinada)
            + "</enviNFe>";
        return envelope(NfeConstantes.SERVICO_AUTORIZACAO, dados);
    }

    public static String retorno(String recibo, String ambiente) {
        String dados = "<consReciNFe xmlns=\"" + NfeConstantes.NS_NFE + "\" versao=\"" + NfeConstantes.VERSAO_NFE + "\">"
            + "<tpAmb>" + NfeConstantes.ambienteCodigo(ambiente) + "</tpAmb>"
            + "<nRec>" + escape(recibo) + "</nRec>"
            + "</consReciNFe>";
        return envelope(NfeConstantes.SERVICO_RETORNO, dados);
    }

    public static String consulta(String chave, String ambiente) {
        String dados = "<consSitNFe xmlns=\"" + NfeConstantes.NS_NFE + "\" versao=\"" + NfeConstantes.VERSAO_NFE + "\">"
            + "<tpAmb>" + NfeConstantes.ambienteCodigo(ambiente) + "</tpAmb>"
            + "<xServ>CONSULTAR</xServ>"
            + "<chNFe>" + escape(chave) + "</chNFe>"
            + "</consSitNFe>";
        return envelope(NfeConstantes.SERVICO_CONSULTA, dados);
    }

    public static String evento(String eventoAssinado) {
        return envelope(NfeConstantes.SERVICO_EVENTO, XmlUtils.removerDeclaracao(eventoAssinado));
    }

    public static String inutilizacao(String inutilizacaoAssinada) {
        return envelope(NfeConstantes.SERVICO_INUTILIZACAO, XmlUtils.removerDeclaracao(inutilizacaoAssinada));
    }

    public static String status(String ambiente, String uf) {
        String dados = "<consStatServ xmlns=\"" + NfeConstantes.NS_NFE + "\" versao=\"" + NfeConstantes.VERSAO_NFE + "\">"
            + "<tpAmb>" + NfeConstantes.ambienteCodigo(ambiente) + "</tpAmb>"
            + "<cUF>" + NfeConstantes.codigoUf(uf) + "</cUF>"
            + "<xServ>STATUS</xServ>"
            + "</consStatServ>";
        return envelope(NfeConstantes.SERVICO_STATUS, dados);
    }

    public static String namespace(String servico) {
        String namespace = NAMESPACES.get(servico);
        if (namespace == null) {
            throw new IllegalArgumentException("Servico SEFAZ desconhecido: " + servico);
        }
        return namespace;
    }

    public static String soapAction(String servico) {
        String namespace = namespace(servico);
        String acao = ACOES.get(servico);
        return namespace + "/" + acao;
    }

    private static String envelope(String servico, String dados) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<soap:Envelope xmlns:soap=\"" + NfeConstantes.NS_SOAP + "\">"
            + "<soap:Header/>"
            + "<soap:Body>"
            + "<nfeDadosMsg xmlns=\"" + namespace(servico) + "\">"
            + dados
            + "</nfeDadosMsg>"
            + "</soap:Body>"
            + "</soap:Envelope>";
    }

    private static String escape(String valor) {
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
