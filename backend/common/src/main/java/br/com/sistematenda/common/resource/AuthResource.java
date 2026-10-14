package br.com.sistematenda.common.resource;

import br.com.sistematenda.common.dto.AuthDTO;
import br.com.sistematenda.common.entity.Usuario;
import br.com.sistematenda.common.security.JwtTokenService;
import br.com.sistematenda.common.util.PasswordUtils;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Endpoints de autenticação e autorização")
public class AuthResource {

    @Inject
    JwtTokenService jwtTokenService;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Realiza login do usuário", description = "Autentica usuário e retorna token JWT")
    public Response login(@Valid AuthDTO.LoginRequest request) {
        Usuario usuario = Usuario.findByEmail(request.email);

        if (usuario == null || !usuario.ativo) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthDTO.LoginResponse(null, null, null, "Credenciais inválidas", 0))
                    .build();
        }

        if (usuario.bloqueadoAte != null && usuario.bloqueadoAte.isAfter(java.time.LocalDateTime.now())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new AuthDTO.LoginResponse(null, null, null, "Conta temporariamente bloqueada", 0))
                    .build();
        }

        if (!PasswordUtils.verify(request.senha, usuario.senhaHash)) {
            usuario.tentativasLogin = (usuario.tentativasLogin != null ? usuario.tentativasLogin : 0) + 1;
            
            if (usuario.tentativasLogin >= 5) {
                usuario.bloqueadoAte = java.time.LocalDateTime.now().plusMinutes(15);
            }
            
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthDTO.LoginResponse(null, null, null, "Credenciais inválidas", 0))
                    .build();
        }

        usuario.tentativasLogin = 0;
        usuario.bloqueadoAte = null;
        usuario.ultimoLogin = java.time.LocalDateTime.now();
        usuario.primeiroAcesso = false;

        String token = jwtTokenService.generateToken(usuario);
        String refreshToken = jwtTokenService.generateRefreshToken(usuario);
        long expiresIn = 24 * 60 * 60; // 24 horas em segundos

        AuthDTO.UserDTO userDTO = new AuthDTO.UserDTO(
                usuario.id,
                usuario.nome,
                usuario.email,
                usuario.perfis,
                usuario.empresaId != null ? usuario.empresaId.toString() : null,
                usuario.pdvId != null ? usuario.pdvId.toString() : null
        );

        return Response.ok(new AuthDTO.LoginResponse(token, refreshToken, userDTO, null, expiresIn)).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Operation(summary = "Renova token de acesso", description = "Gera novo token de acesso usando refresh token")
    public Response refresh(AuthDTO.RefreshRequest request) {
        if (!jwtTokenService.validateToken(request.refreshToken)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthDTO.LoginResponse(null, null, null, "Refresh token inválido ou expirado", 0))
                    .build();
        }

        var claims = jwtTokenService.parseToken(request.refreshToken);
        
        if (!"refresh".equals(claims.type())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthDTO.LoginResponse(null, null, null, "Token inválido", 0))
                    .build();
        }

        Usuario usuario = Usuario.findById(claims.userId());
        
        if (usuario == null || !usuario.ativo) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthDTO.LoginResponse(null, null, null, "Usuário não encontrado ou inativo", 0))
                    .build();
        }

        String newToken = jwtTokenService.generateToken(usuario);
        String newRefreshToken = jwtTokenService.generateRefreshToken(usuario);
        long expiresIn = 24 * 60 * 60;

        AuthDTO.UserDTO userDTO = new AuthDTO.UserDTO(
                usuario.id,
                usuario.nome,
                usuario.email,
                usuario.perfis,
                usuario.empresaId != null ? usuario.empresaId.toString() : null,
                usuario.pdvId != null ? usuario.pdvId.toString() : null
        );

        return Response.ok(new AuthDTO.LoginResponse(newToken, newRefreshToken, userDTO, null, expiresIn)).build();
    }

    @POST
    @Path("/logout")
    @PermitAll
    @Operation(summary = "Logout do usuário", description = "Invalida o token do usuário (client-side)")
    public Response logout() {
        return Response.ok(new AuthDTO.MessageResponse("Logout realizado com sucesso")).build();
    }

    @POST
    @Path("/reset-password")
    @PermitAll
    @Operation(summary = "Solicita recuperação de senha", description = "Envia e-mail com link para redefinir senha")
    public Response resetPassword(@Valid AuthDTO.ResetPasswordRequest request) {
        Usuario usuario = Usuario.findByEmail(request.email);
        
        if (usuario == null) {
            return Response.ok(new AuthDTO.MessageResponse("Se o e-mail existir, instruções serão enviadas")).build();
        }

        // TODO: Implementar envio de e-mail com token de recuperação
        // Por enquanto apenas retorna sucesso
        
        return Response.ok(new AuthDTO.MessageResponse("Se o e-mail existir, instruções serão enviadas")).build();
    }

    @POST
    @Path("/change-password")
    @PermitAll
    @Operation(summary = "Altera senha do usuário", description = "Altera senha usando token de recuperação ou senha atual")
    @Transactional
    public Response changePassword(@Valid AuthDTO.ChangePasswordRequest request) {
        Usuario usuario;
        
        if (request.token != null) {
            // Troca via token de recuperação
            if (!jwtTokenService.validateToken(request.token)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new AuthDTO.MessageResponse("Token inválido ou expirado"))
                        .build();
            }
            var claims = jwtTokenService.parseToken(request.token);
            usuario = Usuario.findById(claims.userId());
        } else {
            // Troca via senha atual (usuário logado)
            // Em produção, pegar usuário do contexto de segurança
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AuthDTO.MessageResponse("Não implementado: use token de recuperação"))
                    .build();
        }

        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new AuthDTO.MessageResponse("Usuário não encontrado"))
                    .build();
        }

        usuario.senhaHash = PasswordUtils.hash(request.novaSenha);
        usuario.senhaExpiracao = java.time.LocalDateTime.now().plusDays(90);
        usuario.primeiroAcesso = false;

        return Response.ok(new AuthDTO.MessageResponse("Senha alterada com sucesso")).build();
    }

    @POST
    @Path("/me")
    @PermitAll
    @Operation(summary = "Obtém dados do usuário logado", description = "Retorna informações do usuário autenticado")
    public Response me() {
        // Em produção, pegar usuário do contexto de segurança
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new AuthDTO.MessageResponse("Não autenticado"))
                .build();
    }
}