package br.com.sistematenda.estoque.mapper;

import br.com.sistematenda.estoque.dto.MovimentoEstoqueDTO;
import br.com.sistematenda.estoque.entity.MovimentoEstoque;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.repository.ProdutoRepository;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.repository.ProdutoBuffetRepository;
import br.com.sistematenda.common.mapper.BaseMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MovimentoEstoqueMapper implements BaseMapper<MovimentoEstoque, MovimentoEstoqueDTO> {

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    ProdutoBuffetRepository produtoBuffetRepository;

    @Override
    public MovimentoEstoqueDTO toDTO(MovimentoEstoque entity) {
        if (entity == null) return null;
        MovimentoEstoqueDTO dto = new MovimentoEstoqueDTO();
        dto.setId(entity.id);
        dto.setProdutoFruteiraId(entity.produtoFruteira != null ? entity.produtoFruteira.id : null);
        dto.setProdutoBuffetId(entity.produtoBuffet != null ? entity.produtoBuffet.id : null);
        dto.setTipo(entity.tipo);
        dto.setOrigem(entity.origem);
        dto.setDestino(entity.destino);
        dto.setQuantidade(entity.quantidade);
        dto.setCustoUnitario(entity.custoUnitario);
        dto.setValorTotal(entity.valorTotal);
        dto.setEstoqueAnterior(entity.estoqueAnterior);
        dto.setEstoquePosterior(entity.estoquePosterior);
        dto.setLote(entity.lote);
        dto.setDataValidade(entity.dataValidade);
        dto.setDocumentoReferencia(entity.documentoReferencia);
        dto.setNumeroDocumento(entity.numeroDocumento);
        dto.setObservacoes(entity.observacoes);
        dto.setMotivoDefeito(entity.motivoDefeito);
        dto.setTipoDefeito(entity.tipoDefeito);
        dto.setLoteDefeituoso(entity.loteDefeituoso);
        dto.setQuantidadeRecuperavel(entity.quantidadeRecuperavel);
        dto.setResponsavel(entity.responsavel);
        dto.setData(entity.data);
        return dto;
    }

    @Override
    public MovimentoEstoque toEntity(MovimentoEstoqueDTO dto) {
        if (dto == null) return null;
        MovimentoEstoque entity = new MovimentoEstoque();
        entity.id = dto.getId();
        
        if (dto.getProdutoFruteiraId() != null) {
            entity.produtoFruteira = produtoRepository.findById(dto.getProdutoFruteiraId());
        }
        if (dto.getProdutoBuffetId() != null) {
            entity.produtoBuffet = produtoBuffetRepository.findById(dto.getProdutoBuffetId());
        }
        
        entity.tipo = dto.getTipo();
        entity.origem = dto.getOrigem();
        entity.destino = dto.getDestino();
        entity.quantidade = dto.getQuantidade();
        entity.custoUnitario = dto.getCustoUnitario();
        entity.valorTotal = dto.getValorTotal();
        entity.estoqueAnterior = dto.getEstoqueAnterior();
        entity.estoquePosterior = dto.getEstoquePosterior();
        entity.lote = dto.getLote();
        entity.dataValidade = dto.getDataValidade();
        entity.documentoReferencia = dto.getDocumentoReferencia();
        entity.numeroDocumento = dto.getNumeroDocumento();
        entity.observacoes = dto.getObservacoes();
        entity.motivoDefeito = dto.getMotivoDefeito();
        entity.tipoDefeito = dto.getTipoDefeito();
        entity.loteDefeituoso = dto.getLoteDefeituoso();
        entity.quantidadeRecuperavel = dto.getQuantidadeRecuperavel();
        entity.responsavel = dto.getResponsavel();
        entity.data = dto.getData();
        return entity;
    }

    @Override
    public void updateEntityFromDTO(MovimentoEstoqueDTO dto, MovimentoEstoque entity) {
        if (dto == null || entity == null) return;
        
        if (dto.getProdutoFruteiraId() != null) {
            entity.produtoFruteira = produtoRepository.findById(dto.getProdutoFruteiraId());
        }
        if (dto.getProdutoBuffetId() != null) {
            entity.produtoBuffet = produtoBuffetRepository.findById(dto.getProdutoBuffetId());
        }
        
        entity.tipo = dto.getTipo();
        entity.origem = dto.getOrigem();
        entity.destino = dto.getDestino();
        entity.quantidade = dto.getQuantidade();
        entity.custoUnitario = dto.getCustoUnitario();
        entity.valorTotal = dto.getValorTotal();
        entity.estoqueAnterior = dto.getEstoqueAnterior();
        entity.estoquePosterior = dto.getEstoquePosterior();
        entity.lote = dto.getLote();
        entity.dataValidade = dto.getDataValidade();
        entity.documentoReferencia = dto.getDocumentoReferencia();
        entity.numeroDocumento = dto.getNumeroDocumento();
        entity.observacoes = dto.getObservacoes();
        entity.motivoDefeito = dto.getMotivoDefeito();
        entity.tipoDefeito = dto.getTipoDefeito();
        entity.loteDefeituoso = dto.getLoteDefeituoso();
        entity.quantidadeRecuperavel = dto.getQuantidadeRecuperavel();
        entity.responsavel = dto.getResponsavel();
        entity.data = dto.getData();
    }
}