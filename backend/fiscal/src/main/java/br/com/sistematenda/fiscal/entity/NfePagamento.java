package br.com.sistematenda.fiscal.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "fiscal_nfe_pagamento", indexes = {
    @Index(name = "idx_fiscal_nfe_pagamento_nfe", columnList = "nfe_id")
})
public class NfePagamento extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "nfe_id", nullable = false)
    @NotNull
    public Nfe nfe;

    @Column(name = "forma_pagamento", nullable = false, length = 2)
    @NotNull
    @Size(max = 2)
    public String formaPagamento;

    @Column(name = "descricao", length = 30)
    @Size(max = 30)
    public String descricao;

    @Column(name = "valor", nullable = false, precision = 15, scale = 4)
    @NotNull
    public BigDecimal valor;
}
