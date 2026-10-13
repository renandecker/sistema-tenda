package br.com.sistematenda.common.enums;

public enum TipoProduto {
    FRUTA("Fruta"),
    VERDURA("Verdura"),
    LEGUME("Legume"),
    SECO("Seco/Mercearia"),
    BEBIDA("Bebida"),
    LIMPEZA("Limpeza"),
    HIGIENE("Higiene"),
    CONGELADO("Congelado"),
    RESFRIADO("Resfriado"),
    PAES("Pães/Padaria"),
    OUTROS("Outros");

    private final String descricao;

    TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}