package br.com.sistematenda.estoque.resource;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.estoque.dto.ProdutoEstoqueDTO;
import br.com.sistematenda.estoque.entity.ProdutoEstoque;
import br.com.sistematenda.estoque.service.ProdutoEstoqueService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/estoque/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoEstoqueResource {

    @Inject
    ProdutoEstoqueService service;

    @GET
    public PageResponse<ProdutoEstoqueDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("categoria") ProdutoEstoque.Categoria categoria,
            @QueryParam("unidadeMedida") UnidadeMedida unidadeMedida,
            @QueryParam("ativo") Boolean ativo) {
        return service.findAll(page, size, sortBy, sortDirection, categoria, unidadeMedida, ativo);
    }

    @GET
    @Path("/ativos")
    public List<ProdutoEstoqueDTO> listarAtivos() {
        return service.findAtivos();
    }

    @GET
    @Path("/estoque-baixo")
    public List<ProdutoEstoqueDTO> listarEstoqueBaixo() {
        return service.findEstoqueBaixo();
    }

    @GET
    @Path("/categoria/{categoria}")
    public List<ProdutoEstoqueDTO> listarPorCategoria(@PathParam("categoria") ProdutoEstoque.Categoria categoria) {
        return service.findByCategoria(categoria);
    }

    @GET
    @Path("/search")
    public List<ProdutoEstoqueDTO> buscar(@QueryParam("q") String termo) {
        return service.search(termo);
    }

    @GET
    @Path("/{id}")
    public ProdutoEstoqueDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/codigo/{codigo}")
    public ProdutoEstoqueDTO buscarPorCodigo(@PathParam("codigo") String codigo) {
        return service.findByCodigo(codigo);
    }

    @GET
    @Path("/codigo-barras/{codigoBarras}")
    public ProdutoEstoqueDTO buscarPorCodigoBarras(@PathParam("codigoBarras") String codigoBarras) {
        return service.findByCodigoBarras(codigoBarras);
    }

    @POST
    public Response criar(@Valid ProdutoEstoqueDTO dto) {
        ProdutoEstoqueDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoEstoqueDTO atualizar(@PathParam("id") UUID id, @Valid ProdutoEstoqueDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @PUT
    @Path("/{id}/estoque")
    public ProdutoEstoqueDTO atualizarEstoque(@PathParam("id") UUID id,
                                              @QueryParam("quantidade") BigDecimal quantidade) {
        return service.atualizarEstoque(id, quantidade);
    }
}
