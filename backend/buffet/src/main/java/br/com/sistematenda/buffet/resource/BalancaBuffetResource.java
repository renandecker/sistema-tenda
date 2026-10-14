package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.scale.BuffetScaleService;
import br.com.sistematenda.fruteira.scale.ScaleParser;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.OutboundSseEvent;

import java.util.concurrent.CompletableFuture;

@Path("/buffet/balanca")
@Produces(MediaType.APPLICATION_JSON)
public class BalancaBuffetResource {

    @Inject
    BuffetScaleService scaleService;

    @Inject
    ScaleParser scaleParser;

    @Inject
    Sse sse;

    @GET
    @Path("/status")
    public BalancaStatus getStatus() {
        return new BalancaStatus(scaleService.isConnected());
    }

    @POST
    @Path("/conectar")
    public Response conectar() {
        try {
            scaleService.connect();
            return Response.ok(new BalancaStatus(true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new BalancaStatus(false, e.getMessage())).build();
        }
    }

    @POST
    @Path("/desconectar")
    public Response desconectar() {
        scaleService.disconnect();
        return Response.ok(new BalancaStatus(false)).build();
    }

    @GET
    @Path("/ler-peso")
    public CompletableFuture<Response> lerPeso() {
        return scaleService.readWeight()
                .thenApply(rawData -> {
                    ScaleParser.ScaleReading reading = scaleParser.parse(rawData, "TOLEDO");
                    return Response.ok(new LeituraPesoResponse(reading)).build();
                })
                .exceptionally(ex -> Response.status(Response.Status.REQUEST_TIMEOUT)
                        .entity(new LeituraPesoResponse(
                                new ScaleParser.ScaleReading(null, false, "Timeout na leitura")))
                        .build());
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamPeso(@Context SseEventSink eventSink) {
        scaleService.setDataListener(rawData -> {
            ScaleParser.ScaleReading reading = scaleParser.parse(rawData, "TOLEDO");
            LeituraPesoResponse response = new LeituraPesoResponse(reading);
            
            OutboundSseEvent event = sse.newEventBuilder()
                    .name("peso")
                    .data(response)
                    .build();
            
            eventSink.send(event);
        });
    }

    @POST
    @Path("/comando")
    public Response enviarComando(@QueryParam("cmd") String comando) {
        try {
            scaleService.sendCommand(comando);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage()).build();
        }
    }

    public static class BalancaStatus {
        public boolean conectada;
        public String erro;

        public BalancaStatus(boolean conectada) {
            this.conectada = conectada;
        }

        public BalancaStatus(boolean conectada, String erro) {
            this.conectada = conectada;
            this.erro = erro;
        }
    }

    public static class LeituraPesoResponse {
        public java.math.BigDecimal pesoKg;
        public boolean estavel;
        public String erro;
        public boolean valida;

        public LeituraPesoResponse(ScaleParser.ScaleReading reading) {
            this.pesoKg = reading.getWeightKg();
            this.estavel = reading.isStable();
            this.erro = reading.getError();
            this.valida = reading.isValid();
        }
    }
}