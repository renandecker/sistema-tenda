package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.AtivoImobilizadoDTO;
import br.com.sistematenda.fiscal.entity.AtivoImobilizado;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AtivoImobilizadoMapper {

    public AtivoImobilizadoDTO toDTO(AtivoImobilizado entity) {
        return AtivoImobilizadoDTO.fromEntity(entity);
    }
}