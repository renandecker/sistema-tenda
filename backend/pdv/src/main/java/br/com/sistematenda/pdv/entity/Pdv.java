package br.com.sistematenda.pdv.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "pdv_terminal", indexes = {
    @Index(name = "idx_pdv_terminal_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_pdv_terminal_ativo", columnList = "ativo")
})
public class Pdv extends BaseEntity {

    public enum StatusPdv {
        LIVRE("Livre"),
        EM_USO("Em Uso"),
        MANUTENCAO("Manutenção"),
        INATIVO("Inativo");

        private final String descricao;
        StatusPdv(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    @NotBlank
    @Size(max = 20)
    public String codigo;

    @Column(name = "nome", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    public String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusPdv status = StatusPdv.LIVRE;

    @Column(name = "ativo", nullable = false)
    public boolean ativo = true;

    @Column(name = "porta_balanca_fruteira", length = 20)
    @Size(max = 20)
    public String portaBalancaFruteira;

    @Column(name = "porta_balanca_buffet", length = 20)
    @Size(max = 20)
    public String portaBalancaBuffet;

    @Column(name = "impressora_fiscal", length = 100)
    @Size(max = 100)
    public String impressoraFiscal;

    @Column(name = "impressora_nao_fiscal", length = 100)
    @Size(max = 100)
    public String impressoraNaoFiscal;

    @Column(name = "tef_habilitado", nullable = false)
    public boolean tefHabilitado = false;

    @Column(name = "tef_ip", length = 50)
    @Size(max = 50)
    public String tefIp;

    @Column(name = "tef_porta")
    public Integer tefPorta;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500)
    public String observacoes;

    @Column(name = "ultima_abertura")
    public java.time.LocalDateTime ultimaAbertura;

    @Column(name = "ultimo_fechamento")
    public java.time.LocalDateTime ultimoFechamento;

    @Column(name = "numero_ultima_venda")
    public Long numeroUltimaVenda = 0L;
}