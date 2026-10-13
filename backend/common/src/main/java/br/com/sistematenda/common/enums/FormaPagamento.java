package br.com.sistematenda.common.enums;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    PIX("Pix"),
    VALE_ALIMENTACAO("Vale Alimentação (VA)"),
    VALE_REFEICAO("Vale Refeição (VR)"),
    VALE_CULTURA("Vale Cultura"),
    CHEQUE("Cheque"),
    TRANSFERENCIA("Transferência Bancária"),
    BOLETO("Boleto"),
    OUTROS("Outros");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isCartao() {
        return this == CARTAO_CREDITO || this == CARTAO_DEBITO;
    }

    public boolean isVale() {
        return this == VALE_ALIMENTACAO || this == VALE_REFEICAO || this == VALE_CULTURA;
    }

    public boolean isEletronico() {
        return isCartao() || this == PIX || isVale() || this == TRANSFERENCIA;
    }
}