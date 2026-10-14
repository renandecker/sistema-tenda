package br.com.sistematenda.common.entity;

import br.com.sistematenda.common.enums.TipoPessoa;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "common_usuario", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email", unique = true),
    @Index(name = "idx_usuario_ativo", columnList = "ativo")
})
@UserDefinition
@Getter
@Setter
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "nome", nullable = false, length = 150)
    @NotBlank
    @Size(max = 150)
    public String nome;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    @NotBlank
    @Email
    @Size(max = 150)
    @Username
    public String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    @Password
    public String senhaHash;

    @Column(name = "cpf_cnpj", length = 18)
    @Size(max = 18)
    public String cpfCnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", length = 20)
    public TipoPessoa tipoPessoa = TipoPessoa.FISICA;

    @Column(name = "telefone", length = 20)
    @Size(max = 20)
    public String telefone;

    @Column(name = "avatar_url", length = 500)
    @Size(max = 500)
    public String avatarUrl;

    @Column(name = "ativo", nullable = false)
    @NotNull
    public Boolean ativo = true;

    @Column(name = "ultimo_login")
    public LocalDateTime ultimoLogin;

    @Column(name = "tentativas_login")
    public Integer tentativasLogin = 0;

    @Column(name = "bloqueado_ate")
    public LocalDateTime bloqueadoAte;

    @Column(name = "senha_expiracao")
    public LocalDateTime senhaExpiracao;

    @Column(name = "primeiro_acesso", nullable = false)
    public Boolean primeiroAcesso = true;

    @Column(name = "empresa_id")
    public UUID empresaId;

    @Column(name = "pdv_id")
    public UUID pdvId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "common_usuario_perfil", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "perfil", length = 50)
    @Roles
    public Set<String> perfis = new HashSet<>();

    @Column(name = "data_criacao", nullable = false, updatable = false)
    public LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    public LocalDateTime dataAtualizacao;

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public void addPerfil(String perfil) {
        perfis.add(perfil);
    }

    public void removePerfil(String perfil) {
        perfis.remove(perfil);
    }

    public boolean hasPerfil(String perfil) {
        return perfis.contains(perfil);
    }

    public boolean isAdmin() {
        return hasPerfil("ADMIN");
    }

    public boolean isGerente() {
        return hasPerfil("GERENTE") || isAdmin();
    }

    public static Usuario findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}