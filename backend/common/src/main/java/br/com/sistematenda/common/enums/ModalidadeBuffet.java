package br.com.sistematenda.common.enums;

public enum ModalidadeBuffet {
    POR_PESO("Por Peso (R$/kg)"),
    LIVRE("Buffet Livre (Valor Fixo)"),
    PRATO_FEITO("Prato Feito (PF)"),
    EXECUTIVO("Executivo"),
    A_LA_CARTE("À La Carte");

    private final String descricao;

    ModalidadeBuffet(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}