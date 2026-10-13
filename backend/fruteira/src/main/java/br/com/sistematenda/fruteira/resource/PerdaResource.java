package br.com.sistematenda.fruteira.resource;

import br.com.sistematenda.fruteira.dto.PerdaDTO;
import br.com.sistematenda.fruteira.entity.Perda;
import br.com.sistematenda.fruteira.service.PerdaService;
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

@Path("/fruteira/perdas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PerdaResource {

    @Inject
    PerdaService service;

    @GET
    public PageResponse<PerdaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("tipo") Perda.TipoPerda tipo,
            @QueryParam("dataInicio") LocalDate dataInicio,
            @QueryParam("dataFim") LocalDate dataFim) {
        return service.findAll(page, size, sortBy, sortDirection, tipo, dataInicio, dataFim);
    }

    @GET
    @Path("/produto/{produtoId}")
    public List<PerdaDTO> listarPorProduto(@PathParam("produtoId") UUID produtoId) {
        return service.findByProduto(produtoId);
    }

    @GET
    @Path("/tipo/{tipo}")
    public List<PerdaDTO> listarPorTipo(@PathParam("tipo") Perda.TipoPerda tipo) {
        return service.findByTipo(tipo);
    }

    @GET
    @Path("/periodo")
    public List<PerdaDTO> listarPorPeriodo(@QueryParam("dataInicio") LocalDate dataInicio,
                                            @QueryParam("dataFim") LocalDate dataFim) {
        return service.findByPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/pendentes-baixa")
    public List<PerdaDTO> listarPendentesBaixaEstoque() {
        return service.findPendentesBaixaEstoque();
    }

    @GET
    @Path("/total-periodo")
    public BigDecimal getTotalPeriodo(@QueryParam("dataInicio") LocalDate dataInicio,
                                       @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalPerdasPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/total-tipo-periodo")
    public BigDecimal getTotalTipoPeriodo(@QueryParam("tipo") Perda.TipoPerda tipo,
                                           @QueryParam("dataInicio") LocalDate dataInicio,
                                           @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalPerdasPorTipoPeriodo(tipo, dataInicio, dataFim);
    }

    @GET
    @Path("/{id}")
    public PerdaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    public Response criar(@Valid PerdaDTO dto) {
        PerdaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Path("/rapida")
    public Response registrarPerdaRapida(@Valid PerdaRapidaRequest request) {
        PerdaDTO criado = service.registrarPerdaRapida(
            request.produtoId, request.tipo, request.quantidade, request.motivo, request.responsavel
        );
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public PerdaDTO atualizar(@PathParam("id") UUID id, @Valid PerdaDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    public static class PerdaRapidaRequest {
        public UUID produtoId;
        public Perda.TipoPerda tipo;
        public BigDecimal quantidade;
        public String motivo;
        public String responsavel;
    }
}