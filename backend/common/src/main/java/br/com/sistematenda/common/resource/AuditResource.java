package br.com.sistematenda.common.resource;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.entity.AuditLog;
import br.com.sistematenda.common.service.AuditLogService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auditoria", description = "Endpoints de auditoria do sistema")
public class AuditResource {

    @Inject
    AuditLogService auditLogService;

    @GET
    @Path("/logs")
    @Authenticated
    @Operation(summary = "Lista logs de auditoria", description = "Retorna a lista de logs de auditoria com filtro opcional")
    public PageResponse<AuditLog> listLogs(
            @QueryParam("usuarioId") UUID usuarioId,
            @QueryParam("acao") String acao,
            @QueryParam("entidade") String entidade,
            @QueryParam("dataInicio") @DefaultValue("") String dataInicioStr,
            @QueryParam("dataFim") @DefaultValue("") String dataFimStr,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("50") int limit) {

        LocalDateTime dataInicio = dataInicioStr.isEmpty() ? null : LocalDateTime.parse(dataInicioStr);
        LocalDateTime dataFim = dataFimStr.isEmpty() ? null : LocalDateTime.parse(dataFimStr);

        List<AuditLog> logs = auditLogService.findByPeriodo(dataInicio, dataFim);
        long total = auditLogService.countByPeriodo(dataInicio, dataFim);

        // Aplicar filtros adicionais
        if (usuarioId != null) {
            logs = auditLogService.findByUsuarioId(usuarioId);
            total = auditLogService.countByPeriodo(dataInicio, dataFim); // TODO: contar por usuário
        }
        if (acao != null && !acao.isEmpty()) {
            logs = auditLogService.findByTipoAcao(acao);
        }
        if (entidade != null && !entidade.isEmpty()) {
            logs = auditLogService.findByEntidade(entidade);
        }

        return PageResponse.of(logs, total, page - 1, limit);
    }

    @GET
    @Path("/logs/recent")
    @Authenticated
    @Operation(summary = "Lista logs recentes", description = "Retorna os logs mais recentes do sistema")
    public List<AuditLog> listRecentLogs(@QueryParam("limit") @DefaultValue("50") int limit) {
        return auditLogService.listRecent(limit);
    }

    @GET
    @Path("/logs/estatisticas")
    @Authenticated
    @Operation(summary = "Estatísticas de auditoria", description = "Retorna estatísticas resumidas dos logs de auditoria")
    public AuditStats getEstatisticas(
            @QueryParam("dataInicio") @DefaultValue("") String dataInicioStr,
            @QueryParam("dataFim") @DefaultValue("") String dataFimStr) {

        LocalDateTime dataInicio = dataInicioStr.isEmpty() ? null : LocalDateTime.parse(dataInicioStr);
        LocalDateTime dataFim = dataFimStr.isEmpty() ? null : LocalDateTime.parse(dataFimStr);

        long total = auditLogService.countByPeriodo(dataInicio, dataFim);
        long success = 0;
        long failed = 0;

        return new AuditStats(total, success, failed);
    }

    public record AuditStats(long total, long success, long failed) {}
}