package br.com.sistematenda.common.enums;

public enum PixProvider {
    EFI("Efí (Gerencianet)"),
    MERCADO_PAGO("Mercado Pago"),
    ASAAS("Asaas"),
    ITAU("Itaú (Open Finance)"),
    BRADESCO("Bradesco (Open Finance)"),
    BANCO_DO_BRASIL("Banco do Brasil (Open Finance)"),
    SICOOB("Sicoob (Open Finance)"),
    SICREDI("Sicredi (Open Finance)"),
    SIMULADO("Simulado (sem cobrança real)");

    private final String descricao;

    PixProvider(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isOpenFinance() {
        return this == ITAU || this == BRADESCO || this == BANCO_DO_BRASIL || this == SICOOB || this == SICREDI;
    }

    public boolean isFintech() {
        return this == EFI || this == MERCADO_PAGO || this == ASAAS;
    }
}