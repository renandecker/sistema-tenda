package br.com.sistematenda.common.config;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "fiscal")
public class FiscalConfig {

    @ConfigItem(name = "ambiente", defaultValue = "HOMOLOGACAO")
    public String ambiente;

    @ConfigItem(name = "certificado-path")
    public String certificadoPath;

    @ConfigItem(name = "certificado-senha")
    public String certificadoSenha;

    @ConfigItem(name = "csc")
    public String csc;

    @ConfigItem(name = "csc-id")
    public String cscId;

    @ConfigItem(name = "uf", defaultValue = "SP")
    public String uf;

    @ConfigItem(name = "municipio-codigo")
    public Integer municipioCodigo;

    @ConfigItem(name = "municipio-nome")
    public String municipioNome;

    @ConfigItem(name = "cnpj")
    public String cnpj;

    @ConfigItem(name = "inscricao-estadual")
    public String inscricaoEstadual;

    @ConfigItem(name = "inscricao-municipal")
    public String inscricaoMunicipal;

    @ConfigItem(name = "crt", defaultValue = "1")
    public Integer crt;

    @ConfigItem(name = "serie-nfce", defaultValue = "1")
    public Integer serieNfce;

    @ConfigItem(name = "numero-nfce-inicial", defaultValue = "1")
    public Integer numeroNfceInicial;

    @ConfigItem(name = "serie-nfe", defaultValue = "1")
    public Integer serieNfe;

    @ConfigItem(name = "numero-nfe-inicial", defaultValue = "1")
    public Integer numeroNfeInicial;

    @ConfigItem(name = "modelo-danfe", defaultValue = "1")
    public String modeloDanfe;

    @ConfigItem(name = "logo-path")
    public String logoPath;

    @ConfigItem(name = "webservice-timeout", defaultValue = "30000")
    public int webserviceTimeout;

    @ConfigItem(name = "schemas-path", defaultValue = "schemas/")
    public String schemasPath;
}