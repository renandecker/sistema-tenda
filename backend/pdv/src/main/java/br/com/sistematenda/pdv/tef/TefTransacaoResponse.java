package br.com.sistematenda.pdv.tef;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TefTransacaoResponse {

    public String requestId;
    public BigDecimal valor;
    public TipoTransacaoTef tipoTransacao;
    public Integer parcelas;
    public String bandeira;
    public String documento;
    public StatusTransacaoTef status;
    public String nsu;
    public String codigoAutorizacao;
    public String comprovanteCliente;
    public String comprovanteEstabelecimento;
    public String motivoNegativa;
    public LocalDateTime createdAt;
    public LocalDateTime atualizadoEm;
    public boolean sucesso;
    public String mensagem;
}