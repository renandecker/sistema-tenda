package br.com.sistematenda.estoque.mapper;

import br.com.sistematenda.estoque.dto.AlertaEstoqueDTO;
import br.com.sistematenda.estoque.entity.AlertaEstoque;
import br.com.sistematenda.common.mapper.BaseMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertaEstoqueMapper implements BaseMapper<AlertaEstoque, AlertaEstoqueDTO> {

    @Override
    public AlertaEstoqueDTO toDTO(AlertaEstoque entity) {
        if (entity == null) return null;
        AlertaEstoqueDTO dto = new AlertaEstoqueDTO();
        dto.setId(entity.id);
        dto.setProdutoFruteiraId(entity.produtoFruteira != null ? entity.produtoFruteira.id : null);
        dto.setProdutoBuffetId(entity.produtoBuffet != null ? entity.produtoBuffet.id : null);
        dto.setTipo(entity.tipo);
        dto.setStatus(entity.status);
        dto.setPrioridade(entity.prioridade);
        dto.setMensagem(entity.mensagem);
        dto.setValorReferencia(entity.valorReferencia);
        dto.setValorAtual(entity.valorAtual);
        dto.setDataVencimento(entity.dataVencimento);
        dto.setDiasParaVencimento(entity.diasParaVencimento);
        dto.setDataAlerta(entity.dataAlerta);
        dto.setDataVisualizacao(entity.dataVisualizacao);
        dto.setDataResolucao(entity.dataResolucao);
        dto.setVisualizadoPor(entity.visualizadoPor);
        dto.setResolvidoPor(entity.resolvidoPor);
        dto.setObservacoesResolucao(entity.observacoesResolucao);
        return dto;
    }

    @Override
    public AlertaEstoque toEntity(AlertaEstoqueDTO dto) {
        if (dto == null) return null;
        AlertaEstoque entity = new AlertaEstoque();
        entity.id = dto.getId();
        entity.tipo = dto.getTipo();
        entity.status = dto.getStatus();
        entity.prioridade = dto.getPrioridade();
        entity.mensagem = dto.getMensagem();
        entity.valorReferencia = dto.getValorReferencia();
        entity.valorAtual = dto.getValorAtual();
        entity.dataVencimento = dto.getDataVencimento();
        entity.diasParaVencimento = dto.getDiasParaVencimento();
        entity.dataAlerta = dto.getDataAlerta();
        entity.dataVisualizacao = dto.getDataVisualizacao();
        entity.dataResolucao = dto.getDataResolucao();
        entity.visualizadoPor = dto.getVisualizadoPor();
        entity.resolvidoPor = dto.getResolvidoPor();
        entity.observacoesResolucao = dto.getObservacoesResolucao();
        return entity;
    }

    @Override
    public void updateEntityFromDTO(AlertaEstoqueDTO dto, AlertaEstoque entity) {
        if (dto == null || entity == null) return;
        entity.tipo = dto.getTipo();
        entity.status = dto.getStatus();
        entity.prioridade = dto.getPrioridade();
        entity.mensagem = dto.getMensagem();
        entity.valorReferencia = dto.getValorReferencia();
        entity.valorAtual = dto.getValorAtual();
        entity.dataVencimento = dto.getDataVencimento();
        entity.diasParaVencimento = dto.getDiasParaVencimento();
        entity.dataAlerta = dto.getDataAlerta();
        entity.dataVisualizacao = dto.getDataVisualizacao();
        entity.dataResolucao = dto.getDataResolucao();
        entity.visualizadoPor = dto.getVisualizadoPor();
        entity.resolvidoPor = dto.getResolvidoPor();
        entity.observacoesResolucao = dto.getObservacoesResolucao();
    }
}