package br.com.sistematenda.fiscal.resource;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.fiscal.dto.DreDTO;
import br.com.sistematenda.fiscal.entity.Dre;
import br.com.sistematenda.fiscal.mapper.DreMapper;
import br.com.sistematenda.fiscal.repository.DreRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/fiscal/dre")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DreResource {

    @Inject
    DreRepository repository;

    @Inject
    DreMapper mapper;

    @GET
    public PageResponse<DreDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("unidadeNegocio") Dre.UnidadeNegocio unidadeNegocio,
            @QueryParam("status") Dre.StatusDre status,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        PageResponse<Dre> pageResult = repository.findPaginated(page, size, sortBy, sortDirection, unidadeNegocio, status, dataInicio, dataFim);
        return PageResponse.of(
                pageResult.getContent().stream().map(mapper::toDTO).toList(),
                pageResult.getTotalElements(),
                pageResult.getCurrentPage(),
                pageResult.getPageSize());
    }

    @GET
    @Path("/{id}")
    public DreDTO buscarPorId(@PathParam("id") UUID id) {
        Dre dre = repository.findById(id);
        if (dre == null || dre.deleted) {
            throw new NotFoundException("Dre", id);
        }
        return mapper.toDTO(dre);
    }
}