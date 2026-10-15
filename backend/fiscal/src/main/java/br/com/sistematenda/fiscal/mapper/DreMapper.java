package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.DreDTO;
import br.com.sistematenda.fiscal.entity.Dre;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DreMapper {

    public DreDTO toDTO(Dre entity) {
        return DreDTO.fromEntity(entity);
    }
}