package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.ReceitaDTO;
import br.com.sistematenda.buffet.service.ReceitaService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/buffet/receitas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceitaResource {

    @Inject
    ReceitaService service;

    @GET
    public PageResponse<ReceitaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("ativa") Boolean ativa) {
        return service.findAll(page, size, sortBy, sortDirection, ativa);
    }

    @GET
    @Path("/ativas")
    public List<ReceitaDTO> listarAtivas() {
        return service.findAtivas();
    }

    @GET
    @Path("/produto/{produtoId}")
    public ReceitaDTO buscarPorProduto(@PathParam("produtoId") UUID produtoId) {
        return service.findByProduto(produtoId);
    }

    @GET
    @Path("/{id}")
    public ReceitaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid ReceitaDTO dto) {
        ReceitaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ReceitaDTO atualizar(@PathParam("id") UUID id, @Valid ReceitaDTO dto) {
        return service.atualizarReceita(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }
}