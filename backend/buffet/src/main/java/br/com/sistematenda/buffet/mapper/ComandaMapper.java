package br.com.sistematenda.buffet.mapper;

import br.com.sistematenda.buffet.dto.ComandaDTO;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = {MesaMapper.class, ItemComandaMapper.class})
public interface ComandaMapper extends BaseMapper<Comanda, ComandaDTO> {

    @Mapping(target = "mesa.id", source = "mesa.id")
    @Mapping(target = "mesa.numero", source = "mesa.numero")
    @Mapping(target = "mesa.nome", source = "mesa.nome")
    @Mapping(target = "itens", ignore = true)
    ComandaDTO toDTO(Comanda entity);

    @Mapping(target = "mesa", ignore = true)
    Comanda toEntity(ComandaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "mesa", ignore = true)
    void updateEntityFromDTO(ComandaDTO dto, @org.mapstruct.MappingTarget Comanda entity);

    default Mesa mapMesa(ComandaDTO.MesaResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Mesa m = new Mesa();
        m.id = java.util.UUID.fromString(dto.getId());
        return m;
    }
}