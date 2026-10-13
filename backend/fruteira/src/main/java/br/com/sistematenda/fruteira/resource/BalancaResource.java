package br.com.sistematenda.fruteira.resource;

import br.com.sistematenda.fruteira.scale.ScaleService;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.entity.EtiquetaBalanca;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.BusinessException;
import jakarta.inject.Inject;
import jakarta.validation.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;

import java.math.BigDecimal;
import java.util.UUID;

@Path("/fruteira/balanca")
@Produces(MediaType.APPLICATION_JSON)
public class BalancaResource {

    @Inject
    ScaleService scaleService;

    @Inject
    ProdutoService produtoService;

    @Sse
    Sse sse;

    @GET
    @Path("/status")
    public BalancaStatus getStatus() {
        return new BalancaStatus(
            scaleService.isConnected(),
            scaleService.isModeAuto(),
            scaleService.isModeManual()
        );
    }

    @POST
    @Path("/modo")
    public Response setModo(@QueryParam("auto") Boolean auto) {
        if (auto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parâmetro 'auto' é obrigatório").build();
        }
        scaleService.setAutoMode(auto);
        return Response.ok().build();
    }

    @GET
    @Path("/ler-peso")
    public CompletableFuture<Response> lerPeso() {
        return scaleService.readWeight()
                .thenApply(rawData -> {
                    // Processar peso e gerar comprovante
                    return Response.ok(new LeituraPesoResponse(
                        rawData,
                        scaleService.isModeAuto()
                    )).build();
                })
                .exceptionally(ex -> Response.status(Response.Status.REQUEST_TIMEOUT)
                        .entity(new LeituraPesoResponse(null, false, "Timeout")).build());
    }

    @POST
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void streamPeso(@Context SseEventSink eventSink) {
        // Stream de pesos em tempo real
        scaleService.setDataListener(rawData -> {
            OutboundSseEvent event = sse.newEventBuilder()
                    .name("peso")
                    .data(rawData)
                    .build();
            eventSink.send(event);
        });
    }

    public static class BalancaStatus {
        public boolean conectada;
        public boolean modoAuto;
        public boolean modoManual;

        public BalancaStatus(boolean conectada, boolean modoAuto, boolean modoManual) {
            this.conectada = conectada;
            this.modoAuto = modoAuto;
            this.modoManual = modoManual;
        }
    }

    public static class LeituraPesoResponse {
        public String rawData;
        public boolean modoAuto;
        public String mensagem;

        public LeituraPesoResponse(String rawData, boolean modoAuto) {
            this.rawData = rawData;
            this.modoAuto = modoAuto;
            this.mensagem = modoAuto 
                ? "Pesagem automática - Comprovante gerado" 
                : "Pesagem manual - Definir produto no PDV";
        }

        public LeituraPesoResponse(String rawData, boolean modoAuto, String mensagem) {
            this.rawData = rawData;
            this.modoAuto = modoAuto;
            this.mensagem = mensagem;
        }
    }
}