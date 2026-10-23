package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;

import java.util.Locale;

public final class SefazEndpoints {

    public record Endpoints(
        String autorizacao,
        String retorno,
        String consulta,
        String evento,
        String inutilizacao,
        String status,
        String qrcode
    ) {
    }

    private SefazEndpoints() {
    }

    public static Endpoints resolver(FiscalConfig config, String modelo) {
        String uf = config.uf() != null ? config.uf().trim().toUpperCase(Locale.ROOT) : "";
        boolean nfce = NfeConstantes.MODELO_NFCE.equals(modelo);
        boolean producao = NfeConstantes.isProducao(config.ambiente());

        String autorizacaoPadrao = null;
        String retornoPadrao = null;
        String consultaPadrao = null;
        String eventoPadrao = null;
        String inutilizacaoPadrao = null;
        String statusPadrao = null;
        String qrcodeServicoPadrao = null;

        if ("RS".equals(uf)) {
            String base;
            if (nfce) {
                base = producao
                    ? "https://nfce.sefazrs.rs.gov.br/ws"
                    : "https://nfce-homologacao.sefazrs.rs.gov.br/ws";
            } else {
                base = producao
                    ? "https://nfe.sefazrs.rs.gov.br/ws"
                    : "https://nfe-homologacao.sefazrs.rs.gov.br/ws";
            }
            autorizacaoPadrao = base + "/NfeAutorizacao/NFeAutorizacao4.asmx";
            retornoPadrao = base + "/NfeRetAutorizacao/NFeRetAutorizacao4.asmx";
            consultaPadrao = base + "/NfeConsulta/NfeConsulta4.asmx";
            eventoPadrao = base + "/recepcaoevento/recepcaoevento4.asmx";
            inutilizacaoPadrao = base + "/nfeinutilizacao/nfeinutilizacao4.asmx";
            statusPadrao = base + "/NfeStatusServico/NfeStatusServico4.asmx";
            qrcodeServicoPadrao = "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx";
        }

        String autorizacaoServico = autorizacaoPadrao;
        String retornoServico = retornoPadrao;
        String consultaServico = consultaPadrao;
        String eventoServico = eventoPadrao;
        String inutilizacaoServico = inutilizacaoPadrao;
        String statusServico = statusPadrao;

        return new Endpoints(
            config.sefazAutorizacaoUrl().filter(SefazEndpoints::preenchida).orElseGet(() -> obrigatorio(autorizacaoServico, "autorizacao", uf)),
            config.sefazRetornoUrl().filter(SefazEndpoints::preenchida).orElseGet(() -> obrigatorio(retornoServico, "retorno", uf)),
            config.sefazConsultaUrl().filter(SefazEndpoints::preenchida).orElseGet(() -> obrigatorio(consultaServico, "consulta", uf)),
            config.sefazEventoUrl().filter(SefazEndpoints::preenchida).orElseGet(() -> obrigatorio(eventoServico, "evento", uf)),
            config.sefazInutilizacaoUrl().filter(SefazEndpoints::preenchida).orElseGet(() -> obrigatorio(inutilizacaoServico, "inutilizacao", uf)),
            config.sefazStatusUrl().filter(SefazEndpoints::preenchida).orElseGet(() -> obrigatorio(statusServico, "status", uf)),
            config.qrcodeUrl().filter(SefazEndpoints::preenchida)
                .orElse(qrcodeServicoPadrao != null ? qrcodeServicoPadrao : qrcodePadrao(uf))
        );
    }

    public static String qrcodePadrao(String uf) {
        if (uf == null) {
            return "";
        }
        return "RS".equalsIgnoreCase(uf.trim())
            ? "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx"
            : "";
    }

    private static String obrigatorio(String valor, String servico, String uf) {
        if (valor != null && !valor.isBlank()) {
            return valor;
        }
        throw new BusinessException("SEFAZ_NAO_CONFIGURADA",
            "Web service de " + servico + " da SEFAZ nao configurado para a UF " + uf
                + ". Configure fiscal.sefaz-" + servico + "-url");
    }

    private static boolean preenchida(String valor) {
        return valor != null && !valor.isBlank();
    }
}
