package br.com.sistematenda.common.enums;

public enum CstPisCofins {
    // PIS
    PIS_01("01", "Operação Tributável com Alíquota Básica"),
    PIS_02("02", "Operação Tributável com Alíquota Diferenciada"),
    PIS_03("03", "Operação Tributável com Alíquota por Unidade de Medida"),
    PIS_04("04", "Operação Tributável Monofásica - Revenda a Alíquota Zero"),
    PIS_05("05", "Operação Tributável por Substituição Tributária"),
    PIS_06("06", "Operação Tributável a Alíquota Zero"),
    PIS_07("07", "Operação Isenta da Contribuição"),
    PIS_08("08", "Operação Sem Incidência da Contribuição"),
    PIS_09("09", "Operação com Suspensão da Contribuição"),
    PIS_49("49", "Outras Operações de Saída"),
    PIS_50("50", "Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Tributada no Mercado Interno"),
    PIS_51("51", "Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Não Tributada no Mercado Interno"),
    PIS_52("52", "Operação com Direito a Crédito - Vinculada Exclusivamente a Receita de Exportação"),
    PIS_53("53", "Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno"),
    PIS_54("54", "Operação com Direito a Crédito - Vinculada a Receitas Tributadas no Mercado Interno e de Exportação"),
    PIS_55("55", "Operação com Direito a Crédito - Vinculada a Receitas Não Tributadas no Mercado Interno e de Exportação"),
    PIS_56("56", "Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno, e de Exportação"),
    PIS_60("60", "Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Tributada no Mercado Interno"),
    PIS_61("61", "Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Não Tributada no Mercado Interno"),
    PIS_62("62", "Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita de Exportação"),
    PIS_63("63", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno"),
    PIS_64("64", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas no Mercado Interno e de Exportação"),
    PIS_65("65", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Não Tributadas no Mercado Interno e de Exportação"),
    PIS_66("66", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno, e de Exportação"),
    PIS_67("67", "Crédito Presumido - Outras Operações"),
    PIS_70("70", "Operação de Aquisição Sem Direito a Crédito"),
    PIS_71("71", "Operação de Aquisição com Isenção"),
    PIS_72("72", "Operação de Aquisição com Suspensão"),
    PIS_73("73", "Operação de Aquisição a Alíquota Zero"),
    PIS_74("74", "Operação de Aquisição Sem Incidência da Contribuição"),
    PIS_75("75", "Operação de Aquisição por Substituição Tributária"),
    PIS_98("98", "Outras Operações de Entrada"),
    PIS_99("99", "Outras Operações"),

    // COFINS (mesmos códigos, mas para COFINS)
    COFINS_01("01", "Operação Tributável com Alíquota Básica"),
    COFINS_02("02", "Operação Tributável com Alíquota Diferenciada"),
    COFINS_03("03", "Operação Tributável com Alíquota por Unidade de Medida"),
    COFINS_04("04", "Operação Tributável Monofásica - Revenda a Alíquota Zero"),
    COFINS_05("05", "Operação Tributável por Substituição Tributária"),
    COFINS_06("06", "Operação Tributável a Alíquota Zero"),
    COFINS_07("07", "Operação Isenta da Contribuição"),
    COFINS_08("08", "Operação Sem Incidência da Contribuição"),
    COFINS_09("09", "Operação com Suspensão da Contribuição"),
    COFINS_49("49", "Outras Operações de Saída"),
    COFINS_50("50", "Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Tributada no Mercado Interno"),
    COFINS_51("51", "Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Não Tributada no Mercado Interno"),
    COFINS_52("52", "Operação com Direito a Crédito - Vinculada Exclusivamente a Receita de Exportação"),
    COFINS_53("53", "Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno"),
    COFINS_54("54", "Operação com Direito a Crédito - Vinculada a Receitas Tributadas no Mercado Interno e de Exportação"),
    COFINS_55("55", "Operação com Direito a Crédito - Vinculada a Receitas Não Tributadas no Mercado Interno e de Exportação"),
    COFINS_56("56", "Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno, e de Exportação"),
    COFINS_60("60", "Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Tributada no Mercado Interno"),
    COFINS_61("61", "Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Não Tributada no Mercado Interno"),
    COFINS_62("62", "Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita de Exportação"),
    COFINS_63("63", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno"),
    COFINS_64("64", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas no Mercado Interno e de Exportação"),
    COFINS_65("65", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Não Tributadas no Mercado Interno e de Exportação"),
    COFINS_66("66", "Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas e Não Tributadas no Mercado Interno, e de Exportação"),
    COFINS_67("67", "Crédito Presumido - Outras Operações"),
    COFINS_70("70", "Operação de Aquisição Sem Direito a Crédito"),
    COFINS_71("71", "Operação de Aquisição com Isenção"),
    COFINS_72("72", "Operação de Aquisição com Suspensão"),
    COFINS_73("73", "Operação de Aquisição a Alíquota Zero"),
    COFINS_74("74", "Operação de Aquisição Sem Incidência da Contribuição"),
    COFINS_75("75", "Operação de Aquisição por Substituição Tributária"),
    COFINS_98("98", "Outras Operações de Entrada"),
    COFINS_99("99", "Outras Operações");

    private final String codigo;
    private final String descricao;

    CstPisCofins(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPis() {
        return this.name().startsWith("PIS_");
    }

    public boolean isCofins() {
        return this.name().startsWith("COFINS_");
    }

    public static CstPisCofins fromCodigo(String codigo, boolean isPis) {
        String prefix = isPis ? "PIS_" : "COFINS_";
        for (CstPisCofins cst : values()) {
            if (cst.name().startsWith(prefix) && cst.codigo.equals(codigo)) {
                return cst;
            }
        }
        return null;
    }
}