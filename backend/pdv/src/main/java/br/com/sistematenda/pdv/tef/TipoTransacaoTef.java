package br.com.sistematenda.pdv.tef;

public enum TipoTransacaoTef {
    DEBITO("Débito"),
    CREDITO_A_VISTA("Crédito à Vista"),
    CREDITO_PARCELADO("Crédito Parcelado"),
    VALE_ALIMENTACAO("Vale Alimentação"),
    VALE_REFEICAO("Vale Refeição"),
    VALE_CULTURA("Vale Cultura");

    private final String descricao;

    TipoTransacaoTef(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoTransacaoTef fromFormaPagamento(String formaPagamento) {
        return switch (formaPagamento) {
            case "CARTAO_DEBITO" -> DEBITO;
            case "CARTAO_CREDITO" -> CREDITO_A_VISTA;
            case "VALE_ALIMENTACAO" -> VALE_ALIMENTACAO;
            case "VALE_REFEICAO" -> VALE_REFEICAO;
            case "VALE_CULTURA" -> VALE_CULTURA;
            default -> DEBITO;
        };
    }
}