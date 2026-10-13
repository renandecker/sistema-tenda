package br.com.sistematenda.common.dto;

import br.com.sistematenda.common.entity.Fornecedor;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class FornecedorDTO extends BaseDTO {

    private String nome;
    private String razaoSocial;
    private String email;
    private String telefone;
    private String cnpjCpf;
    private String tipo; // Distribuidor, Manufacturer, Atacadista
    private String segmentoMercado;
    private String status; // ATIVO, INATIVO
    private String codigoNcm; // Tabela NCM
    private String cstIcms; // CST do ICMS
    private BigDecimal aliquotaIcms;
    private Integer prazoEntregaDias;
    private String condicoesPagamento;
    private String situacaoTributaria; // Simples Nacional, Lucro Presumido, Lucro Real
    private BigDecimal aliquotaPis;
    private BigDecimal aliquotaCofins;
    private BigDecimal percentualDesconto;
    private String situacaoEntrega; // PENDENTE, ENTREGUE, ATRASADO
    private Integer pontosParceria;
    private BigDecimal totalCompras;
    private LocalDate ultimaCompra;

    // Helper methods
    public boolean isAtivo() {
        return this.status != null && this.status.equals("ATIVO");
    }

    public boolean isInativo() {
        return this.status != null && this.status.equals("INATIVO");
    }
}