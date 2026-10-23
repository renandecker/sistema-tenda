package br.com.sistematenda.fiscal.nfe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

public final class QrCodeNfce {

    private QrCodeNfce() {
    }

    public static String gerarUrl(String urlBase, String chaveAcesso, String ambiente,
                                  String cscId, String csc) {
        String id = normalizarCscId(cscId);
        String dados = chaveAcesso + "|2|" + ambiente + "|" + id + csc;
        String hash = sha1Hex(dados);
        return urlBase + "?p=" + chaveAcesso + "|2|" + ambiente + "|" + id + "|" + hash;
    }

    public static String gerarUrlOffline(String urlBase, String chaveAcesso, String ambiente,
                                         String cscId, String csc, String diaEmissao,
                                         BigDecimal valorTotal, String digestValue) {
        String id = normalizarCscId(cscId);
        String valor = (valorTotal != null ? valorTotal : BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP).toPlainString();
        String digest = digestValue == null ? "" : digestValue;
        String dados = chaveAcesso + "|2|" + ambiente + "|" + diaEmissao + "|" + valor + "|" + digest + "|" + id + csc;
        String hash = sha1Hex(dados);
        return urlBase + "?p=" + chaveAcesso + "|2|" + ambiente + "|" + diaEmissao + "|" + valor + "|" + digest + "|" + id + "|" + hash;
    }

    public static String sha1Hex(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel calcular o hash do QR Code", e);
        }
    }

    public static String normalizarCscId(String cscId) {
        if (cscId == null) {
            return "";
        }
        String digitos = cscId.replaceAll("\\D", "");
        int indice = 0;
        while (indice < digitos.length() - 1 && digitos.charAt(indice) == '0') {
            indice++;
        }
        return digitos.substring(indice).toUpperCase(Locale.ROOT);
    }

}
