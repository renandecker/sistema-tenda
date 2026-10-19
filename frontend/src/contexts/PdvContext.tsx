import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiPdv } from '@/services/api';

interface Pdv {
  id: string;
  codigo: string;
  nome: string;
  status: string;
  portaBalancaFruteira?: string;
  portaBalancaBuffet?: string;
}

interface SessaoCaixa {
  id: string;
  pdvId: string;
  operadorAbertura: string;
  valorInicial: number;
  status: string;
  dataAbertura: string;
}

interface PdvContextType {
  pdvs: Pdv[];
  selectedPdv: Pdv | null;
  sessaoAtiva: SessaoCaixa | null;
  loading: boolean;
  fetchPdvs: () => Promise<void>;
  selectPdv: (pdv: Pdv) => void;
  abrirCaixa: (valorInicial: number) => Promise<SessaoCaixa>;
  fecharCaixa: (valorFinalContado: number) => Promise<SessaoCaixa>;
  fetchSessaoAtiva: () => Promise<void>;
}

const PdvContext = createContext<PdvContextType | undefined>(undefined);

export function PdvProvider({ children }: { children: React.ReactNode }) {
  const [pdvs, setPdvs] = useState<Pdv[]>([]);
  const [selectedPdv, setSelectedPdv] = useState<Pdv | null>(null);
  const [sessaoAtiva, setSessaoAtiva] = useState<SessaoCaixa | null>(null);
  const [loading] = useState(false);

  const fetchPdvs = async () => {
    try {
      const response = await apiPdv.listTerminaisAtivos();
      setPdvs(response.data.content || response.data);
    } catch (error) {
      console.error('Erro ao buscar PDVs:', error);
    }
  };

  const selectPdv = (pdv: Pdv) => {
    setSelectedPdv(pdv);
    localStorage.setItem('selectedPdv', JSON.stringify(pdv));
    fetchSessaoAtiva();
  };

  const fetchSessaoAtiva = async () => {
    if (!selectedPdv) return;
    try {
      const response = await api.get(`/pdv/sessoes-caixa/pdv/${selectedPdv.id}/aberta`);
      setSessaoAtiva(response.data || null);
    } catch (error) {
      setSessaoAtiva(null);
    }
  };

  const abrirCaixa = async (valorInicial: number) => {
    if (!selectedPdv) throw new Error('PDV não selecionado');
    const response = await api.post('/pdv/sessoes-caixa/abrir', {
      pdvId: selectedPdv.id,
      operador: 'Operador', // TODO: pegar do auth
      valorInicial,
    });
    setSessaoAtiva(response.data);
    return response.data;
  };

  const fecharCaixa = async (valorFinalContado: number) => {
    if (!sessaoAtiva) throw new Error('Nenhuma sessão aberta');
    const response = await api.post(`/pdv/sessoes-caixa/${sessaoAtiva.id}/fechar`, {
      operador: 'Operador',
      valorFinalContado,
    });
    setSessaoAtiva(null);
    return response.data;
  };

  useEffect(() => {
    fetchPdvs();
    const savedPdv = localStorage.getItem('selectedPdv');
    if (savedPdv) {
      try {
        const pdv = JSON.parse(savedPdv);
        setSelectedPdv(pdv);
      } catch {
        localStorage.removeItem('selectedPdv');
      }
    }
  }, []);

  useEffect(() => {
    if (selectedPdv) {
      fetchSessaoAtiva();
    }
  }, [selectedPdv]);

  return (
    <PdvContext.Provider value={{ pdvs, selectedPdv, sessaoAtiva, loading, fetchPdvs, selectPdv, abrirCaixa, fecharCaixa, fetchSessaoAtiva }}>
      {children}
    </PdvContext.Provider>
  );
}

export function usePdv() {
  const context = useContext(PdvContext);
  if (!context) {
    throw new Error('usePdv must be used within a PdvProvider');
  }
  return context;
}