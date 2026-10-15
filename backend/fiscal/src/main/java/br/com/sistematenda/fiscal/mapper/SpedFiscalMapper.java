package br.com.sistematenda.fiscal.mapper;

import br.com.sistematenda.fiscal.dto.SpedFiscalDTO;
import br.com.sistematenda.fiscal.entity.SpedFiscal;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SpedFiscalMapper {

    public SpedFiscalDTO toDTO(SpedFiscal entity) {
        return SpedFiscalDTO.fromEntity(entity);
    }
}