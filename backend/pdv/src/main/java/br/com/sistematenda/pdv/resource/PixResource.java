package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.common.pix.PixWebhookNotification;
import br.com.sistematenda.pdv.pix.PixService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.UUID;

@Path("/pix")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PixResource {

    @Inject
    PixService pixService;

    @POST
    @Path("/cobrar")
    public PixChargeResponse criarCobranca(@QueryParam("vendaId") UUID vendaId,
                                           @Valid PixChargeRequest request) {
        return pixService.gerarPixParaVenda(vendaId, request.valor, request.txid);
    }

    @POST
    @Path("/venda/{vendaId}/gerar")
    public PixChargeResponse gerarPixParaVenda(
            @PathParam("vendaId") UUID vendaId,
            @QueryParam("valor") BigDecimal valor,
            @QueryParam("txid") String txid) {
        return pixService.gerarPixParaVenda(vendaId, valor, txid);
    }

    @GET
    @Path("/cobranca/{txid}")
    public PixChargeResponse consultarCobranca(@PathParam("txid") String txid) {
        return pixService.consultarStatus(txid);
    }

    @DELETE
    @Path("/cobranca/{txid}")
    public void cancelarCobranca(@PathParam("txid") String txid) {
        pixService.cancelarPix(txid);
    }

    @POST
    @Path("/webhook/efi")
    public Response webhookEfi(
            String payload,
            @HeaderParam("X-Signature") String signature,
            @HeaderParam("X-Timestamp") String timestamp) {
        processarWebhook(payload, signature, timestamp, "EFI");
        return Response.ok().build();
    }

    @POST
    @Path("/webhook/mercadopago")
    public Response webhookMercadoPago(
            String payload,
            @HeaderParam("X-Signature") String signature,
            @HeaderParam("X-Request-Timestamp") String timestamp) {
        processarWebhook(payload, signature, timestamp, "MERCADO_PAGO");
        return Response.ok().build();
    }

    @POST
    @Path("/webhook/asaas")
    public Response webhookAsaas(
            String payload,
            @HeaderParam("asaas-signature") String signature,
            @HeaderParam("asaas-timestamp") String timestamp) {
        processarWebhook(payload, signature, timestamp, "ASAAS");
        return Response.ok().build();
    }

    @POST
    @Path("/webhook/openfinance")
    public Response webhookOpenFinance(
            String payload,
            @HeaderParam("X-Signature") String signature,
            @HeaderParam("X-Timestamp") String timestamp) {
        processarWebhook(payload, signature, timestamp, "OPENFINANCE");
        return Response.ok().build();
    }

    private void processarWebhook(String payload, String signature, String timestamp, String provider) {
        try {
            PixWebhookNotification notification;
            
            br.com.sistematenda.common.pix.PixProviderService providerService = 
                switch (provider) {
                    case "EFI" -> new br.com.sistematenda.pdv.pix.EfiPixProvider();
                    case "MERCADO_PAGO" -> new br.com.sistematenda.pdv.pix.MercadoPagoPixProvider();
                    case "ASAAS" -> new br.com.sistematenda.pdv.pix.AsaasPixProvider();
                    case "OPENFINANCE" -> new br.com.sistematenda.pdv.pix.OpenFinancePixProvider();
                    default -> throw new IllegalArgumentException("Provider desconhecido: " + provider);
                };

            notification = providerService.parseWebhook(payload, signature, timestamp);
            pixService.processarWebhook(notification);
            
        } catch (Exception e) {
            throw new jakarta.ws.rs.WebApplicationException(
                "Erro ao processar webhook: " + e.getMessage(), 
                Response.Status.BAD_REQUEST
            );
        }
    }
}