package br.com.sistematenda.common.mapper;

import br.com.sistematenda.common.dto.ClienteDTO;
import br.com.sistematenda.common.entity.Cliente;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ClienteMapper extends BaseMapper<Cliente, ClienteDTO> {
}