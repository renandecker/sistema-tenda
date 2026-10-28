import {
  TefConfig,
  TEF_CONFIG_DEFAULT,
  IniciarTransacaoParams,
  TransacaoTEF,
  TefResponse,
  ComprovanteTEF,
  MAPEAMENTO_FORMA_PAGAMENTO_TEF,
  MAPEAMENTO_BANDEIRA_TEF
} from './types';

export class TefClient {
  private config: TefConfig;
  private ws: WebSocket | null = null;
  private reconnectTimer: any = null;
  private pendingRequests: Map<string, { resolve: (v: TefResponse) => void; reject: (e: Error) => void }> = new Map();
  private requestCounter = 0;
  private isConnecting = false;
  private shouldReconnect = false;
  private listeners: { [key: string]: Function[] } = {};

  constructor(config: Partial<TefConfig> = {}) {
    this.config = { ...TEF_CONFIG_DEFAULT, ...config };
  }

  on(event: string, listener: Function) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(listener);
  }

  off(event: string, listener: Function) {
    if (!this.listeners[event]) return;
    this.listeners[event] = this.listeners[event].filter(l => l !== listener);
  }

  once(event: string, listener: Function) {
    const onceWrapper = (...args: any[]) => {
      this.off(event, onceWrapper);
      listener(...args);
    };
    this.on(event, onceWrapper);
  }

  emit(event: string, ...args: any[]) {
    if (!this.listeners[event]) return;
    this.listeners[event].forEach(listener => listener(...args));
  }

  setMaxListeners(_n: number) {}

  connect(): Promise<void> {
    if (this.ws?.readyState === WebSocket.OPEN) {
      return Promise.resolve();
    }
    if (this.isConnecting) {
      return new Promise((resolve, reject) => {
        this.once('connected', resolve);
        this.once('error', reject);
      });
    }

    this.isConnecting = true;
    this.shouldReconnect = true;

    return new Promise((resolve, reject) => {
      try {
        this.ws = new WebSocket(this.config.wsUrl);
        
        this.ws.onopen = () => {
          this.isConnecting = false;
          this.emit('connected');
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            this.handleMessage(message);
          } catch (e) {
            console.warn('Failed to parse TEF message:', e);
          }
        };

        this.ws.onclose = () => {
          this.isConnecting = false;
          this.emit('disconnected');
          if (this.shouldReconnect && this.config.autoReconnect) {
            this.scheduleReconnect();
          }
        };

        this.ws.onerror = () => {
          this.isConnecting = false;
          const err = new Error('WebSocket connection error');
          this.emit('error', err);
          reject(err);
        };
      } catch (error) {
        this.isConnecting = false;
        reject(error);
      }
    });
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.connect().catch(() => {});
    }, this.config.reconnectInterval);
  }

  private handleMessage(message: any) {
    switch (message.type) {
      case 'TEF_EVENT':
        this.emit('event', {
          requestId: message.requestId,
          event: message.event,
          message: message.message,
          data: message.data,
          timestamp: message.timestamp
        });
        break;
      case 'TEF_RESPONSE':
        this.emit('response', {
          requestId: message.requestId,
          success: message.success,
          message: message.message,
          data: message.data,
          error: message.error
        });
        this.resolvePending(message.requestId, message);
        break;
      case 'CONNECTED':
        console.log('TEF Agent connected:', message.mode);
        break;
      case 'PONG':
        break;
      default:
        console.warn('Unknown TEF message type:', message.type);
    }
  }

  private resolvePending(requestId: string, response: TefResponse) {
    const pending = this.pendingRequests.get(requestId);
    if (pending) {
      this.pendingRequests.delete(requestId);
      if (response.success) {
        pending.resolve(response);
      } else {
        pending.reject(new Error(response.error || 'TEF command failed'));
      }
    }
  }

  private generateRequestId(): string {
    return `req_${Date.now()}_${++this.requestCounter}`;
  }

  private async sendHttpCommand(command: string, params: Record<string, any>): Promise<TefResponse> {
    this.generateRequestId();
    
    const response = await fetch(`${this.config.httpUrl}/tef/command`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ command, params }),
      signal: AbortSignal.timeout(this.config.timeout)
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: 'HTTP error' }));
      throw new Error(error.error || `HTTP ${response.status}`);
    }

    return response.json();
  }

  async iniciarTransacao(params: IniciarTransacaoParams): Promise<TransacaoTEF> {
    await this.ensureConnected();
    
    const response = await this.sendHttpCommand('INICIAR_TRANSACAO', params);
    
    if (!response.success) {
      throw new Error(response.error || 'Falha ao iniciar transação');
    }

    return this.aguardarFinalizacao(response.requestId);
  }

  private async aguardarFinalizacao(requestId: string): Promise<TransacaoTEF> {
    return new Promise((resolve, reject) => {
      const timeout = setTimeout(() => {
        this.pendingRequests.delete(requestId);
        reject(new Error('Timeout aguardando finalização da transação'));
      }, this.config.timeout);

      this.pendingRequests.set(requestId, { 
        resolve: (resp) => {
          clearTimeout(timeout);
          if (resp.data?.transacao) {
            resolve(resp.data.transacao);
          } else {
            resolve(this.buildTransacaoFromResponse(requestId, resp));
          }
        }, 
        reject: (err) => {
          clearTimeout(timeout);
          reject(err);
        }
      });
    });
  }

  private buildTransacaoFromResponse(requestId: string, response: TefResponse): TransacaoTEF {
    return {
      requestId,
      valor: response.data?.valor || 0,
      tipoTransacao: response.data?.tipoTransacao || 'DEBITO',
      parcelas: response.data?.parcelas || 1,
      bandeira: response.data?.bandeira || '',
      documento: response.data?.documento || '',
      status: 'AUTORIZADA',
      nsu: response.data?.nsu,
      codigoAutorizacao: response.data?.codigoAutorizacao,
      comprovanteCliente: response.data?.comprovanteCliente,
      comprovanteEstabelecimento: response.data?.comprovanteEstabelecimento,
      createdAt: new Date(),
      atualizadoEm: new Date()
    };
  }

  async confirmarTransacao(requestId: string): Promise<ComprovanteTEF> {
    await this.ensureConnected();
    
    const response = await this.sendHttpCommand('CONFIRMAR_TRANSACAO', { originalRequestId: requestId });
    
    if (!response.success) {
      throw new Error(response.error || 'Falha ao confirmar transação');
    }

    return {
      cliente: response.data?.comprovanteCliente || '',
      estabelecimento: response.data?.comprovanteEstabelecimento || ''
    };
  }

  async desfazerTransacao(requestId: string): Promise<void> {
    await this.ensureConnected();
    
    const response = await this.sendHttpCommand('DESFAZER_TRANSACAO', { originalRequestId: requestId });
    
    if (!response.success) {
      throw new Error(response.error || 'Falha ao desfazer transação');
    }
  }

  async reimprimirComprovante(requestId: string, via: 'CLIENTE' | 'ESTABELECIMENTO'): Promise<string> {
    await this.ensureConnected();
    
    const response = await this.sendHttpCommand('REIMPRIMIR_COMPROVANTE', { 
      originalRequestId: requestId, 
      via 
    });
    
    if (!response.success) {
      throw new Error(response.error || 'Falha ao reimprimir comprovante');
    }

    return response.data?.comprovante || '';
  }

  async consultarStatus(requestId: string): Promise<Partial<TransacaoTEF>> {
    await this.ensureConnected();
    
    const response = await this.sendHttpCommand('CONSULTAR_STATUS', { originalRequestId: requestId });
    
    if (!response.success) {
      throw new Error(response.error || 'Falha ao consultar status');
    }

    return response.data || {};
  }

  async cancelar(requestId: string): Promise<void> {
    await this.ensureConnected();
    
    const response = await fetch(`${this.config.httpUrl}/tef/cancel`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ requestId })
    });

    if (!response.ok) {
      throw new Error('Falha ao cancelar transação');
    }
  }

  private async ensureConnected() {
    if (this.ws?.readyState !== WebSocket.OPEN) {
      await this.connect();
    }
  }

  // Helpers para mapear formas de pagamento do frontend para TEF
  static formaPagamentoParaTef(formaPagamento: string): any {
    return MAPEAMENTO_FORMA_PAGAMENTO_TEF[formaPagamento] || 'DEBITO';
  }

  static bandeiraParaTef(bandeira: string): string {
    return MAPEAMENTO_BANDEIRA_TEF[bandeira] || bandeira;
  }

  disconnect() {
    this.shouldReconnect = false;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.pendingRequests.forEach(({ reject }) => {
      reject(new Error('TEF client disconnected'));
    });
    this.pendingRequests.clear();
  }

  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}

export const tefClient = new TefClient();