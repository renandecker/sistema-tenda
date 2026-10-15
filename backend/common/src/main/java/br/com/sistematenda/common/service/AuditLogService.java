package br.com.sistematenda.common.service;

import br.com.sistematenda.common.entity.AuditLog;
import br.com.sistematenda.common.repository.AuditLogRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuditLogService {

    @Inject
    AuditLogRepository auditLogRepository;

    public void logAction(UUID usuarioId, String acao, String entidade, UUID registroId, String descricao, String ipAddress, String userAgent, Boolean sucesso) {
        AuditLog log = new AuditLog();
        log.usuarioId = usuarioId;
        log.acao = acao;
        log.entidade = entidade;
        log.registroId = registroId;
        log.descricao = descricao;
        log.ipAddress = ipAddress;
        log.userAgent = userAgent;
        log.dataAcao = LocalDateTime.now();
        log.sucesso = sucesso;
        log.persist();
    }

    public List<AuditLog> listRecent(int limit) {
        return auditLogRepository.findRecent(limit);
    }

    public List<AuditLog> findByUsuarioId(UUID usuarioId) {
        return auditLogRepository.findByUsuarioId(usuarioId);
    }

    public List<AuditLog> findByTipoAcao(String tipoAcao) {
        return auditLogRepository.findByTipoAcao(tipoAcao);
    }

    public List<AuditLog> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return auditLogRepository.findByPeriodo(dataInicio, dataFim);
    }

    public long countByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return auditLogRepository.countByPeriodo(dataInicio, dataFim);
    }

    public List<AuditLog> findByEntidade(String entidade) {
        return auditLogRepository.findByEntidade(entidade);
    }
}