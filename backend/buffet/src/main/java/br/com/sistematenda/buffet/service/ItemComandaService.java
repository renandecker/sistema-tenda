package br.com.sistematenda.buffet.service;

import br.com.sistematenda.buffet.dto.ItemComandaDTO;
import br.com.sistematenda.buffet.dto.ProdutoBuffetDTO;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.entity.ItemComanda;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.mapper.ItemComandaMapper;
import br.com.sistematenda.buffet.repository.ItemComandaRepository;
import br.com.sistematenda.buffet.repository.ComandaRepository;
import br.com.sistematenda.common.dto.PageResponse;
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
public class ItemComandaService extends BaseCrudServiceImpl<ItemComanda, ItemComandaDTO, UUID> {

    @Inject
    ItemComandaRepository repository;

    @Inject
    ComandaRepository comandaRepository;

    @Inject
    ProdutoBuffetService produtoBuffetService;

    @Override
    public ItemComandaRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<ItemComanda> getEntityClass() {
        return ItemComanda.class;
    }

    public List<ItemComandaDTO> findByComanda(UUID comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        return repository.findByComanda(comanda).stream().map(mapper::toDTO).toList();
    }

    public PageResponse<ItemComandaDTO> findByComandaPaginated(UUID comandaId, int page, int size,
                                                                String sortBy, String sortDirection) {
        Comanda comanda = comandaRepository.findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        return toDtoPage(repository.findByComandaPaginated(comanda, page, size, sortBy, sortDirection));
    }

    public BigDecimal getTotalByComanda(UUID comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        return repository.getTotalByComanda(comanda);
    }

    @Override
    @Transactional
    public ItemComandaDTO create(@Valid ItemComandaDTO dto) {
        validateCreate(dto);
        
        Comanda comanda = comandaRepository.findById(UUID.fromString(dto.getComanda().getId()));
        if (comanda == null) {
            throw new NotFoundException("Comanda", dto.getComanda().getId());
        }
        
        if (comanda.isFechada()) {
            throw new BusinessException("COMANDA_FECHADA", "Não é possível adicionar itens a uma comanda fechada");
        }
        
        ItemComanda entity = mapper.toEntity(dto);
        entity.comanda = comanda;
        entity.dataLancamento = LocalDateTime.now();
        entity.lancadoPor = "SISTEMA"; // TODO: usuário logado
        
        // Se for produto do buffet, buscar preço
        if (dto.getProdutoId() != null) {
            ProdutoBuffet produto = produtoBuffetService.getRepository().findById(
                java.util.UUID.fromString(dto.getProdutoId()));
            if (produto != null) {
                entity.precoUnitario = produto.precoVenda;
                entity.codigoProduto = produto.codigo;
                entity.nomeProduto = produto.nome;
            }
        }
        
        // Calcular peso líquido se tiver peso bruto e tara
        if (entity.pesoBruto != null && entity.tara != null) {
            entity.pesoLiquido = entity.pesoBruto.subtract(entity.tara);
            if (entity.pesoLiquido.compareTo(BigDecimal.ZERO) < 0) {
                entity.pesoLiquido = BigDecimal.ZERO;
            }
        }
        
        // Para itens por peso, quantidade = peso líquido
        if (entity.tipo == ItemComanda.TipoItem.PRATO_BUFFET && entity.pesoLiquido != null) {
            entity.quantidade = entity.pesoLiquido;
        }
        
        entity.valorTotal = entity.precoUnitario.multiply(entity.quantidade);
        
        getRepository().persist(entity);
        
        // Atualizar subtotal da comanda
        atualizarSubtotalComanda(comanda);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public ItemComandaDTO lancarPratoBalanca(UUID comandaId, BigDecimal pesoBruto, BigDecimal tara) {
        Comanda comanda = comandaRepository.findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        
        if (comanda.isFechada()) {
            throw new BusinessException("COMANDA_FECHADA", "Comanda está fechada");
        }
        
        if (comanda.precoKg == null || comanda.precoKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("PRECO_KG_NAO_DEFINIDO", "Preço por kg não definido para esta comanda");
        }
        
        BigDecimal pesoLiquido = pesoBruto.subtract(tara != null ? tara : comanda.taraPadrao);
        if (pesoLiquido.compareTo(BigDecimal.ZERO) < 0) {
            pesoLiquido = BigDecimal.ZERO;
        }

        ItemComandaDTO dto = new ItemComandaDTO();
        dto.setComanda(new ItemComandaDTO.ComandaResumoDTO());
        dto.getComanda().setId(comandaId.toString());
        dto.setTipo(ItemComanda.TipoItem.PRATO_BUFFET);
        dto.setNomeProduto("Prato Buffet (Peso)");
        dto.setQuantidade(pesoLiquido);
        dto.setPesoBruto(pesoBruto);
        dto.setTara(tara != null ? tara : comanda.taraPadrao);
        dto.setPesoLiquido(pesoLiquido);
        dto.setPrecoUnitario(comanda.precoKg);
        
        return create(dto);
    }

    @Transactional
    public ItemComandaDTO lancarProdutoBuffet(UUID comandaId, UUID produtoId, BigDecimal quantidade) {
        ProdutoBuffetDTO produto = produtoBuffetService.findById(produtoId);
        
        if (produto.getControlaEstoque() && produto.getEstoqueAtual().compareTo(quantidade) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", 
                "Estoque insuficiente. Disponível: " + produto.getEstoqueAtual());
        }
        
        ItemComandaDTO dto = new ItemComandaDTO();
        dto.setComanda(new ItemComandaDTO.ComandaResumoDTO());
        dto.getComanda().setId(comandaId.toString());
        dto.setProdutoId(produtoId.toString());
        dto.setTipo(mapTipoFromCategoria(produto.getCategoria()));
        dto.setQuantidade(quantidade);
        dto.setPrecoUnitario(produto.getPrecoVenda());
        
        ItemComandaDTO criado = create(dto);
        
        // Baixar estoque se controla
        if (produto.getControlaEstoque()) {
            produtoBuffetService.atualizarEstoque(produtoId, quantidade.negate());
        }
        
        return criado;
    }

