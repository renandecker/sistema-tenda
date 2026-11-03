package br.com.sistematenda.pdv.service;

import br.com.sistematenda.pdv.dto.VendaDTO;
import br.com.sistematenda.pdv.entity.Venda;
import br.com.sistematenda.pdv.entity.ItemVenda;
import br.com.sistematenda.pdv.mapper.VendaMapper;
import br.com.sistematenda.pdv.entity.Pagamento;
import br.com.sistematenda.pdv.repository.PagamentoRepository;
import br.com.sistematenda.pdv.repository.VendaRepository;
import br.com.sistematenda.pdv.repository.ItemVendaRepository;
import br.com.sistematenda.buffet.entity.Comanda;
import br.com.sistematenda.buffet.service.ComandaService;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.buffet.entity.ProdutoBuffet;
import br.com.sistematenda.buffet.service.ProdutoBuffetService;
import br.com.sistematenda.fiscal.dto.NfceRequest;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.service.NfceService;
import br.com.sistematenda.common.dto.PageResponse;
import br.com.sistematenda.common.enums.FormaPagamento;
import br.com.sistematenda.common.enums.StatusVenda;
import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import br.com.sistematenda.common.exception.ValidationException;
import br.com.sistematenda.common.service.BaseCrudServiceImpl;
import br.com.sistematenda.common.util.CpfUtils;
import br.com.sistematenda.common.util.ChaveAcessoUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VendaService extends BaseCrudServiceImpl<Venda, VendaDTO, UUID> {

    @Inject
    VendaRepository repository;

    @Inject
    VendaMapper mapper;

    @Inject
    ItemVendaRepository itemRepository;

    @Inject
    ItemVendaService itemVendaService;

    @Inject
    PagamentoService pagamentoService;

    @Inject
    ComandaService comandaService;

    @Inject
    ProdutoService produtoFruteiraService;

    @Inject
    ProdutoBuffetService produtoBuffetService;

    @Inject
    PdvService pdvService;

    @Inject
    NfceService nfceService;

    @Inject
    FiscalConfig fiscalConfig;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Override
    protected VendaRepository getRepository() {
        return repository;
    }

    protected VendaMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<Venda> getEntityClass() {
        return Venda.class;
    }

    @Override
    protected String getEntidadeNome() {
        return getEntityClass().getSimpleName();
    }

    public VendaDTO findByNumero(String numero) {
        return repository.findByNumero(numero)
            .map(mapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Venda", numero));
    }

    public VendaDTO findByComanda(UUID comandaId) {
        Comanda comanda = comandaService.getRepository().findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        return repository.findByComanda(comanda)
            .map(mapper::toDTO)
            .orElse(null);
    }

    @Override
    public PageResponse<VendaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null, null, null));
    }

    public PageResponse<VendaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                          StatusVenda status, LocalDate dataInicio, LocalDate dataFim,
                                          UUID pdvId, String clienteDocumento) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, status, dataInicio, dataFim, pdvId, clienteDocumento));
    }

    @Override
    @Transactional
    public VendaDTO create(@Valid VendaDTO dto) {
        validateCreate(dto);
        
        Venda entity = mapper.toEntity(dto);
        entity.numero = repository.getProximoNumero(dto.getPdvId());
        entity.data = LocalDateTime.now();
        entity.status = StatusVenda.ABERTA;
        entity.subtotalFruteira = BigDecimal.ZERO;
        entity.subtotalBuffet = BigDecimal.ZERO;
        entity.subtotal = BigDecimal.ZERO;
        entity.desconto = BigDecimal.ZERO;
        entity.acrescimo = BigDecimal.ZERO;
        entity.valorTotal = BigDecimal.ZERO;
        entity.valorPago = BigDecimal.ZERO;
        entity.troco = BigDecimal.ZERO;
        entity.operador = "SISTEMA"; // TODO: usuário logado
        
        if (dto.getComanda() != null && dto.getComanda().getId() != null) {
            entity.comanda = comandaService.getRepository().findById(UUID.fromString(dto.getComanda().getId()));
            if (entity.comanda == null) {
                throw new NotFoundException("Comanda", dto.getComanda().getId());
            }
            if (entity.comanda.isFechada()) {
                throw new BusinessException("COMANDA_FECHADA", "Comanda já está fechada");
            }
        }
        
        if (dto.getPdvId() != null) {
            entity.pdvId = dto.getPdvId();
        }
        
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public VendaDTO adicionarItemFruteira(UUID vendaId, UUID produtoId, BigDecimal quantidade) {
        VendaDTO venda = findById(vendaId);
        if (venda.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível adicionar itens a uma venda finalizada");
        }
        
        itemVendaService.adicionarItemFruteira(vendaId, produtoId, quantidade);
        return findById(vendaId);
    }

    @Transactional
    public VendaDTO adicionarItemBuffet(UUID vendaId, UUID produtoId, BigDecimal quantidade) {
        VendaDTO venda = findById(vendaId);
        if (venda.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível adicionar itens a uma venda finalizada");
        }
        
        itemVendaService.adicionarItemBuffet(vendaId, produtoId, quantidade);
        return findById(vendaId);
    }

    @Transactional
    public VendaDTO adicionarPratoBalanca(UUID vendaId, BigDecimal pesoBruto, BigDecimal tara) {
        VendaDTO venda = findById(vendaId);
        if (venda.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível adicionar itens a uma venda finalizada");
        }
        
        itemVendaService.adicionarPratoBalanca(vendaId, pesoBruto, tara);
        return findById(vendaId);
    }

    @Transactional
    public VendaDTO adicionarItemComanda(UUID vendaId, UUID comandaId) {
        VendaDTO venda = findById(vendaId);
        if (venda.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível adicionar itens a uma venda finalizada");
        }
        
        Comanda comanda = comandaService.getRepository().findById(comandaId);
        if (comanda == null) {
            throw new NotFoundException("Comanda", comandaId);
        }
        
        if (!comanda.isFechada()) {
            throw new BusinessException("COMANDA_NAO_FECHADA", "Comanda deve estar fechada para ser adicionada à venda");
        }
        
        // Transferir itens da comanda para a venda
        itemVendaService.adicionarItensComanda(vendaId, comandaId);
        return findById(vendaId);
    }

    @Transactional
    public VendaDTO aplicarDesconto(UUID vendaId, BigDecimal desconto) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        if (entity.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível alterar venda finalizada");
        }
        
        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Desconto não pode ser negativo");
        }
        
        if (desconto.compareTo(entity.subtotal) > 0) {
            throw new ValidationException("Desconto não pode ser maior que o subtotal");
        }
        
        entity.desconto = desconto;
        recalcularTotais(entity);
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public VendaDTO aplicarAcrescimo(UUID vendaId, BigDecimal acrescimo) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        if (entity.isFinalizada()) {
            throw new BusinessException("VENDA_FINALIZADA", "Não é possível alterar venda finalizada");
        }
        
        if (acrescimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Acréscimo não pode ser negativo");
        }
        
        entity.acrescimo = acrescimo;
        recalcularTotais(entity);
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public VendaDTO processarPagamento(UUID vendaId, PagamentoRequest request) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        if (entity.isPaga()) {
            throw new BusinessException("VENDA_JA_PAGA", "Venda já está paga");
        }
        
        return pagamentoService.processarPagamento(vendaId, request);
    }

