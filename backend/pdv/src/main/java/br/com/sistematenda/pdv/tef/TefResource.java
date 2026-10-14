package br.com.sistematenda.pdv.tef;

import br.com.sistematenda.common.exception.BusinessException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/pdv/tef")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TefResource {

    @Inject
    TefService service;

    @POST
    @Path("/iniciar")
    public TefTransacaoResponse iniciarTransacao(@Valid TefIniciarTransacaoRequest request) {
        return service.iniciarTransacao(request);
    }

    @POST
    @Path("/confirmar")
    public TefTransacaoResponse confirmarTransacao(@QueryParam("requestId") String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "requestId é obrigatório");
        }
        return service.confirmarTransacao(requestId);
    }

    @POST
    @Path("/desfazer")
    public Response desfazerTransacao(@QueryParam("requestId") String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "requestId é obrigatório");
        }
        service.desfazerTransacao(requestId);
        return Response.ok(Map.of("success", true, "message", "Transação desfeita")).build();
    }

    @GET
    @Path("/comprovante")
    public Response reimprimirComprovante(
            @QueryParam("requestId") String requestId,
            @QueryParam("via") @DefaultValue("CLIENTE") String via) {
        if (requestId == null || requestId.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "requestId é obrigatório");
        }
        String comprovante = service.reimprimirComprovante(requestId, via);
        return Response.ok(Map.of("comprovante", comprovante)).build();
    }

    @POST
    @Path("/webhook/evento")
    public Response receberEvento(TefEventoResponse evento) {
        service.notificarEvento(evento);
        return Response.ok(Map.of("success", true)).build();
    }

    @GET
    @Path("/status")
    public Response status() {
        return Response.ok(Map.of("status", "ok", "tefAgentUrl", "configured")).build();
    }
}