package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "buffet_cliente_visita", indexes = {
    @Index(name = "idx_buffet_visita_cliente", columnList = "cliente_id"),
    @Index(name = "idx_buffet_visita_data", columnList = "data_visita")
})
public class VisitaCliente extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    public ClienteBuffet cliente;

    @ManyToOne
    @JoinColumn(name = "comanda_id")
    public Comanda comanda;

    @Column(name = "data_visita", nullable = false)
    public LocalDateTime dataVisita;

    @Column(name = "valor_consumido", precision = 15, scale = 4)
    public BigDecimal valorConsumido = BigDecimal.ZERO;
}
