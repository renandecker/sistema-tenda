import { useState, useEffect, useCallback, useRef } from 'react';
import { toast } from 'react-toastify';
import { tefClient, TefClient } from './TefClient';
import { 
  TransacaoTEF, 
  TefEvent, 
  IniciarTransacaoParams, 
  TipoTransacaoTEF,
  ComprovanteTEF 
} from './types';

interface UseTefReturn {
  isConnected: boolean;
  transacaoAtual: TransacaoTEF | null;
  eventoAtual: TefEvent | null;
  iniciarTransacao: (params: IniciarTransacaoParams) => Promise<TransacaoTEF>;
  confirmarTransacao: () => Promise<ComprovanteTEF>;
  desfazerTransacao: () => Promise<void>;
  reimprimirComprovante: (via: 'CLIENTE' | 'ESTABELECIMENTO') => Promise<string>;
  cancelarTransacao: () => Promise<void>;
  limparTransacao: () => void;
}

export function useTef(config?: { 
  httpUrl?: string; 
  wsUrl?: string;
  autoConnect?: boolean;
}): UseTefReturn {
  const [isConnected, setIsConnected] = useState(false);
  const [transacaoAtual, setTransacaoAtual] = useState<TransacaoTEF | null>(null);
  const [eventoAtual, setEventoAtual] = useState<TefEvent | null>(null);
  const clientRef = useRef<TefClient>(tefClient);
  const initializedRef = useRef(false);

  useEffect(() => {
    if (config?.httpUrl || config?.wsUrl) {
      clientRef.current = new TefClient({ 
        httpUrl: config.httpUrl, 
        wsUrl: config.wsUrl 
      });
    }

    const client = clientRef.current;

    const handleConnected = () => setIsConnected(true);
    const handleDisconnected = () => setIsConnected(false);
    const handleEvent = (event: TefEvent) => {
      setEventoAtual(event);
      setTransacaoAtual(prev => prev ? { ...prev, atualizadoEm: new Date(), status: event.event as any } : null);
      
      if (event.event === 'AUTORIZADA') {
        toast.success(event.message);
      } else if (event.event === 'NEGADA') {
        toast.error(event.message);
      } else if (event.event === 'ERRO') {
        toast.error(event.message);
      } else {
        toast.info(event.message);
      }
    };
    const handleError = (error: Error) => {
      toast.error(`Erro TEF: ${error.message}`);
    };

    client.on('connected', handleConnected);
    client.on('disconnected', handleDisconnected);
    client.on('event', handleEvent);
    client.on('error', handleError);

    if (config?.autoConnect !== false) {
      client.connect().catch(err => {
        console.warn('TEF auto-connect failed:', err.message);
      });
    }

    return () => {
      client.off('connected', handleConnected);
      client.off('disconnected', handleDisconnected);
      client.off('event', handleEvent);
      client.off('error', handleError);
    };
  }, [config?.httpUrl, config?.wsUrl, config?.autoConnect]);

  const iniciarTransacao = useCallback(async (params: IniciarTransacaoParams) => {
    try {
      setTransacaoAtual(null);
      setEventoAtual(null);
      
      const transacao = await clientRef.current.iniciarTransacao(params);
      setTransacaoAtual(transacao);
      return transacao;
    } catch (error: any) {
      toast.error(`Erro ao iniciar transação: ${error.message}`);
      throw error;
    }
  }, []);

  const confirmarTransacao = useCallback(async () => {
    if (!transacaoAtual) throw new Error('Nenhuma transação ativa');
    
    try {
      const comprovante = await clientRef.current.confirmarTransacao(transacaoAtual.requestId);
      setTransacaoAtual(prev => prev ? { ...prev, status: 'CONFIRMADA' as any } : null);
      toast.success('Transação confirmada com sucesso!');
      return comprovante;
    } catch (error: any) {
      toast.error(`Erro ao confirmar: ${error.message}`);
      throw error;
    }
  }, [transacaoAtual]);

  const desfazerTransacao = useCallback(async () => {
    if (!transacaoAtual) throw new Error('Nenhuma transação ativa');
    
    try {
      await clientRef.current.desfazerTransacao(transacaoAtual.requestId);
      setTransacaoAtual(prev => prev ? { ...prev, status: 'DESFEITA' as any } : null);
      toast.success('Transação desfeita/cancelada');
    } catch (error: any) {
      toast.error(`Erro ao desfazer: ${error.message}`);
      throw error;
    }
  }, [transacaoAtual]);

  const reimprimirComprovante = useCallback(async (via: 'CLIENTE' | 'ESTABELECIMENTO') => {
    if (!transacaoAtual) throw new Error('Nenhuma transação ativa');
    
    try {
      const comprovante = await clientRef.current.reimprimirComprovante(transacaoAtual.requestId, via);
      toast.success(`Comprovante ${via === 'CLIENTE' ? 'do cliente' : 'do estabelecimento'} reimpresso`);
      return comprovante;
    } catch (error: any) {
      toast.error(`Erro ao reimprimir: ${error.message}`);
      throw error;
    }
  }, [transacaoAtual]);

  const cancelarTransacao = useCallback(async () => {
    if (!transacaoAtual) throw new Error('Nenhuma transação ativa');
    
    try {
      await clientRef.current.cancelar(transacaoAtual.requestId);
      setTransacaoAtual(prev => prev ? { ...prev, status: 'CANCELADA' as any } : null);
      toast.info('Transação cancelada pelo operador');
    } catch (error: any) {
      toast.error(`Erro ao cancelar: ${error.message}`);
      throw error;
    }
  }, [transacaoAtual]);

  const limparTransacao = useCallback(() => {
    setTransacaoAtual(null);
    setEventoAtual(null);
  }, []);

  return {
    isConnected,
    transacaoAtual,
    eventoAtual,
    iniciarTransacao,
    confirmarTransacao,
    desfazerTransacao,
    reimprimirComprovante,
    cancelarTransacao,
    limparTransacao
  };
}

export function useTefSimples() {
  return useTef({ autoConnect: true });
}