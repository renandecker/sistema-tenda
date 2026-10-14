package br.com.sistematenda.pdv.service;

import br.com.sistematenda.pdv.dto.SessaoCaixaDTO;
import br.com.sistematenda.pdv.entity.SessaoCaixa;
import br.com.sistematenda.pdv.entity.SessaoCaixa.StatusSessao;
import br.com.sistematenda.pdv.entity.MovimentoCaixa;
import br.com.sistematenda.pdv.entity.Pdv;
import br.com.sistematenda.pdv.mapper.SessaoCaixaMapper;
import br.com.sistematenda.pdv.repository.SessaoCaixaRepository;
import br.com.sistematenda.pdv.repository.MovimentoCaixaRepository;
import br.com.sistematenda.pdv.repository.VendaRepository;
import br.com.sistematenda.pdv.repository.PagamentoRepository;
import br.com.sistematenda.pdv.service.PdvService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessaoCaixaService extends BaseCrudServiceImpl<SessaoCaixa, SessaoCaixaDTO, UUID> {

    @Inject
    SessaoCaixaRepository repository;

    @Inject
    SessaoCaixaMapper mapper;

    @Inject
    MovimentoCaixaRepository movimentoRepository;

    @Inject
    VendaRepository vendaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    PdvService pdvService;

    @Override
    protected SessaoCaixaRepository getRepository() {
        return repository;
    }

    protected SessaoCaixaMapper getMapper() {
        return mapper;
    }

    @Override
    protected Class<SessaoCaixa> getEntityClass() {
        return SessaoCaixa.class;
    }

    public SessaoCaixaDTO findAbertaByPdv(UUID pdvId) {
        Pdv pdv = pdvService.getRepository().findById(pdvId);
        if (pdv == null) {
            throw new NotFoundException("PDV", pdvId);
        }
        return repository.findAbertaByPdv(pdv)
            .map(mapper::toDTO)
            .orElse(null);
    }

    public List<SessaoCaixaDTO> findByPdv(UUID pdvId) {
        Pdv pdv = pdvService.getRepository().findById(pdvId);
        if (pdv == null) {
            throw new NotFoundException("PDV", pdvId);
        }
        return repository.findByPdv(pdv).stream().map(mapper::toDTO).toList();
    }

    public List<SessaoCaixaDTO> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.findByPeriodo(dataInicio, dataFim).stream()
            .map(mapper::toDTO).toList();
    }

    @Override
    public PageResponse<SessaoCaixaDTO> findAll(int page, int size, String sortBy, String sortDirection) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, null, null, null, null));
    }

    public PageResponse<SessaoCaixaDTO> findAll(int page, int size, String sortBy, String sortDirection,
                                                UUID pdvId, StatusSessao status,
                                                LocalDate inicio, LocalDate fim) {
        return toDtoPage(repository.findPaginated(page, size, sortBy, sortDirection, pdvId, status, inicio, fim));
    }

    @Transactional
    public SessaoCaixaDTO abrirCaixa(UUID pdvId, String operador, BigDecimal valorInicial) {
        Pdv pdv = pdvService.getRepository().findById(pdvId);
        if (pdv == null) {
            throw new NotFoundException("PDV", pdvId);
        }
        
        SessaoCaixa entity = new SessaoCaixa();
        entity.pdv = pdv;
        entity.operadorAbertura = operador;
        entity.valorInicial = valorInicial != null ? valorInicial : BigDecimal.ZERO;
        entity.status = SessaoCaixa.StatusSessao.ABERTA;
        entity.dataAbertura = LocalDateTime.now();
        entity.numeroUltimaVenda = pdv.numeroUltimaVenda != null ? pdv.numeroUltimaVenda : 0L;
        
        getRepository().persist(entity);
        
        pdv.status = Pdv.StatusPdv.EM_USO;
        pdv.ultimaAbertura = LocalDateTime.now();
        pdvService.getRepository().persist(pdv);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public SessaoCaixaDTO fecharCaixa(UUID sessaoId, String operador, BigDecimal valorFinalContado) {
        SessaoCaixa entity = getRepository().findById(sessaoId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Sessão de caixa", sessaoId);
        }
        
        if (entity.status != SessaoCaixa.StatusSessao.ABERTA) {
            throw new BusinessException("CAIXA_NAO_ABERTO", "Sessão não está aberta");
        }
        
        BigDecimal sangriasTotal = movimentoRepository.getTotalSangriasSessao(entity);
        BigDecimal reforcosTotal = movimentoRepository.getTotalReforcosSessao(entity);
        
        LocalDateTime inicioPeriodo = entity.dataAbertura;
        LocalDateTime fimPeriodo = LocalDateTime.now();
        BigDecimal vendasDinheiro = pagamentoRepository.getTotalByFormaPeriodo(
            br.com.sistematenda.common.enums.FormaPagamento.DINHEIRO, inicioPeriodo, fimPeriodo);
        BigDecimal vendasCartao = pagamentoRepository.getTotalByFormaPeriodo(
            br.com.sistematenda.common.enums.FormaPagamento.CARTAO_CREDITO, inicioPeriodo, fimPeriodo)
            .add(pagamentoRepository.getTotalByFormaPeriodo(
                br.com.sistematenda.common.enums.FormaPagamento.CARTAO_DEBITO, inicioPeriodo, fimPeriodo));
        BigDecimal vendasPix = pagamentoRepository.getTotalByFormaPeriodo(
            br.com.sistematenda.common.enums.FormaPagamento.PIX, inicioPeriodo, fimPeriodo);
        BigDecimal vendasVale = pagamentoRepository.getTotalByFormaPeriodo(
            br.com.sistematenda.common.enums.FormaPagamento.VALE_ALIMENTACAO, inicioPeriodo, fimPeriodo)
            .add(pagamentoRepository.getTotalByFormaPeriodo(
                br.com.sistematenda.common.enums.FormaPagamento.VALE_REFEICAO, inicioPeriodo, fimPeriodo))
            .add(pagamentoRepository.getTotalByFormaPeriodo(
                br.com.sistematenda.common.enums.FormaPagamento.VALE_CULTURA, inicioPeriodo, fimPeriodo));
        BigDecimal totalVendas = vendasDinheiro.add(vendasCartao).add(vendasPix).add(vendasVale);
        
        BigDecimal valorFinalSistema = entity.valorInicial
            .add(vendasDinheiro)
            .subtract(sangriasTotal)
            .add(reforcosTotal);
        
        entity.operadorFechamento = operador;
        entity.valorFinalSistema = valorFinalSistema;
        entity.valorFinalContado = valorFinalContado != null ? valorFinalContado : BigDecimal.ZERO;
        entity.diferenca = entity.valorFinalContado.subtract(valorFinalSistema).abs();
        entity.vendasDinheiro = vendasDinheiro;
        entity.vendasCartao = vendasCartao;
        entity.vendasPix = vendasPix;
        entity.vendasVale = vendasVale;
        entity.totalVendas = totalVendas;
        entity.sangriasTotal = sangriasTotal;
        entity.reforcosTotal = reforcosTotal;
        entity.status = SessaoCaixa.StatusSessao.FECHADA;
        entity.dataFechamento = LocalDateTime.now();
        
        getRepository().persist(entity);
        
        entity.pdv.status = Pdv.StatusPdv.LIVRE;
        entity.pdv.ultimoFechamento = LocalDateTime.now();
        entity.pdv.numeroUltimaVenda = entity.numeroUltimaVenda;
        pdvService.getRepository().persist(entity.pdv);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public SessaoCaixaDTO sangria(UUID sessaoId, BigDecimal valor, String descricao, String operador) {
        SessaoCaixa entity = getRepository().findById(sessaoId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Sessão de caixa", sessaoId);
        }
        
        if (entity.status != SessaoCaixa.StatusSessao.ABERTA) {
            throw new BusinessException("CAIXA_NAO_ABERTO", "Sessão não está aberta");
        }
        
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valor da sangria deve ser maior que zero");
        }
        
        br.com.sistematenda.pdv.entity.MovimentoCaixa movimento = new br.com.sistematenda.pdv.entity.MovimentoCaixa();
        movimento.sessaoCaixa = entity;
        movimento.tipo = br.com.sistematenda.pdv.entity.MovimentoCaixa.TipoMovimento.SANGRIA;
        movimento.valor = valor;
        movimento.descricao = descricao;
        movimento.operador = operador;
        movimento.data = LocalDateTime.now();
        movimentoRepository.persist(movimento);
        
        entity.sangriasTotal = entity.sangriasTotal.add(valor);
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    @Transactional
    public SessaoCaixaDTO reforco(UUID sessaoId, BigDecimal valor, String descricao, String operador) {
        SessaoCaixa entity = getRepository().findById(sessaoId);
        if (entity == null || entity.deleted) {
            throw new NotFoundException("Sessão de caixa", sessaoId);
        }
        
        if (entity.status != SessaoCaixa.StatusSessao.ABERTA) {
            throw new BusinessException("CAIXA_NAO_ABERTO", "Sessão não está aberta");
        }
        
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valor do reforço deve ser maior que zero");
        }
        
        br.com.sistematenda.pdv.entity.MovimentoCaixa movimento = new br.com.sistematenda.pdv.entity.MovimentoCaixa();
        movimento.sessaoCaixa = entity;
        movimento.tipo = br.com.sistematenda.pdv.entity.MovimentoCaixa.TipoMovimento.REFORCO;
        movimento.valor = valor;
        movimento.descricao = descricao;
        movimento.operador = operador;
        movimento.data = LocalDateTime.now();
        movimentoRepository.persist(movimento);
        
        entity.reforcosTotal = entity.reforcosTotal.add(valor);
        getRepository().persist(entity);
        
        return mapper.toDTO(entity);
    }

    public BigDecimal getTotalVendasPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return repository.getTotalVendasPeriodo(dataInicio, dataFim);
    }
}