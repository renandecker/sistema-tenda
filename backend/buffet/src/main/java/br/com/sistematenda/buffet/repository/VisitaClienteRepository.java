package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.VisitaCliente;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VisitaClienteRepository implements PanacheRepositoryBase<VisitaCliente, UUID> {

    public List<VisitaCliente> findByCliente(UUID clienteId) {
        return find("cliente.id = ?1 and deleted = false order by dataVisita desc", clienteId).list();
    }

    public VisitaCliente findByComanda(UUID comandaId) {
        return find("comanda.id = ?1 and deleted = false", comandaId).firstResult();
    }
}
