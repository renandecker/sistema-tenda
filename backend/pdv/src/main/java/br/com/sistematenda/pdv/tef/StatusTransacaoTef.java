package br.com.sistematenda.pdv.tef;

public enum StatusTransacaoTef {
    INICIADA("Iniciada"),
    AGUARDANDO_CARTAO("Aguardando cartão"),
    PROCESSANDO("Processando"),
    AGUARDANDO_SENHA("Aguardando senha"),
    AUTORIZANDO("Autorizando"),
    AUTORIZADA("Autorizada"),
    NEGADA("Negada"),
    CANCELADA("Cancelada"),
    CONFIRMADA("Confirmada"),
    DESFEITA("Desfeita"),
    ERRO("Erro");

    private final String descricao;

    StatusTransacaoTef(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFinalizado() {
        return this == AUTORIZADA || this == NEGADA || this == CANCELADA 
            || this == CONFIRMADA || this == DESFEITA || this == ERRO;
    }

    public boolean isSucesso() {
        return this == AUTORIZADA || this == CONFIRMADA;
    }
}