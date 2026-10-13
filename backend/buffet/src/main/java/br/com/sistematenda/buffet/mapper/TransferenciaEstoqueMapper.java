package br.com.sistematenda.buffet.mapper;

import br.com.sistematenda.buffet.dto.TransferenciaEstoqueDTO;
import br.com.sistematenda.buffet.entity.TransferenciaEstoque;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface TransferenciaEstoqueMapper extends BaseMapper<TransferenciaEstoque, TransferenciaEstoqueDTO> {

    @Mapping(target = "produto.id", source = "produto.id")
    @Mapping(target = "produto.codigo", source = "produto.codigo")
    @Mapping(target = "produto.nome", source = "produto.nome")
    @Mapping(target = "produto.unidadeMedida", source = "produto.unidadeMedida")
    TransferenciaEstoqueDTO toDTO(TransferenciaEstoque entity);

    @Mapping(target = "produto", ignore = true)
    TransferenciaEstoque toEntity(TransferenciaEstoqueDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "produto", ignore = true)
    void updateEntityFromDTO(TransferenciaEstoqueDTO dto, @org.mapstruct.MappingTarget TransferenciaEstoque entity);

    default Produto mapProduto(TransferenciaEstoqueDTO.ProdutoFruteiraResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Produto p = new Produto();
        p.id = java.util.UUID.fromString(dto.getId());
        return p;
    }
}