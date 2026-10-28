package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.SessaoCaixaDTO;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.pdv.service.SessaoCaixaService;
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

@Path("/pdv/sessoes-caixa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessaoCaixaResource {

    @Inject
    SessaoCaixaService service;

    @GET
    public PageResponse<SessaoCaixaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("pdvId") UUID pdvId,
            @QueryParam("status") SessaoCaixa.StatusSessao status,
            @QueryParam("dataInicio") LocalDate dataInicio,
            @QueryParam("dataFim") LocalDate dataFim) {
        return service.findAll(page, size, sortBy, sortDirection, pdvId, status, dataInicio, dataFim);
    }

    @GET
    @Path("/pdv/{pdvId}/aberta")
    public SessaoCaixaDTO buscarAbertaPorPdv(@PathParam("pdvId") UUID pdvId) {
        return service.findAbertaByPdv(pdvId);
    }

    @GET
    @Path("/pdv/{pdvId}")
    public List<SessaoCaixaDTO> listarPorPdv(@PathParam("pdvId") UUID pdvId) {
        return service.findByPdv(pdvId);
    }

    @GET
    @Path("/periodo")
    public List<SessaoCaixaDTO> listarPorPeriodo(@QueryParam("dataInicio") LocalDate dataInicio,
                                                 @QueryParam("dataFim") LocalDate dataFim) {
        return service.findByPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/{id}")
    public SessaoCaixaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @POST
    @Path("/abrir")
    public Response abrirCaixa(@Valid AbrirCaixaRequest request) {
        SessaoCaixaDTO aberta = service.abrirCaixa(request.pdvId, request.operador, request.valorInicial);
        return Response.status(Response.Status.CREATED).entity(aberta).build();
    }

    @POST
    @Path("/abrir-usuario")
    public Response abrirCaixaUsuario(@Valid AbrirCaixaRequest request) {
        SessaoCaixaDTO sessao = service.abrirCaixaPorUsuario(request.operador, request.pdvId, request.valorInicial);
        return Response.ok(sessao).build();
    }

    @POST
    @Path("/abrir-automatico")
    public Response abrirCaixaAutomatico(@Valid AbrirCaixaRequest request) {
        SessaoCaixaDTO sessao = service.abrirCaixaAutomatico(request.pdvId, request.operador, request.valorInicial);
        return Response.ok(sessao).build();
    }

    @GET
    @Path("/validar-vinculo/{sessaoId}/{pdvId}")
    public Response validarVinculo(@PathParam("sessaoId") UUID sessaoId, @PathParam("pdvId") UUID pdvId) {
        boolean vinculado = service.validarVinculoPdv(sessaoId, pdvId);
        return Response.ok(new VinculoResponse(vinculado)).build();
    }

    public static class VinculoResponse {
        public boolean vinculado;
        public VinculoResponse(boolean vinculado) { this.vinculado = vinculado; }
    }

    @POST
    @Path("/{id}/fechar")
    public SessaoCaixaDTO fecharCaixa(@PathParam("id") UUID id, @Valid FecharCaixaRequest request) {
        return service.fecharCaixa(id, request.operador, request.valorFinalContado);
    }

    @POST
    @Path("/{id}/sangria")
    public SessaoCaixaDTO sangria(@PathParam("id") UUID id, @Valid MovimentoRequest request) {
        return service.sangria(id, request.valor, request.descricao, request.operador);
    }

    @POST
    @Path("/{id}/reforco")
    public SessaoCaixaDTO reforco(@PathParam("id") UUID id, @Valid MovimentoRequest request) {
        return service.reforco(id, request.valor, request.descricao, request.operador);
    }

    @GET
    @Path("/total-vendas")
    public BigDecimal getTotalVendas(@QueryParam("dataInicio") LocalDate dataInicio,
                                      @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalVendasPeriodo(dataInicio, dataFim);
    }

    public static class AbrirCaixaRequest {
        public UUID pdvId;
        public String operador;
        public BigDecimal valorInicial;
    }

    public static class FecharCaixaRequest {
        public String operador;
        public BigDecimal valorFinalContado;
    }

    public static class MovimentoRequest {
        public BigDecimal valor;
        public String descricao;
        public String operador;
    }
}