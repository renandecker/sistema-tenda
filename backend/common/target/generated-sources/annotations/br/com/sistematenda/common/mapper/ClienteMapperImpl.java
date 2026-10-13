package br.com.sistematenda.common.mapper;

import br.com.sistematenda.common.dto.ClienteDTO;
import br.com.sistematenda.common.entity.Cliente;
import jakarta.enterprise.context.ApplicationScoped;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-13T14:23:39-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@ApplicationScoped
public class ClienteMapperImpl implements ClienteMapper {

    @Override
    public ClienteDTO toDTO(Cliente entity) {
        if ( entity == null ) {
            return null;
        }

        ClienteDTO clienteDTO = new ClienteDTO();

        return clienteDTO;
    }

    @Override
    public Cliente toEntity(ClienteDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Cliente cliente = new Cliente();

        return cliente;
    }

    @Override
    public void updateEntityFromDTO(ClienteDTO dto, Cliente entity) {
        if ( dto == null ) {
            return;
        }
    }
}
