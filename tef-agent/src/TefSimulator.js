const EventEmitter = require('events');
const { v4: uuidv4 } = require('uuid');

class TefSimulator extends EventEmitter {
  constructor(logger) {
    super();
    this.logger = logger;
    this.transactions = new Map();
  }

  async sendCommand(command, params, requestId) {
    this.logger.info('SIMULATOR: Command received', { command, requestId, params });

    switch (command) {
      case 'INICIAR_TRANSACAO':
        return this.simularIniciarTransacao(requestId, params);
      case 'CONFIRMAR_TRANSACAO':
        return this.simularConfirmarTransacao(requestId, params);
      case 'DESFAZER_TRANSACAO':
        return this.simularDesfazerTransacao(requestId, params);
      case 'REIMPRIMIR_COMPROVANTE':
        return this.simularReimprimirComprovante(requestId, params);
      case 'CONSULTAR_STATUS':
        return this.simularConsultarStatus(requestId, params);
      default:
        throw new Error(`Comando desconhecido: ${command}`);
    }
  }

  async simularIniciarTransacao(requestId, params) {
    const { valor, tipoTransacao, parcelas, bandeira, documento } = params;
    
    const transacao = {
      requestId,
      valor,
      tipoTransacao,
      parcelas: parcelas || 1,
      bandeira: bandeira || 'ALELO_ALIMENTACAO',
      documento: documento || `DOC-${Date.now()}`,
      status: 'INICIADA',
      nsu: null,
      codigoAutorizacao: null,
      comprovanteCliente: null,
      comprovanteEstabelecimento: null,
      createdAt: new Date()
    };

    this.transactions.set(requestId, transacao);

    this.emitEvent(requestId, 'AGUARDANDO_CARTAO', 'Insira ou aproxime o cartão');
    
    setTimeout(() => {
      this.emitEvent(requestId, 'PROCESSANDO', 'Processando transação...');
    }, 2000);

    setTimeout(() => {
      this.emitEvent(requestId, 'AGUARDANDO_SENHA', 'Digite a senha no pinpad');
    }, 4000);

    setTimeout(() => {
      this.emitEvent(requestId, 'AUTORIZANDO', 'Autorizando com a administradora...');
    }, 6000);

    setTimeout(() => {
      const aprovado = Math.random() > 0.1;
      
      if (aprovado) {
        transacao.status = 'AUTORIZADA';
        transacao.nsu = `NSU${Date.now()}${Math.floor(Math.random() * 1000)}`;
        transacao.codigoAutorizacao = String(Math.floor(100000 + Math.random() * 900000));
        transacao.comprovanteCliente = this.gerarComprovanteCliente(transacao);
        transacao.comprovanteEstabelecimento = this.gerarComprovanteEstabelecimento(transacao);
        
        this.emitEvent(requestId, 'AUTORIZADA', 'Transação aprovada!', {
          nsu: transacao.nsu,
          codigoAutorizacao: transacao.codigoAutorizacao,
          comprovanteCliente: transacao.comprovanteCliente,
          comprovanteEstabelecimento: transacao.comprovanteEstabelecimento
        });
      } else {
        transacao.status = 'NEGADA';
        transacao.motivoNegativa = 'Saldo insuficiente';
        
        this.emitEvent(requestId, 'NEGADA', 'Transação negada: Saldo insuficiente', {
          motivo: transacao.motivoNegativa
        });
      }
    }, 8000);

    return { 
      success: true, 
      requestId, 
      message: 'Transação iniciada. Aguarde eventos via WebSocket.',
      documento: transacao.documento
    };
  }

  async simularConfirmarTransacao(requestId, params) {
    const { originalRequestId } = params;
    const transacao = this.transactions.get(originalRequestId);
    
    if (!transacao) {
      throw new Error('Transação original não encontrada');
    }

    if (transacao.status !== 'AUTORIZADA') {
      throw new Error('Transação não está autorizada para confirmação');
    }

    transacao.status = 'CONFIRMADA';
    transacao.confirmadoEm = new Date();

    this.emitEvent(originalRequestId, 'CONFIRMADA', 'Transação confirmada com sucesso', {
      nsu: transacao.nsu,
      codigoAutorizacao: transacao.codigoAutorizacao
    });

    return {
      success: true,
      nsu: transacao.nsu,
      codigoAutorizacao: transacao.codigoAutorizacao,
      comprovanteCliente: transacao.comprovanteCliente,
      comprovanteEstabelecimento: transacao.comprovanteEstabelecimento
    };
  }

  async simularDesfazerTransacao(requestId, params) {
    const { originalRequestId } = params;
    const transacao = this.transactions.get(originalRequestId);
    
    if (!transacao) {
      throw new Error('Transação original não encontrada');
    }

    transacao.status = 'DESFEITA';
    transacao.desfeitoEm = new Date();

    this.emitEvent(originalRequestId, 'DESFEITA', 'Transação desfeita/cancelada', {});

    return {
      success: true,
      message: 'Transação desfeita com sucesso'
    };
  }

