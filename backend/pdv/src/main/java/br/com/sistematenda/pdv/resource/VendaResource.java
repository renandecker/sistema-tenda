package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.VendaDTO;
import br.com.sistematenda.pdv.service.VendaService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.StatusVenda;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/pdv/vendas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendaResource {

    @Inject
    VendaService service;

    @GET
    public PageResponse<VendaDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") StatusVenda status,
            @QueryParam("dataInicio") LocalDate dataInicio,
            @QueryParam("dataFim") LocalDate dataFim,
            @QueryParam("pdvId") UUID pdvId,
            @QueryParam("clienteDocumento") String clienteDocumento) {
        return service.findAll(page, size, sortBy, sortDirection, status, dataInicio, dataFim, pdvId, clienteDocumento);
    }

    @GET
    @Path("/{id}")
    public VendaDTO buscarPorId(@PathParam("id") UUID id) {
        return service.findById(id);
    }

    @GET
    @Path("/numero/{numero}")
    public VendaDTO buscarPorNumero(@PathParam("numero") String numero) {
        return service.findByNumero(numero);
    }

    @GET
    @Path("/comanda/{comandaId}")
    public VendaDTO buscarPorComanda(@PathParam("comandaId") UUID comandaId) {
        return service.findByComanda(comandaId);
    }

    @POST
    public Response criar(@Valid VendaDTO dto) {
        VendaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    public VendaDTO atualizar(@PathParam("id") UUID id, @Valid VendaDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.delete(id);
    }

    @POST
    @Path("/{id}/item-fruteira")
    public VendaDTO adicionarItemFruteira(@PathParam("id") UUID id, 
                                           @QueryParam("produtoId") UUID produtoId,
                                           @QueryParam("quantidade") BigDecimal quantidade) {
        return service.adicionarItemFruteira(id, produtoId, quantidade);
    }

    @POST
    @Path("/{id}/item-buffet")
    public VendaDTO adicionarItemBuffet(@PathParam("id") UUID id, 
                                         @QueryParam("produtoId") UUID produtoId,
                                         @QueryParam("quantidade") BigDecimal quantidade) {
        return service.adicionarItemBuffet(id, produtoId, quantidade);
    }

    @POST
    @Path("/{id}/prato-balanca")
    public VendaDTO adicionarPratoBalanca(@PathParam("id") UUID id, 
                                           @QueryParam("pesoBruto") BigDecimal pesoBruto,
                                           @QueryParam("tara") BigDecimal tara) {
        return service.adicionarPratoBalanca(id, pesoBruto, tara);
    }

    @POST
    @Path("/{id}/item-comanda")
    public VendaDTO adicionarItemComanda(@PathParam("id") UUID id, 
                                          @QueryParam("comandaId") UUID comandaId) {
        return service.adicionarItemComanda(id, comandaId);
    }

    @PUT
    @Path("/{id}/desconto")
    public VendaDTO aplicarDesconto(@PathParam("id") UUID id, @QueryParam("valor") BigDecimal valor) {
        return service.aplicarDesconto(id, valor);
    }

    @PUT
    @Path("/{id}/acrescimo")
    public VendaDTO aplicarAcrescimo(@PathParam("id") UUID id, @QueryParam("valor") BigDecimal valor) {
        return service.aplicarAcrescimo(id, valor);
    }

    @POST
    @Path("/{id}/pagamento")
    public VendaDTO processarPagamento(@PathParam("id") UUID id, PagamentoRequest request) {
        return service.processarPagamento(id, request);
    }

    @POST
    @Path("/{id}/finalizar")
    public VendaDTO finalizar(@PathParam("id") UUID id) {
        return service.finalizarVenda(id);
    }

    @POST
    @Path("/{id}/cancelar")
    public VendaDTO cancelar(@PathParam("id") UUID id, @QueryParam("motivo") String motivo) {
        return service.cancelarVenda(id, motivo);
    }

    @GET
    @Path("/total-vendas")
    public BigDecimal getTotalVendas(@QueryParam("dataInicio") LocalDate dataInicio,
                                      @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalVendasPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/total-fruteira")
    public BigDecimal getTotalFruteira(@QueryParam("dataInicio") LocalDate dataInicio,
                                        @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalFruteiraPeriodo(dataInicio, dataFim);
    }

    @GET
    @Path("/total-buffet")
    public BigDecimal getTotalBuffet(@QueryParam("dataInicio") LocalDate dataInicio,
                                      @QueryParam("dataFim") LocalDate dataFim) {
        return service.getTotalBuffetPeriodo(dataInicio, dataFim);
    }

    public static class PagamentoRequest {
        public br.com.sistematenda.common.enums.FormaPagamento formaPagamento;
        public BigDecimal valor;
        public Integer parcelas;
        public String nsu;
        public String codigoAutorizacao;
        public String bandeiraCartao;
        public String ultimosDigitosCartao;
        public String pixTxId;
        public String pixQrCode;
        public String valeNumero;
        public java.time.LocalDateTime valeValidade;
    }
}