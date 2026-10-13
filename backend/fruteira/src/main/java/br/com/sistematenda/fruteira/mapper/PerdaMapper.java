package br.com.sistematenda.fruteira.mapper;

import br.com.sistematenda.fruteira.dto.PerdaDTO;
import br.com.sistematenda.fruteira.entity.Perda;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = {ProdutoMapper.class})
public interface PerdaMapper extends BaseMapper<Perda, PerdaDTO> {

    @Mapping(target = "produto.id", source = "produto.id")
    @Mapping(target = "produto.codigo", source = "produto.codigo")
    @Mapping(target = "produto.nome", source = "produto.nome")
    @Mapping(target = "produto.unidadeMedida", source = "produto.unidadeMedida")
    PerdaDTO toDTO(Perda entity);

    @Mapping(target = "produto", ignore = true)
    Perda toEntity(PerdaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "produto", ignore = true)
    void updateEntityFromDTO(PerdaDTO dto, @org.mapstruct.MappingTarget Perda entity);

    default Produto mapProduto(PerdaDTO.ProdutoResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Produto p = new Produto();
        p.id = java.util.UUID.fromString(dto.getId());
        return p;
    }
}