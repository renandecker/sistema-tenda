package br.com.sistematenda.common.mapper;

import br.com.sistematenda.common.dto.FornecedorDTO;
import br.com.sistematenda.common.entity.Fornecedor;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface FornecedorMapper extends BaseMapper<Fornecedor, FornecedorDTO> {
}