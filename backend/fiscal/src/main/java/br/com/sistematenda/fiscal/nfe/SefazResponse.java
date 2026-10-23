package br.com.sistematenda.fiscal.nfe;

public class SefazResponse {

    public int httpStatus;
    public String cStat;
    public String xMotivo;
    public String protocolo;
    public String chave;
    public String recibo;
    public String digVal;
    public String protNFeXml;
    public String nfeProcXml;
    public String rawXml;
    public boolean cancelamento;

    public boolean denegada() {
        return NfeConstantes.STATUS_DENEGADA.equals(cStat);
    }

    public boolean emProcessamento() {
        return NfeConstantes.STATUS_LOTE_RECEBIDO.equals(cStat)
            || NfeConstantes.STATUS_LOTE_EM_PROCESSAMENTO.equals(cStat);
    }

    public boolean autorizada() {
        return NfeConstantes.STATUS_AUTORIZADA.equals(cStat)
            || NfeConstantes.STATUS_AUTORIZADA_FORA_PRAZO.equals(cStat);
    }

    public boolean cancelada() {
        return NfeConstantes.STATUS_CANCELADA.equals(cStat)
            || NfeConstantes.STATUS_CANCELADA_FORA_PRAZO.equals(cStat);
    }

    public boolean eventoHomologado() {
        return NfeConstantes.STATUS_EVENTO_HOMOLOGADO.equals(cStat)
            || NfeConstantes.STATUS_EVENTO_HOMOLOGADO_FORA_PRAZO.equals(cStat);
    }

    public boolean loteRecebido() {
        return NfeConstantes.STATUS_LOTE_RECEBIDO.equals(cStat);
    }

    public boolean sucesso() {
        return autorizada() || cancelada() || eventoHomologado()
            || NfeConstantes.STATUS_INUTILIZACAO_HOMOLOGADA.equals(cStat)
            || NfeConstantes.STATUS_SERVICO_EM_OPERACAO.equals(cStat);
    }

    public String descricao() {
        return cStat + " - " + xMotivo;
    }
}
