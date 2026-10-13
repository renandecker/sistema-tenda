package br.com.sistematenda.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface BaseMapper<E, D> {
    D toDTO(E entity);
    E toEntity(D dto);
    void updateEntityFromDTO(D dto, @org.mapstruct.MappingTarget E entity);
}