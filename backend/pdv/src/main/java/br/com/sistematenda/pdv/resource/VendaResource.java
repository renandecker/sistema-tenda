package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.common.pix.PixChargeRequest;
import br.com.sistematenda.common.pix.PixChargeResponse;
import br.com.sistematenda.pdv.dto.VendaDTO;
import br.com.sistematenda.pdv.service.VendaService;
import br.com.sistematenda.pdv.pix.PixService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.StatusVenda;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
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
public class VendaResource {

    private static final Logger LOG = Logger.getLogger(VendaResource.class);

    @Inject
    VendaService service;

    @Inject
    PixService pixService;

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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response criar(@Valid VendaDTO dto) {
        VendaDTO criado = service.create(dto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
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
    @Consumes(MediaType.APPLICATION_JSON)
    public VendaDTO processarPagamento(@PathParam("id") UUID id, VendaService.PagamentoRequest request) {
        return service.processarPagamento(id, request);
    }

    @POST
    @Path("/{id}/pix/gerar")
    public PixChargeResponse gerarPix(@PathParam("id") UUID id, 
                                       @QueryParam("valor") BigDecimal valor,
                                       @QueryParam("txid") String txid) {
        return pixService.gerarPixParaVenda(id, valor, txid);
    }

    @GET
    @Path("/{id}/pix/status")
    public PixChargeResponse consultarPix(@PathParam("id") UUID id,
                                           @QueryParam("txid") String txid) {
        return pixService.consultarStatus(txid);
    }

    @POST
    @Path("/{id}/finalizar")
    public VendaDTO finalizar(@PathParam("id") UUID id,
                              @QueryParam("cpfConsumidor") String cpfConsumidor,
                              @QueryParam("emitirNfce") @DefaultValue("false") boolean emitirNfce) {
        VendaDTO venda = service.finalizarVenda(id, cpfConsumidor);
        if (emitirNfce) {
            try {
                venda = service.emitirNfceVenda(id, cpfConsumidor);
            } catch (Exception e) {
                LOG.warnf(e, "Falha ao emitir NFC-e da venda %s", id);
                venda.setNfceErro(e.getMessage() != null ? e.getMessage() : "Falha ao emitir NFC-e");
            }
        }
        return venda;
    }

    @POST
    @Path("/{id}/nfce")
    public VendaDTO emitirNfce(@PathParam("id") UUID id,
                               @QueryParam("cpfConsumidor") String cpfConsumidor) {
        return service.emitirNfceVenda(id, cpfConsumidor);
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

}