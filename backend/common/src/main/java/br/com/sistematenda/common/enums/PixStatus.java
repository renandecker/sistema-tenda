package br.com.sistematenda.common.enums;

public enum PixStatus {
    ATIVA("Ativa - Aguardando pagamento"),
    CONCLUIDA("Concluída - Pagamento recebido"),
    REMOVIDA_PELO_USUARIO_RECEBEDOR("Removida pelo recebedor"),
    EXPIRADA("Expirada - Tempo limite atingido");

    private final String descricao;

    PixStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFinal() {
        return this == CONCLUIDA || this == REMOVIDA_PELO_USUARIO_RECEBEDOR || this == EXPIRADA;
    }

    public boolean isPaga() {
        return this == CONCLUIDA;
    }
}