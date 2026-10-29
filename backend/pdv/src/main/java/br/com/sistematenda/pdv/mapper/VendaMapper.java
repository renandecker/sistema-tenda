package br.com.sistematenda.pdv.mapper;

import br.com.sistematenda.common.mapper.BaseMapper;
import br.com.sistematenda.pdv.dto.VendaDTO;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.pdv.entity.FormaPagamentoVenda;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface VendaMapper extends BaseMapper<Venda, VendaDTO> {

    VendaMapper INSTANCE = Mappers.getMapper(VendaMapper.class);

    @Mapping(target = "comanda.id", source = "comanda.id")
    @Mapping(target = "comanda.numero", source = "comanda.numero")
    @Mapping(target = "comanda.clienteNome", source = "comanda.clienteNome")
    @Mapping(target = "nfce.id", source = "nfce.id")
    @Mapping(target = "nfce.chaveAcesso", source = "nfce.chaveAcesso")
    @Mapping(target = "nfce.numero", source = "nfce.numero")
    @Mapping(target = "nfce.serie", source = "nfce.serie")
    @Mapping(target = "nfce.status", source = "nfce.status")
    @Mapping(target = "nfce.protocoloAutorizacao", source = "nfce.protocoloAutorizacao")
    @Mapping(target = "nfce.xmlAutorizado", source = "nfce.xmlAutorizado")
    @Mapping(target = "itens", ignore = true)
    @Mapping(target = "pagamentos", ignore = true)
    @Mapping(target = "formasPagamento", ignore = true)
    VendaDTO toDTO(Venda entity);

    @Mapping(target = "comanda", ignore = true)
    @Mapping(target = "nfce", ignore = true)
    @Mapping(target = "itens", ignore = true)
    @Mapping(target = "pagamentos", ignore = true)
    @Mapping(target = "formasPagamento", ignore = true)
    Venda toEntity(VendaDTO dto);

    default List<FormaPagamentoVenda> mapFormasPagamento(List<VendaDTO.FormaPagamentoDTO> formasPagamentoDTO) {
        if (formasPagamentoDTO == null) {
            return null;
        }
        return formasPagamentoDTO.stream()
                .map(dto -> {
                    FormaPagamentoVenda formaPagamento = new FormaPagamentoVenda();
                    formaPagamento.setFormaPagamento(dto.getFormaPagamento());
                    formaPagamento.setValor(dto.getValor());
                    return formaPagamento;
                })
                .collect(Collectors.toList());
    }

    default List<VendaDTO.FormaPagamentoDTO> mapFormasPagamentoDTO(List<FormaPagamentoVenda> formasPagamento) {
        if (formasPagamento == null) {
            return null;
        }
        return formasPagamento.stream()
                .map(formaPagamento -> {
                    VendaDTO.FormaPagamentoDTO dto = new VendaDTO.FormaPagamentoDTO();
                    dto.setFormaPagamento(formaPagamento.getFormaPagamento());
                    dto.setValor(formaPagamento.getValor());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Mapping(target = "formasPagamento", ignore = true)
    void updateEntityFromDTO(@MappingTarget Venda entity, VendaDTO dto);
}
