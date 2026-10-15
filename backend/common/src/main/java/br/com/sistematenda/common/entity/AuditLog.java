package br.com.sistematenda.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "common_audit_log", indexes = {
    @Index(name = "idx_audit_data", columnList = "data_acao"),
    @Index(name = "idx_audit_usuario", columnList = "usuario_id"),
    @Index(name = "idx_a_tipo_acao", columnList = "tipo_acao")
})
@Getter
@Setter
public class AuditLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "usuario_id", length = 36)
    public UUID usuarioId;

    @Column(name = "acao", nullable = false, length = 100)
    public String acao;

    @Column(name = "entidade", length = 50)
    public String entidade;

    @Column(name = "registro_id", length = 36)
    public UUID registroId;

    @Column(name = "descricao", columnDefinition = "TEXT")
    public String descricao;

    @Column(name = "ip_address", length = 45)
    public String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    public String userAgent;

    @Column(name = "data_acao", nullable = false)
    public LocalDateTime dataAcao = LocalDateTime.now();

    @Column(name = "sucesso", nullable = false)
    public Boolean sucesso = true;
}