    @Transactional
    public void cancelarItem(UUID itemId, String motivo) {
        ItemComanda entity = getRepository().findById(itemId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Item de comanda", itemId);
        }
        
        if (entity.cancelado) {
            throw new BusinessException("ITEM_JA_CANCELADO", "Item já está cancelado");
        }
        
        if (entity.comanda.isFechada()) {
            throw new BusinessException("COMANDA_FECHADA", "Não é possível cancelar item de comanda fechada");
        }
        
        entity.cancelado = true;
        entity.motivoCancelamento = motivo;
        entity.canceladoPor = "SISTEMA"; // TODO: usuário logado
        entity.dataCancelamento = LocalDateTime.now();
        getRepository().persist(entity);
        
        // Atualizar subtotal da comanda
        atualizarSubtotalComanda(entity.comanda);
    }

    @Transactional
    public void cancelarItensComanda(UUID comandaId, String motivo) {
        Comanda comanda = comandaRepository.findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        
        List<ItemComanda> itens = repository.findByComanda(comanda);
        for (ItemComanda item : itens) {
            if (!item.cancelado) {
                item.cancelado = true;
                item.motivoCancelamento = motivo;
                item.canceladoPor = "SISTEMA";
                item.dataCancelamento = LocalDateTime.now();
                getRepository().persist(item);
            }
        }
    }

    private void atualizarSubtotalComanda(Comanda comanda) {
        BigDecimal subtotal = repository.getTotalByComanda(comanda);
        comanda.subtotal = subtotal;
        comanda.valorTotal = subtotal
            .subtract(comanda.desconto != null ? comanda.desconto : BigDecimal.ZERO)
            .add(comanda.acrescimo != null ? comanda.acrescimo : BigDecimal.ZERO);
        comandaRepository.persist(comanda);
    }

    private void validateCreate(ItemComandaDTO dto) {
        if (dto.getComanda() == null || dto.getComanda().getId() == null) {
            throw new ValidationException("Comanda é obrigatória");
        }
        if (dto.getTipo() == null) {
            throw new ValidationException("Tipo do item é obrigatório");
        }
    }

    private ItemComanda.TipoItem mapTipoFromCategoria(ProdutoBuffet.Categoria categoria) {
        return switch (categoria) {
            case PRATO_QUENTE, PRATO_FRIO, SALADA, GUARNICAO -> ItemComanda.TipoItem.PRATO_BUFFET;
            case SOBREMESA -> ItemComanda.TipoItem.SOBREMESA;
            case BEBIDA, BEBIDA_ALCOOLICA -> ItemComanda.TipoItem.BEBIDA;
            case ADICIONAL -> ItemComanda.TipoItem.ADICIONAL;
            case PRATO_FEITO -> ItemComanda.TipoItem.PRATO_FEITO;
            case EXECUTIVO -> ItemComanda.TipoItem.PRATO_FEITO;
            default -> ItemComanda.TipoItem.OUTROS;
        };
    }
}