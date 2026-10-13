package br.com.sistematenda.buffet.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "buffet_mesa", indexes = {
    @Index(name = "idx_buffet_mesa_numero", columnList = "numero", unique = true),
    @Index(name = "idx_buffet_mesa_status", columnList = "status"),
    @Index(name = "idx_buffet_mesa_area", columnList = "area")
})
public class Mesa extends BaseEntity {

    public enum StatusMesa {
        LIVRE("Livre"),
        OCUPADA("Ocupada"),
        RESERVADA("Reservada"),
        MANUTENCAO("Manutenção"),
        INATIVA("Inativa");

        private final String descricao;
        StatusMesa(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "numero", unique = true, nullable = false, length = 20)
    @NotBlank
    @Size(max = 20)
    public String numero;

    @Column(name = "nome", length = 100)
    @Size(max = 100)
    public String nome;

    @Column(name = "capacidade")
    public Integer capacidade = 4;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusMesa status = StatusMesa.LIVRE;

    @Column(name = "area", length = 50)
    @Size(max = 50)
    public String area;

    @Column(name = "posicao_x")
    public Integer posicaoX;

    @Column(name = "posicao_y")
    public Integer posicaoY;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @Column(name = "ativa", nullable = false)
    public boolean ativa = true;

    @OneToOne(mappedBy = "mesa")
    public Comanda comandaAtiva;

    public boolean isDisponivel() {
        return ativa && status == StatusMesa.LIVRE;
    }
}