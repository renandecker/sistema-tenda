package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.pdv.dto.VendaDTO;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = {ItemVendaMapper.class, PagamentoMapper.class})
public interface VendaMapper extends BaseMapper<Venda, VendaDTO> {

    @Mapping(target = "comanda.id", source = "comanda.id")
    @Mapping(target = "comanda.numero", source = "comanda.numero")
    @Mapping(target = "comanda.clienteNome", source = "comanda.clienteNome")
    @Mapping(target = "nfce.id", source = "nfce.id")
    @Mapping(target = "nfce.chaveAcesso", source = "nfce.chaveAcesso")
    @Mapping(target = "nfce.numero", source = "nfce.numero")
    @Mapping(target = "nfce.serie", source = "nfce.serie")
    @Mapping(target = "nfce.status", source = "nfce.status")
    @Mapping(target = "nfce.protocoloAutorizacao", source = "nfce.protocoloAutorizacao")
    @Mapping(target = "nfce.xmlAutorizado", source = "nfce.xmlAutorizado")
    @Mapping(target = "itens", ignore = true)
    @Mapping(target = "pagamentos", ignore = true)
    VendaDTO toDTO(Venda entity);

    @Mapping(target = "comanda", ignore = true)
    @Mapping(target = "nfce", ignore = true)
    Venda toEntity(VendaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "comanda", ignore = true)
    @Mapping(target = "nfce", ignore = true)
    void updateEntityFromDTO(VendaDTO dto, @org.mapstruct.MappingTarget Venda entity);

    default Comanda mapComanda(VendaDTO.ComandaResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Comanda c = new Comanda();
        c.id = java.util.UUID.fromString(dto.getId());
        return c;
    }

    default Nfe mapNfe(VendaDTO.NfeResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Nfe n = new Nfe();
        n.id = java.util.UUID.fromString(dto.getId());
        return n;
    }
}