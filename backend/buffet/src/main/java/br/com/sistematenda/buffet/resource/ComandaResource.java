package br.com.sistematenda.buffet.resource;

import br.com.sistematenda.buffet.dto.ComandaDTO;
import br.com.sistematenda.buffet.service.ComandaService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.StatusComanda;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/buffet/comandas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComandaResource {

    @Inject
    ComandaService service;

    @GET
    public PageResponse<ComandaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") StatusComanda status,
            @QueryParam("dataInicio") LocalDate dataInicio,
            @QueryParam("dataFim") LocalDate dataFim,
            @QueryParam("mesaId") UUID mesaId) {
        return service.findAll(page, size, sortBy, sortDirection, status, dataInicio, dataFim, mesaId);
    }

    @GET
    @Path("/abertas")
    public List<ComandaDTO> listarAbertas() {
        return service.findAbertas();
    }

    @GET
    @Path("/status/{status}")
    public List<ComandaDTO> listarPorStatus(@PathParam("status") StatusComanda status) {
        return service.findByStatus(status);
    }

    @GET
    @Path("/periodo")
    public List<ComandaDTO> listarPorPeriodo(@QueryParam("dataInicio") LocalDate dataInicio,
                                              @QueryParam("dataFim") LocalDate dataFim) {
        return service.findByPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/cliente")
    public List<ComandaDTO> buscarPorCliente(@QueryParam("nome") String nome) {
        return service.findByCliente(nome);
    }

    @GET
    @Path("/{id}")
    public ComandaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/numero/{numero}")
    public ComandaDTO buscarPorNumero(@PathParam("numero") String numero) {
        return service.findByNumero(numero);
    }

    @POST
    public Response criar(@Valid ComandaDTO dto) {
        ComandaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public ComandaDTO atualizar(@PathParam("id") UUID id, @Valid ComandaDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/{id}/fechar")
    public ComandaDTO fechar(@PathParam("id") UUID id) {
        return service.fecharComanda(id);
    }

    @POST
    @Path("/{id}/reabrir")
    public ComandaDTO reabrir(@PathParam("id") UUID id) {
        return service.reabrirComanda(id);
    }

    @POST
    @Path("/{id}/cancelar")
    public ComandaDTO cancelar(@PathParam("id") UUID id, @QueryParam("motivo") String motivo) {
        return service.cancelarComanda(id, motivo);
    }

    @PUT
    @Path("/{id}/desconto")
    public ComandaDTO aplicarDesconto(@PathParam("id") UUID id, @QueryParam("valor") BigDecimal valor) {
        return service.aplicarDesconto(id, valor);
    }

    @PUT
    @Path("/{id}/acrescimo")
    public ComandaDTO aplicarAcrescimo(@PathParam("id") UUID id, @QueryParam("valor") BigDecimal valor) {
        return service.aplicarAcrescimo(id, valor);
    }

    @GET
    @Path("/total-vendas")
    public BigDecimal getTotalVendas(@QueryParam("dataInicio") LocalDate dataInicio,
                                      @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalVendasPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/count-abertas")
    public long countAbertas() {
        return service.countAbertas();
    }
}