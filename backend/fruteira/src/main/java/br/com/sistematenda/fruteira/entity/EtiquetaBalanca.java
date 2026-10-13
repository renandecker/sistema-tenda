package br.com.sistematenda.fruteira.entity;

import br.com.sistematenda.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fruteira_etiqueta_balanca", indexes = {
    @Index(name = "idx_fruteira_etiqueta_produto", columnList = "produto_id"),
    @Index(name = "idx_fruteira_etiqueta_codigo", columnList = "codigo"),
    @Index(name = "idx_fruteira_etiqueta_ativa", columnList = "ativa")
})
public class EtiquetaBalanca extends BaseEntity {

    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    @NotNull
    @Size(min = 1, max = 20)
    public String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    @NotNull
    public Produto produto;

    @Column(name = "preco", nullable = false, precision = 15, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    public BigDecimal preco;

    @Column(name = "validade_dias")
    public Integer validadeDias;

    @Column(name = "data_validade")
    public LocalDate dataValidade;

    @Column(name = "tara", precision = 10, scale = 4)
    @DecimalMin("0.0")
    public BigDecimal tara;

    @Column(name = "texto_livre_1", length = 100)
    @Size(max = 100)
    public String textoLivre1;

    @Column(name = "texto_livre_2", length = 100)
    @Size(max = 100)
    public String textoLivre2;

    @Column(name = "imprimir_codigo_barras", nullable = false)
    public boolean imprimirCodigoBarras = true;

    @Column(name = "imprimir_qr_code", nullable = false)
    public boolean imprimirQrCode = false;

    @Column(name = "modelo_etiqueta", length = 50)
    @Size(max = 50)
    public String modeloEtiqueta = "PADRAO";

    @Column(name = "ativa", nullable = false)
    public boolean ativa = true;

    @Column(name = "ultima_impressao")
    public LocalDateTime ultimaImpressao;

    @Column(name = "quantidade_impressa")
    public Integer quantidadeImpressa = 0;
}