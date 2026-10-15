package br.com.sistematenda.estoque.mapper;

import br.com.sistematenda.estoque.dto.InventarioDTO;
import br.com.sistematenda.estoque.entity.Inventario;
import br.com.sistematenda.common.mapper.BaseMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventarioMapper implements BaseMapper<Inventario, InventarioDTO> {

    @Override
    public InventarioDTO toDTO(Inventario entity) {
        if (entity == null) return null;
        InventarioDTO dto = new InventarioDTO();
        dto.setId(entity.id);
        dto.setDescricao(entity.descricao);
        dto.setStatus(entity.status);
        dto.setDataAbertura(entity.dataInicio);
        dto.setDataFechamento(entity.dataFim);
        dto.setResponsavel(entity.responsavel);
        dto.setTotalDivergencias(entity.itensDivergentes);
        dto.setDataAlerta(entity.dataInicio);
        return dto;
    }

    @Override
    public Inventario toEntity(InventarioDTO dto) {
        if (dto == null) return null;
        Inventario entity = new Inventario();
        entity.id = dto.getId();
        entity.descricao = dto.getDescricao();
        entity.status = dto.getStatus();
        entity.dataInicio = dto.getDataAbertura();
        entity.dataFim = dto.getDataFechamento();
        entity.responsavel = dto.getResponsavel();
        return entity;
    }

    @Override
    public void updateEntityFromDTO(InventarioDTO dto, Inventario entity) {
        if (dto == null || entity == null) return;
        entity.descricao = dto.getDescricao();
        entity.status = dto.getStatus();
        entity.dataInicio = dto.getDataAbertura();
        entity.dataFim = dto.getDataFechamento();
        entity.responsavel = dto.getResponsavel();
    }
}