import { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Typography,
  Divider,
  Button,
} from '@mui/material';

import { useAuth } from '@/hooks/useAuth';
import { NotificationModal, NotificationSeverity } from '@/components/NotificationModal';

import { TesteProducaoPanel } from '@/components/TesteProducaoPanel';

interface SaudeState {
  tef: any;
  pix: any;
  impressora: any;
  ultimaVerificacao: string | null;
  historico: any[];
  verificando: boolean;
}

const statusInicial: any = {
  status: 'nao_configurado',
  icone: '',
  cor: 'gray',
  mensagem: '',
  detalhe: '',
  horaVerificacao: '',
};

export function SaudePage() {
  const { isAuthenticated, hasPermission, isInRole }: any = useAuth();
  const [state, setState] = useState<SaudeState>({
    tef: { ...statusInicial },
    pix: { ...statusInicial },
    impressora: { ...statusInicial },
    ultimaVerificacao: null,
    historico: [],
    verificando: false,
  });

  const [notificacao, setNotificacao] = useState<{
    open: boolean;
    title?: string;
    message: string;
    severity: NotificationSeverity;
  }>({ open: false, message: '', severity: 'info' });

  const notificar = (message: string, severity: NotificationSeverity, title?: string) =>
    setNotificacao({ open: true, message, severity, title });

  // Verificar se o usuário tem permissão (supervisor ou gerente)
  const podeVerificarAgora = () => {
    if (!isAuthenticated) return false;
    // Supervisor e Gerente podem verificar
    return isInRole('SUPERVISOR') || isInRole('GERENTE') || hasPermission('pagamentos.verificar');
  };

  // Health check API call
  const executarVerificacao = async () => {
    if (!podeVerificarAgora()) {
      notificar(
        'Apenas supervisores e gerentes podem executar verificação manual.',
        'warning',
        'Permissão necessária',
      );
      return;
    }

    setState(prev => ({ ...prev, verificando: true }));
    try {
      const response = await fetch('/api/saude-check', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ manual: true })
      });

      const data = await response.json();
      
      if (data.success) {
        // Atualizar estado com os novos resultados
        const agora = new Date().toISOString();
        const novoStatus: any = {
          status: data.pix.status,
          icone: data.pix.icone,
          cor: data.pix.cor,
          mensagem: data.pix.mensagem,
          detalhe: data.pix.detalhe || '',
          horaVerificacao: agora,
        };

        setState({
          tef: data.tef ? { ...data.tef, horaVerificacao: agora } : state.tef,
          pix: novoStatus,
          impressora: data.impressora ? { ...data.impressora, horaVerificacao: agora } : state.impressora,
          ultimaVerificacao: agora,
          historico: [...state.historico, { ...novoStatus, horaVerificacao: agora }],
          verificando: false,
        });

        // Manter histórico limitado a 10 entradas
        if (state.historico.length >= 10) {
          setState(prev => ({
            ...prev,
            historico: prev.historico.slice(-9),
          }));
        }

        notificar('Verificação concluída com sucesso.', 'success', 'Verificação realizada');
      } else {
        notificar('Erro na verificação: ' + (data.message || 'Erro desconhecido'), 'error');
      }
    } catch (error: any) {
      console.error('Erro na verificação:', error);
      notificar('Erro ao conectar com o serviço de saúde.', 'error');
    } finally {
      setState(prev => ({ ...prev, verificando: false }));
    }
  };

  // Carregar status inicial quando a página monta
  useEffect(() => {
    if (isAuthenticated) {
      const carregarStatus = async () => {
        try {
          const response = await fetch('/api/saude-check', {
            method: 'GET',
          });
          const data = await response.json();
          
          if (data.success) {
            const agora = new Date().toISOString();
            setState({
              tef: data.tef ? { ...data.tef, horaVerificacao: agora } : state.tef,
              pix: data.pix ? { ...data.pix, horaVerificacao: agora } : state.pix,
              impressora: data.impressora ? { ...data.impressora, horaVerificacao: agora } : state.impressora,
              ultimaVerificacao: data.ultimaVerificacao || agora,
              historico: data.historico || [],
              verificando: false,
            });
          }
        } catch (error) {
          console.error('Erro ao carregar status:', error);
        }
      };
      carregarStatus();
    }
  }, [isAuthenticated]);

  return (
    <Box sx={{ p: 2, pb: 0 }}>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>
          Saúde dos Pagamentos
        </Typography>
        <Divider />
      </Box>

      {/* Painel de Teste de Produção - substitui os cards individuais */}
      <TesteProducaoPanel
        podeTestar={podeVerificarAgora()}
        permitirDestinoManual={true}
        mostrarHistorico={state.historico.length > 0}
      />

      {/* Histórico e Botão Verificar Agora */}
      <Box sx={{ mt: 3 }}>
        <Divider />
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 500 }}>
          Histórico de Verificações
        </Typography>
        
        {state.verificando ? (
          <Box sx={{ p: 2 }}>
            <Typography variant="body2" color="primary">
              Verificando... por favor aguarde
            </Typography>
          </Box>
        ) : (
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Box sx={{ p: 1 }}>
                <Button
                  variant="contained"
                  onClick={executarVerificacao}
                  disabled={!podeVerificarAgora()}
                  sx={{
                    width: '100%',
                    background: '#1976d2',
                    color: 'white',
                    '&:hover': { background: '#1565c0' },
                  }}
                >
                  {podeVerificarAgora() ? 'Verificar agora' : 'Sem permissão'}
                </Button>
              </Box>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Box sx={{ p: 1 }}>
                <Button
                  variant="contained"
                  onClick={() => window.location.reload()}
                  sx={{
                    width: '100%',
                    background: '#6c757d',
                    color: 'white',
                    '&:hover': { background: '#5a6268' },
                  }}
                >
                  Atualizar Página
                </Button>
              </Box>
            </Grid>
          </Grid>
        )}

        {/* Histórico de verificações */}
        {state.historico.length > 0 && (
          <Box sx={{ mt: 2, maxHeight: '200px', overflowY: 'auto', p: 1 }}>
            <Typography variant="caption" color="text.secondary" sx={{ mb: 1 }}>
              {state.historico.length} verificação(s) recente(s)
            </Typography>
            {state.historico.map((item, index) => (
              <Box key={index} sx={{ p: 0.5, borderBottom: '1px solid #e0e0e0' }}>
                <Typography variant="caption" color="text.primary">
                  {item.horaVerificacao ? new Date(item.horaVerificacao).toLocaleString('pt-BR') : 'N/A'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  PIX: {item.mensagem} | TEF: {item.icone} {item.mensagem}
                </Typography>
              </Box>
            ))}
          </Box>
        )}
      </Box>

      <NotificationModal
        open={notificacao.open}
        onClose={() => setNotificacao(prev => ({ ...prev, open: false }))}
        title={notificacao.title}
        message={notificacao.message}
        severity={notificacao.severity}
      />
    </Box>
  );
}