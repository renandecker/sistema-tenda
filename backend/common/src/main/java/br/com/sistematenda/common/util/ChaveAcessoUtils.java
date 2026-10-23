package br.com.sistematenda.common.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public final class ChaveAcessoUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, String> UF_IBGE = Map.ofEntries(
        Map.entry("AC", "12"), Map.entry("AL", "27"), Map.entry("AP", "16"),
        Map.entry("AM", "13"), Map.entry("BA", "29"), Map.entry("CE", "23"),
        Map.entry("DF", "53"), Map.entry("ES", "32"), Map.entry("GO", "52"),
        Map.entry("MA", "21"), Map.entry("MT", "51"), Map.entry("MS", "50"),
        Map.entry("MG", "31"), Map.entry("PA", "15"), Map.entry("PB", "25"),
        Map.entry("PR", "41"), Map.entry("PE", "26"), Map.entry("PI", "22"),
        Map.entry("RJ", "33"), Map.entry("RN", "24"), Map.entry("RS", "43"),
        Map.entry("RO", "11"), Map.entry("RR", "14"), Map.entry("SC", "42"),
        Map.entry("SP", "35"), Map.entry("SE", "28"), Map.entry("TO", "17")
    );

    private ChaveAcessoUtils() {
    }

    public static String gerar(String uf, LocalDate dataEmissao, String cnpj,
                               String modelo, int serie, long numero, int tipoEmissao) {
        String codigoUf = codigoIbge(uf);
        String cnpjLimpo = somenteDigitos(cnpj);
        if (cnpjLimpo.length() != 14) {
            throw new IllegalArgumentException("CNPJ deve conter 14 digitos para gerar a chave de acesso");
        }
        if (modelo == null || modelo.length() != 2) {
            throw new IllegalArgumentException("Modelo do documento deve conter 2 digitos");
        }
        if (serie < 0 || serie > 999) {
            throw new IllegalArgumentException("Serie deve estar entre 0 e 999");
        }
        if (numero < 1 || numero > 999_999_999L) {
            throw new IllegalArgumentException("Numero deve estar entre 1 e 999999999");
        }

        String aamm = String.format(Locale.ROOT, "%02d%02d",
            dataEmissao.getYear() % 100, dataEmissao.getMonthValue());
        String cnf = String.format(Locale.ROOT, "%08d", RANDOM.nextInt(100_000_000));

        String base = codigoUf
            + aamm
            + cnpjLimpo
            + modelo
            + String.format(Locale.ROOT, "%03d", serie)
            + String.format(Locale.ROOT, "%09d", numero)
            + tipoEmissao
            + cnf;

        return base + calcularDv(base);
    }

    public static int calcularDv(String base43) {
        if (base43 == null || base43.length() != 43) {
            throw new IllegalArgumentException("Base da chave de acesso deve conter 43 digitos");
        }
        int soma = 0;
        int peso = 2;
        for (int i = base43.length() - 1; i >= 0; i--) {
            char c = base43.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("Base da chave de acesso deve conter apenas digitos");
            }
            soma += (c - '0') * peso;
            peso = peso == 9 ? 2 : peso + 1;
        }
        int resto = soma % 11;
        int dv = 11 - resto;
        return dv >= 10 ? 0 : dv;
    }

    public static String codigoIbge(String uf) {
        if (uf == null) {
            throw new IllegalArgumentException("UF nao informada");
        }
        String codigo = UF_IBGE.get(uf.trim().toUpperCase(Locale.ROOT));
        if (codigo == null) {
            throw new IllegalArgumentException("UF invalida: " + uf);
        }
        return codigo;
    }

    public static boolean ufValida(String uf) {
        return uf != null && UF_IBGE.containsKey(uf.trim().toUpperCase(Locale.ROOT));
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
