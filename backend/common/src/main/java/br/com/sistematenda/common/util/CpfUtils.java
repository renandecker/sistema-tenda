package br.com.sistematenda.common.util;

public class CpfUtils {

    private CpfUtils() {}

    public static boolean isValid(String cpf) {
        if (cpf == null) {
            return false;
        }
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        if (cleanCpf.length() != 11) {
            return false;
        }
        if (cleanCpf.chars().distinct().count() == 1) {
            return false;
        }
        return calculateDigit(cleanCpf, 9) == Character.getNumericValue(cleanCpf.charAt(9))
            && calculateDigit(cleanCpf, 10) == Character.getNumericValue(cleanCpf.charAt(10));
    }

    public static String clean(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }

    public static String format(String cpf) {
        String clean = clean(cpf);
        if (clean == null || clean.length() != 11) {
            return cpf;
        }
        return clean.substring(0, 3) + "." + clean.substring(3, 6) + "." + clean.substring(6, 9) + "-" + clean.substring(9, 11);
    }

    private static int calculateDigit(String cpf, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (position + 1 - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}