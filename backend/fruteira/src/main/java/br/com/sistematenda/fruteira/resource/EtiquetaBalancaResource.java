package br.com.sistematenda.fruteira.resource;

import br.com.sistematenda.fruteira.dto.EtiquetaBalancaDTO;
import br.com.sistematenda.fruteira.service.EtiquetaBalancaService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/fruteira/etiquetas-balanca")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EtiquetaBalancaResource {

    @Inject
    EtiquetaBalancaService service;

    @GET
    public PageResponse<EtiquetaBalancaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("ativa") Boolean ativa) {
        return service.findAll(page, size, sortBy, sortDirection, ativa);
    }

    @GET
    @Path("/ativas")
    public List<EtiquetaBalancaDTO> listarAtivas() {
        return service.findAtivas();
    }

    @GET
    @Path("/produto/{produtoId}")
    public List<EtiquetaBalancaDTO> listarPorProduto(@PathParam("produtoId") UUID produtoId) {
        return service.findByProduto(produtoId);
    }

    @GET
    @Path("/produto/{produtoId}/ativas")
    public List<EtiquetaBalancaDTO> listarAtivasPorProduto(@PathParam("produtoId") UUID produtoId) {
        return service.findAtivasPorProduto(produtoId);
    }

    @GET
    @Path("/{id}")
    public EtiquetaBalancaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/codigo/{codigo}")
    public EtiquetaBalancaDTO buscarPorCodigo(@PathParam("codigo") String codigo) {
        return service.findByCodigo(codigo);
    }

    @POST
    public Response criar(@Valid EtiquetaBalancaDTO dto) {
        EtiquetaBalancaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Path("/gerar/{produtoId}")
    public Response gerarParaProduto(@PathParam("produtoId") UUID produtoId) {
        EtiquetaBalancaDTO criado = service.gerarParaProduto(produtoId);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public EtiquetaBalancaDTO atualizar(@PathParam("id") UUID id, @Valid EtiquetaBalancaDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/{codigo}/imprimir")
    public EtiquetaBalancaDTO imprimir(@PathParam("codigo") String codigo) {
        return service.imprimirEtiqueta(codigo);
    }
}