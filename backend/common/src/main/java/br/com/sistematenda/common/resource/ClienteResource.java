package br.com.sistematenda.common.resource;

import br.com.sistematenda.common.dto.ClienteDTO;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.TipoPessoa;
import br.com.sistematenda.common.service.ClienteService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes")
public class ClienteResource {

    @Inject
    ClienteService service;

    @GET
    @Operation(summary = "Lista clientes paginados")
    public PageResponse<ClienteDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("tipo") TipoPessoa tipo,
            @QueryParam("nome") String nome,
            @QueryParam("ativo") Boolean ativo) {
        return service.findAll(page, size, sortBy, sortDirection, tipo, nome, ativo);
    }

    @GET
    @Path("/ativos")
    @Operation(summary = "Lista clientes ativos")
    public List<ClienteDTO> listarAtivos() {
        return service.findAtivos();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca clientes por nome")
    public List<ClienteDTO> buscar(@QueryParam("q") String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return service.search(q);
    }

    @GET
    @Path("/documento/{cpfCnpj}")
    @Operation(summary = "Busca cliente por CPF/CNPJ")
    public ClienteDTO buscarPorDocumento(@PathParam("cpfCnpj") String cpfCnpj) {
        return service.findByCpfCnpj(cpfCnpj);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca cliente por ID")
    public ClienteDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Operation(summary = "Cadastra um cliente")
    public Response criar(@Valid ClienteDTO dto) {
        ClienteDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza um cliente")
    public ClienteDTO atualizar(@PathParam("id") UUID id, @Valid ClienteDTO dto) {
        return service.update(id, dto);
    }

    @POST
    @Path("/{id}/status")
    @Operation(summary = "Alterna o status (ativo/inativo) do cliente")
    public void alternarStatus(@PathParam("id") UUID id) {
        service.toggleStatus(id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um cliente")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }
}
