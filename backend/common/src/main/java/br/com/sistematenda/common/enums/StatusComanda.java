package br.com.sistematenda.common.enums;

public enum StatusComanda {
    ABERTA("Aberta"),
    EM_USO("Em Uso"),
    FECHADA("Fechada"),
    PAGA("Paga"),
    CANCELADA("Cancelada"),
    EM_ATRASO("Em Atraso");

    private final String descricao;

    StatusComanda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}