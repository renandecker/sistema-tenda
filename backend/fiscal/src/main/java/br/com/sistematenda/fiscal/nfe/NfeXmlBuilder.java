package br.com.sistematenda.fiscal.nfe;

import br.com.sistematenda.common.config.FiscalConfig;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.fiscal.entity.Nfe;
import br.com.sistematenda.fiscal.entity.NfeItem;
import br.com.sistematenda.fiscal.entity.NfePagamento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.Locale;

@ApplicationScoped
public class NfeXmlBuilder {

    @Inject
    FiscalConfig fiscalConfig;

    public String build(Nfe nfe) {
        validarNfe(nfe);
        Document document = XmlUtils.novoDocumento();
        Element raiz = document.createElementNS(NfeConstantes.NS_NFE, "NFe");
        document.appendChild(raiz);

        Element infNFe = XmlUtils.el(document, raiz, "infNFe");
        XmlUtils.atributo(infNFe, "versao", NfeConstantes.VERSAO_NFE);
        XmlUtils.atributo(infNFe, "Id", "NFe" + nfe.chaveAcesso);

        buildIde(document, infNFe, nfe);
        buildEmit(document, infNFe, nfe);
        buildDest(document, infNFe, nfe);
        buildItens(document, infNFe, nfe);
        buildTotal(document, infNFe, nfe);
        buildTransp(document, infNFe, nfe);
        buildPag(document, infNFe, nfe);
        buildInfAdic(document, infNFe, nfe);
        buildRespTecnico(document, infNFe);

        if (isNfce(nfe) && temCsc()) {
            buildInfNFeSupl(document, raiz, nfe);
        }

        return XmlUtils.serializar(document, true);
    }

    private void buildIde(Document document, Element infNFe, Nfe nfe) {
        Element ide = XmlUtils.el(document, infNFe, "ide");
        String ambiente = NfeConstantes.ambienteCodigo(nfe.ambiente);
        String uf = fiscalConfig.uf();
        int cMun = fiscalConfig.municipioCodigo()
            .orElseThrow(() -> new BusinessException("MUNICIPIO_NAO_CONFIGURADO",
                "Configure o codigo IBGE do municipio em fiscal.municipio-codigo"));

        XmlUtils.elComTexto(ide, "cUF", NfeConstantes.codigoUf(uf));
        XmlUtils.elComTexto(ide, "cNF", cNF(nfe.chaveAcesso));
        XmlUtils.elComTexto(ide, "natOp", limitar(nfe.naturezaOperacao, 60, "VENDA"));
        XmlUtils.elComTexto(ide, "mod", nfe.modelo);
        XmlUtils.elComTexto(ide, "serie", String.valueOf(nfe.serie));
        XmlUtils.elComTexto(ide, "nNF", String.valueOf(nfe.numero));
        XmlUtils.elComTexto(ide, "dhEmi", XmlUtils.dataHora(nfe.dataEmissao));
        if (!isNfce(nfe) && nfe.dataSaidaEntrada != null) {
            XmlUtils.elComTexto(ide, "dhSaiEnt", XmlUtils.dataHora(nfe.dataSaidaEntrada));
        }
        XmlUtils.elComTexto(ide, "tpNF", String.valueOf(nfe.tipoOperacao != null ? nfe.tipoOperacao : 1));
        XmlUtils.elComTexto(ide, "idDest", String.valueOf(idDestino(nfe)));
        XmlUtils.elComTexto(ide, "cMunFG", String.valueOf(cMun));
        XmlUtils.elComTexto(ide, "tpImp", isNfce(nfe) ? "4" : "1");
        XmlUtils.elComTexto(ide, "tpEmis", String.valueOf(nfe.tipoEmissao != null ? nfe.tipoEmissao : 1));
        XmlUtils.elComTexto(ide, "cDV", String.valueOf(nfe.digitoValidador != null
            ? nfe.digitoValidador : calcularDv(nfe.chaveAcesso)));
        XmlUtils.elComTexto(ide, "tpAmb", ambiente);
        XmlUtils.elComTexto(ide, "finNFe", String.valueOf(nfe.finalidadeNfe != null ? nfe.finalidadeNfe : 1));
        XmlUtils.elComTexto(ide, "indFinal", String.valueOf(nfe.consumidorFinal != null ? nfe.consumidorFinal : 1));
        XmlUtils.elComTexto(ide, "indPres", String.valueOf(nfe.presencaComprador != null ? nfe.presencaComprador : 1));
        XmlUtils.elComTexto(ide, "procEmi", "0");
        XmlUtils.elComTexto(ide, "verProc", "SistemaTenda-1.0");
    }

