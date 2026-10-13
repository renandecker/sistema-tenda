package br.com.sistematenda.fruteira.resource;

import br.com.sistematenda.fruteira.dto.TabelaPrecoDTO;
import br.com.sistematenda.fruteira.service.TabelaPrecoService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/fruteira/tabelas-preco")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TabelaPrecoResource {

    @Inject
    TabelaPrecoService service;

    @GET
    public PageResponse<TabelaPrecoDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("ativa") Boolean ativa,
            @QueryParam("dataInicio") LocalDate dataInicio,
            @QueryParam("dataFim") LocalDate dataFim) {
        return service.findAll(page, size, sortBy, sortDirection, ativa, dataInicio, dataFim);
    }

    @GET
    @Path("/produto/{produtoId}")
    public List<TabelaPrecoDTO> listarPorProduto(@PathParam("produtoId") UUID produtoId) {
        return service.findByProduto(produtoId);
    }

    @GET
    @Path("/vigentes")
    public List<TabelaPrecoDTO> listarVigentes(@QueryParam("data") LocalDate data) {
        return service.findVigentes(data != null ? data : LocalDate.now());
    }

    @GET
    @Path("/produto/{produtoId}/vigente")
    public TabelaPrecoDTO buscarVigentePorProduto(@PathParam("produtoId") UUID produtoId,
                                                   @QueryParam("data") LocalDate data) {
        return service.findVigenteByProduto(produtoId, data != null ? data : LocalDate.now());
    }

    @GET
    @Path("/{id}")
    public TabelaPrecoDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid TabelaPrecoDTO dto) {
        TabelaPrecoDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public TabelaPrecoDTO atualizar(@PathParam("id") UUID id, @Valid TabelaPrecoDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/aplicar-vigente/{produtoId}")
    public TabelaPrecoDTO aplicarVigente(@PathParam("produtoId") UUID produtoId) {
        return service.aplicarTabelaVigente(produtoId);
    }

    @POST
    @Path("/aplicar-todas-vigentes")
    public void aplicarTodasVigentes() {
        service.aplicarTodasVigentes();
    }
}