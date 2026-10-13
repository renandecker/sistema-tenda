package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.TransferenciaEstoqueDTO;
import br.com.sistematenda.buffet.entity.TransferenciaEstoque;
import br.com.sistematenda.buffet.service.TransferenciaEstoqueService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Path("/buffet/transferencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransferenciaEstoqueResource {

    @Inject
    TransferenciaEstoqueService service;

    @GET
    public PageResponse<TransferenciaEstoqueDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") TransferenciaEstoque.StatusTransferencia status,
            @QueryParam("produtoId") UUID produtoId,
            @QueryParam("inicio") LocalDateTime inicio,
            @QueryParam("fim") LocalDateTime fim) {
        return service.findAll(page, size, sortBy, sortDirection, status, produtoId, inicio, fim);
    }

    @GET
    @Path("/pendentes")
    public List<TransferenciaEstoqueDTO> listarPendentes() {
        return service.findPendentes();
    }

    @GET
    @Path("/status/{status}")
    public List<TransferenciaEstoqueDTO> listarPorStatus(@PathParam("status") TransferenciaEstoque.StatusTransferencia status) {
        return service.findByStatus(status);
    }

    @GET
    @Path("/produto/{produtoId}")
    public List<TransferenciaEstoqueDTO> listarPorProduto(@PathParam("produtoId") UUID produtoId) {
        return service.findByProduto(produtoId);
    }

    @GET
    @Path("/periodo")
    public List<TransferenciaEstoqueDTO> listarPorPeriodo(@QueryParam("inicio") LocalDateTime inicio,
                                                           @QueryParam("fim") LocalDateTime fim) {
        return service.findByPeriodo(inicio, fim);
    }

    @GET
    @Path("/{id}")
    public TransferenciaEstoqueDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid TransferenciaEstoqueDTO dto) {
        TransferenciaEstoqueDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Path("/{id}/aprovar")
    public TransferenciaEstoqueDTO aprovar(@PathParam("id") UUID id, @QueryParam("aprovadoPor") String aprovadoPor) {
        return service.aprovar(id, aprovadoPor);
    }

    @POST
    @Path("/{id}/rejeitar")
    public TransferenciaEstoqueDTO rejeitar(@PathParam("id") UUID id, @QueryParam("motivo") String motivo,
                                             @QueryParam("rejeitadoPor") String rejeitadoPor) {
        return service.rejeitar(id, motivo, rejeitadoPor);
    }

    @POST
    @Path("/{id}/concluir")
    public TransferenciaEstoqueDTO concluir(@PathParam("id") UUID id, @QueryParam("recebidoPor") String recebidoPor) {
        return service.concluir(id, recebidoPor);
    }

    @POST
    @Path("/{id}/cancelar")
    public TransferenciaEstoqueDTO cancelar(@PathParam("id") UUID id, @QueryParam("motivo") String motivo) {
        return service.cancelar(id, motivo);
    }

    @GET
    @Path("/total-periodo")
    public BigDecimal getTotalPeriodo(@QueryParam("inicio") LocalDateTime inicio,
                                       @QueryParam("fim") LocalDateTime fim) {
        return service.getTotalTransferidoPeriodo(inicio, fim);
    }
}