package br.com.sistematenda.buffet.repository;

import br.com.sistematenda.buffet.entity.ClienteBuffet;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ClienteBuffetRepository implements PanacheRepositoryBase<ClienteBuffet, UUID> {

    public Optional<ClienteBuffet> findByCpf(String cpf) {
        return find("cpf = ?1 and deleted = false", cpf).firstResultOptional();
    }

    public List<ClienteBuffet> findAllAtivos() {
        return find("deleted = false").list();
    }

    public List<ClienteBuffet> findTopFrequentes(int limit) {
        return find("deleted = false order by totalVisitas desc").page(0, limit).list();
    }

    public List<ClienteBuffet> findTopConsumidores(int limit) {
        return find("deleted = false order by totalConsumido desc").page(0, limit).list();
    }
}
