package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.ConciliacaoCartaoDTO;
import br.com.sistematenda.fiscal.entity.ConciliacaoCartao;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConciliacaoCartaoMapper {

    public ConciliacaoCartaoDTO toDTO(ConciliacaoCartao entity) {
        return ConciliacaoCartaoDTO.fromEntity(entity);
    }
}