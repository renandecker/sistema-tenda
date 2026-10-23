package br.com.sistematenda.common.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.math.BigDecimal;
import java.util.Optional;

@ConfigMapping(prefix = "fiscal")
public interface FiscalConfig {

    @WithDefault("HOMOLOGACAO")
    String ambiente();

    @WithName("certificado-path")
    Optional<String> certificadoPath();

    @WithName("certificado-senha")
    Optional<String> certificadoSenha();

    Optional<String> csc();

    @WithName("csc-id")
    Optional<String> cscId();

    @WithDefault("RS")
    String uf();

    @WithName("razao-social")
    Optional<String> razaoSocial();

    @WithName("nome-fantasia")
    Optional<String> nomeFantasia();

    @WithName("endereco")
    Optional<String> endereco();

    @WithName("logradouro")
    Optional<String> logradouro();

    @WithName("numero")
    Optional<String> numero();

    @WithName("bairro")
    Optional<String> bairro();

    @WithName("complemento")
    Optional<String> complemento();

    @WithName("cep")
    Optional<String> cep();

    @WithName("fone")
    Optional<String> fone();

    @WithName("email")
    Optional<String> email();

    @WithName("municipio-codigo")
    Optional<Integer> municipioCodigo();

    @WithName("municipio-nome")
    Optional<String> municipioNome();

    Optional<String> cnpj();

    @WithName("inscricao-estadual")
    Optional<String> inscricaoEstadual();

    @WithName("inscricao-municipal")
    Optional<String> inscricaoMunicipal();

    @WithDefault("1")
    Integer crt();

    @WithName("serie-nfce")
    @WithDefault("1")
    Integer serieNfce();

    @WithName("numero-nfce-inicial")
    @WithDefault("1")
    Integer numeroNfceInicial();

    @WithName("serie-nfe")
    @WithDefault("1")
    Integer serieNfe();

    @WithName("numero-nfe-inicial")
    @WithDefault("1")
    Integer numeroNfeInicial();

    @WithName("modelo-danfe")
    @WithDefault("1")
    String modeloDanfe();

    @WithName("logo-path")
    Optional<String> logoPath();

    @WithName("webservice-timeout")
    @WithDefault("30000")
    int webserviceTimeout();

    @WithName("cfop-padrao")
    @WithDefault("5102")
    String cfopPadrao();

    @WithName("cfop-interestadual")
    @WithDefault("6102")
    String cfopInterestadual();

    @WithName("aliquota-icms-padrao")
    @WithDefault("18.00")
    BigDecimal aliquotaIcmsPadrao();

    @WithName("aliquota-pis-padrao")
    @WithDefault("1.65")
    BigDecimal aliquotaPisPadrao();

    @WithName("aliquota-cofins-padrao")
    @WithDefault("7.60")
    BigDecimal aliquotaCofinsPadrao();

    @WithName("schemas-path")
    @WithDefault("schemas/")
    String schemasPath();

    @WithName("cacerts-path")
    Optional<String> cacertsPath();

    @WithName("ssl-insecure")
    @WithDefault("false")
    boolean sslInsecure();

    @WithName("sefaz-autorizacao-url")
    Optional<String> sefazAutorizacaoUrl();

    @WithName("sefaz-retorno-url")
    Optional<String> sefazRetornoUrl();

    @WithName("sefaz-consulta-url")
    Optional<String> sefazConsultaUrl();

    @WithName("sefaz-evento-url")
    Optional<String> sefazEventoUrl();

    @WithName("sefaz-inutilizacao-url")
    Optional<String> sefazInutilizacaoUrl();

    @WithName("sefaz-status-url")
    Optional<String> sefazStatusUrl();

    @WithName("qrcode-url")
    Optional<String> qrcodeUrl();

    @WithName("resp-tecnico-cnpj")
    Optional<String> respTecnicoCnpj();

    @WithName("resp-tecnico-contato")
    Optional<String> respTecnicoContato();

    @WithName("resp-tecnico-email")
    Optional<String> respTecnicoEmail();

    @WithName("resp-tecnico-fone")
    Optional<String> respTecnicoFone();

    @WithName("resp-tecnico-id-csrt")
    Optional<String> respTecnicoIdCsrt();

    @WithName("resp-tecnico-hash-csrt")
    Optional<String> respTecnicoHashCsrt();

    Nfce nfce();

    interface Nfce {

        @WithDefault("true")
        boolean enabled();
    }
}
