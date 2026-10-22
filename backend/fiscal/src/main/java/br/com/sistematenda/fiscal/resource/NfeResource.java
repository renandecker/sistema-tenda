package br.com.sistematenda.fiscal.resource;

import br.com.sistematenda.fiscal.dto.NfeDTO;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.mapper.NfeMapper;
import br.com.sistematenda.fiscal.repository.NfeRepository;
import br.com.sistematenda.fiscal.service.NfceService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Path("/fiscal/nfe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NfeResource {

    @Inject
    NfeRepository repository;

    @Inject
    NfeMapper mapper;

    @Inject
    NfceService nfceService;

    @GET
    public PageResponse<NfeDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("status") String status,
            @QueryParam("modelo") String modelo,
            @QueryParam("busca") String busca,
            @QueryParam("dataInicio") String dataInicio,
            @QueryParam("dataFim") String dataFim) {
        PageResponse<Nfe> pageResult = repository.findPaginated(page, size, sortBy, sortDirection, status, modelo, busca, dataInicio, dataFim);
        return PageResponse.of(
                pageResult.getContent().stream().map(mapper::toDTO).toList(),
                pageResult.getTotalElements(),
                pageResult.getCurrentPage(),
                pageResult.getPageSize());
    }

    @GET
    @Path("/{id}")
    public NfeDTO buscarPorId(@PathParam("id") UUID id) {
        Nfe nfe = repository.findById(id);
        if (nfe == null || nfe.deleted) {
            throw new NotFoundException("Nfe", id);
        }
        return mapper.toDTO(nfe);
    }

    @GET
    @Path("/chave/{chaveAcesso}")
    public NfeDTO buscarPorChave(@PathParam("chaveAcesso") String chaveAcesso) {
        Nfe nfe = repository.findByChaveAcesso(chaveAcesso);
        if (nfe == null || nfe.deleted) {
            throw new NotFoundException("Nfe", chaveAcesso);
        }
        return mapper.toDTO(nfe);
    }

    @POST
    @Path("/{id}/cancelar")
    public NfeDTO cancelar(@PathParam("id") UUID id, @QueryParam("justificativa") String justificativa) {
        Nfe nfe = repository.findById(id);
        if (nfe == null || nfe.deleted) {
            throw new NotFoundException("Nfe", id);
        }
        if (justificativa == null || justificativa.trim().length() < 15) {
            throw new BusinessException("JUSTIFICATIVA_INVALIDA", "Justificativa deve ter no mínimo 15 caracteres");
        }
        Nfe atual = nfceService.cancelarNfce(nfe, justificativa);
        return mapper.toDTO(atual);
    }

    @POST
    @Path("/{id}/consultar")
    public NfeDTO consultarStatus(@PathParam("id") UUID id) {
        Nfe nfe = repository.findById(id);
        if (nfe == null || nfe.deleted) {
            throw new NotFoundException("Nfe", id);
        }
        nfe = nfceService.consultarStatus(nfe);
        return mapper.toDTO(nfe);
    }

    @GET
    @Path("/{id}/xml")
    public Response downloadXml(@PathParam("id") UUID id) {
        Nfe nfe = repository.findById(id);
        if (nfe == null || nfe.deleted) {
            throw new NotFoundException("Nfe", id);
        }
        String xml = nfe.xmlAutorizado != null ? nfe.xmlAutorizado : nfe.xmlOriginal;
        if (xml == null) {
            throw new BusinessException("XML_NAO_ENCONTRADO", "XML não encontrado para esta nota");
        }
        return Response.ok(xml)
                .header("Content-Disposition", "attachment; filename=nfe-" + nfe.chaveAcesso + ".xml")
                .build();
    }

    @GET
    @Path("/{id}/danfe")
    public Response downloadDanfe(@PathParam("id") UUID id) {
        Nfe nfe = repository.findById(id);
        if (nfe == null || nfe.deleted) {
            throw new NotFoundException("Nfe", id);
        }
        if (nfe.danfe == null) {
            throw new BusinessException("DANFE_NAO_ENCONTRADO", "DANFE não encontrado para esta nota");
        }
        StreamingOutput stream = output -> {
            try (ByteArrayInputStream input = new ByteArrayInputStream(nfe.danfe)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        };
        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=danfe-" + nfe.chaveAcesso + ".pdf")
                .header("Content-Type", "application/pdf")
                .build();
    }

    @POST
    @Path("/inutilizar")
    public Response inutilizar(
            @QueryParam("justificativa") String justificativa,
            @QueryParam("numeroInicial") int numeroInicial,
            @QueryParam("numeroFinal") int numeroFinal) {
        if (justificativa == null || justificativa.trim().length() < 15) {
            throw new BusinessException("JUSTIFICATIVA_INVALIDA", "Justificativa deve ter no mínimo 15 caracteres");
        }
        nfceService.inutilizarNumeracao(justificativa, numeroInicial, numeroFinal);
        return Response.ok().build();
    }
}