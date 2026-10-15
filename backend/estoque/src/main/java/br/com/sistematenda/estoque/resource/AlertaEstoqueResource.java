package br.com.sistematenda.estoque.resource;

import br.com.sistematenda.estoque.dto.AlertaEstoqueDTO;
import br.com.sistematenda.estoque.entity.AlertaEstoque;
import br.com.sistematenda.estoque.service.AlertaEstoqueService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/estoque/alertas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertaEstoqueResource {

    @Inject
    AlertaEstoqueService service;

    @GET
    public PageResponse<AlertaEstoqueDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("200") int size,
            @QueryParam("tipo") String tipo,
            @QueryParam("status") String status,
            @QueryParam("produto") UUID produto) {
        return service.findAll(page, size, "dataAlerta", "desc", tipo, status, produto);
    }
}