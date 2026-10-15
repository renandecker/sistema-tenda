package br.com.sistematenda.estoque.resource;

import br.com.sistematenda.estoque.dto.MovimentoEstoqueDTO;
import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.estoque.entity.MovimentoEstoque.TipoMovimento;
import br.com.sistematenda.estoque.entity.MovimentoEstoque.OrigemDestino;
import br.com.sistematenda.estoque.service.MovimentoEstoqueService;
import br.com.sistematenda.common.dto.PageResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/estoque/movimentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovimentoEstoqueResource {

    @Inject
    MovimentoEstoqueService service;

    @GET
    public PageResponse<MovimentoEstoqueDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("tipo") TipoMovimento tipo,
            @QueryParam("origem") OrigemDestino origem,
            @QueryParam("destino") OrigemDestino destino,
            @QueryParam("produto") UUID produto,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.findAll(page, size, sortBy, sortDirection, tipo, origem, destino, produto, dataInicio, dataFim);
    }

    @GET
    @Path("/estoque-baixo")
    public PageResponse<MovimentoEstoqueDTO> listarEstoqueBaixo(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return service.findEstoqueBaixo(page, size);
    }

    @GET
    @Path("/entradas")
    public PageResponse<MovimentoEstoqueDTO> listarEntradas(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.findEntradas(page, size, dataInicio, dataFim);
    }

    @GET
    @Path("/saidas")
    public PageResponse<MovimentoEstoqueDTO> listarSaidas(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.findSaidas(page, size, dataInicio, dataFim);
    }

    @GET
    @Path("/defeitos")
    public PageResponse<MovimentoEstoqueDTO> listarDefeitos(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        return service.findDefeitos(page, size, dataInicio, dataFim);
    }

    @POST
    @Transactional
    public Response criar(@Valid MovimentoEstoqueDTO dto) {
        MovimentoEstoqueDTO created = service.createMovement(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/entrada-compra")
    @Transactional
    public Response registrarEntradaCompra(@Valid MovimentoEstoqueDTO dto) {
        MovimentoEstoqueDTO created = service.registrarEntradaCompra(
                dto.getProdutoFruteiraId(),
                dto.getProdutoBuffetId(),
                dto.getQuantidade(),
                dto.getCustoUnitario(),
                dto.getDocumentoReferencia(),
                dto.getNumeroDocumento(),
                dto.getLote(),
                dto.getDataValidade(),
                dto.getResponsavel()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/entrada-transferencia")
    @Transactional
    public Response registrarEntradaTransferencia(@Valid MovimentoEstoqueDTO dto) {
        MovimentoEstoqueDTO created = service.registrarEntradaTransferencia(
                dto.getProdutoFruteiraId(),
                dto.getProdutoBuffetId(),
                dto.getQuantidade(),
                dto.getCustoUnitario(),
                dto.getOrigem(),
                dto.getResponsavel()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/saida-venda")
    @Transactional
    public Response registrarSaidaVenda(@Valid MovimentoEstoqueDTO dto) {
        MovimentoEstoqueDTO created = service.registrarSaidaVenda(
                dto.getProdutoFruteiraId(),
                dto.getProdutoBuffetId(),
                dto.getQuantidade(),
                dto.getCustoUnitario(),
                dto.getResponsavel()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/ajuste")
    @Transactional
    public Response registrarAjuste(@Valid MovimentoEstoqueDTO dto) {
        MovimentoEstoqueDTO created = service.registrarAjuste(
                dto.getProdutoFruteiraId(),
                dto.getProdutoBuffetId(),
                dto.getQuantidade(),
                dto.getCustoUnitario(),
                dto.getTipo() == TipoMovimento.ENTRADA_AJUSTE,
                dto.getObservacoes(),
                dto.getResponsavel()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/defeito")
    @Transactional
    public Response registrarDefeito(@Valid MovimentoEstoqueDTO dto) {
        MovimentoEstoqueDTO created = service.registrarDefeito(
                dto.getProdutoFruteiraId(),
                dto.getProdutoBuffetId(),
                dto.getTipo(),
                dto.getQuantidade(),
                dto.getCustoUnitario(),
                dto.getMotivoDefeito(),
                dto.getTipoDefeito(),
                dto.getLoteDefeituoso(),
                dto.getResponsavel(),
                dto.getQuantidadeRecuperavel()
        );
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}