  async simularReimprimirComprovante(requestId, params) {
    const { originalRequestId, via } = params;
    const transacao = this.transactions.get(originalRequestId);
    
    if (!transacao) {
      throw new Error('Transação original não encontrada');
    }

    const comprovante = via === 'CLIENTE' 
      ? transacao.comprovanteCliente 
      : transacao.comprovanteEstabelecimento;

    return {
      success: true,
      comprovante
    };
  }

  async simularConsultarStatus(requestId, params) {
    const { originalRequestId } = params;
    const transacao = this.transactions.get(originalRequestId);
    
    if (!transacao) {
      return { success: false, error: 'Transação não encontrada' };
    }

    return {
      success: true,
      status: transacao.status,
      nsu: transacao.nsu,
      codigoAutorizacao: transacao.codigoAutorizacao
    };
  }

  emitEvent(requestId, eventType, message, data = {}) {
    this.emit('event', {
      requestId,
      event: eventType,
      message,
      data,
      timestamp: new Date().toISOString()
    });
  }

  gerarComprovanteCliente(transacao) {
    return `
================================
     COMPROVANTE DO CLIENTE
================================
ESTABELECIMENTO: TENDA ATACADO
CNPJ: 00.000.000/0001-00
ENDERECO: RUA EXEMPLO, 123
CIDADE: SAO PAULO - SP

DATA/HORA: ${new Date().toLocaleString('pt-BR')}
NSU: ${transacao.nsu}
COD. AUTORIZACAO: ${transacao.codigoAutorizacao}
CARTAO: **** **** **** ${Math.floor(1000 + Math.random() * 9000)}
BANDEIRA: ${this.getBandeiraLabel(transacao.bandeira)}
TIPO: ${this.getTipoTransacaoLabel(transacao.tipoTransacao)}
PARCELAS: ${transacao.parcelas}x

VALOR TOTAL: R$ ${(transacao.valor / 100).toFixed(2).replace('.', ',')}

TRANSACAO APROVADA

================================
   VIA DO CLIENTE - GUARDE
================================
`;
  }

  gerarComprovanteEstabelecimento(transacao) {
    return `
================================
  COMPROVANTE ESTABELECIMENTO
================================
ESTABELECIMENTO: TENDA ATACADO
CNPJ: 00.000.000/0001-00
ENDERECO: RUA EXEMPLO, 123
CIDADE: SAO PAULO - SP

DATA/HORA: ${new Date().toLocaleString('pt-BR')}
NSU: ${transacao.nsu}
COD. AUTORIZACAO: ${transacao.codigoAutorizacao}
CARTAO: **** **** **** ${Math.floor(1000 + Math.random() * 9000)}
BANDEIRA: ${this.getBandeiraLabel(transacao.bandeira)}
TIPO: ${this.getTipoTransacaoLabel(transacao.tipoTransacao)}
PARCELAS: ${transacao.parcelas}x
DOCUMENTO: ${transacao.documento}

VALOR TOTAL: R$ ${(transacao.valor / 100).toFixed(2).replace('.', ',')}

ASSINATURA DO CLIENTE: ___________________

TRANSACAO APROVADA

================================
   VIA DO ESTABELECIMENTO
================================
`;
  }

  getBandeiraLabel(bandeira) {
    const labels = {
      'ALELO_ALIMENTACAO': 'Alelo Alimentação',
      'ALELO_REFEICAO': 'Alelo Refeição',
      'ALELO_TUDO': 'Alelo Tudo',
      'BANRICOMPRAS_BANRICARD': 'Banricompras/Banricard',
      'TICKET_EDENRED': 'Ticket (Edenred)',
      'PLUXEE': 'Pluxee (Sodexo)',
      'VR_BENEFICIOS': 'VR Benefícios',
      'GREEN_CARD': 'Green Card',
      'VALECARD_VOLUS': 'ValeCard/Vólus'
    };
    return labels[bandeira] || bandeira;
  }

  getTipoTransacaoLabel(tipo) {
    const labels = {
      'DEBITO': 'Débito',
      'CREDITO_A_VISTA': 'Crédito à Vista',
      'CREDITO_PARCELADO': 'Crédito Parcelado',
      'VALE_ALIMENTACAO': 'Vale Alimentação',
      'VALE_REFEICAO': 'Vale Refeição'
    };
    return labels[tipo] || tipo;
  }

  async cancel(requestId) {
    this.logger.info('SIMULATOR: Cancel request', requestId);
    return { success: true };
  }

  shutdown() {
    this.logger.info('SIMULATOR: Shutting down');
    this.transactions.clear();
  }
}

module.exports = TefSimulator;