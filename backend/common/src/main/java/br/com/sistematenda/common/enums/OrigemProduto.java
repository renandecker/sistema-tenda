package br.com.sistematenda.common.enums;

public enum OrigemProduto {
    FRUTEIRA("Fruteira (Varejo)"),
    BUFFET("Buffet (Produção Própria)"),
    FORNECEDOR("Fornecedor Externo"),
    TRANSFERENCIA("Transferência Interna");

    private final String descricao;

    OrigemProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}