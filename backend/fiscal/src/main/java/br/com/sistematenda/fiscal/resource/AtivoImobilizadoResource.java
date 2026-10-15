package br.com.sistematenda.fiscal.resource;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.fiscal.dto.AtivoImobilizadoDTO;
import br.com.sistematenda.fiscal.entity.AtivoImobilizado;
import br.com.sistematenda.fiscal.mapper.AtivoImobilizadoMapper;
import br.com.sistematenda.fiscal.repository.AtivoImobilizadoRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Path("/fiscal/ativos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AtivoImobilizadoResource {

    @Inject
    AtivoImobilizadoRepository repository;

    @Inject
    AtivoImobilizadoMapper mapper;

    @GET
    public PageResponse<AtivoImobilizadoDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") AtivoImobilizado.StatusAtivo status,
            @QueryParam("tipo") AtivoImobilizado.TipoAtivo tipo,
            @QueryParam("unidadeNegocio") AtivoImobilizado.UnidadeNegocio unidadeNegocio,
            @QueryParam("busca") String busca) {
        PageResponse<AtivoImobilizado> pageResult = repository.findPaginated(page, size, sortBy, sortDirection, status, tipo, unidadeNegocio, busca);
        return PageResponse.of(
                pageResult.getContent().stream().map(mapper::toDTO).toList(),
                pageResult.getTotalElements(),
                pageResult.getCurrentPage(),
                pageResult.getPageSize());
    }

    @GET
    @Path("/{id}")
    public AtivoImobilizadoDTO buscarPorId(@PathParam("id") UUID id) {
        AtivoImobilizado ativo = repository.findById(id);
        if (ativo == null || ativo.deleted) {
            throw new NotFoundException("AtivoImobilizado", id);
        }
        return mapper.toDTO(ativo);
    }

    @GET
    @Path("/codigo/{codigo}")
    public AtivoImobilizadoDTO buscarPorCodigo(@PathParam("codigo") String codigo) {
        AtivoImobilizado ativo = repository.findByCodigo(codigo);
        if (ativo == null || ativo.deleted) {
            throw new NotFoundException("AtivoImobilizado", codigo);
        }
        return mapper.toDTO(ativo);
    }

    @POST
    public Response criar(AtivoImobilizado ativo) {
        if (ativo.id != null) {
            ativo.id = null;
        }
        ativo.persist();
        return Response.status(Response.Status.CREATED).entity(mapper.toDTO(ativo)).build();
    }

    @PUT
    @Path("/{id}")
    public AtivoImobilizadoDTO atualizar(@PathParam("id") UUID id, AtivoImobilizado ativoAtualizado) {
        AtivoImobilizado ativo = repository.findById(id);
        if (ativo == null || ativo.deleted) {
            throw new NotFoundException("AtivoImobilizado", id);
        }

        ativo.codigo = ativoAtualizado.codigo;
        ativo.descricao = ativoAtualizado.descricao;
        ativo.tipo = ativoAtualizado.tipo;
        ativo.unidadeNegocio = ativoAtualizado.unidadeNegocio;
        ativo.status = ativoAtualizado.status;
        ativo.dataAquisicao = ativoAtualizado.dataAquisicao;
        ativo.valorAquisicao = ativoAtualizado.valorAquisicao;
        ativo.vidaUtilMeses = ativoAtualizado.vidaUtilMeses;
        ativo.metodoDepreciacao = ativoAtualizado.metodoDepreciacao;
        ativo.taxaDepreciacaoAnual = ativoAtualizado.taxaDepreciacaoAnual;
        ativo.valorResidual = ativoAtualizado.valorResidual;
        ativo.fornecedor = ativoAtualizado.fornecedor;
        ativo.notaFiscal = ativoAtualizado.notaFiscal;
        ativo.localizacao = ativoAtualizado.localizacao;
        ativo.responsavel = ativoAtualizado.responsavel;
        ativo.observacoes = ativoAtualizado.observacoes;

        ativo.persist();
        return mapper.toDTO(ativo);
    }

    @POST
    @Path("/{id}/baixar")
    public AtivoImobilizadoDTO baixar(@PathParam("id") UUID id,
                                      @QueryParam("dataBaixa") String dataBaixa,
                                      @QueryParam("motivo") String motivo,
                                      @QueryParam("valorBaixa") String valorBaixa) {
        AtivoImobilizado ativo = repository.findById(id);
        if (ativo == null || ativo.deleted) {
            throw new NotFoundException("AtivoImobilizado", id);
        }

        ativo.status = AtivoImobilizado.StatusAtivo.BAIXADO;
        ativo.dataBaixa = dataBaixa != null ? LocalDate.parse(dataBaixa) : LocalDate.now();
        ativo.motivoBaixa = motivo;
        if (valorBaixa != null && !valorBaixa.isEmpty()) {
            ativo.valorBaixa = new BigDecimal(valorBaixa);
        }

        ativo.persist();
        return mapper.toDTO(ativo);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") UUID id) {
        AtivoImobilizado ativo = repository.findById(id);
        if (ativo == null || ativo.deleted) {
            throw new NotFoundException("AtivoImobilizado", id);
        }
        ativo.deleted = true;
        ativo.persist();
        return Response.noContent().build();
    }
}