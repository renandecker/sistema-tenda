import { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Divider,
  Alert,
  Button,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Refresh as RefreshIcon,
  Lock as LockIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { useAuth } from '@/hooks/useAuth';
import { NotificationModal, NotificationSeverity } from '@/components/NotificationModal';

interface StatusItem {
  status: 'funcionando' | 'atencao' | 'problema' | 'nao_configurado';
  icone: string;
  cor: 'green' | 'orange' | 'red' | 'gray';
  mensagem: string;
  detalhe: string;
  horaVerificacao: string;
}

interface SaudeState {
  pix: StatusItem;
  tef: StatusItem;
  ultimaVerificacao: string | null;
  historico: StatusItem[];
  verificando: boolean;
}

const statusInicial: StatusItem = {
  status: 'nao_configurado',
  icone: '',
  cor: 'gray',
  mensagem: '',
  detalhe: '',
  horaVerificacao: '',
};

function useStatusClass(status: string) {
  switch (status) {
    case 'funcionando': return 'bg-green-100 text-green-800';
    case 'atencao': return 'bg-orange-100 text-orange-800';
    case 'problema': return 'bg-red-100 text-red-800';
    default: return 'bg-gray-100 text-gray-800';
  }
}

export function SaudePage() {
  const { user, isAuthenticated, hasPermission, isInRole } = useAuth();
  const [state, setState] = useState<SaudeState>({
    pix: { ...statusInicial },
    tef: { ...statusInicial },
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
      // Chama o endpoint de health check do tef-agent
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
        const novoStatus: StatusItem = {
          status: data.pix.status,
          icone: data.pix.icone,
          cor: data.pix.cor,
          mensagem: data.pix.mensagem,
          detalhe: data.pix.detalhe || '',
          horaVerificacao: agora,
        };

        setState({
          pix: novoStatus,
          tef: data.tef ? { ...data.tef, horaVerificacao: agora } : state.tef,
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
              pix: data.pix ? { ...data.pix, horaVerificacao: agora } : state.pix,
              tef: data.tef ? { ...data.tef, horaVerificacao: agora } : state.tef,
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

      {/* Status Cards */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={4}>
          <Card sx={{ p: 2 }}>
            <CardContent>
              <Typography variant="h5" sx={{ mb: 1 }}>
                PIX
                <span style={{ fontSize: '0.75em', marginLeft: 1, textTransform: 'uppercase' }}>
                  {state.pix.status === 'nao_configurado' ? '(não configurado)' : ''}
                </span>
              </Typography>
              <Alert
                severity={state.pix.cor as 'success' | 'warning' | 'error' | 'info'}
                sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}
              >
                <span style={{ fontSize: 1.25 }}>💠</span>
                <span>{state.pix.mensagem}</span>
              </Alert>
              <Typography variant="caption" color="text.secondary">
                Última verificação: {state.pix.horaVerificacao ? new Date(state.pix.horaVerificacao).toLocaleString('pt-BR') : 'Nunca'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={4}>
          <Card sx={{ p: 2 }}>
            <CardContent>
              <Typography variant="h5" sx={{ mb: 1 }}>
                TEF
                <span style={{ fontSize: '0.75em', marginLeft: 1, textTransform: 'uppercase' }}>
                  {state.tef.status === 'nao_configurado' ? '(não configurado)' : ''}
                </span>
              </Typography>
              <Alert
                severity={state.tef.cor as 'success' | 'warning' | 'error' | 'info'}
                sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}
              >
                <span style={{ fontSize: 1.25 }}>📟</span>
                <span>{state.tef.mensagem}</span>
              </Alert>
              <Typography variant="caption" color="text.secondary">
                Última verificação: {state.tef.horaVerificacao ? new Date(state.tef.horaVerificacao).toLocaleString('pt-BR') : 'Nunca'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ p: 2 }}>
            <CardContent>
              <Typography variant="h5" sx={{ mb: 2 }}>
                Última Verificação Global
              </Typography>
              {state.ultimaVerificacao ? (
                <Typography variant="body1">
                  {new Date(state.ultimaVerificacao).toLocaleString('pt-BR')}
                </Typography>
              ) : (
                <Typography variant="body1" color="text.secondary">
                  Ainda não verificado
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

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