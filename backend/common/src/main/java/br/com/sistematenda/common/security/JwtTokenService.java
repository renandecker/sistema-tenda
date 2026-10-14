package br.com.sistematenda.common.security;

import br.com.sistematenda.common.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class JwtTokenService {

    @ConfigProperty(name = "jwt.secret")
    String secret;

    @ConfigProperty(name = "jwt.expiration-hours", defaultValue = "24")
    long expirationHours;

    @ConfigProperty(name = "jwt.issuer", defaultValue = "sistema-tenda")
    String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(usuario.id.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", usuario.email)
                .claim("nome", usuario.nome)
                .claim("perfis", usuario.perfis)
                .claim("empresaId", usuario.empresaId != null ? usuario.empresaId.toString() : null)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(30, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(usuario.id.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("type", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public JwtClaims parseToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtClaims(
                UUID.fromString(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("nome", String.class),
                claims.get("perfis", Set.class),
                claims.get("empresaId", String.class),
                claims.get("type", String.class),
                claims.getExpiration().toInstant()
        );
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        return parseToken(token).userId();
    }

    public record JwtClaims(
            UUID userId,
            String email,
            String nome,
            Set<String> perfis,
            String empresaId,
            String type,
            Instant expiration
    ) {}
}