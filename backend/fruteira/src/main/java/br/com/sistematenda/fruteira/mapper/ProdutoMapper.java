package br.com.sistematenda.fruteira.mapper;

import br.com.sistematenda.fruteira.dto.ProdutoDTO;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ProdutoMapper extends BaseMapper<Produto, ProdutoDTO> {

    @Mapping(target = "tipo", source = "tipo")
    @Mapping(target = "unidadeMedida", source = "unidadeMedida")
    @Mapping(target = "cstIcms", source = "cstIcms")
    @Mapping(target = "cstPis", source = "cstPis")
    @Mapping(target = "cstCofins", source = "cstCofins")
    ProdutoDTO toDTO(Produto entity);

    @Mapping(target = "tipo", source = "tipo")
    @Mapping(target = "unidadeMedida", source = "unidadeMedida")
    @Mapping(target = "cstIcms", source = "cstIcms")
    @Mapping(target = "cstPis", source = "cstPis")
    @Mapping(target = "cstCofins", source = "cstCofins")
    @Mapping(target = "codigo", ignore = true)
    Produto toEntity(ProdutoDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    void updateEntityFromDTO(ProdutoDTO dto, @org.mapstruct.MappingTarget Produto entity);
}