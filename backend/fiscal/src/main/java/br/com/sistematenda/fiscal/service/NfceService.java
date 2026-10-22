package br.com.sistematenda.fiscal.service;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.util.ChaveAcessoUtils;
import br.com.sistematenda.fiscal.dto.NfceRequest;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.entity.NfeItem;
import br.com.sistematenda.fiscal.entity.NfePagamento;
import br.com.sistematenda.fiscal.repository.NfeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class NfceService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    private static final String MODELO_NFCE = "65";

    @Inject
    FiscalConfig fiscalConfig;

    @Inject
    NfeRepository nfeRepository;

    @Inject
    EntityManager entityManager;

    @Transactional
    public Nfe emitirNfce(NfceRequest request) {
        if (!fiscalConfig.nfce().enabled()) {
            throw new BusinessException("NFCE_DISABLED", "Emissao de NFC-e nao esta habilitada");
        }
        if (request == null) {
            throw new BusinessException("NFCE_REQUEST_INVALIDO", "Dados da NFC-e nao informados");
        }

        validarConfiguracao();
        validarItens(request.getItens());

        int serie = fiscalConfig.serieNfce() != null ? fiscalConfig.serieNfce() : 1;
        int numeroInicial = fiscalConfig.numeroNfceInicial() != null ? fiscalConfig.numeroNfceInicial() : 1;
        long numero = reservarNumero(MODELO_NFCE, serie, numeroInicial);

        LocalDateTime agora = LocalDateTime.now();
        Nfe nfe = new Nfe();
        nfe.modelo = MODELO_NFCE;
        nfe.serie = serie;
        nfe.numero = numero;
        nfe.status = Nfe.StatusNfe.PENDENTE;
        nfe.naturezaOperacao = textoOuPadrao(request.getNaturezaOperacao(), "VENDA");
        nfe.dataEmissao = agora;
        nfe.dataSaidaEntrada = agora;
        nfe.clienteNome = request.getClienteNome();
        nfe.clienteDocumento = request.getClienteDocumento();
        nfe.ambiente = fiscalConfig.ambiente();
        nfe.vendaId = request.getVendaId();
        nfe.informacoesComplementares = request.getInformacoesComplementares();
        nfe.tentativasEnvio = 0;

        String cnpj = ChaveAcessoUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
        String chave = ChaveAcessoUtils.gerar(
            fiscalConfig.uf(),
            LocalDate.now(),
            cnpj,
            MODELO_NFCE,
            serie,
            numero,
            1
        );
        nfe.chaveAcesso = chave;
        nfe.digitoValidador = Integer.parseInt(chave.substring(42, 43));

        BigDecimal subtotal = ZERO;
        BigDecimal totalIcms = ZERO;
        BigDecimal totalPis = ZERO;
        BigDecimal totalCofins = ZERO;
        int numeroItem = 1;

        for (NfceRequest.ItemNfceRequest itemRequest : request.getItens()) {
            NfeItem item = montarItem(nfe, itemRequest, numeroItem++);
            nfe.itens.add(item);
            subtotal = subtotal.add(valor(item.valorTotal));
            totalIcms = totalIcms.add(valor(item.valorIcms));
            totalPis = totalPis.add(valor(item.valorPis));
            totalCofins = totalCofins.add(valor(item.valorCofins));
        }

        nfe.valorTotal = request.getValorTotal() != null ? valor(request.getValorTotal()) : subtotal;
        nfe.valorDesconto = valor(request.getDesconto());
        nfe.valorAcrescimo = valor(request.getAcrescimo());
        nfe.valorTroco = valor(request.getValorTroco());
        nfe.valorIcms = totalIcms;
        nfe.valorPis = totalPis;
        nfe.valorCofins = totalCofins;
        nfe.valorIpi = ZERO;

        NfeItem primeiroItem = nfe.itens.get(0);
        nfe.codigoCfop = primeiroItem.cfop;
        nfe.codigoNcm = primeiroItem.ncm;
        nfe.cstIcms = primeiroItem.cstIcms;
        nfe.cstPis = primeiroItem.cstPis;
        nfe.cstCofins = primeiroItem.cstCofins;

        for (NfceRequest.PagamentoNfceRequest pagamentoRequest : request.getPagamentos()) {
            NfePagamento pagamento = new NfePagamento();
            pagamento.nfe = nfe;
            pagamento.formaPagamento = textoOuPadrao(pagamentoRequest.getFormaPagamento(), "99");
            pagamento.descricao = limitar(textoOuPadrao(pagamentoRequest.getDescricao(), "Outros"), 30);
            pagamento.valor = valor(pagamentoRequest.getValor());
            nfe.pagamentos.add(pagamento);
        }

        if (nfe.pagamentos.isEmpty()) {
            throw new BusinessException("NFCE_PAGAMENTO_OBRIGATORIO", "Nenhum pagamento autorizado foi encontrado para a venda");
        }

        nfe.meioPagamento = limitar(nfe.pagamentos.get(0).descricao, 20);

        nfeRepository.persist(nfe);
        return nfe;
    }

    @Transactional
    public Nfe cancelarNfce(Nfe nfe, String justificativa) {
        if (nfe == null || nfe.id == null) {
            throw new BusinessException("NFCE_NAO_ENCONTRADA", "NFC-e nao encontrada para cancelamento");
        }
        Nfe atual = nfeRepository.findById(nfe.id);
        if (atual == null || atual.deleted) {
            throw new BusinessException("NFCE_NAO_ENCONTRADA", "NFC-e nao encontrada para cancelamento");
        }
        if (atual.status != Nfe.StatusNfe.AUTORIZADA) {
            throw new BusinessException("NFCE_NAO_CANCELAVEL", "Somente NFC-e autorizada pode ser cancelada");
        }
        if (justificativa == null || justificativa.trim().length() < 15) {
            throw new BusinessException("JUSTIFICATIVA_INVALIDA", "A justificativa deve ter no minimo 15 caracteres");
        }
        throw new BusinessException("CANCELAMENTO_NAO_IMPLEMENTADO",
            "Cancelamento na SEFAZ sera habilitado na proxima etapa da integracao fiscal");
    }

    public void inutilizarNumeracao(String justificativa, int numeroInicial, int numeroFinal) {
        throw new BusinessException("INUTILIZACAO_NAO_IMPLEMENTADA",
            "Inutilizacao de numeracao sera habilitada na proxima etapa da integracao fiscal");
    }

    public Nfe consultarStatus(Nfe nfe) {
        if (nfe == null || nfe.id == null) {
            throw new BusinessException("NFCE_NAO_ENCONTRADA", "NFC-e nao encontrada");
        }
        Nfe atual = nfeRepository.findById(nfe.id);
        if (atual == null || atual.deleted) {
            throw new BusinessException("NFCE_NAO_ENCONTRADA", "NFC-e nao encontrada");
        }
        return atual;
    }

    private NfeItem montarItem(Nfe nfe, NfceRequest.ItemNfceRequest request, int numeroItem) {
        String ncm = somenteDigitos(request.getNcm());
        if (ncm.length() != 8) {
            throw new BusinessException("NCM_INVALIDO",
                "NCM invalido para o produto " + textoOuPadrao(request.getDescricao(), request.getCodigoProduto()));
        }

        String cfop = somenteDigitos(request.getCfop());
        if (cfop.length() != 4) {
            cfop = fiscalConfig.cfopPadrao();
        }

        String cstIcms = codigoCst(request.getCstIcms(), cstIcmsPadrao());
        String cstPis = codigoCst(request.getCstPis(), cstPisPadrao());
        String cstCofins = codigoCst(request.getCstCofins(), cstCofinsPadrao());
        validarCstIcms(cstIcms);
        validarCst(cstPis, 2, "PIS");
        validarCst(cstCofins, 2, "COFINS");

        BigDecimal quantidade = valor(request.getQuantidade());
        BigDecimal valorUnitario = valor(request.getValorUnitario());
        BigDecimal valorTotal = request.getValorTotal() != null
            ? valor(request.getValorTotal())
            : quantidade.multiply(valorUnitario).setScale(4, RoundingMode.HALF_UP);

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("QUANTIDADE_INVALIDA",
                "Quantidade invalida para o produto " + textoOuPadrao(request.getDescricao(), request.getCodigoProduto()));
        }
        if (valorTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("VALOR_INVALIDO",
                "Valor invalido para o produto " + textoOuPadrao(request.getDescricao(), request.getCodigoProduto()));
        }

        BigDecimal aliquotaIcms = isCsosn(cstIcms) ? ZERO : valor(fiscalConfig.aliquotaIcmsPadrao());
        BigDecimal valorIcms = calcularIcms(valorTotal, cstIcms, aliquotaIcms);
        BigDecimal valorPis = calcularPisCofins(valorTotal, cstPis, valor(fiscalConfig.aliquotaPisPadrao()));
        BigDecimal valorCofins = calcularPisCofins(valorTotal, cstCofins, valor(fiscalConfig.aliquotaCofinsPadrao()));

        NfeItem item = new NfeItem();
        item.nfe = nfe;
        item.numeroItem = numeroItem;
        item.codigoProduto = limitar(request.getCodigoProduto(), 60);
        item.descricao = limitar(textoOuPadrao(request.getDescricao(), "PRODUTO"), 120);
        item.ncm = ncm;
        item.cfop = cfop;
        item.cstIcms = cstIcms;
        item.cstPis = cstPis;
        item.cstCofins = cstCofins;
        item.unidadeComercial = limitar(textoOuPadrao(request.getUnidadeComercial(), "UN"), 6).toUpperCase(Locale.ROOT);
        item.quantidade = quantidade;
        item.valorUnitario = valorUnitario;
        item.valorTotal = valorTotal;
        item.valorDesconto = valor(request.getValorDesconto());
        item.origemMercadoria = request.getOrigemMercadoria() != null ? request.getOrigemMercadoria() : 0;
        item.aliquotaIcms = aliquotaIcms;
        item.valorIcms = valorIcms;
        item.valorPis = valorPis;
        item.valorCofins = valorCofins;
        return item;
    }

    private void validarConfiguracao() {
        String cnpj = ChaveAcessoUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
        if (cnpj.length() != 14 || "00000000000000".equals(cnpj)) {
            throw new BusinessException("CNPJ_NAO_CONFIGURADO",
                "Configure o CNPJ da empresa em fiscal.cnpj antes de emitir NFC-e");
        }
        if (!ChaveAcessoUtils.ufValida(fiscalConfig.uf())) {
            throw new BusinessException("UF_INVALIDA", "UF fiscal invalida: " + fiscalConfig.uf());
        }
        if (fiscalConfig.municipioCodigo().isEmpty()) {
            throw new BusinessException("MUNICIPIO_NAO_CONFIGURADO",
                "Configure o codigo IBGE do municipio em fiscal.municipio-codigo");
        }
        String ie = fiscalConfig.inscricaoEstadual().orElse("");
        if (ChaveAcessoUtils.somenteDigitos(ie).isEmpty()) {
            throw new BusinessException("IE_NAO_CONFIGURADA",
                "Configure a inscricao estadual em fiscal.inscricao-estadual");
        }
        if (fiscalConfig.crt() == null || fiscalConfig.crt() < 1 || fiscalConfig.crt() > 3) {
            throw new BusinessException("CRT_INVALIDO", "Configure o CRT em fiscal.crt (1, 2 ou 3)");
        }
    }

    private void validarItens(List<NfceRequest.ItemNfceRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("NFCE_SEM_ITENS", "A NFC-e precisa ter pelo menos um item");
        }
    }

    private long reservarNumero(String modelo, int serie, int numeroInicial) {
        Object resultado = entityManager.createNativeQuery("""
                INSERT INTO fiscal_nfe_sequencia (modelo, serie, ultimo_numero, created_at, updated_at)
                VALUES (:modelo, :serie, :numeroInicial, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (modelo, serie) DO UPDATE
                SET ultimo_numero = fiscal_nfe_sequencia.ultimo_numero + 1,
                    updated_at = CURRENT_TIMESTAMP
                RETURNING ultimo_numero
                """)
            .setParameter("modelo", modelo)
            .setParameter("serie", serie)
            .setParameter("numeroInicial", numeroInicial)
            .getSingleResult();

        return ((Number) resultado).longValue();
    }

    private BigDecimal calcularIcms(BigDecimal base, String cstIcms, BigDecimal aliquota) {
        if (isCsosn(cstIcms)) {
            return ZERO;
        }
        return switch (cstIcms) {
            case "00", "10", "20", "70", "90" ->
                base.multiply(aliquota).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            default -> ZERO;
        };
    }

    private BigDecimal calcularPisCofins(BigDecimal base, String cst, BigDecimal aliquota) {
        return switch (cst) {
            case "01", "02" -> base.multiply(aliquota).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            default -> ZERO;
        };
    }

    private boolean isCsosn(String cst) {
        return cst != null && cst.length() == 3;
    }

    private String cstIcmsPadrao() {
        return fiscalConfig.crt() != null && fiscalConfig.crt() == 1 ? "102" : "00";
    }

    private String cstPisPadrao() {
        return fiscalConfig.crt() != null && fiscalConfig.crt() == 1 ? "49" : "01";
    }

    private String cstCofinsPadrao() {
        return fiscalConfig.crt() != null && fiscalConfig.crt() == 1 ? "49" : "01";
    }

    private String codigoCst(String valor, String padrao) {
        String codigo = somenteDigitos(valor);
        return codigo.isEmpty() ? padrao : codigo;
    }

    private void validarCstIcms(String cst) {
        if (cst == null || (cst.length() != 2 && cst.length() != 3)) {
            throw new BusinessException("CST_ICMS_INVALIDO",
                "CST/CSOSN de ICMS invalido: " + cst);
        }
    }

    private void validarCst(String cst, int tamanho, String tributo) {
        if (cst == null || cst.length() != tamanho) {
            throw new BusinessException("CST_" + tributo + "_INVALIDO",
                "CST de " + tributo + " invalido: " + cst);
        }
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    private String limitar(String valor, int tamanho) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor == null ? ZERO : valor.setScale(4, RoundingMode.HALF_UP);
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
