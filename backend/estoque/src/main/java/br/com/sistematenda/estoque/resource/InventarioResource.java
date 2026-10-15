package br.com.sistematenda.estoque.resource;

import br.com.sistematenda.estoque.dto.InventarioDTO;
import br.com.sistematenda.estoque.entity.Inventario;
import br.com.sistematenda.estoque.service.InventarioService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/estoque/inventarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventarioResource {

    @Inject
    InventarioService service;

    @GET
    public PageResponse<InventarioDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size,
            @QueryParam("status") String status,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.findAll(page, size, "dataInicio", "desc", status, dataInicio, dataFim);
    }
}