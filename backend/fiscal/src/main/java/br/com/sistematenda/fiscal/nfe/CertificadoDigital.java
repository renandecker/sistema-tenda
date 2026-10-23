package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@ApplicationScoped
public class CertificadoDigital {

    private static final Logger LOG = Logger.getLogger(CertificadoDigital.class);

    @Inject
    FiscalConfig fiscalConfig;

    private volatile KeyStore keyStore;
    private volatile String alias;
    private volatile PrivateKey privateKey;
    private volatile X509Certificate[] chain;
    private volatile SSLContext sslContext;

    public boolean configurado() {
        return fiscalConfig.certificadoPath().filter(path -> !path.isBlank()).isPresent();
    }

    public String nomeTitular() {
        carregar();
        return chain != null && chain.length > 0 ? chain[0].getSubjectX500Principal().getName() : null;
    }

    public synchronized void carregar() {
        if (privateKey != null && chain != null) {
            return;
        }
        String caminhoConfigurado = fiscalConfig.certificadoPath()
            .filter(path -> !path.isBlank())
            .orElseThrow(() -> new BusinessException("CERTIFICADO_NAO_CONFIGURADO",
                "Configure o certificado digital A1 em fiscal.certificado-path"));

        Path caminho = resolverCaminho(caminhoConfigurado);
        if (!Files.exists(caminho)) {
            throw new BusinessException("CERTIFICADO_NAO_ENCONTRADO",
                "Certificado digital nao encontrado em " + caminho);
        }

        char[] senha = fiscalConfig.certificadoSenha().orElse("").toCharArray();
        try (InputStream input = Files.newInputStream(caminho)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(input, senha);

            String aliasEncontrado = null;
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String atual = aliases.nextElement();
                if (ks.isKeyEntry(atual)) {
                    aliasEncontrado = atual;
                    break;
                }
            }
            if (aliasEncontrado == null) {
                throw new BusinessException("CERTIFICADO_INVALIDO",
                    "Nenhuma chave privada encontrada no certificado digital");
            }

            PrivateKey chave = (PrivateKey) ks.getKey(aliasEncontrado, senha);
            Certificate[] certificados = ks.getCertificateChain(aliasEncontrado);
            if (chave == null || certificados == null || certificados.length == 0) {
                throw new BusinessException("CERTIFICADO_INVALIDO",
                    "Certificado digital sem chave privada ou cadeia de certificados");
            }

            X509Certificate[] cadeia = new X509Certificate[certificados.length];
            for (int i = 0; i < certificados.length; i++) {
                cadeia[i] = (X509Certificate) certificados[i];
            }

            this.keyStore = ks;
            this.alias = aliasEncontrado;
            this.privateKey = chave;
            this.chain = cadeia;
            this.sslContext = criarSslContext(ks, senha);
            LOG.infof("Certificado digital carregado: %s", cadeia[0].getSubjectX500Principal().getName());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("CERTIFICADO_INVALIDO",
                "Nao foi possivel carregar o certificado digital: " + e.getMessage(), e);
        }
    }

    public PrivateKey privateKey() {
        carregar();
        return privateKey;
    }

    public X509Certificate[] chain() {
        carregar();
        return chain != null ? chain.clone() : new X509Certificate[0];
    }

    public String alias() {
        carregar();
        return alias;
    }

    public SSLContext sslContext() {
        carregar();
        return sslContext;
    }

    private SSLContext criarSslContext(KeyStore ks, char[] senha) throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, senha);

        List<X509TrustManager> trustManagers = new ArrayList<>();
        if (fiscalConfig.sslInsecure()) {
            LOG.warn("fiscal.ssl-insecure=true: a validacao do certificado do servidor SEFAZ esta desabilitada");
            trustManagers.add(trustAll());
        } else {
            trustManagers.add(trustManagerPadrao());
            trustManagers.add(trustManagerDe(ks));
            fiscalConfig.cacertsPath()
                .filter(path -> !path.isBlank())
                .ifPresent(path -> {
                    try {
                        trustManagers.add(trustManagerDe(carregarCacerts(path)));
                    } catch (Exception e) {
                        throw new BusinessException("CACERTS_INVALIDO",
                            "Nao foi possivel carregar o arquivo de CAs: " + path, e);
                    }
                });
        }

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), new TrustManager[]{new CompositeTrustManager(trustManagers)}, null);
        return context;
    }

    private KeyStore carregarCacerts(String caminho) {
        Path path = resolverCaminho(caminho);
        char[] senha = fiscalConfig.certificadoSenha().orElse("").toCharArray();
        try (InputStream input = Files.newInputStream(path)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(input, senha);
            return ks;
        } catch (Exception pkcs12Error) {
            try (InputStream input = Files.newInputStream(path)) {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(input, senha);
                return ks;
            } catch (Exception jksError) {
                throw new BusinessException("CACERTS_INVALIDO",
                    "Nao foi possivel ler " + path + " como PKCS12 ou JKS");
            }
        }
    }

    private X509TrustManager trustManagerPadrao() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        return primeiroTrustManager(tmf);
    }

    private X509TrustManager trustManagerDe(KeyStore ks) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        return primeiroTrustManager(tmf);
    }

    private X509TrustManager primeiroTrustManager(TrustManagerFactory tmf) {
        for (TrustManager manager : tmf.getTrustManagers()) {
            if (manager instanceof X509TrustManager x509) {
                return x509;
            }
        }
        throw new BusinessException("TRUST_MANAGER_INVALIDO",
            "Nao foi possivel configurar a validacao TLS da SEFAZ");
    }

    private X509TrustManager trustAll() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private Path resolverCaminho(String caminho) {
        Path path = Path.of(caminho);
        if (path.isAbsolute() && Files.exists(path)) {
            return path;
        }
        Path atual = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 5 && atual != null; i++) {
            Path candidato = atual.resolve(caminho);
            if (Files.exists(candidato)) {
                return candidato;
            }
            atual = atual.getParent();
        }
        return path.toAbsolutePath();
    }

    private static final class CompositeTrustManager implements X509TrustManager {

        private final List<X509TrustManager> delegates;

        private CompositeTrustManager(List<X509TrustManager> delegates) {
            this.delegates = delegates;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            CertificateException ultimo = null;
            for (X509TrustManager delegate : delegates) {
                try {
                    delegate.checkClientTrusted(chain, authType);
                    return;
                } catch (CertificateException e) {
                    ultimo = e;
                }
            }
            throw ultimo != null ? ultimo : new CertificateException("Certificado do cliente nao confiavel");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            CertificateException ultimo = null;
            for (X509TrustManager delegate : delegates) {
                try {
                    delegate.checkServerTrusted(chain, authType);
                    return;
                } catch (CertificateException e) {
                    ultimo = e;
                }
            }
            throw ultimo != null ? ultimo : new CertificateException("Certificado do servidor nao confiavel");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            List<X509Certificate> certificados = new ArrayList<>();
            for (X509TrustManager delegate : delegates) {
                certificados.addAll(Arrays.asList(delegate.getAcceptedIssuers()));
            }
            return certificados.toArray(new X509Certificate[0]);
        }
    }
}
