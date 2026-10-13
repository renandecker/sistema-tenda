package br.com.sistematenda.buffet.mapper;

import br.com.sistematenda.buffet.dto.ReceitaDTO;
import br.com.sistematenda.buffet.entity.Receita;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, uses = {ProdutoBuffetMapper.class})
public interface ReceitaMapper extends BaseMapper<Receita, ReceitaDTO> {

    @Mapping(target = "produto.id", source = "produto.id")
    @Mapping(target = "produto.codigo", source = "produto.codigo")
    @Mapping(target = "produto.nome", source = "produto.nome")
    @Mapping(target = "ingredientes", ignore = true)
    ReceitaDTO toDTO(Receita entity);

    @Mapping(target = "produto", ignore = true)
    Receita toEntity(ReceitaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "produto", ignore = true)
    void updateEntityFromDTO(ReceitaDTO dto, @org.mapstruct.MappingTarget Receita entity);

    default ProdutoBuffet mapProduto(ReceitaDTO.ProdutoBuffetResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        ProdutoBuffet p = new ProdutoBuffet();
        p.id = java.util.UUID.fromString(dto.getId());
        return p;
    }

    default Produto mapProdutoFruteira(ReceitaDTO.ReceitaIngredienteDTO.ProdutoFruteiraResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Produto p = new Produto();
        p.id = java.util.UUID.fromString(dto.getId());
        return p;
    }
}