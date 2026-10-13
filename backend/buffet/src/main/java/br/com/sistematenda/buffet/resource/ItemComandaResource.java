package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.ItemComandaDTO;
import br.com.sistematenda.buffet.service.ItemComandaService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/buffet/itens-comanda")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemComandaResource {

    @Inject
    ItemComandaService service;

    @GET
    @Path("/comanda/{comandaId}")
    public List<ItemComandaDTO> listarPorComanda(@PathParam("comandaId") UUID comandaId) {
        return service.findByComanda(comandaId);
    }

    @GET
    @Path("/comanda/{comandaId}/paginado")
    public PageResponse<ItemComandaDTO> listarPorComandaPaginado(
            @PathParam("comandaId") UUID comandaId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection) {
        return service.findByComandaPaginated(comandaId, page, size, sortBy, sortDirection);
    }

    @GET
    @Path("/comanda/{comandaId}/total")
    public BigDecimal getTotalComanda(@PathParam("comandaId") UUID comandaId) {
        return service.getTotalByComanda(comandaId);
    }

    @GET
    @Path("/{id}")
    public ItemComandaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid ItemComandaDTO dto) {
        ItemComandaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Path("/prato-balanca")
    public Response lancarPratoBalanca(@Valid PratoBalancaRequest request) {
        ItemComandaDTO criado = service.lancarPratoBalanca(request.comandaId, request.pesoBruto, request.tara);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Path("/produto-buffet")
    public Response lancarProdutoBuffet(@Valid ProdutoBuffetRequest request) {
        ItemComandaDTO criado = service.lancarProdutoBuffet(request.comandaId, request.produtoId, request.quantidade);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ItemComandaDTO atualizar(@PathParam("id") UUID id, @Valid ItemComandaDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/{id}/cancelar")
    public void cancelar(@PathParam("id") UUID id, @QueryParam("motivo") String motivo) {
        service.cancelarItem(id, motivo);
    }

    public static class PratoBalancaRequest {
        public UUID comandaId;
        public BigDecimal pesoBruto;
        public BigDecimal tara;
    }

    public static class ProdutoBuffetRequest {
        public UUID comandaId;
        public UUID produtoId;
        public BigDecimal quantidade;
    }
}