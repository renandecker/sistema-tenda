package br.com.sistematenda.common.mapper;

public interface BaseMapper<E, D> {
    D toDTO(E entity);
    E toEntity(D dto);
    void updateEntityFromDTO(D dto, @org.mapstruct.MappingTarget E entity);
}
