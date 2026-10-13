package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.pdv.dto.ItemVendaDTO;
import br.com.sistematenda.pdv.entity.ItemVenda;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ItemVendaMapper extends BaseMapper<ItemVenda, ItemVendaDTO> {

    @Mapping(target = "venda.id", source = "venda.id")
    @Mapping(target = "venda.numero", source = "venda.numero")
    @Mapping(target = "venda.clienteNome", source = "venda.clienteNome")
    ItemVendaDTO toDTO(ItemVenda entity);

    @Mapping(target = "venda", ignore = true)
    @Mapping(target = "produtoFruteira", ignore = true)
    @Mapping(target = "produtoBuffet", ignore = true)
    ItemVenda toEntity(ItemVendaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "venda", ignore = true)
    @Mapping(target = "produtoFruteira", ignore = true)
    @Mapping(target = "produtoBuffet", ignore = true)
    void updateEntityFromDTO(ItemVendaDTO dto, @org.mapstruct.MappingTarget ItemVenda entity);

    default Venda mapVenda(ItemVendaDTO.VendaResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Venda v = new Venda();
        v.id = java.util.UUID.fromString(dto.getId());
        return v;
    }

    default Produto mapProdutoFruteira(String id) {
        if (id == null) return null;
        Produto p = new Produto();
        p.id = java.util.UUID.fromString(id);
        return p;
    }

    default ProdutoBuffet mapProdutoBuffet(String id) {
        if (id == null) return null;
        ProdutoBuffet p = new ProdutoBuffet();
        p.id = java.util.UUID.fromString(id);
        return p;
    }
}