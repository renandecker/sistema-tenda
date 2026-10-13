package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.PagamentoDTO;
import br.com.sistematenda.pdv.service.PagamentoService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/pdv/pagamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PagamentoResource {

    @Inject
    PagamentoService service;

    @GET
    @Path("/venda/{vendaId}")
    public List<PagamentoDTO> listarPorVenda(@PathParam("vendaId") UUID vendaId) {
        return service.findByVenda(vendaId);
    }

    @GET
    @Path("/venda/{vendaId}/total")
    public java.math.BigDecimal getTotalPago(@PathParam("vendaId") UUID vendaId) {
        return service.getTotalPago(vendaId);
    }

    @GET
    @Path("/{id}")
    public PagamentoDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid PagamentoDTO dto) {
        PagamentoDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public PagamentoDTO atualizar(@PathParam("id") UUID id, @Valid PagamentoDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }
}