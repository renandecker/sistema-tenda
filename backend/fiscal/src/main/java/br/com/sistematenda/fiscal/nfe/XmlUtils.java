package br.com.sistematenda.fiscal.nfe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class XmlUtils {

    private static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private XmlUtils() {
    }

    public static Document novoDocumento() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel criar o documento XML", e);
        }
    }

    public static Document parse(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("XML vazio");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalArgumentException("XML invalido", e);
        }
    }

    public static Element raiz(Document document) {
        return document.getDocumentElement();
    }

    public static Element el(Document document, Node parent, String nome) {
        Element elemento = document.createElementNS(NfeConstantes.NS_NFE, nome);
        parent.appendChild(elemento);
        return elemento;
    }

    public static Element el(Element parent, String nome) {
        return el(parent.getOwnerDocument(), parent, nome);
    }

    public static Element elComTexto(Document document, Node parent, String nome, String valor) {
        Element elemento = el(document, parent, nome);
        if (valor != null) {
            elemento.setTextContent(valor);
        }
        return elemento;
    }

    public static Element elComTexto(Element parent, String nome, String valor) {
        return elComTexto(parent.getOwnerDocument(), parent, nome, valor);
    }

    public static Element atributo(Element elemento, String nome, String valor) {
        if (valor != null) {
            elemento.setAttribute(nome, valor);
        }
        return elemento;
    }

    public static String texto(Element parent, String nome) {
        Element filho = primeiroFilho(parent, nome);
        return filho != null ? filho.getTextContent() : null;
    }

    public static Element primeiroFilho(Node parent, String nome) {
        if (parent == null) {
            return null;
        }
        NodeList filhos = parent.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node filho = filhos.item(i);
            if (filho instanceof Element elemento && nome.equals(nomeLocal(elemento))) {
                return elemento;
            }
        }
        return null;
    }

    public static List<Element> filhos(Node parent, String nome) {
        List<Element> resultado = new ArrayList<>();
        if (parent == null) {
            return resultado;
        }
        NodeList filhos = parent.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node filho = filhos.item(i);
            if (filho instanceof Element elemento && nome.equals(nomeLocal(elemento))) {
                resultado.add(elemento);
            }
        }
        return resultado;
    }

    public static Element buscarPrimeiro(Node parent, String nome) {
        if (parent == null) {
            return null;
        }
        if (parent instanceof Element elemento && nome.equals(nomeLocal(elemento))) {
            return elemento;
        }
        NodeList filhos = parent.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Element encontrado = buscarPrimeiro(filhos.item(i), nome);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
    }

    public static String nomeLocal(Node node) {
        String nome = node.getLocalName();
        return nome != null ? nome : node.getNodeName();
    }

    public static String serializar(Document document, boolean declaracao) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, declaracao ? "no" : "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel serializar o XML", e);
        }
    }

    public static String serializarElemento(Element elemento, boolean declaracao) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, declaracao ? "no" : "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(elemento), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel serializar o elemento XML", e);
        }
    }

    public static String removerDeclaracao(String xml) {
        if (xml == null) {
            return null;
        }
        String resultado = xml.trim();
        if (resultado.startsWith("<?xml")) {
            int fim = resultado.indexOf("?>");
            if (fim >= 0) {
                resultado = resultado.substring(fim + 2).trim();
            }
        }
        return resultado;
    }

    public static String decimal(BigDecimal valor) {
        return decimal(valor, 2);
    }

    public static String decimal(BigDecimal valor, int casas) {
        BigDecimal numero = valor != null ? valor : BigDecimal.ZERO;
        return numero.setScale(casas, RoundingMode.HALF_UP).toPlainString();
    }

    public static String dataHora(LocalDateTime dataHora) {
        LocalDateTime valor = dataHora != null ? dataHora : LocalDateTime.now();
        return valor.atZone(ZONE_SAO_PAULO).format(DATA_HORA);
    }

    public static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
