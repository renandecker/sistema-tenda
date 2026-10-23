package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.util.ChaveAcessoUtils;

import java.util.Locale;

public final class NfeConstantes {

    public static final String NS_NFE = "http://www.portalfiscal.inf.br/nfe";
    public static final String NS_DSIG = "http://www.w3.org/2000/09/xmldsig#";
    public static final String NS_SOAP = "http://www.w3.org/2003/05/soap-envelope";

    public static final String VERSAO_NFE = "4.00";
    public static final String VERSAO_EVENTO = "1.00";
    public static final String VERSAO_INUT = "4.00";

    public static final String MODELO_NFE = "55";
    public static final String MODELO_NFCE = "65";

    public static final String SERVICO_AUTORIZACAO = "NFeAutorizacao4";
    public static final String SERVICO_RETORNO = "NFeRetAutorizacao4";
    public static final String SERVICO_CONSULTA = "NFeConsultaProtocolo4";
    public static final String SERVICO_EVENTO = "NFeRecepcaoEvento4";
    public static final String SERVICO_INUTILIZACAO = "NFeInutilizacao4";
    public static final String SERVICO_STATUS = "NFeStatusServico4";

    public static final String TP_EVENTO_CANCELAMENTO = "110111";
    public static final String TP_EVENTO_CANCELAMENTO_SUBSTITUICAO = "110112";

    public static final String STATUS_AUTORIZADA = "100";
    public static final String STATUS_AUTORIZADA_FORA_PRAZO = "150";
    public static final String STATUS_CANCELADA = "101";
    public static final String STATUS_CANCELADA_FORA_PRAZO = "151";
    public static final String STATUS_DENEGADA = "110";
    public static final String STATUS_LOTE_RECEBIDO = "103";
    public static final String STATUS_LOTE_PROCESSADO = "104";
    public static final String STATUS_LOTE_EM_PROCESSAMENTO = "105";
    public static final String STATUS_EVENTO_HOMOLOGADO = "135";
    public static final String STATUS_EVENTO_HOMOLOGADO_FORA_PRAZO = "136";
    public static final String STATUS_INUTILIZACAO_HOMOLOGADA = "102";
    public static final String STATUS_SERVICO_EM_OPERACAO = "107";

    private NfeConstantes() {
    }

    public static String ambienteCodigo(String ambiente) {
        if (ambiente == null) {
            return "2";
        }
        String valor = ambiente.trim().toUpperCase(Locale.ROOT);
        return "PRODUCAO".equals(valor) || "PRODUCTION".equals(valor) ? "1" : "2";
    }

    public static boolean isProducao(String ambiente) {
        return "1".equals(ambienteCodigo(ambiente));
    }

    public static String codigoUf(String uf) {
        return ChaveAcessoUtils.codigoIbge(uf);
    }

    public static String modelo(String modelo) {
        return MODELO_NFE.equals(modelo) ? MODELO_NFE : MODELO_NFCE;
    }
}
