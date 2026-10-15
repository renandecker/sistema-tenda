package br.com.sistematenda.estoque.resource;

import br.com.sistematenda.estoque.dto.MovimentoEstoqueDTO;
import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.estoque.entity.MovimentoEstoque.TipoMovimento;
import br.com.sistematenda.estoque.entity.MovimentoEstoque.OrigemDestino;
import br.com.sistematenda.estoque.service.MovimentoEstoqueService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/estoque/movimentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovimentoEstoqueResource {

    @Inject
    MovimentoEstoqueService service;

    @GET
    public PageResponse<MovimentoEstoqueDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("tipo") TipoMovimento tipo,
            @QueryParam("origem") OrigemDestino origem,
            @QueryParam("destino") OrigemDestino destino,
            @QueryParam("produto") UUID produto,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.findAll(page, size, sortBy, sortDirection, tipo, origem, destino, produto, dataInicio, dataFim);
    }

    @GET
    @Path("/estoque-baixo")
    public PageResponse<MovimentoEstoqueDTO> listarEstoqueBaixo(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return service.findEstoqueBaixo(page, size);
    }
}