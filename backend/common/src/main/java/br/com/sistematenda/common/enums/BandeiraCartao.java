package br.com.sistematenda.common.enums;

public enum BandeiraCartao {
    ALELO_ALIMENTACAO("Alelo Alimentação", "Voltado para compras de ingredientes de cesta básica em supermercados, açougues, mercearias e padarias."),
    ALELO_REFEICAO("Alelo Refeição", "Voltado para refeições prontas em restaurantes, lanchonetes e redes de fast food."),
    ALELO_TUDO("Alelo Tudo (Flexível)", "Reúne em um único cartão os saldos de Alimentação, Refeição, Mobilidade, Farmácia e Saúde."),
    BANRICOMPRAS_BANRICARD("Banricompras & Banricard (Grupo Banrisul)", "O Banrisul atua no segmento de benefícios e pagamentos por meio dos cartões corporativos Banricard e pelo tradicional cartão de débito/crédito Banricompras."),
    TICKET_EDENRED("Ticket (Edenred)", "Uma das operadoras mais antigas do mercado, aceita em milhares de supermercados e restaurantes."),
    PLUXEE("Pluxee (antiga Sodexo)", "Uma das líderes do segmento no Brasil, com ampla aceitação nacional em redes de alimentação e refeição."),
    VR_BENEFICIOS("VR Benefícios", "Tradicional no setor comercial e corporativo, oferecendo soluções integradas para empresas e trabalhadores."),
    GREEN_CARD("Green Card", "Bastante forte em diversas regiões do Brasil, atuando no setor de benefícios do PAT (Programa de Alimentação do Trabalhador)."),
    VALECARD_VOLUS("ValeCard / Vólus", "Opções focadas em soluções corporativas regionais e gestão de benefícios.");

    private final String descricao;
    private final String detalhes;

    BandeiraCartao(String descricao, String detalhes) {
        this.descricao = descricao;
        this.detalhes = detalhes;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public static BandeiraCartao fromDescricao(String descricao) {
        for (BandeiraCartao bandeira : values()) {
            if (bandeira.descricao.equalsIgnoreCase(descricao)) {
                return bandeira;
            }
        }
        return null;
    }

    public static boolean isValida(String descricao) {
        return fromDescricao(descricao) != null;
    }
}