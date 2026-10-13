package br.com.sistematenda.common.enums;

public enum RegimeTributario {
    SIMPLES_NACIONAL_ANEXO_I("Simples Nacional - Anexo I (Comércio)"),
    SIMPLES_NACIONAL_ANEXO_II("Simples Nacional - Anexo II (Indústria)"),
    SIMPLES_NACIONAL_ANEXO_III("Simples Nacional - Anexo III (Serviços)"),
    SIMPLES_NACIONAL_ANEXO_IV("Simples Nacional - Anexo IV (Serviços)"),
    SIMPLES_NACIONAL_ANEXO_V("Simples Nacional - Anexo V (Serviços)"),
    LUCRO_PRESUMIDO("Lucro Presumido"),
    LUCRO_REAL("Lucro Real"),
    MEI("MEI");

    private final String descricao;

    RegimeTributario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isSimplesNacional() {
        return this.name().startsWith("SIMPLES_NACIONAL");
    }
}