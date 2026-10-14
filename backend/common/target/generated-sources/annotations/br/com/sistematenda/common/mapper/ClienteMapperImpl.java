package br.com.sistematenda.common.mapper;

import br.com.sistematenda.common.dto.ClienteDTO;
import br.com.sistematenda.common.entity.Cliente;
import jakarta.enterprise.context.ApplicationScoped;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-14T08:02:26-0300",
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

        clienteDTO.setId( entity.id );
        clienteDTO.setCreatedAt( entity.createdAt );
        clienteDTO.setUpdatedAt( entity.updatedAt );
        clienteDTO.setNome( entity.nome );
        clienteDTO.setEmail( entity.email );
        clienteDTO.setTelefone( entity.telefone );
        clienteDTO.setCpfCnpj( entity.cpfCnpj );
        clienteDTO.setTipoPessoa( entity.tipoPessoa );
        clienteDTO.setTipoCliente( entity.tipoCliente );
        clienteDTO.setStatus( entity.status );
        clienteDTO.setLimiteCredito( entity.limiteCredito );
        clienteDTO.setPontosFidelidade( entity.pontosFidelidade );
        clienteDTO.setDataNascimento( entity.dataNascimento );
        clienteDTO.setRgIe( entity.rgIe );
        clienteDTO.setInscricaoEstadual( entity.inscricaoEstadual );
        clienteDTO.setEndereco( entity.endereco );
        clienteDTO.setNumero( entity.numero );
        clienteDTO.setBairro( entity.bairro );
        clienteDTO.setCidade( entity.cidade );
        clienteDTO.setCep( entity.cep );
        clienteDTO.setNomeFantasia( entity.nomeFantasia );
        clienteDTO.setObservacoes( entity.observacoes );
        clienteDTO.setContatoComercial( entity.contatoComercial );
        clienteDTO.setEmailComercial( entity.emailComercial );

        return clienteDTO;
    }

    @Override
    public Cliente toEntity(ClienteDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Cliente cliente = new Cliente();

        cliente.id = dto.getId();
        cliente.createdAt = dto.getCreatedAt();
        cliente.updatedAt = dto.getUpdatedAt();
        cliente.nome = dto.getNome();
        cliente.email = dto.getEmail();
        cliente.telefone = dto.getTelefone();
        cliente.cpfCnpj = dto.getCpfCnpj();
        cliente.tipoPessoa = dto.getTipoPessoa();
        cliente.rgIe = dto.getRgIe();
        cliente.inscricaoEstadual = dto.getInscricaoEstadual();
        cliente.endereco = dto.getEndereco();
        cliente.numero = dto.getNumero();
        cliente.bairro = dto.getBairro();
        cliente.cidade = dto.getCidade();
        cliente.cep = dto.getCep();
        cliente.dataNascimento = dto.getDataNascimento();
        cliente.nomeFantasia = dto.getNomeFantasia();
        cliente.status = dto.getStatus();
        cliente.limiteCredito = dto.getLimiteCredito();
        cliente.pontosFidelidade = dto.getPontosFidelidade();
        cliente.tipoCliente = dto.getTipoCliente();
        cliente.observacoes = dto.getObservacoes();
        cliente.contatoComercial = dto.getContatoComercial();
        cliente.emailComercial = dto.getEmailComercial();

        return cliente;
    }

    @Override
    public void updateEntityFromDTO(ClienteDTO dto, Cliente entity) {
        if ( dto == null ) {
            return;
        }

        entity.id = dto.getId();
        entity.createdAt = dto.getCreatedAt();
        entity.updatedAt = dto.getUpdatedAt();
        entity.nome = dto.getNome();
        entity.email = dto.getEmail();
        entity.telefone = dto.getTelefone();
        entity.cpfCnpj = dto.getCpfCnpj();
        entity.tipoPessoa = dto.getTipoPessoa();
        entity.rgIe = dto.getRgIe();
        entity.inscricaoEstadual = dto.getInscricaoEstadual();
        entity.endereco = dto.getEndereco();
        entity.numero = dto.getNumero();
        entity.bairro = dto.getBairro();
        entity.cidade = dto.getCidade();
        entity.cep = dto.getCep();
        entity.dataNascimento = dto.getDataNascimento();
        entity.nomeFantasia = dto.getNomeFantasia();
        entity.status = dto.getStatus();
        entity.limiteCredito = dto.getLimiteCredito();
        entity.pontosFidelidade = dto.getPontosFidelidade();
        entity.tipoCliente = dto.getTipoCliente();
        entity.observacoes = dto.getObservacoes();
        entity.contatoComercial = dto.getContatoComercial();
        entity.emailComercial = dto.getEmailComercial();
    }
}