    private void buildEmit(Document document, Element infNFe, Nfe nfe) {
        Element emit = XmlUtils.el(document, infNFe, "emit");
        String cnpj = XmlUtils.somenteDigitos(fiscalConfig.cnpj().orElse(""));
        String razaoSocial = fiscalConfig.razaoSocial()
            .filter(valor -> !valor.isBlank())
            .orElseThrow(() -> new BusinessException("RAZAO_SOCIAL_NAO_CONFIGURADA",
                "Configure a razao social em fiscal.razao-social"));

        XmlUtils.elComTexto(emit, "CNPJ", cnpj);
        XmlUtils.elComTexto(emit, "xNome", limitar(razaoSocial, 60, razaoSocial));
        fiscalConfig.nomeFantasia()
            .filter(valor -> !valor.isBlank())
            .ifPresent(valor -> XmlUtils.elComTexto(emit, "xFant", limitar(valor, 60, valor)));

        buildEnderEmit(document, emit);

        String ie = fiscalConfig.inscricaoEstadual().orElse("");
        XmlUtils.elComTexto(emit, "IE", ie.isBlank() ? "ISENTO" : XmlUtils.somenteDigitos(ie));
        fiscalConfig.inscricaoMunicipal()
            .filter(valor -> !valor.isBlank())
            .ifPresent(valor -> XmlUtils.elComTexto(emit, "IM", valor));
        XmlUtils.elComTexto(emit, "CRT", String.valueOf(fiscalConfig.crt()));
    }

    private void buildEnderEmit(Document document, Element emit) {
        Element ender = XmlUtils.el(document, emit, "enderEmit");
        String[] partes = fiscalConfig.endereco().orElse("").split("\\s*,\\s*");
        String logradouro = fiscalConfig.logradouro()
            .orElse(partes.length > 0 && !partes[0].isBlank() ? partes[0] : "ENDERECO NAO INFORMADO");
        String numero = fiscalConfig.numero()
            .orElse(partes.length > 1 && !partes[1].isBlank() ? partes[1] : "SN");
        String bairro = fiscalConfig.bairro()
            .orElse(partes.length > 2 && !partes[2].isBlank() ? partes[2] : "CENTRO");

        XmlUtils.elComTexto(ender, "xLgr", limitar(logradouro, 60, logradouro));
        XmlUtils.elComTexto(ender, "nro", limitar(numero, 60, numero));
        fiscalConfig.complemento()
            .filter(valor -> !valor.isBlank())
            .ifPresent(valor -> XmlUtils.elComTexto(ender, "xCpl", limitar(valor, 60, valor)));
        XmlUtils.elComTexto(ender, "xBairro", limitar(bairro, 60, bairro));
        XmlUtils.elComTexto(ender, "cMun", String.valueOf(fiscalConfig.municipioCodigo()
            .orElseThrow(() -> new BusinessException("MUNICIPIO_NAO_CONFIGURADO",
                "Configure o codigo IBGE do municipio em fiscal.municipio-codigo"))));
        XmlUtils.elComTexto(ender, "xMun", limitar(fiscalConfig.municipioNome().orElse("MUNICIPIO"), 60, "MUNICIPIO"));
        XmlUtils.elComTexto(ender, "UF", fiscalConfig.uf());
        XmlUtils.elComTexto(ender, "CEP", cep(fiscalConfig.cep().orElse("")));
        XmlUtils.elComTexto(ender, "cPais", "1058");
        XmlUtils.elComTexto(ender, "xPais", "BRASIL");
        fiscalConfig.fone()
            .filter(valor -> !valor.isBlank())
            .ifPresent(valor -> XmlUtils.elComTexto(ender, "fone", XmlUtils.somenteDigitos(valor)));
    }

