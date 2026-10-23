package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NfeXmlSigner {

    @Inject
    CertificadoDigital certificadoDigital;

    public String assinarNfe(String xml) {
        return assinar(xml, "infNFe");
    }

    public String assinarEvento(String xml) {
        return assinar(xml, "infEvento");
    }

    public String assinarInutilizacao(String xml) {
        return assinar(xml, "infInut");
    }

    public String assinar(String xml, String elementoComId) {
        if (xml == null || xml.isBlank()) {
            throw new BusinessException("XML_VAZIO", "XML para assinatura nao informado");
        }
        try {
            System.setProperty("org.jcp.xml.dsig.secureValidation", "false");
            Document document = XmlUtils.parse(xml);
            Element raiz = document.getDocumentElement();
            Element elementoId = XmlUtils.primeiroFilho(raiz, elementoComId);
            if (elementoId == null) {
                throw new BusinessException("XML_SEM_ID",
                    "Elemento " + elementoComId + " nao encontrado no XML para assinatura");
            }
            String id = elementoId.getAttribute("Id");
            if (id == null || id.isBlank()) {
                throw new BusinessException("XML_SEM_ID",
                    "Atributo Id nao encontrado em " + elementoComId);
            }
            elementoId.setIdAttribute("Id", true);

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
            Reference reference = factory.newReference(
                "#" + id,
                factory.newDigestMethod(DigestMethod.SHA1, null),
                List.of(
                    factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                    factory.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null)
                ),
                null,
                null
            );

            SignedInfo signedInfo = factory.newSignedInfo(
                factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                List.of(reference)
            );

            X509Certificate[] cadeia = certificadoDigital.chain();
            List<Object> conteudoX509 = new ArrayList<>();
            for (X509Certificate certificado : cadeia) {
                conteudoX509.add(certificado);
            }
            KeyInfoFactory keyInfoFactory = factory.getKeyInfoFactory();
            X509Data x509Data = keyInfoFactory.newX509Data(conteudoX509);
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(List.of(x509Data));

            DOMSignContext contexto = new DOMSignContext(certificadoDigital.privateKey(), raiz);
            XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(contexto);

            return XmlUtils.serializar(document, true);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("ASSINATURA_FALHOU",
                "Nao foi possivel assinar o XML: " + e.getMessage(), e);
        }
    }
}
