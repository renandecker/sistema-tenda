package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.pdv.dto.PagamentoDTO;
import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PagamentoMapper extends BaseMapper<Pagamento, PagamentoDTO> {

    @Mapping(target = "venda.id", source = "venda.id")
    @Mapping(target = "venda.numero", source = "venda.numero")
    @Mapping(target = "venda.clienteNome", source = "venda.clienteNome")
    PagamentoDTO toDTO(Pagamento entity);

    @Mapping(target = "venda", ignore = true)
    Pagamento toEntity(PagamentoDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "venda", ignore = true)
    void updateEntityFromDTO(PagamentoDTO dto, @org.mapstruct.MappingTarget Pagamento entity);

    default Venda mapVenda(PagamentoDTO.VendaResumoDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        Venda v = new Venda();
        v.id = java.util.UUID.fromString(dto.getId());
        return v;
    }
}