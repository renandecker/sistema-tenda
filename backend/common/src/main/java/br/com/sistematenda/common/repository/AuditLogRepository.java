package br.com.sistematenda.common.repository;

import br.com.sistematenda.common.entity.AuditLog;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuditLogRepository implements PanacheRepository<AuditLog> {

    public List<AuditLog> findByUsuarioId(UUID usuarioId) {
        return find("usuarioId", usuarioId).list();
    }

    public List<AuditLog> findByTipoAcao(String tipoAcao) {
        return find("acao", tipoAcao).list();
    }

    public List<AuditLog> findByEntidade(String entidade) {
        return find("entidade", entidade).list();
    }

    public List<AuditLog> findByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return find("dataAcao >= ?1 and dataAcao <= ?2 order by dataAcao desc", dataInicio, dataFim).list();
    }

    public long countByPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return count("dataAcao >= ?1 and dataAcao <= ?2", dataInicio, dataFim);
    }

    public List<AuditLog> findRecent(int limit) {
        return find("order by dataAcao desc").page(Page.of(0, limit)).list();
    }
}