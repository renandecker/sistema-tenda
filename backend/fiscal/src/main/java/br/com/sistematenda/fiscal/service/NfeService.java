package br.com.sistematenda.fiscal.service;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.util.ChaveAcessoUtils;
import br.com.sistematenda.fiscal.dto.NfeRequest;
import br.com.sistematenda.fiscal.dto.NfceRequest;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.entity.NfeItem;
import br.com.sistematenda.fiscal.entity.NfePagamento;
import br.com.sistematenda.fiscal.nfe.NfeEmissor;
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
public class NfeService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    private static final String MODELO_NFE = "55";

    @Inject
    FiscalConfig fiscalConfig;

    @Inject
    NfeRepository nfeRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    NfeEmissor nfeEmissor;

    @Transactional
    public Nfe emitirNfe(NfeRequest request) {
        if (request == null) {
            throw new BusinessException("NFE_REQUEST_INVALIDO", "Dados da NF-e nao informados");
        }
        validarConfiguracao();
        validarDestinatario(request);
        validarItens(request.getItens());

        int serie = fiscalConfig.serieNfe() != null ? fiscalConfig.serieNfe() : 1;
        int numeroInicial = fiscalConfig.numeroNfeInicial() != null ? fiscalConfig.numeroNfeInicial() : 1;
        long numero = reservarNumero(MODELO_NFE, serie, numeroInicial);

        LocalDateTime agora = LocalDateTime.now();
        Nfe nfe = new Nfe();
        nfe.modelo = MODELO_NFE;
        nfe.serie = serie;
        nfe.numero = numero;
        nfe.status = Nfe.StatusNfe.PENDENTE;
        nfe.naturezaOperacao = textoOuPadrao(request.getNaturezaOperacao(), "VENDA");
        nfe.dataEmissao = agora;
        nfe.dataSaidaEntrada = agora;
        nfe.ambiente = fiscalConfig.ambiente();
        nfe.vendaId = request.getVendaId();
        nfe.informacoesComplementares = request.getInformacoesComplementares();
        nfe.informacoesFisco = request.getInformacoesFisco();
        nfe.clienteNome = request.getClienteNome();
        nfe.clienteDocumento = ChaveAcessoUtils.somenteDigitos(request.getClienteDocumento());
        nfe.destinatarioIe = request.getClienteIe();
        nfe.destinatarioLogradouro = request.getClienteLogradouro();
        nfe.destinatarioNumero = request.getClienteNumero();
        nfe.destinatarioComplemento = request.getClienteComplemento();
        nfe.destinatarioBairro = request.getClienteBairro();
        nfe.destinatarioCodigoMunicipio = request.getClienteCodigoMunicipio();
        nfe.destinatarioMunicipio = request.getClienteMunicipio();
        nfe.destinatarioUf = upper(request.getClienteUf());
        nfe.destinatarioCep = ChaveAcessoUtils.somenteDigitos(request.getClienteCep());
        nfe.destinatarioEmail = request.getClienteEmail();
        nfe.modalidadeFrete = request.getModalidadeFrete() != null ? request.getModalidadeFrete() : 9;
        nfe.transportadoraNome = request.getTransportadoraNome();
        nfe.transportadoraDocumento = request.getTransportadoraDocumento();
        nfe.transportadoraIe = request.getTransportadoraIe();
        nfe.transportadoraEndereco = request.getTransportadoraEndereco();
        nfe.transportadoraMunicipio = request.getTransportadoraMunicipio();
        nfe.transportadoraUf = upper(request.getTransportadoraUf());
        nfe.volumeQuantidade = request.getVolumeQuantidade();
        nfe.volumeEspecie = request.getVolumeEspecie();
        nfe.volumePesoLiquido = request.getVolumePesoLiquido();
        nfe.volumePesoBruto = request.getVolumePesoBruto();
        nfe.finalidadeNfe = request.getFinalidadeNfe() != null ? request.getFinalidadeNfe() : 1;
        nfe.tipoOperacao = request.getTipoOperacao() != null ? request.getTipoOperacao() : 1;
        nfe.consumidorFinal = request.getConsumidorFinal() != null ? request.getConsumidorFinal() : 1;
        nfe.presencaComprador = request.getPresencaComprador() != null ? request.getPresencaComprador() : 1;
        nfe.tipoEmissao = request.getTipoEmissao() != null ? request.getTipoEmissao() : 1;
        nfe.tentativasEnvio = 0;

        String cnpj = ChaveAcessoUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
        String chave = ChaveAcessoUtils.gerar(
            fiscalConfig.uf(),
            LocalDate.now(),
            cnpj,
            MODELO_NFE,
            serie,
            numero,
            nfe.tipoEmissao
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
            throw new BusinessException("NFE_PAGAMENTO_OBRIGATORIO",
                "Informe ao menos uma forma de pagamento para a NF-e");
        }
        nfe.meioPagamento = limitar(nfe.pagamentos.get(0).descricao, 20);

        nfeRepository.persist(nfe);
        return nfeEmissor.emitir(nfe);
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
                "Configure o CNPJ da empresa em fiscal.cnpj antes de emitir NF-e");
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

    private void validarDestinatario(NfeRequest request) {
        String documento = ChaveAcessoUtils.somenteDigitos(request.getClienteDocumento());
        if (documento.length() != 11 && documento.length() != 14) {
            throw new BusinessException("DESTINATARIO_INVALIDO",
                "Informe o CPF ou CNPJ do destinatario da NF-e");
        }
        if (textoOuPadrao(request.getClienteLogradouro(), "").isBlank()
            || textoOuPadrao(request.getClienteNumero(), "").isBlank()
            || textoOuPadrao(request.getClienteBairro(), "").isBlank()
            || request.getClienteCodigoMunicipio() == null
            || textoOuPadrao(request.getClienteMunicipio(), "").isBlank()
            || !ChaveAcessoUtils.ufValida(request.getClienteUf())) {
            throw new BusinessException("DESTINATARIO_INVALIDO",
                "Informe endereco completo do destinatario (logradouro, numero, bairro, municipio e UF)");
        }
    }

    private void validarItens(List<NfceRequest.ItemNfceRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("NFE_SEM_ITENS", "A NF-e precisa ter pelo menos um item");
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
            throw new BusinessException("CST_ICMS_INVALIDO", "CST/CSOSN de ICMS invalido: " + cst);
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

    private String upper(String valor) {
        return valor == null ? null : valor.trim().toUpperCase(Locale.ROOT);
    }
}
