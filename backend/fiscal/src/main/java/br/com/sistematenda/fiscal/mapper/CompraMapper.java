package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.CompraDTO;
import br.com.sistematenda.fiscal.entity.Compra;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface CompraMapper extends BaseMapper<Compra, CompraDTO> {

    @Override
    @Mapping(target = "fornecedor", expression = "java(entity.fornecedor != null ? entity.fornecedor.nome : null)")
    CompraDTO toDTO(Compra entity);

    @Override
    @Mapping(target = "fornecedor", ignore = true)
    Compra toEntity(CompraDTO dto);

    @Override
    @Mapping(target = "fornecedor", ignore = true)
    void updateEntityFromDTO(CompraDTO dto, @org.mapstruct.MappingTarget Compra entity);
}