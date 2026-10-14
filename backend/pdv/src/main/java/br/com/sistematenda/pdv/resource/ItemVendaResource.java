package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.ItemVendaDTO;
import br.com.sistematenda.pdv.service.ItemVendaService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/pdv/itens-venda")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemVendaResource {

    @Inject
    ItemVendaService service;

    @GET
    @Path("/venda/{vendaId}")
    public List<ItemVendaDTO> listarPorVenda(@PathParam("vendaId") UUID vendaId) {
        return service.findByVenda(vendaId);
    }

    @GET
    @Path("/venda/{vendaId}/paginado")
    public PageResponse<ItemVendaDTO> listarPorVendaPaginado(
            @PathParam("vendaId") UUID vendaId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection) {
        return service.findByVendaPaginated(vendaId, page, size, sortBy, sortDirection);
    }

    @GET
    @Path("/{id}")
    public ItemVendaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid ItemVendaDTO dto) {
        ItemVendaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ItemVendaDTO atualizar(@PathParam("id") UUID id, @Valid ItemVendaDTO dto) {
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
}