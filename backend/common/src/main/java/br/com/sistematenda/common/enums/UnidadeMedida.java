package br.com.sistematenda.common.enums;

public enum UnidadeMedida {
    KG("Quilograma", "kg"),
    UN("Unidade", "un"),
    L("Litro", "l"),
    ML("Mililitro", "ml"),
    G("Grama", "g"),
    PC("Peça", "pc"),
    CX("Caixa", "cx"),
    FD("Fardo", "fd"),
    KT("Kit", "kt"),
    MT("Metro", "m"),
    M2("Metro Quadrado", "m²"),
    M3("Metro Cúbico", "m³");

    private final String descricao;
    private final String sigla;

    UnidadeMedida(String descricao, String sigla) {
        this.descricao = descricao;
        this.sigla = sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return sigla;
    }
}