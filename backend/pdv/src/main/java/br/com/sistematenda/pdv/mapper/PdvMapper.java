package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.pdv.dto.PdvDTO;
import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PdvMapper extends BaseMapper<Pdv, PdvDTO> {
}