    private void buildDest(Document document, Element infNFe, Nfe nfe) {
        String documento = XmlUtils.somenteDigitos(nfe.clienteDocumento);
        if (documento.isBlank()) {
            if (!isNfce(nfe)) {
                throw new BusinessException("DESTINATARIO_OBRIGATORIO",
                    "Informe o CPF/CNPJ do destinatario para emitir NF-e modelo 55");
            }
            return;
        }

        Element dest = XmlUtils.el(document, infNFe, "dest");
        if (documento.length() == 14) {
            XmlUtils.elComTexto(dest, "CNPJ", documento);
        } else if (documento.length() == 11) {
            XmlUtils.elComTexto(dest, "CPF", documento);
        } else {
            throw new BusinessException("DESTINATARIO_INVALIDO",
                "CPF/CNPJ do destinatario deve conter 11 ou 14 digitos");
        }

        String nome = nfe.clienteNome;
        if (nome != null && !nome.isBlank()) {
            XmlUtils.elComTexto(dest, "xNome", limitar(nome, 60, nome));
        }

        boolean temEndereco = nfe.destinatarioLogradouro != null && !nfe.destinatarioLogradouro.isBlank();
        if (temEndereco) {
            Element ender = XmlUtils.el(document, dest, "enderDest");
            XmlUtils.elComTexto(ender, "xLgr", limitar(nfe.destinatarioLogradouro, 60, nfe.destinatarioLogradouro));
            XmlUtils.elComTexto(ender, "nro", limitar(nfe.destinatarioNumero, 60, "SN"));
            if (nfe.destinatarioComplemento != null && !nfe.destinatarioComplemento.isBlank()) {
                XmlUtils.elComTexto(ender, "xCpl", limitar(nfe.destinatarioComplemento, 60, nfe.destinatarioComplemento));
            }
            XmlUtils.elComTexto(ender, "xBairro", limitar(nfe.destinatarioBairro, 60, "CENTRO"));
            XmlUtils.elComTexto(ender, "cMun", String.valueOf(nfe.destinatarioCodigoMunicipio != null
                ? nfe.destinatarioCodigoMunicipio : fiscalConfig.municipioCodigo().orElse(0)));
            XmlUtils.elComTexto(ender, "xMun", limitar(nfe.destinatarioMunicipio, 60, fiscalConfig.municipioNome().orElse("MUNICIPIO")));
            XmlUtils.elComTexto(ender, "UF", nfe.destinatarioUf != null ? nfe.destinatarioUf : fiscalConfig.uf());
            XmlUtils.elComTexto(ender, "CEP", cep(nfe.destinatarioCep));
            XmlUtils.elComTexto(ender, "cPais", "1058");
            XmlUtils.elComTexto(ender, "xPais", "BRASIL");
        } else if (!isNfce(nfe)) {
            throw new BusinessException("DESTINATARIO_SEM_ENDERECO",
                "Informe o endereco completo do destinatario para emitir NF-e modelo 55");
        }

        String ie = nfe.destinatarioIe;
        boolean isento = ie != null && ie.equalsIgnoreCase("ISENTO");
        boolean temIe = ie != null && !ie.isBlank() && !isento;
        XmlUtils.elComTexto(dest, "indIEDest", temIe ? "1" : isento ? "2" : "9");
        if (temIe) {
            XmlUtils.elComTexto(dest, "IE", XmlUtils.somenteDigitos(ie));
        }
        if (nfe.destinatarioEmail != null && !nfe.destinatarioEmail.isBlank()) {
            XmlUtils.elComTexto(dest, "email", nfe.destinatarioEmail);
        }
    }

    private void buildItens(Document document, Element infNFe, Nfe nfe) {
        for (NfeItem item : nfe.itens) {
            Element det = XmlUtils.el(document, infNFe, "det");
            XmlUtils.atributo(det, "nItem", String.valueOf(item.numeroItem));

            Element prod = XmlUtils.el(document, det, "prod");
            XmlUtils.elComTexto(prod, "cProd", limitar(item.codigoProduto, 60, "PRODUTO"));
            XmlUtils.elComTexto(prod, "cEAN", "SEM GTIN");
            XmlUtils.elComTexto(prod, "xProd", limitar(item.descricao, 120, "PRODUTO"));
            XmlUtils.elComTexto(prod, "NCM", item.ncm);
            XmlUtils.elComTexto(prod, "CFOP", item.cfop);
            XmlUtils.elComTexto(prod, "uCom", limitar(item.unidadeComercial, 6, "UN"));
            XmlUtils.elComTexto(prod, "qCom", XmlUtils.decimal(item.quantidade, 4));
            XmlUtils.elComTexto(prod, "vUnCom", XmlUtils.decimal(item.valorUnitario, 10));
            XmlUtils.elComTexto(prod, "vProd", XmlUtils.decimal(item.valorTotal));
            XmlUtils.elComTexto(prod, "cEANTrib", "SEM GTIN");
            XmlUtils.elComTexto(prod, "uTrib", limitar(item.unidadeComercial, 6, "UN"));
            XmlUtils.elComTexto(prod, "qTrib", XmlUtils.decimal(item.quantidade, 4));
            XmlUtils.elComTexto(prod, "vUnTrib", XmlUtils.decimal(item.valorUnitario, 10));
            if (item.valorDesconto != null && item.valorDesconto.signum() > 0) {
                XmlUtils.elComTexto(prod, "vDesc", XmlUtils.decimal(item.valorDesconto));
            }
            XmlUtils.elComTexto(prod, "indTot", "1");

            Element imposto = XmlUtils.el(document, det, "imposto");
            buildIcms(document, imposto, item);
            buildPis(document, imposto, item);
            buildCofins(document, imposto, item);
        }
    }

