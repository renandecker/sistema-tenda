package br.com.sistematenda.buffet.mapper;

import br.com.sistematenda.buffet.dto.MesaDTO;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface MesaMapper extends BaseMapper<Mesa, MesaDTO> {

    @Mapping(target = "comandaAtiva.id", source = "comandaAtiva.id")
    @Mapping(target = "comandaAtiva.numero", source = "comandaAtiva.numero")
    @Mapping(target = "comandaAtiva.clienteNome", source = "comandaAtiva.clienteNome")
    @Mapping(target = "comandaAtiva.status", source = "comandaAtiva.status")
    MesaDTO toDTO(Mesa entity);

    Mesa toEntity(MesaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "comandaAtiva", ignore = true)
    void updateEntityFromDTO(MesaDTO dto, @org.mapstruct.MappingTarget Mesa entity);
}