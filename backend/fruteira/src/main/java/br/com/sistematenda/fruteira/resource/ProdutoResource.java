package br.com.sistematenda.fruteira.resource;

import br.com.sistematenda.fruteira.dto.ProdutoDTO;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.TipoProduto;
import br.com.sistematenda.common.enums.UnidadeMedida;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/fruteira/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    @GET
    public PageResponse<ProdutoDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("tipo") TipoProduto tipo,
            @QueryParam("unidadeMedida") UnidadeMedida unidadeMedida,
            @QueryParam("ativo") Boolean ativo) {
        return service.findAll(page, size, sortBy, sortDirection, tipo, unidadeMedida, ativo);
    }

    @GET
    @Path("/ativos")
    public List<ProdutoDTO> listarAtivos() {
        return service.findAtivos();
    }

    @GET
    @Path("/estoque-baixo")
    public List<ProdutoDTO> listarEstoqueBaixo() {
        return service.findEstoqueBaixo();
    }

    @GET
    @Path("/pesaveis")
    public List<ProdutoDTO> listarPesaveisBalanca() {
        return service.findPesaveisBalanca();
    }

    @GET
    @Path("/search")
    public List<ProdutoDTO> buscar(@QueryParam("q") String termo) {
        return service.search(termo);
    }

    @GET
    @Path("/{id}")
    public ProdutoDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/codigo/{codigo}")
    public ProdutoDTO buscarPorCodigo(@PathParam("codigo") String codigo) {
        return service.findByCodigo(codigo);
    }

    @GET
    @Path("/codigo-barras/{codigoBarras}")
    public ProdutoDTO buscarPorCodigoBarras(@PathParam("codigoBarras") String codigoBarras) {
        return service.findByCodigoBarras(codigoBarras);
    }

    @GET
    @Path("/codigo-balanca/{codigoBalanca}")
    public ProdutoDTO buscarPorCodigoBalanca(@PathParam("codigoBalanca") String codigoBalanca) {
        return service.findByCodigoBalanca(codigoBalanca);
    }

    @POST
    public Response criar(@Valid ProdutoDTO dto) {
        ProdutoDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoDTO atualizar(@PathParam("id") UUID id, @Valid ProdutoDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @PUT
    @Path("/{id}/estoque")
    public ProdutoDTO atualizarEstoque(@PathParam("id") UUID id, 
                                        @QueryParam("quantidade") java.math.BigDecimal quantidade) {
        return service.atualizarEstoque(id, quantidade);
    }

    @PUT
    @Path("/{id}/preco")
    public ProdutoDTO atualizarPreco(@PathParam("id") UUID id, 
                                      @QueryParam("preco") java.math.BigDecimal preco) {
        return service.atualizarPrecoVenda(id, preco);
    }
}