    private void buildIcms(Document document, Element imposto, NfeItem item) {
        String cst = item.cstIcms != null ? item.cstIcms : "00";
        Element icms = XmlUtils.el(document, imposto, "ICMS");
        String orig = String.valueOf(item.origemMercadoria != null ? item.origemMercadoria : 0);
        String base = XmlUtils.decimal(item.valorTotal);
        String aliquota = XmlUtils.decimal(item.aliquotaIcms, 4);
        String valor = XmlUtils.decimal(item.valorIcms);

        if (cst.length() == 3) {
            switch (cst) {
                case "101" -> {
                    Element grupo = XmlUtils.el(document, icms, "ICMSSN101");
                    XmlUtils.elComTexto(grupo, "orig", orig);
                    XmlUtils.elComTexto(grupo, "CSOSN", cst);
                    XmlUtils.elComTexto(grupo, "pCredSN", "0.00");
                    XmlUtils.elComTexto(grupo, "vCredICMSSN", "0.00");
                }
                case "102", "103", "300", "400" -> {
                    Element grupo = XmlUtils.el(document, icms, "ICMSSN" + cst);
                    XmlUtils.elComTexto(grupo, "orig", orig);
                    XmlUtils.elComTexto(grupo, "CSOSN", cst);
                }
                case "201", "202", "203" -> {
                    Element grupo = XmlUtils.el(document, icms, "ICMSSN" + cst);
                    XmlUtils.elComTexto(grupo, "orig", orig);
                    XmlUtils.elComTexto(grupo, "CSOSN", cst);
                    if ("201".equals(cst)) {
                        XmlUtils.elComTexto(grupo, "pCredSN", "0.00");
                        XmlUtils.elComTexto(grupo, "vCredICMSSN", "0.00");
                    } else if ("202".equals(cst)) {
                        XmlUtils.elComTexto(grupo, "pRedBCST", "0.00");
                    }
                    XmlUtils.elComTexto(grupo, "modBCST", "4");
                    XmlUtils.elComTexto(grupo, "pMVAST", "0.00");
                    XmlUtils.elComTexto(grupo, "vBCST", "0.00");
                    XmlUtils.elComTexto(grupo, "pICMSST", "0.00");
                    XmlUtils.elComTexto(grupo, "vICMSST", "0.00");
                }
                case "500" -> {
                    Element grupo = XmlUtils.el(document, icms, "ICMSSN500");
                    XmlUtils.elComTexto(grupo, "orig", orig);
                    XmlUtils.elComTexto(grupo, "CSOSN", cst);
                    XmlUtils.elComTexto(grupo, "vBCSTRet", "0.00");
                    XmlUtils.elComTexto(grupo, "pST", "0.00");
                    XmlUtils.elComTexto(grupo, "vICMSSTRet", "0.00");
                }
                default -> {
                    Element grupo = XmlUtils.el(document, icms, "ICMSSN900");
                    XmlUtils.elComTexto(grupo, "orig", orig);
                    XmlUtils.elComTexto(grupo, "CSOSN", cst);
                }
            }
            return;
        }

        switch (cst) {
            case "00" -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS00");
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", cst);
                XmlUtils.elComTexto(grupo, "modBC", "3");
                XmlUtils.elComTexto(grupo, "vBC", base);
                XmlUtils.elComTexto(grupo, "pICMS", aliquota);
                XmlUtils.elComTexto(grupo, "vICMS", valor);
            }
            case "10" -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS10");
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", cst);
                XmlUtils.elComTexto(grupo, "modBC", "3");
                XmlUtils.elComTexto(grupo, "vBC", base);
                XmlUtils.elComTexto(grupo, "pICMS", aliquota);
                XmlUtils.elComTexto(grupo, "vICMS", valor);
                XmlUtils.elComTexto(grupo, "modBCST", "4");
                XmlUtils.elComTexto(grupo, "pMVAST", "0.00");
                XmlUtils.elComTexto(grupo, "pRedBCST", "0.00");
                XmlUtils.elComTexto(grupo, "vBCST", "0.00");
                XmlUtils.elComTexto(grupo, "pICMSST", "0.00");
                XmlUtils.elComTexto(grupo, "vICMSST", "0.00");
            }
            case "20" -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS20");
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", cst);
                XmlUtils.elComTexto(grupo, "modBC", "3");
                XmlUtils.elComTexto(grupo, "pRedBC", "0.00");
                XmlUtils.elComTexto(grupo, "vBC", base);
                XmlUtils.elComTexto(grupo, "pICMS", aliquota);
                XmlUtils.elComTexto(grupo, "vICMS", valor);
            }
            case "40", "41", "50", "51" -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS" + cst);
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", cst);
            }
            case "60" -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS60");
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", cst);
                XmlUtils.elComTexto(grupo, "vBCSTRet", "0.00");
                XmlUtils.elComTexto(grupo, "pST", "0.00");
                XmlUtils.elComTexto(grupo, "vICMSSTRet", "0.00");
            }
            case "70" -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS70");
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", cst);
                XmlUtils.elComTexto(grupo, "modBC", "3");
                XmlUtils.elComTexto(grupo, "pRedBC", "0.00");
                XmlUtils.elComTexto(grupo, "vBC", base);
                XmlUtils.elComTexto(grupo, "pICMS", aliquota);
                XmlUtils.elComTexto(grupo, "vICMS", valor);
                XmlUtils.elComTexto(grupo, "modBCST", "4");
                XmlUtils.elComTexto(grupo, "pMVAST", "0.00");
                XmlUtils.elComTexto(grupo, "pRedBCST", "0.00");
                XmlUtils.elComTexto(grupo, "vBCST", "0.00");
                XmlUtils.elComTexto(grupo, "pICMSST", "0.00");
                XmlUtils.elComTexto(grupo, "vICMSST", "0.00");
            }
            default -> {
                Element grupo = XmlUtils.el(document, icms, "ICMS90");
                XmlUtils.elComTexto(grupo, "orig", orig);
                XmlUtils.elComTexto(grupo, "CST", "90");
                XmlUtils.elComTexto(grupo, "modBC", "3");
                XmlUtils.elComTexto(grupo, "vBC", base);
                XmlUtils.elComTexto(grupo, "pICMS", aliquota);
                XmlUtils.elComTexto(grupo, "vICMS", valor);
            }
        }
    }

    private void buildPis(Document document, Element imposto, NfeItem item) {
        String cst = item.cstPis != null ? item.cstPis : "49";
        Element pis = XmlUtils.el(document, imposto, "PIS");
        if (Listas.PIS_ALIQUOTA.contains(cst)) {
            Element grupo = XmlUtils.el(document, pis, "PISAliq");
            XmlUtils.elComTexto(grupo, "CST", cst);
            XmlUtils.elComTexto(grupo, "vBC", XmlUtils.decimal(item.valorTotal));
            XmlUtils.elComTexto(grupo, "pPIS", XmlUtils.decimal(fiscalConfig.aliquotaPisPadrao(), 4));
            XmlUtils.elComTexto(grupo, "vPIS", XmlUtils.decimal(item.valorPis));
        } else if (Listas.PIS_NT.contains(cst)) {
            Element grupo = XmlUtils.el(document, pis, "PISNT");
            XmlUtils.elComTexto(grupo, "CST", cst);
        } else {
            Element grupo = XmlUtils.el(document, pis, "PISOutr");
            XmlUtils.elComTexto(grupo, "CST", cst);
            XmlUtils.elComTexto(grupo, "vBC", "0.00");
            XmlUtils.elComTexto(grupo, "pPIS", "0.00");
            XmlUtils.elComTexto(grupo, "vPIS", XmlUtils.decimal(item.valorPis));
        }
    }

    private void buildCofins(Document document, Element imposto, NfeItem item) {
        String cst = item.cstCofins != null ? item.cstCofins : "49";
        Element cofins = XmlUtils.el(document, imposto, "COFINS");
        if (Listas.COFINS_ALIQUOTA.contains(cst)) {
            Element grupo = XmlUtils.el(document, cofins, "COFINSAliq");
            XmlUtils.elComTexto(grupo, "CST", cst);
            XmlUtils.elComTexto(grupo, "vBC", XmlUtils.decimal(item.valorTotal));
            XmlUtils.elComTexto(grupo, "pCOFINS", XmlUtils.decimal(fiscalConfig.aliquotaCofinsPadrao(), 4));
            XmlUtils.elComTexto(grupo, "vCOFINS", XmlUtils.decimal(item.valorCofins));
        } else if (Listas.COFINS_NT.contains(cst)) {
            Element grupo = XmlUtils.el(document, cofins, "COFINSNT");
            XmlUtils.elComTexto(grupo, "CST", cst);
        } else {
            Element grupo = XmlUtils.el(document, cofins, "COFINSOutr");
            XmlUtils.elComTexto(grupo, "CST", cst);
            XmlUtils.elComTexto(grupo, "vBC", "0.00");
            XmlUtils.elComTexto(grupo, "pCOFINS", "0.00");
            XmlUtils.elComTexto(grupo, "vCOFINS", XmlUtils.decimal(item.valorCofins));
        }
    }

    private void buildTotal(Document document, Element infNFe, Nfe nfe) {
        BigDecimal vProd = BigDecimal.ZERO;
        BigDecimal vBc = BigDecimal.ZERO;
        for (NfeItem item : nfe.itens) {
            BigDecimal total = item.valorTotal != null ? item.valorTotal : BigDecimal.ZERO;
            vProd = vProd.add(total);
            if (item.cstIcms != null && Listas.ICMS_TRIBUTADO.contains(item.cstIcms)) {
                vBc = vBc.add(total);
            }
        }
        BigDecimal vDesc = nfe.valorDesconto != null ? nfe.valorDesconto : BigDecimal.ZERO;
        BigDecimal vOutro = nfe.valorAcrescimo != null ? nfe.valorAcrescimo : BigDecimal.ZERO;
        BigDecimal vNf = nfe.valorTotal != null ? nfe.valorTotal : vProd.subtract(vDesc).add(vOutro);
        if (vNf.signum() < 0) {
            vNf = BigDecimal.ZERO;
        }

        Element total = XmlUtils.el(document, infNFe, "total");
        Element icmsTot = XmlUtils.el(document, total, "ICMSTot");
        XmlUtils.elComTexto(icmsTot, "vBC", XmlUtils.decimal(vBc));
        XmlUtils.elComTexto(icmsTot, "vICMS", XmlUtils.decimal(nfe.valorIcms));
        XmlUtils.elComTexto(icmsTot, "vICMSDeson", "0.00");
        XmlUtils.elComTexto(icmsTot, "vFCP", "0.00");
        XmlUtils.elComTexto(icmsTot, "vBCST", "0.00");
        XmlUtils.elComTexto(icmsTot, "vST", "0.00");
        XmlUtils.elComTexto(icmsTot, "vFCPST", "0.00");
        XmlUtils.elComTexto(icmsTot, "vFCPSTRet", "0.00");
        XmlUtils.elComTexto(icmsTot, "vProd", XmlUtils.decimal(vProd));
        XmlUtils.elComTexto(icmsTot, "vFrete", "0.00");
        XmlUtils.elComTexto(icmsTot, "vSeg", "0.00");
        XmlUtils.elComTexto(icmsTot, "vDesc", XmlUtils.decimal(vDesc));
        XmlUtils.elComTexto(icmsTot, "vII", "0.00");
        XmlUtils.elComTexto(icmsTot, "vIPI", XmlUtils.decimal(nfe.valorIpi));
        XmlUtils.elComTexto(icmsTot, "vIPIDevol", "0.00");
        XmlUtils.elComTexto(icmsTot, "vPIS", XmlUtils.decimal(nfe.valorPis));
        XmlUtils.elComTexto(icmsTot, "vCOFINS", XmlUtils.decimal(nfe.valorCofins));
        XmlUtils.elComTexto(icmsTot, "vOutro", XmlUtils.decimal(vOutro));
        XmlUtils.elComTexto(icmsTot, "vNF", XmlUtils.decimal(vNf));
    }

    private void buildTransp(Document document, Element infNFe, Nfe nfe) {
        Element transp = XmlUtils.el(document, infNFe, "transp");
        XmlUtils.elComTexto(transp, "modFrete",
            String.valueOf(nfe.modalidadeFrete != null ? nfe.modalidadeFrete : 9));

        boolean temTransportadora = nfe.transportadoraNome != null && !nfe.transportadoraNome.isBlank();
        if (temTransportadora) {
            Element transporta = XmlUtils.el(document, transp, "transporta");
            String documento = XmlUtils.somenteDigitos(nfe.transportadoraDocumento);
            if (documento.length() == 14) {
                XmlUtils.elComTexto(transporta, "CNPJ", documento);
            } else if (documento.length() == 11) {
                XmlUtils.elComTexto(transporta, "CPF", documento);
            }
            XmlUtils.elComTexto(transporta, "xNome", limitar(nfe.transportadoraNome, 60, nfe.transportadoraNome));
            if (nfe.transportadoraEndereco != null) {
                XmlUtils.elComTexto(transporta, "xEnder", limitar(nfe.transportadoraEndereco, 60, nfe.transportadoraEndereco));
            }
            if (nfe.transportadoraMunicipio != null) {
                XmlUtils.elComTexto(transporta, "xMun", limitar(nfe.transportadoraMunicipio, 60, nfe.transportadoraMunicipio));
            }
            if (nfe.transportadoraUf != null) {
                XmlUtils.elComTexto(transporta, "UF", nfe.transportadoraUf);
            }
            if (nfe.transportadoraIe != null) {
                XmlUtils.elComTexto(transporta, "IE", XmlUtils.somenteDigitos(nfe.transportadoraIe));
            }
        }

        if (nfe.volumeQuantidade != null || nfe.volumeEspecie != null) {
            Element vol = XmlUtils.el(document, transp, "vol");
            if (nfe.volumeQuantidade != null) {
                XmlUtils.elComTexto(vol, "qVol", XmlUtils.decimal(nfe.volumeQuantidade, 0));
            }
            if (nfe.volumeEspecie != null) {
                XmlUtils.elComTexto(vol, "esp", limitar(nfe.volumeEspecie, 60, nfe.volumeEspecie));
            }
            if (nfe.volumePesoLiquido != null) {
                XmlUtils.elComTexto(vol, "pesoL", XmlUtils.decimal(nfe.volumePesoLiquido, 3));
            }
            if (nfe.volumePesoBruto != null) {
                XmlUtils.elComTexto(vol, "pesoB", XmlUtils.decimal(nfe.volumePesoBruto, 3));
            }
        }
    }

    private void buildPag(Document document, Element infNFe, Nfe nfe) {
        Element pag = XmlUtils.el(document, infNFe, "pag");
        for (NfePagamento pagamento : nfe.pagamentos) {
            Element detPag = XmlUtils.el(document, pag, "detPag");
            XmlUtils.elComTexto(detPag, "tPag", limitar(pagamento.formaPagamento, 2, "99"));
            if (pagamento.descricao != null && !pagamento.descricao.isBlank()
                && "99".equals(pagamento.formaPagamento)) {
                XmlUtils.elComTexto(detPag, "xPag", limitar(pagamento.descricao, 60, pagamento.descricao));
            }
            XmlUtils.elComTexto(detPag, "vPag", XmlUtils.decimal(pagamento.valor));
            if ("03".equals(pagamento.formaPagamento) || "04".equals(pagamento.formaPagamento)) {
                Element card = XmlUtils.el(document, detPag, "card");
                XmlUtils.elComTexto(card, "tpIntegra", "2");
            }
        }
        if (nfe.valorTroco != null && nfe.valorTroco.signum() > 0) {
            XmlUtils.elComTexto(pag, "vTroco", XmlUtils.decimal(nfe.valorTroco));
        }
    }

    private void buildInfAdic(Document document, Element infNFe, Nfe nfe) {
        boolean temFisco = nfe.informacoesFisco != null && !nfe.informacoesFisco.isBlank();
        boolean temComplementar = nfe.informacoesComplementares != null && !nfe.informacoesComplementares.isBlank();
        if (!temFisco && !temComplementar) {
            return;
        }
        Element infAdic = XmlUtils.el(document, infNFe, "infAdic");
        if (temFisco) {
            XmlUtils.elComTexto(infAdic, "infAdFisco", limitar(nfe.informacoesFisco, 2000, nfe.informacoesFisco));
        }
        if (temComplementar) {
            XmlUtils.elComTexto(infAdic, "infCpl", limitar(nfe.informacoesComplementares, 5000, nfe.informacoesComplementares));
        }
    }

    private void buildRespTecnico(Document document, Element infNFe) {
        String cnpj = fiscalConfig.respTecnicoCnpj().orElse("");
        String email = fiscalConfig.respTecnicoEmail().orElse("");
        String fone = XmlUtils.somenteDigitos(fiscalConfig.respTecnicoFone().orElse(""));
        if (cnpj.isBlank() || email.isBlank() || fone.isBlank()) {
            return;
        }
        Element resp = XmlUtils.el(document, infNFe, "infRespTec");
        XmlUtils.elComTexto(resp, "CNPJ", XmlUtils.somenteDigitos(cnpj));
        XmlUtils.elComTexto(resp, "xContato", fiscalConfig.respTecnicoContato().orElse("Suporte"));
        XmlUtils.elComTexto(resp, "email", email);
        XmlUtils.elComTexto(resp, "fone", fone);
        fiscalConfig.respTecnicoIdCsrt()
            .filter(valor -> !valor.isBlank())
            .ifPresent(valor -> XmlUtils.elComTexto(resp, "idCSRT", valor));
        fiscalConfig.respTecnicoHashCsrt()
            .filter(valor -> !valor.isBlank())
            .ifPresent(valor -> XmlUtils.elComTexto(resp, "hashCSRT", valor));
    }

    private void buildInfNFeSupl(Document document, Element raiz, Nfe nfe) {
        String ambiente = NfeConstantes.ambienteCodigo(nfe.ambiente);
        String urlBase = fiscalConfig.qrcodeUrl()
            .filter(valor -> !valor.isBlank())
            .orElseGet(() -> SefazEndpoints.qrcodePadrao(fiscalConfig.uf()));
        if (urlBase == null || urlBase.isBlank()) {
            throw new BusinessException("QRCODE_URL_NAO_CONFIGURADA",
                "Configure a URL de consulta do QR Code em fiscal.qrcode-url para a UF " + fiscalConfig.uf());
        }
        String qrCode = QrCodeNfce.gerarUrl(urlBase, nfe.chaveAcesso, ambiente,
            fiscalConfig.cscId().orElse(""), fiscalConfig.csc().orElse(""));

        Element supl = XmlUtils.el(document, raiz, "infNFeSupl");
        XmlUtils.elComTexto(supl, "qrCode", qrCode);
        XmlUtils.elComTexto(supl, "urlChave", urlBase);
    }

    private void validarNfe(Nfe nfe) {
        if (nfe == null) {
            throw new BusinessException("NFE_INVALIDA", "Nota fiscal nao informada");
        }
        if (nfe.chaveAcesso == null || nfe.chaveAcesso.length() != 44) {
            throw new BusinessException("CHAVE_INVALIDA", "Chave de acesso invalida");
        }
        if (nfe.itens == null || nfe.itens.isEmpty()) {
            throw new BusinessException("NFE_SEM_ITENS", "A nota fiscal precisa ter pelo menos um item");
        }
    }

    private boolean temCsc() {
        return fiscalConfig.csc().filter(valor -> !valor.isBlank()).isPresent()
            && fiscalConfig.cscId().filter(valor -> !valor.isBlank()).isPresent();
    }

    private boolean isNfce(Nfe nfe) {
        return NfeConstantes.MODELO_NFCE.equals(nfe.modelo);
    }

    private int idDestino(Nfe nfe) {
        String ufDestino = nfe.destinatarioUf;
        if (ufDestino == null || ufDestino.isBlank()) {
            return 1;
        }
        if ("EX".equalsIgnoreCase(ufDestino)) {
            return 3;
        }
        return ufDestino.equalsIgnoreCase(fiscalConfig.uf()) ? 1 : 2;
    }

    private String cNF(String chave) {
        return chave.substring(35, 43);
    }

    private int calcularDv(String chave) {
        if (chave == null || chave.length() != 44) {
            return 0;
        }
        return chave.charAt(43) - '0';
    }

    private String cep(String valor) {
        String digitos = XmlUtils.somenteDigitos(valor);
        if (digitos.isEmpty()) {
            return "00000000";
        }
        return digitos.length() >= 8 ? digitos.substring(0, 8) : String.format(Locale.ROOT, "%08d", Long.parseLong(digitos));
    }

    private String limitar(String valor, int tamanho, String padrao) {
        String texto = valor == null || valor.isBlank() ? padrao : valor.trim();
        if (texto == null) {
            return null;
        }
        return texto.length() <= tamanho ? texto : texto.substring(0, tamanho);
    }

    private static final class Listas {
        private static final java.util.Set<String> ICMS_TRIBUTADO =
            java.util.Set.of("00", "10", "20", "70", "90");
        private static final java.util.Set<String> PIS_ALIQUOTA = java.util.Set.of("01", "02");
        private static final java.util.Set<String> PIS_NT = java.util.Set.of("04", "05", "06", "07", "08", "09");
        private static final java.util.Set<String> COFINS_ALIQUOTA = java.util.Set.of("01", "02");
        private static final java.util.Set<String> COFINS_NT = java.util.Set.of("04", "05", "06", "07", "08", "09");
    }
}
