package br.com.sistematenda.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

public class AuthDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        @Email
        public String email;

        @NotBlank
        @Size(min = 6, max = 100)
        public String senha;

        public Boolean rememberMe;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        public String token;
        public String refreshToken;
        public UserDTO user;
        public String message;
        public long expiresIn;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        public UUID id;
        public String nome;
        public String email;
        public Set<String> perfis;
        public String empresaId;
        public String pdvId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank
        public String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank
        @Email
        public String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        public String token;
        
        @NotBlank
        @Size(min = 6, max = 100)
        public String senhaAtual;

        @NotBlank
        @Size(min = 6, max = 100)
        public String novaSenha;

        @NotBlank
        @Size(min = 6, max = 100)
        public String confirmarSenha;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        public String message;
    }
}