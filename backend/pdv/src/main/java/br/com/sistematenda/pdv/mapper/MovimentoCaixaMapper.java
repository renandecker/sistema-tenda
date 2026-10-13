package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.pdv.dto.MovimentoCaixaDTO;
import br.com.sistematenda.pdv.entity.MovimentoCaixa;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface MovimentoCaixaMapper extends BaseMapper<MovimentoCaixa, MovimentoCaixaDTO> {

    @Mapping(target = "sessaoCaixa.id", source = "sessaoCaixa.id")
    @Mapping(target = "sessaoCaixa.pdvCodigo", source = "sessaoCaixa.pdv.codigo")
    @Mapping(target = "sessaoCaixa.operadorAbertura", source = "sessaoCaixa.operadorAbertura")
    MovimentoCaixaDTO toDTO(MovimentoCaixa entity);

    @Mapping(target = "sessaoCaixa", ignore = true)
    MovimentoCaixa toEntity(MovimentoCaixaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "sessaoCaixa", ignore = true)
    void updateEntityFromDTO(MovimentoCaixaDTO dto, @org.mapstruct.MappingTarget MovimentoCaixa entity);

    default SessaoCaixa mapSessaoCaixa(MovimentoCaixaDTO.SessaoCaixaResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        SessaoCaixa s = new SessaoCaixa();
        s.id = java.util.UUID.fromString(dto.getId());
        return s;
    }
}