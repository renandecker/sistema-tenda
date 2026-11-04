package br.com.sistematenda.estoque.mapper;

import br.com.sistematenda.common.mapper.BaseMapper;
import br.com.sistematenda.estoque.dto.ProdutoEstoqueDTO;
import br.com.sistematenda.estoque.entity.ProdutoEstoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ProdutoEstoqueMapper extends BaseMapper<ProdutoEstoque, ProdutoEstoqueDTO> {

    ProdutoEstoqueDTO toDTO(ProdutoEstoque entity);

    @Mapping(target = "codigo", ignore = true)
    ProdutoEstoque toEntity(ProdutoEstoqueDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    void updateEntityFromDTO(ProdutoEstoqueDTO dto, @org.mapstruct.MappingTarget ProdutoEstoque entity);
}
