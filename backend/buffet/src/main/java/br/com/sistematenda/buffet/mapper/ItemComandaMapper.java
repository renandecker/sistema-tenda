package br.com.sistematenda.buffet.mapper;

import br.com.sistematenda.buffet.dto.ItemComandaDTO;
import br.com.sistematenda.buffet.entity.ItemComanda;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ItemComandaMapper extends BaseMapper<ItemComanda, ItemComandaDTO> {

    @Mapping(target = "comanda.id", source = "comanda.id")
    @Mapping(target = "comanda.numero", source = "comanda.numero")
    @Mapping(target = "comanda.clienteNome", source = "comanda.clienteNome")
    ItemComandaDTO toDTO(ItemComanda entity);

    @Mapping(target = "comanda", ignore = true)
    ItemComanda toEntity(ItemComandaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "comanda", ignore = true)
    void updateEntityFromDTO(ItemComandaDTO dto, @org.mapstruct.MappingTarget ItemComanda entity);

    default Comanda mapComanda(ItemComandaDTO.ComandaResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Comanda c = new Comanda();
        c.id = java.util.UUID.fromString(dto.getId());
        return c;
    }
}