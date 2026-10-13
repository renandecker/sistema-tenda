package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.ProdutoBuffetDTO;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.service.ProdutoBuffetService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/buffet/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoBuffetResource {

    @Inject
    ProdutoBuffetService service;

    @GET
    public PageResponse<ProdutoBuffetDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("categoria") ProdutoBuffet.Categoria categoria,
            @QueryParam("unidadeMedida") UnidadeMedida unidadeMedida,
            @QueryParam("ativo") Boolean ativo,
            @QueryParam("disponivelBalcao") Boolean disponivelBalcao) {
        return service.findAll(page, size, sortBy, sortDirection, categoria, unidadeMedida, ativo, disponivelBalcao);
    }

    @GET
    @Path("/ativos")
    public List<ProdutoBuffetDTO> listarAtivos() {
        return service.findAtivos();
    }

    @GET
    @Path("/disponiveis-balcao")
    public List<ProdutoBuffetDTO> listarDisponiveisBalcao() {
        return service.findDisponiveisBalcao();
    }

    @GET
    @Path("/estoque-baixo")
    public List<ProdutoBuffetDTO> listarEstoqueBaixo() {
        return service.findEstoqueBaixo();
    }

    @GET
    @Path("/categoria/{categoria}")
    public List<ProdutoBuffetDTO> listarPorCategoria(@PathParam("categoria") ProdutoBuffet.Categoria categoria) {
        return service.findByCategoria(categoria);
    }

    @GET
    @Path("/search")
    public List<ProdutoBuffetDTO> buscar(@QueryParam("q") String termo) {
        return service.search(termo);
    }

    @GET
    @Path("/{id}")
    public ProdutoBuffetDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/codigo/{codigo}")
    public ProdutoBuffetDTO buscarPorCodigo(@PathParam("codigo") String codigo) {
        return service.findByCodigo(codigo);
    }

    @GET
    @Path("/codigo-barras/{codigoBarras}")
    public ProdutoBuffetDTO buscarPorCodigoBarras(@PathParam("codigoBarras") String codigoBarras) {
        return service.findByCodigoBarras(codigoBarras);
    }

    @POST
    public Response criar(@Valid ProdutoBuffetDTO dto) {
        ProdutoBuffetDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoBuffetDTO atualizar(@PathParam("id") UUID id, @Valid ProdutoBuffetDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @PUT
    @Path("/{id}/estoque")
    public ProdutoBuffetDTO atualizarEstoque(@PathParam("id") UUID id, @QueryParam("quantidade") BigDecimal quantidade) {
        return service.atualizarEstoque(id, quantidade);
    }
}