package br.com.sistematenda.fiscal.nfe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class SefazResponseParser {

    private SefazResponseParser() {
    }

    public static SefazResponse parse(String xml, int httpStatus, String nfeAssinada) {
        SefazResponse response = new SefazResponse();
        response.httpStatus = httpStatus;
        response.rawXml = xml;
        if (xml == null || xml.isBlank()) {
            response.cStat = String.valueOf(httpStatus);
            response.xMotivo = "Resposta vazia da SEFAZ";
            return response;
        }

        try {
            Document document = XmlUtils.parse(xml);
            Element infProt = XmlUtils.buscarPrimeiro(document, "infProt");
            Element infEvento = XmlUtils.buscarPrimeiro(document, "infEvento");
            Element infInut = XmlUtils.buscarPrimeiro(document, "infInut");
            Element retConsStat = XmlUtils.buscarPrimeiro(document, "retConsStatServ");
            Element protNFe = XmlUtils.buscarPrimeiro(document, "protNFe");

            if (infProt != null) {
                response.cStat = XmlUtils.texto(infProt, "cStat");
                response.xMotivo = XmlUtils.texto(infProt, "xMotivo");
                response.protocolo = XmlUtils.texto(infProt, "nProt");
                response.chave = XmlUtils.texto(infProt, "chNFe");
                response.digVal = XmlUtils.texto(infProt, "digVal");
            } else if (infEvento != null) {
                response.cStat = XmlUtils.texto(infEvento, "cStat");
                response.xMotivo = XmlUtils.texto(infEvento, "xMotivo");
                response.protocolo = XmlUtils.texto(infEvento, "nProt");
                response.chave = XmlUtils.texto(infEvento, "chNFe");
            } else if (infInut != null) {
                response.cStat = XmlUtils.texto(infInut, "cStat");
                response.xMotivo = XmlUtils.texto(infInut, "xMotivo");
                response.protocolo = XmlUtils.texto(infInut, "nProt");
            } else if (retConsStat != null) {
                response.cStat = XmlUtils.texto(retConsStat, "cStat");
                response.xMotivo = XmlUtils.texto(retConsStat, "xMotivo");
            } else {
                Element raiz = document.getDocumentElement();
                response.cStat = XmlUtils.texto(raiz, "cStat");
                response.xMotivo = XmlUtils.texto(raiz, "xMotivo");
            }

            Element nRec = XmlUtils.buscarPrimeiro(document, "nRec");
            if (nRec != null) {
                response.recibo = nRec.getTextContent();
            }

            if (protNFe != null) {
                response.protNFeXml = XmlUtils.serializarElemento(protNFe, false);
                if (nfeAssinada != null && !nfeAssinada.isBlank() && response.protocolo != null) {
                    response.nfeProcXml = montarNfeProc(nfeAssinada, protNFe);
                }
            }

            response.cancelamento = contemCancelamentoHomologado(document);
            if (response.cStat == null) {
                response.cStat = String.valueOf(httpStatus);
            }
            if (response.xMotivo == null) {
                Element faultString = XmlUtils.buscarPrimeiro(document, "faultstring");
                response.xMotivo = faultString != null
                    ? faultString.getTextContent()
                    : "Resposta da SEFAZ sem motivo informado";
            }
            return response;
        } catch (Exception e) {
            response.cStat = String.valueOf(httpStatus);
            response.xMotivo = "Nao foi possivel interpretar a resposta da SEFAZ: " + e.getMessage();
            return response;
        }
    }

    private static boolean contemCancelamentoHomologado(Document document) {
        return contemTexto(document.getDocumentElement(), "tpEvento", NfeConstantes.TP_EVENTO_CANCELAMENTO)
            && contemTexto(document.getDocumentElement(), "cStat", NfeConstantes.STATUS_EVENTO_HOMOLOGADO);
    }

    private static boolean contemTexto(Element elemento, String nome, String valor) {
        if (elemento == null) {
            return false;
        }
        if (nome.equals(XmlUtils.nomeLocal(elemento)) && valor.equals(elemento.getTextContent().trim())) {
            return true;
        }
        org.w3c.dom.NodeList filhos = elemento.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            org.w3c.dom.Node filho = filhos.item(i);
            if (filho instanceof Element elementoFilho && contemTexto(elementoFilho, nome, valor)) {
                return true;
            }
        }
        return false;
    }

    private static String montarNfeProc(String nfeAssinada, Element protNFe) {
        try {
            Document nfeDocument = XmlUtils.parse(nfeAssinada);
            Document procDocument = XmlUtils.novoDocumento();
            Element proc = procDocument.createElementNS(NfeConstantes.NS_NFE, "nfeProc");
            proc.setAttribute("versao", NfeConstantes.VERSAO_NFE);
            procDocument.appendChild(proc);

            proc.appendChild(procDocument.importNode(nfeDocument.getDocumentElement(), true));
            proc.appendChild(procDocument.importNode(protNFe, true));
            return XmlUtils.serializar(procDocument, true);
        } catch (Exception e) {
            return nfeAssinada;
        }
    }
}
