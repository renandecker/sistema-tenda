package br.com.sistematenda.pdv.resource;

import br.com.sistematenda.pdv.dto.CadernoClienteResumoDTO;
import br.com.sistematenda.pdv.dto.CadernoDividaDTO;
import br.com.sistematenda.pdv.dto.CadernoTotalDTO;
import br.com.sistematenda.pdv.dto.LancamentoCadernoRequest;
import br.com.sistematenda.pdv.dto.SituacaoClienteDTO;
import br.com.sistematenda.pdv.service.CadernoDividaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/pdv/caderno")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Caderno de Dívidas", description = "Registro de dívidas (fiado) dos clientes no PDV")
public class CadernoResource {

    @Inject
    CadernoDividaService service;

    @GET
    @Operation(summary = "Lista lançamentos do caderno",
        description = "Lista as dívidas registradas, opcionalmente filtrando por cliente/venda e somente em aberto")
    public List<CadernoDividaDTO> listar(
            @QueryParam("q") String q,
            @QueryParam("emAberto") @DefaultValue("true") boolean emAberto) {
        return service.listarTodos(q, emAberto);
    }

    @GET
    @Path("/clientes")
    @Operation(summary = "Lista clientes com dívida em aberto",
        description = "Agrupa os lançamentos em aberto por cliente, retornando o total devido")
    public List<CadernoClienteResumoDTO> listarClientesComDivida() {
        return service.listarClientesComDivida();
    }

    @GET
    @Path("/totais")
    @Operation(summary = "Totais do caderno", description = "Totais em aberto, recebidos e em atraso")
    public CadernoTotalDTO totais() {
        return service.totais();
    }

    @GET
    @Path("/clientes/{clienteId}")
    @Operation(summary = "Situação de dívida de um cliente")
    public SituacaoClienteDTO situacaoCliente(@PathParam("clienteId") UUID clienteId) {
        return service.situacaoCliente(clienteId);
    }

    @GET
    @Path("/documento/{documento}")
    @Operation(summary = "Situação de dívida por CPF/CNPJ")
    public SituacaoClienteDTO situacaoPorDocumento(@PathParam("documento") String documento) {
        return service.situacaoPorDocumento(documento);
    }

    @GET
    @Path("/{id}")
    public CadernoDividaDTO buscar(@PathParam("id") UUID id) {
        return service.buscarDTO(id);
    }

    @POST
    @Operation(summary = "Lança uma dívida no caderno (fiado)")
    public Response lancar(@Valid LancamentoCadernoRequest request) {
        CadernoDividaDTO criado = service.lancar(request);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Path("/{id}/pagamento")
    @Operation(summary = "Registra o pagamento (total ou parcial) de uma dívida")
    public CadernoDividaDTO registrarPagamento(@PathParam("id") UUID id,
                                               @QueryParam("valor") BigDecimal valor) {
        return service.registrarPagamento(id, valor);
    }

    @POST
    @Path("/{id}/cancelar")
    @Operation(summary = "Cancela um lançamento do caderno")
    public CadernoDividaDTO cancelar(@PathParam("id") UUID id,
                                     @QueryParam("motivo") String motivo) {
        return service.cancelar(id, motivo);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um lançamento do caderno")
    public void excluir(@PathParam("id") UUID id) {
        service.excluir(id);
    }
}
