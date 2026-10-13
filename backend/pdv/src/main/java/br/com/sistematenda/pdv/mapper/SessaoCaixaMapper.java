package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.pdv.dto.SessaoCaixaDTO;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface SessaoCaixaMapper extends BaseMapper<SessaoCaixa, SessaoCaixaDTO> {

    @Mapping(target = "pdv.id", source = "pdv.id")
    @Mapping(target = "pdv.codigo", source = "pdv.codigo")
    @Mapping(target = "pdv.nome", source = "pdv.nome")
    SessaoCaixaDTO toDTO(SessaoCaixa entity);

    @Mapping(target = "pdv", ignore = true)
    SessaoCaixa toEntity(SessaoCaixaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "pdv", ignore = true)
    void updateEntityFromDTO(SessaoCaixaDTO dto, @org.mapstruct.MappingTarget SessaoCaixa entity);

    default Pdv mapPdv(SessaoCaixaDTO.PdvResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Pdv p = new Pdv();
        p.id = java.util.UUID.fromString(dto.getId());
        return p;
    }
}