package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.MesaDTO;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.buffet.service.MesaService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/buffet/mesas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MesaResource {

    @Inject
    MesaService service;

    @GET
    public PageResponse<MesaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") Mesa.StatusMesa status,
            @QueryParam("ativa") Boolean ativa,
            @QueryParam("area") String area) {
        return service.findAll(page, size, sortBy, sortDirection, status, ativa, area);
    }

    @GET
    @Path("/disponiveis")
    public List<MesaDTO> listarDisponiveis() {
        return service.findDisponiveis();
    }

    @GET
    @Path("/ativas")
    public List<MesaDTO> listarAtivas() {
        return service.findAtivas();
    }

    @GET
    @Path("/status/{status}")
    public List<MesaDTO> listarPorStatus(@PathParam("status") Mesa.StatusMesa status) {
        return service.findByStatus(status);
    }

    @GET
    @Path("/area/{area}")
    public List<MesaDTO> listarPorArea(@PathParam("area") String area) {
        return service.findByArea(area);
    }

    @GET
    @Path("/{id}")
    public MesaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/numero/{numero}")
    public MesaDTO buscarPorNumero(@PathParam("numero") String numero) {
        return service.findByNumero(numero);
    }

    @POST
    public Response criar(@Valid MesaDTO dto) {
        MesaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public MesaDTO atualizar(@PathParam("id") UUID id, @Valid MesaDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/{id}/ocupar")
    public MesaDTO ocupar(@PathParam("id") UUID id) {
        return service.ocuparMesa(id);
    }

    @POST
    @Path("/{id}/liberar")
    public MesaDTO liberar(@PathParam("id") UUID id) {
        return service.liberarMesa(id);
    }

    @POST
    @Path("/{id}/reservar")
    public MesaDTO reservar(@PathParam("id") UUID id) {
        return service.reservarMesa(id);
    }

    @PUT
    @Path("/{id}/status")
    public MesaDTO atualizarStatus(@PathParam("id") UUID id, @QueryParam("status") Mesa.StatusMesa status) {
        return service.atualizarStatus(id, status);
    }
}