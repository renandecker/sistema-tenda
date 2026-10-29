package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import br.com.sistematenda.common.enums.FormaPagamento;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "pdv_forma_pagamento_venda")
public class FormaPagamentoVenda extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private BigDecimal valor;
}
