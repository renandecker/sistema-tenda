package br.com.sistematenda.common.enums;

public enum TipoAcaoAuditoria {
    CRIAR("CRIAR"),
    EDITAR("EDITAR"),
    EXCLUIR("EXCLUIR"),
    VISUALIZAR("VISUALIZAR"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    ALTERAR_STATUS("ALTERAR_STATUS"),
    IMPORTAR("IMPORTAR"),
    EXPORTAR("EXPORTAR"),
    OUTRO("OUTRO");

    private final String valor;

    TipoAcaoAuditoria(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}