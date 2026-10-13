package br.com.sistematenda.fiscal.resource;

import br.com.sistematenda.fiscal.dto.CompraDTO;
import br.com.sistematenda.fiscal.service.CompraService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/fiscal/compras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompraResource {

    @Inject
    CompraService service;

    @GET
    public PageResponse<CompraDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") br.com.sistematenda.fiscal.dto.CompraDTO.StatusCompra status,
            @QueryParam("dataInicio") LocalDate dataInicio,
            @QueryParam("dataFim") LocalDate dataFim,
            @QueryParam("fornecedorId") UUID fornecedorId) {
        return service.findAll(page, size, sortBy, sortDirection, status, dataInicio, dataFim, fornecedorId);
    }

    @GET
    @Path("/fornecedor/{fornecedorId}")
    public List<CompraDTO> listarPorFornecedor(@PathParam("fornecedorId") UUID fornecedorId) {
        return service.findByFornecedor(fornecedorId);
    }

    @GET
    @Path("/periodo")
    public List<CompraDTO> listarPorPeriodo(@QueryParam("dataInicio") LocalDate dataInicio,
                                           @QueryParam("dataFim") LocalDate dataFim) {
        return service.findByPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/total-compras")
    public BigDecimal getTotalCompras(@QueryParam("dataInicio") LocalDate dataInicio,
                                      @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalComprasPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/total-ceasa")
    public BigDecimal getTotalCeasa(@QueryParam("dataInicio") LocalDate dataInicio,
                                     @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalCeasaPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/{id}")
    public CompraDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid CompraDTO dto) {
        CompraDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public CompraDTO atualizar(@PathParam("id") UUID id, @Valid CompraDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/{id}/receber")
    public CompraDTO marcarRecebida(@PathParam("id") UUID id) {
        CompraDTO compra = service.findById(id);
        compra.setStatus(br.com.sistematenda.fiscal.dto.CompraDTO.StatusCompra.RECEBIDA);
        // TODO: Atualizar estoque e contabilizar
        return service.update(id, compra);
    }

    @POST
    @Path("/{id}/cancelar")
    public CompraDTO cancelar(@PathParam("id") UUID id, @QueryParam("motivo") String motivo) {
        CompraDTO compra = service.findById(id);
        compra.setStatus(br.com.sistematenda.fiscal.dto.CompraDTO.StatusCompra.CANCELADA);
        compra.setObservacoes((compra.getObservacoes() != null ? compra.getObservacoes() + "\n" : "") + "CANCELADA: " + motivo);
        return service.update(id, compra);
    }

    public static class StatusRequest {
        public String status;
    }
}