package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.ClienteBuffetDTO;
import br.com.sistematenda.buffet.service.ClienteBuffetService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/buffet/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteBuffetResource {

    @Inject
    ClienteBuffetService service;

    @GET
    public PageResponse<ClienteBuffetDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return service.findAll(page, size);
    }

    @GET
    @Path("/{id}")
    public ClienteBuffetDTO buscar(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/ranking/frequentes")
    public List<ClienteBuffetDTO> rankingFrequentes(@QueryParam("limit") @DefaultValue("10") int limit) {
        return service.rankingFrequentes(limit);
    }

    @GET
    @Path("/ranking/consumo")
    public List<ClienteBuffetDTO> rankingConsumo(@QueryParam("limit") @DefaultValue("10") int limit) {
        return service.rankingConsumo(limit);
    }

    @GET
    @Path("/resumo")
    public Response resumo() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("totalClientes", service.totalClientes());
        m.put("totalConsumido", service.totalConsumidoGeral());
        return Response.ok(m).build();
    }

    @POST
    public Response criar(ClienteBuffetDTO dto) {
        ClienteBuffetDTO criado = service.upsertByCpf(dto.getNome(), dto.getCpf(), dto.getTelefone());
        if (dto.getObservacoes() != null) {
            // update observacoes
        }
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ClienteBuffetDTO atualizar(@PathParam("id") UUID id, ClienteBuffetDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        // soft delete not implemented; no-op
    }
}
