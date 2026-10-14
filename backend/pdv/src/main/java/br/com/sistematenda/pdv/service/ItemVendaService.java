package br.com.sistematenda.pdv.service;

import br.com.sistematenda.pdv.dto.ItemVendaDTO;
import br.com.sistematenda.pdv.entity.ItemVenda;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.pdv.mapper.ItemVendaMapper;
import br.com.sistematenda.pdv.repository.ItemVendaRepository;
import br.com.sistematenda.pdv.repository.VendaRepository;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.ItemComanda;
import br.com.sistematenda.buffet.service.ComandaService;
import br.com.sistematenda.buffet.service.ItemComandaService;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.service.ProdutoBuffetService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.FormaPagamento;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ItemVendaService extends BaseCrudServiceImpl<ItemVenda, ItemVendaDTO, UUID> {

    @Inject
    ItemVendaRepository repository;

    @Inject
    ItemVendaMapper mapper;

    @Inject
    VendaRepository vendaRepository;

    @Inject
    ProdutoService produtoFruteiraService;

    @Inject
    ProdutoBuffetService produtoBuffetService;

    @Inject
    ComandaService comandaService;

    @Inject
    ItemComandaService itemComandaService;

    @Override
    protected ItemVendaRepository getRepository() {
        return repository;
    }

    protected ItemVendaMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<ItemVenda> getEntityClass() {
        return ItemVenda.class;
    }

    public List<ItemVendaDTO> findByVenda(UUID vendaId) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        return repository.findByVenda(venda).stream().map(mapper::toDTO).toList();
    }

    public PageResponse<ItemVendaDTO> findByVendaPaginated(UUID vendaId, int page, int size,
                                                           String sortBy, String sortDirection) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        return toDtoPage(repository.findByVendaPaginated(venda, page, size, sortBy, sortDirection));
    }

    @Transactional
    public ItemVendaDTO adicionarItemFruteira(UUID vendaId, UUID produtoId, BigDecimal quantidade) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        Produto produto = produtoFruteiraService.getRepository().findById(produtoId);
        if (produto == null) {
            throw new NotFoundException("Produto", produtoId);
        }
        
        if (produto.estoqueAtual.compareTo(quantidade) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", 
                "Estoque insuficiente. Disponível: " + produto.estoqueAtual);
        }
        
        ItemVenda entity = new ItemVenda();
        entity.venda = venda;
        entity.origem = ItemVenda.OrigemItem.FRUTEIRA;
        entity.produtoFruteira = produto;
        entity.codigoProduto = produto.codigo;
        entity.nomeProduto = produto.nome;
        entity.tipo = mapTipoFromFruteira(produto.tipo);
        entity.quantidade = quantidade;
        entity.precoUnitario = produto.precoVenda;
        entity.valorTotal = entity.precoUnitario.multiply(quantidade);
        
        getRepository().persist(entity);
        
        // Atualizar totais da venda
        atualizarTotaisVenda(venda);
        
        // Baixar estoque
        produtoFruteiraService.atualizarEstoque(produtoId, quantidade.negate());
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ItemVendaDTO adicionarItemBuffet(UUID vendaId, UUID produtoId, BigDecimal quantidade) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        ProdutoBuffet produto = produtoBuffetService.getRepository().findById(produtoId);
        if (produto == null) {
            throw new NotFoundException("Produto Buffet", produtoId);
        }
        
        if (produto.controlaEstoque && produto.estoqueAtual.compareTo(quantidade) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", 
                "Estoque insuficiente. Disponível: " + produto.estoqueAtual);
        }
        
        ItemVenda entity = new ItemVenda();
        entity.venda = venda;
        entity.origem = ItemVenda.OrigemItem.BUFFET;
        entity.produtoBuffet = produto;
        entity.codigoProduto = produto.codigo;
        entity.nomeProduto = produto.nome;
        entity.tipo = mapTipoFromBuffet(produto.categoria);
        entity.quantidade = quantidade;
        entity.precoUnitario = produto.precoVenda;
        entity.valorTotal = entity.precoUnitario.multiply(quantidade);
        
        getRepository().persist(entity);
        
        atualizarTotaisVenda(venda);
        
        if (produto.controlaEstoque) {
            produtoBuffetService.atualizarEstoque(produtoId, quantidade.negate());
        }
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ItemVendaDTO adicionarPratoBalanca(UUID vendaId, BigDecimal pesoBruto, BigDecimal tara) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        // Usar preço/kg padrão ou configurar via parâmetro
        BigDecimal precoKg = venda.comanda != null ? venda.comanda.precoKg : null;
        if (precoKg == null || precoKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("PRECO_KG_NAO_DEFINIDO", "Preço por kg não definido");
        }
        
        BigDecimal pesoLiquido = pesoBruto.subtract(tara != null ? tara : BigDecimal.ZERO);
        if (pesoLiquido.compareTo(BigDecimal.ZERO) < 0) {
            pesoLiquido = BigDecimal.ZERO;
        }
        
        ItemVenda entity = new ItemVenda();
        entity.venda = venda;
        entity.origem = ItemVenda.OrigemItem.BUFFET;
        entity.codigoProduto = "PRATO_BALANCA";
        entity.nomeProduto = "Prato Buffet (Peso)";
        entity.tipo = ItemVenda.TipoItem.PRATO_BUFFET;
        entity.quantidade = pesoLiquido;
        entity.pesoBruto = pesoBruto;
        entity.tara = tara;
        entity.pesoLiquido = pesoLiquido;
        entity.precoUnitario = precoKg;
        entity.valorTotal = entity.precoUnitario.multiply(pesoLiquido);
        
        getRepository().persist(entity);
        atualizarTotaisVenda(venda);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ItemVendaDTO adicionarItensComanda(UUID vendaId, UUID comandaId) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null || venda.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        Comanda comanda = comandaService.getRepository().findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        
        List<ItemComanda> itensComanda = itemComandaService.getRepository().findByComanda(comanda);
        for (ItemComanda item : itensComanda) {
            if (item.cancelado) continue;
            
            ItemVenda entity = new ItemVenda();
            entity.venda = venda;
            entity.origem = ItemVenda.OrigemItem.BUFFET;
            entity.codigoProduto = item.codigoProduto;
            entity.nomeProduto = item.nomeProduto;
            entity.tipo = mapTipoFromComanda(item.tipo);
            entity.quantidade = item.quantidade;
            entity.pesoBruto = item.pesoBruto;
            entity.tara = item.tara;
            entity.pesoLiquido = item.pesoLiquido;
            entity.precoUnitario = item.precoUnitario;
            entity.valorTotal = item.valorTotal;
            
            getRepository().persist(entity);
        }
        
        atualizarTotaisVenda(venda);
        
        // Marcar comanda como paga
        comanda.status = br.com.sistematenda.common.enums.StatusComanda.PAGA;
        comanda.dataPagamento = LocalDateTime.now();
        comanda.valorPago = comanda.valorTotal;
        comandaService.getRepository().persist(comanda);
        
        venda.comanda = comanda;
        vendaRepository.persist(venda);
        
        return null; // Retornar o último item ou null
    }

    @Transactional
    public void cancelarItem(UUID itemId, String motivo) {
        ItemVenda entity = getRepository().findById(itemId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Item de venda", itemId);
        }
        
        if (entity.cancelado) {
            throw new BusinessException("ITEM_JA_CANCELADO", "Item já está cancelado");
        }
        
        if (entity.venda.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível cancelar item de venda finalizada");
        }
        
        entity.cancelado = true;
        getRepository().persist(entity);
        
        atualizarTotaisVenda(entity.venda);
    }

    @Transactional
    public void cancelarItensVenda(UUID vendaId, String motivo) {
        Venda venda = vendaRepository.findById(vendaId);
        if (venda == null) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        List<ItemVenda> itens = repository.findByVenda(venda);
        for (ItemVenda item : itens) {
            if (!item.cancelado) {
                item.cancelado = true;
                getRepository().persist(item);
            }
        }
    }

    private void atualizarTotaisVenda(Venda venda) {
        BigDecimal totalFruteira = repository.getTotalByOrigemPeriodo(
            ItemVenda.OrigemItem.FRUTEIRA, 
            venda.data.minusDays(1), 
            venda.data.plusDays(1)
        );
        
        BigDecimal totalBuffet = repository.getTotalByOrigemPeriodo(
            ItemVenda.OrigemItem.BUFFET, 
            venda.data.minusDays(1), 
            venda.data.plusDays(1)
        );
        
        // Recalcular apenas itens desta venda
        totalFruteira = repository.findByVenda(venda).stream()
            .filter(i -> i.origem == ItemVenda.OrigemItem.FRUTEIRA && !i.cancelado)
            .map(i -> i.valorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalBuffet = repository.findByVenda(venda).stream()
            .filter(i -> i.origem == ItemVenda.OrigemItem.BUFFET && !i.cancelado)
            .map(i -> i.valorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        venda.subtotalFruteira = totalFruteira;
        venda.subtotalBuffet = totalBuffet;
        venda.subtotal = totalFruteira.add(totalBuffet);
        venda.valorTotal = venda.subtotal
            .subtract(venda.desconto != null ? venda.desconto : BigDecimal.ZERO)
            .add(venda.acrescimo != null ? venda.acrescimo : BigDecimal.ZERO);
        
        vendaRepository.persist(venda);
    }

    private ItemVenda.TipoItem mapTipoFromFruteira(br.com.sistematenda.common.enums.TipoProduto tipo) {
        return switch (tipo) {
            case FRUTA, VERDURA, LEGUME -> ItemVenda.TipoItem.PRODUTO;
            case BEBIDA -> ItemVenda.TipoItem.BEBIDA;
            default -> ItemVenda.TipoItem.PRODUTO;
        };
    }

    private ItemVenda.TipoItem mapTipoFromBuffet(br.com.sistematenda.buffet.entity.ProdutoBuffet.Categoria categoria) {
        return switch (categoria) {
            case PRATO_QUENTE, PRATO_FRIO, SALADA, GUARNICAO -> ItemVenda.TipoItem.PRATO_BUFFET;
            case SOBREMESA -> ItemVenda.TipoItem.SOBREMESA;
            case BEBIDA, BEBIDA_ALCOOLICA -> ItemVenda.TipoItem.BEBIDA;
            case ADICIONAL -> ItemVenda.TipoItem.ADICIONAL;
            case PRATO_FEITO, EXECUTIVO -> ItemVenda.TipoItem.PRATO_FEITO;
            default -> ItemVenda.TipoItem.OUTROS;
        };
    }

    private ItemVenda.TipoItem mapTipoFromComanda(br.com.sistematenda.buffet.entity.ItemComanda.TipoItem tipo) {
        return switch (tipo) {
            case PRATO_BUFFET -> ItemVenda.TipoItem.PRATO_BUFFET;
            case BEBIDA -> ItemVenda.TipoItem.BEBIDA;
            case SOBREMESA -> ItemVenda.TipoItem.SOBREMESA;
            case ADICIONAL -> ItemVenda.TipoItem.ADICIONAL;
            case PRATO_FEITO -> ItemVenda.TipoItem.PRATO_FEITO;
            default -> ItemVenda.TipoItem.OUTROS;
        };
    }
}