package br.com.sistematenda.common.enums;

public enum StatusVenda {
    ABERTA("Aberta"),
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADA("Finalizada"),
    PAGA("Paga"),
    CANCELADA("Cancelada"),
    DEVOLVIDA("Devolvida"),
    PENDENTE_PAGAMENTO("Pendente Pagamento");

    private final String descricao;

    StatusVenda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}