package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.NfeDTO;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface NfeMapper extends BaseMapper<Nfe, NfeDTO> {

    @Override
    default NfeDTO toDTO(Nfe entity) {
        return NfeDTO.fromEntity(entity);
    }

    @Override
    Nfe toEntity(NfeDTO dto);
}