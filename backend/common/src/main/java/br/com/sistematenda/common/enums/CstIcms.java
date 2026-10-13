package br.com.sistematenda.common.enums;

public enum CstIcms {
    // Tributada integralmente
    CST_00("00", "Tributada integralmente"),
    CST_10("10", "Tributada com cobrança do ICMS por ST"),
    CST_20("20", "Com redução de base de cálculo"),
    CST_30("30", "Isenta ou não tributada com cobrança do ICMS por ST"),
    CST_40("40", "Isenta"),
    CST_41("41", "Não tributada"),
    CST_50("50", "Suspensão"),
    CST_51("51", "Diferimento"),
    CST_60("60", "ICMS cobrado anteriormente por ST"),
    CST_70("70", "Com redução de base de cálculo e cobrança do ICMS por ST"),
    CST_90("90", "Outras"),

    // Simples Nacional
    CSOSN_101("101", "Tributada pelo Simples Nacional com permissão de crédito"),
    CSOSN_102("102", "Tributada pelo Simples Nacional sem permissão de crédito"),
    CSOSN_103("103", "Isenção do ICMS no Simples Nacional para faixa de receita bruta"),
    CSOSN_201("201", "Tributada pelo Simples Nacional com permissão de crédito e com cobrança do ICMS por ST"),
    CSOSN_202("202", "Tributada pelo Simples Nacional sem permissão de crédito e com cobrança do ICMS por ST"),
    CSOSN_203("203", "Isenção do ICMS no Simples Nacional para faixa de receita bruta e com cobrança do ICMS por ST"),
    CSOSN_300("300", "Imune"),
    CSOSN_400("400", "Não tributada pelo Simples Nacional"),
    CSOSN_500("500", "ICMS cobrado anteriormente por ST (subst. tributária)"),
    CSOSN_900("900", "Outras");

    private final String codigo;
    private final String descricao;

    CstIcms(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isSimplesNacional() {
        return this.name().startsWith("CSOSN");
    }

    public static CstIcms fromCodigo(String codigo) {
        for (CstIcms cst : values()) {
            if (cst.codigo.equals(codigo)) {
                return cst;
            }
        }
        return null;
    }
}