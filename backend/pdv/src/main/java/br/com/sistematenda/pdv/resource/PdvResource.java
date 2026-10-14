package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.PdvDTO;
import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.pdv.service.PdvService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/pdv/terminais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PdvResource {

    @Inject
    PdvService service;

    @GET
    public PageResponse<PdvDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") Pdv.StatusPdv status,
            @QueryParam("ativo") Boolean ativo) {
        return service.findAll(page, size, sortBy, sortDirection, status, ativo);
    }

    @GET
    @Path("/livres")
    public List<PdvDTO> listarLivres() {
        return service.findLivres();
    }

    @GET
    @Path("/ativos")
    public List<PdvDTO> listarAtivos() {
        return service.findAtivos();
    }

    @GET
    @Path("/{id}")
    public PdvDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/codigo/{codigo}")
    public PdvDTO buscarPorCodigo(@PathParam("codigo") String codigo) {
        return service.findByCodigo(codigo);
    }

    @POST
    public Response criar(@Valid PdvDTO dto) {
        PdvDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public PdvDTO atualizar(@PathParam("id") UUID id, @Valid PdvDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @PUT
    @Path("/{id}/status")
    public PdvDTO atualizarStatus(@PathParam("id") UUID id, @QueryParam("status") Pdv.StatusPdv status) {
        return service.atualizarStatus(id, status);
    }
}