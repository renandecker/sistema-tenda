package br.com.sistematenda.buffet.dto;

import br.com.sistematenda.common.dto.BaseDTO;
import br.com.sistematenda.buffet.entity.Mesa;
import br.com.sistematenda.common.enums.StatusComanda;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MesaDTO extends BaseDTO {

    private String numero;
    private String nome;
    private Integer capacidade;
    private Mesa.StatusMesa status;
    private String area;
    private Integer posicaoX;
    private Integer posicaoY;
    private String observacoes;
    private Boolean ativa;
    private ComandaResumoDTO comandaAtiva;

    public boolean isDisponivel() {
        return status == Mesa.StatusMesa.LIVRE;
    }

    @Data
    public static class ComandaResumoDTO {
        private String id;
        private String numero;
        private String clienteNome;
        private StatusComanda status;
    }
}