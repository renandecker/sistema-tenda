export type TipoTransacaoTEF = 
  | 'DEBITO'
  | 'CREDITO_A_VISTA'
  | 'CREDITO_PARCELADO'
  | 'VALE_ALIMENTACAO'
  | 'VALE_REFEICAO'
  | 'VALE_CULTURA';

export type StatusTransacaoTEF = 
  | 'INICIADA'
  | 'AGUARDANDO_CARTAO'
  | 'PROCESSANDO'
  | 'AGUARDANDO_SENHA'
  | 'AUTORIZANDO'
  | 'AUTORIZADA'
  | 'NEGADA'
  | 'CANCELADA'
  | 'CONFIRMADA'
  | 'DESFEITA'
  | 'ERRO';

export type EventoTEF = 
  | 'AGUARDANDO_CARTAO'
  | 'PROCESSANDO'
  | 'AGUARDANDO_SENHA'
  | 'AUTORIZANDO'
  | 'AUTORIZADA'
  | 'NEGADA'
  | 'CANCELADA'
  | 'CONFIRMADA'
  | 'DESFEITA'
  | 'ERRO';

export interface IniciarTransacaoParams {
  valor: number; // em centavos
  tipoTransacao: TipoTransacaoTEF;
  parcelas?: number;
  bandeira?: string;
  documento?: string;
  cpfCnpjConsumidor?: string;
}

export interface TransacaoTEF {
  requestId: string;
  valor: number;
  tipoTransacao: TipoTransacaoTEF;
  parcelas: number;
  bandeira: string;
  documento: string;
  status: StatusTransacaoTEF;
  nsu?: string;
  codigoAutorizacao?: string;
  comprovanteCliente?: string;
  comprovanteEstabelecimento?: string;
  motivoNegativa?: string;
  createdAt: Date;
  atualizadoEm: Date;
}

export interface TefEvent {
  requestId: string;
  event: EventoTEF;
  message: string;
  data?: Record<string, any>;
  timestamp: string;
}

export interface TefResponse {
  requestId: string;
  success: boolean;
  message?: string;
  data?: Record<string, any>;
  error?: string;
}

export interface ComprovanteTEF {
  cliente: string;
  estabelecimento: string;
}

export interface TefConfig {
  httpUrl: string;
  wsUrl: string;
  timeout: number;
  autoReconnect: boolean;
  reconnectInterval: number;
}

export const TEF_CONFIG_DEFAULT: TefConfig = {
  httpUrl: 'http://localhost:8080',
  wsUrl: 'ws://localhost:8081',
  timeout: 120000,
  autoReconnect: true,
  reconnectInterval: 5000
};

export const MAPEAMENTO_FORMA_PAGAMENTO_TEF: Record<string, TipoTransacaoTEF> = {
  'CARTAO_DEBITO': 'DEBITO',
  'CARTAO_CREDITO': 'CREDITO_A_VISTA',
  'VALE_ALIMENTACAO': 'VALE_ALIMENTACAO',
  'VALE_REFEICAO': 'VALE_REFEICAO',
  'VALE_CULTURA': 'VALE_CULTURA'
};

export const MAPEAMENTO_BANDEIRA_TEF: Record<string, string> = {
  'ALELO_ALIMENTACAO': 'ALELO_ALIMENTACAO',
  'ALELO_REFEICAO': 'ALELO_REFEICAO',
  'ALELO_TUDO': 'ALELO_TUDO',
  'BANRICOMPRAS_BANRICARD': 'BANRICOMPRAS_BANRICARD',
  'TICKET_EDENRED': 'TICKET_EDENRED',
  'PLUXEE': 'PLUXEE',
  'VR_BENEFICIOS': 'VR_BENEFICIOS',
  'GREEN_CARD': 'GREEN_CARD',
  'VALECARD_VOLUS': 'VALECARD_VOLUS'
};