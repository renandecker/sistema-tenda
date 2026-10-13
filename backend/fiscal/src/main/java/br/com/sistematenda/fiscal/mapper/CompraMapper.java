package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.CompraDTO;
import br.com.sistematenda.fiscal.entity.Compra;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface CompraMapper extends BaseMapper<Compra, CompraDTO> {
}