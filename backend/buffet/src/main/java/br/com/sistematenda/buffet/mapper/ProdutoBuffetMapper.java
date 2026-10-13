package br.com.sistematenda.buffet.mapper;

import br.com.sistematenda.buffet.dto.ProdutoBuffetDTO;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ProdutoBuffetMapper extends BaseMapper<ProdutoBuffet, ProdutoBuffetDTO> {

    ProdutoBuffetDTO toDTO(ProdutoBuffet entity);
    ProdutoBuffet toEntity(ProdutoBuffetDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDTO(ProdutoBuffetDTO dto, @org.mapstruct.MappingTarget ProdutoBuffet entity);
}