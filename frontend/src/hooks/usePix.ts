import { useState, useCallback, useEffect } from 'react';
import { apiPdv } from '@/services/api';

interface PixChargeResponse {
  txid: string;
  qrCode?: string;
  qrCodeBase64?: string;
  pixCopiaECola: string;
  status: string;
  valor: number;
  expiracao?: string;
}

export const usePix = () => {
  const [pixData, setPixData] = useState<PixChargeResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [polling, setPolling] = useState(false);

  const generatePix = useCallback(async (vendaId: string, valor: number, txid?: string) => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiPdv.gerarPix(vendaId, valor, txid);
      setPixData(response.data);
      return response.data;
    } catch (err: any) {
      const message = err.response?.data?.message || 'Erro ao gerar PIX';
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const consultarStatus = useCallback(async (vendaId: string, txid: string) => {
    try {
      const response = await apiPdv.consultarPix(vendaId, txid);
      const data = response.data;
      setPixData(data);
      return data;
    } catch (err: any) {
      const message = err.response?.data?.message || 'Erro ao consultar status';
      setError(message);
      throw new Error(message);
    }
  }, []);

  const startPolling = useCallback((vendaId: string, txid: string, interval: number = 5000) => {
    setPolling(true);
    const poll = async () => {
      if (!polling) return;
      try {
        const data = await consultarStatus(vendaId, txid);
        if (data.status === 'CONCLUIDA' || data.status === 'CONCLUDED' || data.status === 'PAID') {
          setPolling(false);
        } else if (data.status === 'EXPIRADA' || data.status === 'EXPIRED') {
          setPolling(false);
        }
      } catch (err) {
        console.error('Erro no polling PIX:', err);
      }
    };

    const intervalId = setInterval(poll, interval);
    return () => {
      setPolling(false);
      clearInterval(intervalId);
    };
  }, [polling, consultarStatus]);

  const stopPolling = useCallback(() => {
    setPolling(false);
  }, []);

  const copyPixCode = useCallback(async (pixCopiaECola: string) => {
    try {
      await navigator.clipboard.writeText(pixCopiaECola);
      return true;
    } catch {
      return false;
    }
  }, []);

  const reset = useCallback(() => {
    setPixData(null);
    setError(null);
    setLoading(false);
    setPolling(false);
  }, []);

  return {
    pixData,
    loading,
    error,
    polling,
    generatePix,
    consultarStatus,
    startPolling,
    stopPolling,
    copyPixCode,
    reset,
  };
};

export default usePix;