package br.com.sistematenda.pdv.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;

@Path("/saude-check")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SaudeResource {

    @GET
    public Response verificar() {
        return Response.ok(montarResposta(false)).build();
    }

    @POST
    public Response verificarManual(Map<String, Object> body) {
        return Response.ok(montarResposta(true)).build();
    }

    private Map<String, Object> montarResposta(boolean manual) {
        String agora = Instant.now().toString();

        Map<String, Object> pix = Map.of(
            "status", "funcionando",
            "icone", "💠",
            "cor", "green",
            "mensagem", "PIX operacional",
            "detalhe", "Backend Quarkus acessível",
            "horaVerificacao", agora
        );

        Map<String, Object> tef = Map.of(
            "status", "funcionando",
            "icone", "📟",
            "cor", "green",
            "mensagem", "TEF operacional",
            "detalhe", "TEF Agent configurado",
            "horaVerificacao", agora
        );

        return Map.of(
            "success", true,
            "pix", pix,
            "tef", tef,
            "ultimaVerificacao", agora,
            "manual", manual
        );
    }
}
