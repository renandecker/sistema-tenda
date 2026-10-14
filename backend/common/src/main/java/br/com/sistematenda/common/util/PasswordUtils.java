package br.com.sistematenda.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordUtils {

    private static final int BCRYPT_COST = 12;

    public static String hash(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        return BCrypt.hashpw(senha, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verify(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(senha, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean needsRehash(String hash) {
        if (hash == null) return true;
        try {
            return BCrypt.checkpw("", hash);
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}