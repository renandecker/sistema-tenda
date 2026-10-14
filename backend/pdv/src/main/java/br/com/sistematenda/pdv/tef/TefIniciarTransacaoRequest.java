package br.com.sistematenda.pdv.tef;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TefIniciarTransacaoRequest {

    @NotNull
    @DecimalMin("0.01")
    public BigDecimal valor;

    @NotNull
    public TipoTransacaoTef tipoTransacao;

    public Integer parcelas = 1;

    @Size(max = 30)
    public String bandeira;

    @Size(max = 50)
    public String documento;

    @Size(max = 14)
    public String cpfCnpjConsumidor;
}