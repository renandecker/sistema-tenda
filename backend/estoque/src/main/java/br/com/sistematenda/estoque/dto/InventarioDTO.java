package br.com.sistematenda.estoque.dto;

import br.com.sistematenda.estoque.entity.Inventario;
import br.com.sistematenda.estoque.entity.Inventario.StatusInventario;
import java.time.LocalDateTime;
import java.util.UUID;

public class InventarioDTO {

    private UUID id;
    private String descricao;
    private StatusInventario status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String responsavel;
    private Integer totalDivergencias;
    private LocalDateTime dataAlerta;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusInventario getStatus() { return status; }
    public void setStatus(StatusInventario status) { this.status = status; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public Integer getTotalDivergencias() { return totalDivergencias; }
    public void setTotalDivergencias(Integer totalDivergencias) { this.totalDivergencias = totalDivergencias; }

    public LocalDateTime getDataAlerta() { return dataAlerta; }
    public void setDataAlerta(LocalDateTime dataAlerta) { this.dataAlerta = dataAlerta; }
}