package br.com.sistematenda.common.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

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

    @WithName("schemas-path")
    @WithDefault("schemas/")
    String schemasPath();

    Nfce nfce();

    interface Nfce {

        @WithDefault("true")
        boolean enabled();
    }
}
