package br.com.sistematenda.fiscal.resource;

import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.fiscal.dto.SpedFiscalDTO;
import br.com.sistematenda.fiscal.entity.SpedFiscal;
import br.com.sistematenda.fiscal.mapper.SpedFiscalMapper;
import br.com.sistematenda.fiscal.repository.SpedFiscalRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/fiscal/sped-fiscal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SpedFiscalResource {

    @Inject
    SpedFiscalRepository repository;

    @Inject
    SpedFiscalMapper mapper;

    @GET
    public PageResponse<SpedFiscalDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") SpedFiscal.StatusSped status,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        PageResponse<SpedFiscal> pageResult = repository.findPaginated(page, size, sortBy, sortDirection, status, dataInicio, dataFim);
        return PageResponse.of(
                pageResult.getContent().stream().map(mapper::toDTO).toList(),
                pageResult.getTotalElements(),
                pageResult.getCurrentPage(),
                pageResult.getPageSize());
    }

    @GET
    @Path("/{id}")
    public SpedFiscalDTO buscarPorId(@PathParam("id") UUID id) {
        SpedFiscal sped = repository.findById(id);
        if (sped == null || sped.deleted) {
            throw new NotFoundException("SpedFiscal", id);
        }
        return mapper.toDTO(sped);
    }
}