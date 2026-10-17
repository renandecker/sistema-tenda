package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buffet_cliente", indexes = {
    @Index(name = "idx_buffet_cliente_cpf", columnList = "cpf", unique = true),
    @Index(name = "idx_buffet_cliente_nome", columnList = "nome")
})
public class ClienteBuffet extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 200)
    public String nome;

    @Column(name = "cpf", nullable = false, length = 14, unique = true)
    public String cpf;

    @Column(name = "telefone", length = 20)
    public String telefone;

    @Column(name = "total_visitas", nullable = false)
    public Integer totalVisitas = 0;

    @Column(name = "total_consumido", nullable = false, precision = 15, scale = 4)
    public BigDecimal totalConsumido = BigDecimal.ZERO;

    @Column(name = "primeira_visita")
    public LocalDateTime primeiraVisita;

    @Column(name = "ultima_visita")
    public LocalDateTime ultimaVisita;

    @Column(name = "observacoes", length = 500)
    public String observacoes;
}
