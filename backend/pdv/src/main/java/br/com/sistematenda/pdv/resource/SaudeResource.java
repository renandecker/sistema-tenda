package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.StatusIntegracaoDTO;
import br.com.sistematenda.pdv.service.TesteProducaoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;

/**
 * Central de saúde e testes de produção do PDV.
 *
 * <p>{@code GET /api/saude-check} traz um status rápido (sem testes demorados).
 * Os endpoints {@code POST} executam a verificação real de cada componente.</p>
 */
@Path("/saude-check")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SaudeResource {

    @Inject
    TesteProducaoService service;

    @GET
    public Map<String, Object> statusRapido() {
        return service.statusRapido();
    }

    @POST
    public Map<String, Object> testarTudo(Map<String, Object> corpo) {
        return service.testarTudo(corpo);
    }

    @POST
    @Path("/tef")
    public StatusIntegracaoDTO testarTef() {
        return service.testarTef();
    }

    @POST
    @Path("/pix")
    public StatusIntegracaoDTO testarPix() {
        return service.testarPix();
    }

    @POST
    @Path("/impressora")
    public StatusIntegracaoDTO testarImpressora(Map<String, Object> corpo) {
        String destino = null;
        String pdvId = null;
        boolean imprimirTeste = true;

        if (corpo != null) {
            Object destinoObj = corpo.get("destino");
            if (destinoObj != null) {
                destino = String.valueOf(destinoObj);
            }
            Object pdvIdObj = corpo.get("pdvId");
            if (pdvIdObj != null) {
                pdvId = String.valueOf(pdvIdObj);
            }
            Object imprimirObj = corpo.get("imprimirTeste");
            if (imprimirObj instanceof Boolean b) {
                imprimirTeste = b;
            } else if (imprimirObj != null) {
                imprimirTeste = Boolean.parseBoolean(String.valueOf(imprimirObj));
            }
        }

        java.util.UUID pdvUuid = null;
        if (pdvId != null && !pdvId.isBlank()) {
            try {
                pdvUuid = java.util.UUID.fromString(pdvId.trim());
            } catch (IllegalArgumentException ignored) {
                // PDV inválido: segue apenas com o destino informado/configurado.
            }
        }

        return service.testarImpressora(destino, pdvUuid, imprimirTeste);
    }

    @GET
    @Path("/historico")
    public Map<String, Object> historico() {
        List<Map<String, Object>> itens = service.historico();
        return Map.of("success", true, "historico", itens);
    }
}