@Transactional
    public VendaDTO finalizarVenda(UUID vendaId) {
        return finalizarVenda(vendaId, null);
    }

    @Transactional
    public VendaDTO finalizarVenda(UUID vendaId, String cpfConsumidor) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }

        if (entity.isFinalizada()) {
            throw new BusinessException("VENDA_JA_FINALIZADA", "Venda já está finalizada");
        }

        if (cpfConsumidor != null && !cpfConsumidor.isBlank()) {
            String cpf = CpfUtils.clean(cpfConsumidor);
            if (!CpfUtils.isValid(cpf)) {
                throw new BusinessException("CPF_INVALIDO", "CPF do consumidor inválido");
            }
            entity.clienteDocumento = cpf;
        }

        if (entity.valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("VENDA_SEM_VALOR", "Venda não possui itens");
        }

        BigDecimal totalPago = pagamentoService.getTotalPago(vendaId);
        if (totalPago.compareTo(entity.valorTotal) < 0) {
            throw new BusinessException("PAGAMENTO_INCOMPLETO",
                "Valor pago (" + totalPago + ") menor que total (" + entity.valorTotal + ")");
        }

        entity.status = StatusVenda.FINALIZADA;
        entity.dataFinalizacao = LocalDateTime.now();
        entity.troco = totalPago.subtract(entity.valorTotal);
        entity.valorPago = totalPago;

        // Verificar se o cliente pagou com o CNPJ da empresa e emitir NFC-e automaticamente
        if (entity.clienteDocumento != null && entity.clienteDocumento.length() == 14) {
            String cnpjEmpresa = ChaveAcessoUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
            if (cnpjEmpresa.equals(entity.clienteDocumento)) {
                // Cliente pagou com CNPJ da empresa - emitir NFC-e automaticamente
                try {
                    NfceRequest request = montarNfceRequest(entity);
                    Nfe nfe = nfceService.emitirNfce(request);

                    entity.nfce = nfe;
                    entity.numeroNfce = nfe.numero.toString();
                    entity.chaveNfce = nfe.chaveAcesso;
                    entity.xmlNfce = nfe.xmlAutorizado != null ? nfe.xmlAutorizado : nfe.xmlOriginal;
                    entity.danfeNfce = nfe.danfe;
                    getRepository().persist(entity);
                } catch (BusinessException e) {
                    // Se falhar em emitir NFC-e, continua sem NFC-e mas marca como finalizada
                    getRepository().persist(entity);
                    throw e;
                }
            }
        }

        // Se tiver comanda, marcar como paga
        if (entity.comanda != null) {
            entity.comanda.status = br.com.sistematenda.common.enums.StatusComanda.PAGA;
            entity.comanda.dataPagamento = LocalDateTime.now();
            entity.comanda.valorPago = totalPago;
            comandaService.getRepository().persist(entity.comanda);
        }

        // Snapshot do comprovante para permitir reimpressão mesmo se a impressora falhar.
        entity.cupomImpressao = gerarCupomImpressao(entity);
        getRepository().persist(entity);

        return mapper.toDTO(entity);
    }

    @Transactional
    public VendaDTO emitirNfceVenda(UUID vendaId, String cpfConsumidor) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        if (!entity.isFinalizada()) {
            throw new BusinessException("VENDA_NAO_FINALIZADA", "Finalize a venda antes de emitir a NFC-e");
        }
        if (entity.nfce != null
            && entity.nfce.status != Nfe.StatusNfe.CANCELADA
            && entity.nfce.status != Nfe.StatusNfe.REJEITADA) {
            throw new BusinessException("NFCE_JA_EXISTE", "Esta venda já possui NFC-e em andamento ou autorizada");
        }

        if (cpfConsumidor != null && !cpfConsumidor.isBlank()) {
            String cpf = CpfUtils.clean(cpfConsumidor);
            if (!CpfUtils.isValid(cpf)) {
                throw new BusinessException("CPF_INVALIDO", "CPF do consumidor inválido");
            }
            entity.clienteDocumento = cpf;
        }

        NfceRequest request = montarNfceRequest(entity);
        Nfe nfe = nfceService.emitirNfce(request);

        entity.nfce = nfe;
        entity.numeroNfce = nfe.numero.toString();
        entity.chaveNfce = nfe.chaveAcesso;
        entity.xmlNfce = nfe.xmlAutorizado != null ? nfe.xmlAutorizado : nfe.xmlOriginal;
        entity.danfeNfce = nfe.danfe;
        // Atualiza o snapshot já com os dados fiscais (número/chave da NFC-e).
        entity.cupomImpressao = gerarCupomImpressao(entity);
        getRepository().persist(entity);

        return mapper.toDTO(entity);
    }

    private NfceRequest montarNfceRequest(Venda entity) {
        NfceRequest request = new NfceRequest();
        request.setVendaId(entity.id);
        request.setClienteNome(entity.clienteNome);
        request.setClienteDocumento(entity.clienteDocumento);
        request.setValorTotal(entity.valorTotal);
        request.setSubtotal(entity.subtotal);
        request.setDesconto(entity.desconto);
        request.setAcrescimo(entity.acrescimo);
        request.setValorTroco(entity.troco);
        request.setNaturezaOperacao("VENDA");
        request.setInformacoesComplementares(entity.observacoes);
        request.setItens(montarItensNfce(entity));
        request.setPagamentos(montarPagamentosNfce(entity));
        return request;
    }

    private List<NfceRequest.ItemNfceRequest> montarItensNfce(Venda entity) {
        List<NfceRequest.ItemNfceRequest> itens = new ArrayList<>();
        if (entity.itens == null) {
            return itens;
        }

        int numeroItem = 1;
        for (ItemVenda item : entity.itens) {
            if (item.cancelado) {
                continue;
            }

            String ncm = null;
            String cstIcms = null;
            String cstPis = null;
            String cstCofins = null;
            String unidade = "UN";
            Integer origem = 0;

            if (item.produtoFruteira != null) {
                Produto produto = item.produtoFruteira;
                ncm = produto.ncm;
                cstIcms = produto.cstIcms != null ? produto.cstIcms.getCodigo() : null;
                cstPis = produto.cstPis != null ? produto.cstPis.getCodigo() : null;
                cstCofins = produto.cstCofins != null ? produto.cstCofins.getCodigo() : null;
                unidade = produto.unidadeMedida != null ? produto.unidadeMedida.getSigla() : "UN";
                origem = produto.origemMercadoria;
            } else if (item.produtoBuffet != null) {
                ProdutoBuffet produto = item.produtoBuffet;
                ncm = produto.ncm;
                cstIcms = produto.cstIcms != null ? produto.cstIcms.getCodigo() : null;
                cstPis = produto.cstPis != null ? produto.cstPis.getCodigo() : null;
                cstCofins = produto.cstCofins != null ? produto.cstCofins.getCodigo() : null;
                unidade = produto.unidadeMedida != null ? produto.unidadeMedida.getSigla() : "UN";
                origem = produto.origemMercadoria;
            }

            NfceRequest.ItemNfceRequest nfceItem = new NfceRequest.ItemNfceRequest();
            nfceItem.setNumeroItem(numeroItem++);
            nfceItem.setCodigoProduto(item.codigoProduto);
            nfceItem.setDescricao(item.nomeProduto);
            nfceItem.setNcm(ncm);
            nfceItem.setCstIcms(cstIcms);
            nfceItem.setCstPis(cstPis);
            nfceItem.setCstCofins(cstCofins);
            nfceItem.setUnidadeComercial(unidade);
            nfceItem.setQuantidade(item.quantidade);
            nfceItem.setValorUnitario(item.precoUnitario);
            nfceItem.setValorTotal(item.valorTotal);
            nfceItem.setOrigemMercadoria(origem);
            itens.add(nfceItem);
        }
        return itens;
    }

    private List<NfceRequest.PagamentoNfceRequest> montarPagamentosNfce(Venda entity) {
        List<NfceRequest.PagamentoNfceRequest> pagamentos = new ArrayList<>();
        for (Pagamento pagamento : pagamentoRepository.findByVenda(entity)) {
            if (pagamento.status != Pagamento.StatusPagamento.AUTORIZADO) {
                continue;
            }
            NfceRequest.PagamentoNfceRequest nfcePagamento = new NfceRequest.PagamentoNfceRequest();
            nfcePagamento.setFormaPagamento(mapearFormaPagamento(pagamento.formaPagamento));
            nfcePagamento.setDescricao(pagamento.formaPagamento != null
                ? pagamento.formaPagamento.getDescricao()
                : "Outros");
            nfcePagamento.setValor(pagamento.valor);
            pagamentos.add(nfcePagamento);
        }
        return pagamentos;
    }

    private String mapearFormaPagamento(FormaPagamento forma) {
        if (forma == null) {
            return "99";
        }
        return switch (forma) {
            case DINHEIRO -> "01";
            case CHEQUE -> "02";
            case CARTAO_CREDITO -> "03";
            case CARTAO_DEBITO -> "04";
            case VALE_ALIMENTACAO -> "10";
            case VALE_REFEICAO -> "11";
            case PIX -> "17";
            case TRANSFERENCIA -> "18";
            case BOLETO -> "15";
            case VALE_CULTURA, OUTROS -> "99";
        };
    }

    @Transactional
    public VendaDTO cancelarVenda(UUID vendaId, String motivo) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        
        if (entity.isPaga()) {
            throw new BusinessException("VENDA_PAGA", "Não é possível cancelar uma venda já paga");
        }
        
        entity.status = StatusVenda.CANCELADA;
        entity.observacoes = (entity.observacoes != null ? entity.observacoes + "\n" : "") 
            + "CANCELADA: " + motivo;
        getRepository().persist(entity);
        
        // Cancelar itens
        itemVendaService.cancelarItensVenda(vendaId, motivo);
        
        // Estornar pagamentos
        pagamentoService.estornarPagamentos(vendaId, motivo);
        
        return mapper.toDTO(entity);
    }

    /**
     * Registra uma tentativa de reimpressão do comprovante/NFC-e da venda.
     *
     * <p>O conteúdo impresso é o snapshot salvo em {@link Venda#cupomImpressao},
     * gerado na finalização/emissão. Desta forma, mesmo que a impressora falhe,
     * a venda pode ser reimpressa exatamente como foi registrada.</p>
     *
     * @param vendaId    venda a ser reimpressa
     * @param sucesso    resultado informado pelo ponto de venda (null assume sucesso)
     * @param observacao detalhe do resultado (ex.: erro da impressora)
     */
    @Transactional
    public VendaDTO reimprimirVenda(UUID vendaId, Boolean sucesso, String observacao) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        if (!entity.isFinalizada()) {
            throw new BusinessException("VENDA_NAO_FINALIZADA",
                "Finalize a venda antes de reimprimir o comprovante");
        }

        // Garante o snapshot do comprovante para reimpressão.
        if (entity.cupomImpressao == null || entity.cupomImpressao.isBlank()) {
            entity.cupomImpressao = gerarCupomImpressao(entity);
        }

        entity.tentativasImpressao = entity.tentativasImpressao + 1;
        entity.dataUltimaImpressao = LocalDateTime.now();

        boolean impressaoOk = sucesso == null || sucesso;
        if (impressaoOk) {
            entity.statusUltimaImpressao = "SUCESSO";
            entity.observacoesImpressao = observacao != null && !observacao.isBlank()
                ? limitarTexto(observacao, 500)
                : "Reimpressão concluída";
        } else {
            entity.statusUltimaImpressao = "FALHA";
            entity.observacoesImpressao = observacao != null && !observacao.isBlank()
                ? limitarTexto(observacao, 500)
                : "Falha ao imprimir o comprovante";
        }

        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    /**
     * Registra o resultado de uma impressão automática (ex.: logo após emitir a NFC-e),
     * contabilizando a tentativa no fluxo de impressão.
     */
    @Transactional
    public VendaDTO registrarTentativaImpressao(UUID vendaId, boolean sucesso, String observacao) {
        Venda entity = getRepository().findById(vendaId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Venda", vendaId);
        }
        entity.tentativasImpressao = entity.tentativasImpressao + 1;
        entity.dataUltimaImpressao = LocalDateTime.now();
        entity.statusUltimaImpressao = sucesso ? "SUCESSO" : "FALHA";
        entity.observacoesImpressao = observacao != null && !observacao.isBlank()
            ? limitarTexto(observacao, 500)
            : (sucesso ? "Impressão concluída" : "Falha ao imprimir o comprovante");
        getRepository().persist(entity);
        return mapper.toDTO(entity);
    }

    /** Monta o texto do comprovante que será persistido para reimpressão. */
    private String gerarCupomImpressao(Venda entity) {
        String linha = "----------------------------------------";
        StringBuilder sb = new StringBuilder();
        sb.append(linha).append('\n');
        sb.append("SISTEMA TENDA\n");
        if (entity.numero != null) {
            sb.append("Venda: ").append(entity.numero).append('\n');
        }
        if (entity.data != null) {
            sb.append("Data: ").append(entity.data).append('\n');
        }
        if (entity.dataFinalizacao != null) {
            sb.append("Finalizada: ").append(entity.dataFinalizacao).append('\n');
        }
        sb.append("Cliente: ").append(entity.clienteNome != null && !entity.clienteNome.isBlank()
            ? entity.clienteNome : "Consumidor final").append('\n');
        if (entity.clienteDocumento != null && !entity.clienteDocumento.isBlank()) {
            sb.append("Documento: ").append(entity.clienteDocumento).append('\n');
        }
        sb.append(linha).append('\n');
        sb.append("ITENS\n");
        if (entity.itens != null) {
            for (ItemVenda item : entity.itens) {
                if (item.cancelado) {
                    continue;
                }
                sb.append(item.nomeProduto).append('\n');
                sb.append(String.format("%s x %s = %s\n",
                    formatarQuantidade(item.quantidade),
                    formatarMoeda(item.precoUnitario),
                    formatarMoeda(item.valorTotal)));
            }
        }
        sb.append(linha).append('\n');
        sb.append("Subtotal: ").append(formatarMoeda(entity.subtotal)).append('\n');
        sb.append("Desconto: ").append(formatarMoeda(entity.desconto)).append('\n');
        sb.append("Acrescimo: ").append(formatarMoeda(entity.acrescimo)).append('\n');
        sb.append("TOTAL: ").append(formatarMoeda(entity.valorTotal)).append('\n');
        sb.append("Pago: ").append(formatarMoeda(entity.valorPago)).append('\n');
        sb.append("Troco: ").append(formatarMoeda(entity.troco)).append('\n');
        sb.append(linha).append('\n');
        sb.append("PAGAMENTOS\n");
        for (Pagamento pagamento : pagamentoRepository.findByVenda(entity)) {
            if (pagamento.status == Pagamento.StatusPagamento.CANCELADO
                || pagamento.status == Pagamento.StatusPagamento.ESTORNADO) {
                continue;
            }
            sb.append(pagamento.formaPagamento != null
                ? pagamento.formaPagamento.getDescricao() : "Outros")
                .append(": ").append(formatarMoeda(pagamento.valor)).append('\n');
        }
        sb.append(linha).append('\n');
        if (entity.chaveNfce != null && !entity.chaveNfce.isBlank()) {
            sb.append("NFC-e: ").append(entity.numeroNfce != null ? entity.numeroNfce : "-").append('\n');
            sb.append("Chave: ").append(entity.chaveNfce).append('\n');
            sb.append(linha).append('\n');
        }
        sb.append("Obrigado pela preferencia!\n");
        sb.append(linha).append('\n');
        return sb.toString();
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor != null
            ? new java.text.DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR"))).format(valor)
            : "0,00";
    }

    private String formatarQuantidade(BigDecimal quantidade) {
        return quantidade != null
            ? new java.text.DecimalFormat("#,##0.###", new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR"))).format(quantidade)
            : "1";
    }

    private String limitarTexto(String valor, int tamanho) {
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }

    public BigDecimal getTotalVendasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalVendasPeriodo(dataInicio, dataFim);
    }

    public BigDecimal getTotalFruteiraPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalFruteiraPeriodo(dataInicio, dataFim);
    }

    public BigDecimal getTotalBuffetPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalBuffetPeriodo(dataInicio, dataFim);
    }

    private void recalcularTotais(Venda entity) {
        entity.subtotal = entity.subtotalFruteira.add(entity.subtotalBuffet);
        entity.valorTotal = entity.subtotal
            .subtract(entity.desconto != null ? entity.desconto : BigDecimal.ZERO)
            .add(entity.acrescimo != null ? entity.acrescimo : BigDecimal.ZERO);
    }

    private void validateCreate(VendaDTO dto) {
        // Validações básicas
    }

    public static class PagamentoRequest {
        public FormaPagamento formaPagamento;
        public BigDecimal valor;
        public Integer parcelas;
        public String nsu;
        public String codigoAutorizacao;
        public String bandeiraCartao;
        public String ultimosDigitosCartao;
        public String pixTxId;
        public String pixQrCode;
        public String valeNumero;
        public java.time.LocalDateTime valeValidade;
        public String tefRequestId;
        public String cpfConsumidor;
    }
}