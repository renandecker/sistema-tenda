package br.com.sistematenda.fiscal.resource;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.fiscal.dto.ConciliacaoCartaoDTO;
import br.com.sistematenda.fiscal.entity.ConciliacaoCartao;
import br.com.sistematenda.fiscal.mapper.ConciliacaoCartaoMapper;
import br.com.sistematenda.fiscal.repository.ConciliacaoCartaoRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Path("/fiscal/conciliacao-cartao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConciliacaoCartaoResource {

    @Inject
    ConciliacaoCartaoRepository repository;

    @Inject
    ConciliacaoCartaoMapper mapper;

    @GET
    public PageResponse<ConciliacaoCartaoDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") ConciliacaoCartao.StatusConciliacao status,
            @QueryParam("adquirente") String adquirente,
            @QueryParam("bandeira") String bandeira,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        PageResponse<ConciliacaoCartao> pageResult = repository.findPaginated(page, size, sortBy, sortDirection, status, adquirente, bandeira, dataInicio, dataFim);
        return PageResponse.of(
                pageResult.getContent().stream().map(mapper::toDTO).toList(),
                pageResult.getTotalElements(),
                pageResult.getCurrentPage(),
                pageResult.getPageSize());
    }

    @GET
    @Path("/{id}")
    public ConciliacaoCartaoDTO buscarPorId(@PathParam("id") UUID id) {
        ConciliacaoCartao conciliacao = repository.findById(id);
        if (conciliacao == null || conciliacao.deleted) {
            throw new NotFoundException("ConciliacaoCartao", id);
        }
        return mapper.toDTO(conciliacao);
    }

    @GET
    @Path("/nsu/{nsu}")
    public ConciliacaoCartaoDTO buscarPorNsu(@PathParam("nsu") String nsu) {
        ConciliacaoCartao conciliacao = repository.findByNsu(nsu);
        if (conciliacao == null || conciliacao.deleted) {
            throw new NotFoundException("ConciliacaoCartao", nsu);
        }
        return mapper.toDTO(conciliacao);
    }

    @POST
    public Response criar(ConciliacaoCartao conciliacao) {
        if (conciliacao.id != null) {
            conciliacao.id = null;
        }
        conciliacao.persist();
        return Response.status(Response.Status.CREATED).entity(mapper.toDTO(conciliacao)).build();
    }

    @PUT
    @Path("/{id}")
    public ConciliacaoCartaoDTO atualizar(@PathParam("id") UUID id, ConciliacaoCartao conciliacaoAtualizada) {
        ConciliacaoCartao conciliacao = repository.findById(id);
        if (conciliacao == null || conciliacao.deleted) {
            throw new NotFoundException("ConciliacaoCartao", id);
        }

        conciliacao.dataVenda = conciliacaoAtualizada.dataVenda;
        conciliacao.dataRecebimento = conciliacaoAtualizada.dataRecebimento;
        conciliacao.adquirente = conciliacaoAtualizada.adquirente;
        conciliacao.bandeira = conciliacaoAtualizada.bandeira;
        conciliacao.nsu = conciliacaoAtualizada.nsu;
        conciliacao.codigoAutorizacao = conciliacaoAtualizada.codigoAutorizacao;
        conciliacao.valorBruto = conciliacaoAtualizada.valorBruto;
        conciliacao.taxaAdesao = conciliacaoAtualizada.taxaAdesao;
        conciliacao.taxaMdr = conciliacaoAtualizada.taxaMdr;
        conciliacao.valorTaxa = conciliacaoAtualizada.valorTaxa;
        conciliacao.valorLiquido = conciliacaoAtualizada.valorLiquido;
        conciliacao.valorRecebido = conciliacaoAtualizada.valorRecebido;
        conciliacao.status = conciliacaoAtualizada.status;
        conciliacao.vendaId = conciliacaoAtualizada.vendaId;
        conciliacao.numeroVenda = conciliacaoAtualizada.numeroVenda;
        conciliacao.observacoes = conciliacaoAtualizada.observacoes;

        conciliacao.persist();
        return mapper.toDTO(conciliacao);
    }

    @POST
    @Path("/{id}/conciliar")
    public ConciliacaoCartaoDTO conciliar(@PathParam("id") UUID id,
                                          @QueryParam("conciliadoPor") String conciliadoPor,
                                          @QueryParam("dataConciliacao") String dataConciliacao) {
        ConciliacaoCartao conciliacao = repository.findById(id);
        if (conciliacao == null || conciliacao.deleted) {
            throw new NotFoundException("ConciliacaoCartao", id);
        }

        conciliacao.status = ConciliacaoCartao.StatusConciliacao.CONCILIADO;
        conciliacao.conciliadoPor = conciliadoPor;
        conciliacao.dataConciliacao = dataConciliacao != null ? LocalDateTime.parse(dataConciliacao) : LocalDateTime.now();

        conciliacao.persist();
        return mapper.toDTO(conciliacao);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        ConciliacaoCartao conciliacao = repository.findById(id);
        if (conciliacao == null || conciliacao.deleted) {
            throw new NotFoundException("ConciliacaoCartao", id);
        }
        conciliacao.deleted = true;
        conciliacao.persist();
        return Response.noContent().build();
    }
}