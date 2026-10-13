package br.com.sistematenda.pdv.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.pdv.entity.Pdv;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PdvDTO extends BaseDTO {

    private String codigo;
    private String nome;
    private Pdv.StatusPdv status;
    private Boolean ativo;
    private String portaBalancaFruteira;
    private String portaBalancaBuffet;
    private String impressoraFiscal;
    private String impressoraNaoFiscal;
    private Boolean tefHabilitado;
    private String tefIp;
    private Integer tefPorta;
    private String observacoes;
    private java.time.LocalDateTime ultimaAbertura;
    private java.time.LocalDateTime ultimoFechamento;
    private Long numeroUltimaVenda;
}