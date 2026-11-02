import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ReactElement } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  FormControlLabel,
  Grid,
  LinearProgress,
  Switch,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  CheckCircleRounded as CheckIcon,
  ErrorRounded as ErrorIcon,
  HelpOutlineRounded as HelpIcon,
  HourglassEmptyRounded as PendingIcon,
  MemoryRounded as TefIcon,
  PlayArrowRounded as PlayIcon,
  PrintRounded as PrintIcon,
  QrCode2Rounded as PixIcon,
  RefreshRounded as RefreshIcon,
  WarningAmberRounded as WarningIcon,
} from '@mui/icons-material';

import { apiSaude, type SaudeCheckResponse, type StatusIntegracao, type StatusIntegracaoDTO } from '@/services/api';
import { brand } from '@/theme';

type Componente = 'tef' | 'pix' | 'impressora';
type Acao = Componente | 'tudo';

interface TesteProducaoPanelProps {
  pdvId?: string;
  pdvNome?: string;
  podeTestar?: boolean;
  permitirDestinoManual?: boolean;
  mostrarHistorico?: boolean;
}

const COMPONENTES: Componente[] = ['tef', 'pix', 'impressora'];

const configStatus: Record<
  StatusIntegracao,
  { label: string; cor: string; icone: ReactElement; severidade: 'success' | 'warning' | 'error' | 'info' }
> = {
  ok: { label: 'Operacional', cor: brand.green, icone: <CheckIcon fontSize="small" />, severidade: 'success' },
  atencao: { label: 'Atenção', cor: '#E8590C', icone: <WarningIcon fontSize="small" />, severidade: 'warning' },
  erro: { label: 'Falha', cor: brand.red, icone: <ErrorIcon fontSize="small" />, severidade: 'error' },
  nao_configurado: { label: 'Não configurado', cor: '#868E96', icone: <HelpIcon fontSize="small" />, severidade: 'info' },
  pendente: { label: 'Não testado', cor: '#1C7ED6', icone: <PendingIcon fontSize="small" />, severidade: 'info' },
};

const tituloComponente: Record<Componente, string> = {
  tef: 'TEF / Pinpad',
  pix: 'PIX / PSP',
  impressora: 'Impressora Fiscal',
};

const iconeComponente: Record<Componente, JSX.Element> = {
  tef: <TefIcon />,
  pix: <PixIcon />,
  impressora: <PrintIcon />,
};

function mensagemDeErro(erro: any): string {
  return (
    erro?.response?.data?.mensagem ||
    erro?.response?.data?.message ||
    erro?.message ||
    'Erro desconhecido ao executar o teste'
  );
}

function dtoErro(componente: Componente, mensagem: string): StatusIntegracaoDTO {
  return {
    componente,
    status: 'erro',
    mensagem: 'Falha ao executar o teste',
    detalhe: mensagem,
    horaVerificacao: new Date().toISOString(),
  };
}

function formatarHora(valor?: string | null): string {
  if (!valor) return '—';
  try {
    return new Date(valor).toLocaleString('pt-BR');
  } catch {
    return valor;
  }
}

export function TesteProducaoPanel({
  pdvId,
  pdvNome,
  podeTestar = true,
  permitirDestinoManual = false,
  mostrarHistorico = false,
}: TesteProducaoPanelProps) {
  const [status, setStatus] = useState<Record<Componente, StatusIntegracaoDTO | null>>({
    tef: null,
    pix: null,
    impressora: null,
  });
  const [executando, setExecutando] = useState<Record<Acao, boolean>>({
    tef: false,
    pix: false,
    impressora: false,
    tudo: false,
  });
  const [carregandoInicial, setCarregandoInicial] = useState(true);
  const [imprimirTeste, setImprimirTeste] = useState(true);
  const [destinoManual, setDestinoManual] = useState('');
  const [ultimaVerificacao, setUltimaVerificacao] = useState<string | null>(null);
  const [historico, setHistorico] = useState<SaudeCheckResponse[]>([]);
  const [erroGeral, setErroGeral] = useState<string | null>(null);

  const algumExecutando = useMemo(
    () => Object.values(executando).some(Boolean),
    [executando],
  );

  const aplicarResposta = useCallback((resposta: SaudeCheckResponse) => {
    setStatus({
      tef: resposta.tef ?? null,
      pix: resposta.pix ?? null,
      impressora: resposta.impressora ?? null,
    });
    setUltimaVerificacao(resposta.ultimaVerificacao || new Date().toISOString());
    setErroGeral(null);
  }, []);

  const carregarStatus = useCallback(async () => {
    setCarregandoInicial(true);
    setErroGeral(null);
    try {
      const { data } = await apiSaude.verificar();
      aplicarResposta(data);
    } catch (erro: any) {
      setErroGeral(mensagemDeErro(erro));
    } finally {
      setCarregandoInicial(false);
    }
  }, [aplicarResposta]);

  useEffect(() => {
    carregarStatus();
  }, [carregarStatus]);

  const executar = useCallback(
    async (acao: Acao) => {
      if (!podeTestar) return;

      setExecutando(prev => ({ ...prev, [acao]: true }));
      setErroGeral(null);

      try {
        if (acao === 'tudo') {
          const { data } = await apiSaude.verificarManual({
            pdvId,
            destino: destinoManual.trim() || undefined,
            imprimirTeste,
          });
          aplicarResposta(data);
          if (mostrarHistorico) {
            setHistorico(prev => [data, ...prev].slice(0, 10));
          }
        } else if (acao === 'tef') {
          const { data } = await apiSaude.testarTef();
          setStatus(prev => ({ ...prev, tef: data }));
          setUltimaVerificacao(data.horaVerificacao || new Date().toISOString());
        } else if (acao === 'pix') {
          const { data } = await apiSaude.testarPix();
          setStatus(prev => ({ ...prev, pix: data }));
          setUltimaVerificacao(data.horaVerificacao || new Date().toISOString());
        } else {
          const { data } = await apiSaude.testarImpressora({
            pdvId,
            destino: destinoManual.trim() || undefined,
            imprimirTeste,
          });
          setStatus(prev => ({ ...prev, impressora: data }));
          setUltimaVerificacao(data.horaVerificacao || new Date().toISOString());
        }
      } catch (erro: any) {
        const mensagem = mensagemDeErro(erro);
        if (acao === 'tudo') {
          setErroGeral(mensagem);
        } else {
          setStatus(prev => ({ ...prev, [acao]: dtoErro(acao, mensagem) }));
        }
      } finally {
        setExecutando(prev => ({ ...prev, [acao]: false }));
      }
    },
    [aplicarResposta, destinoManual, imprimirTeste, mostrarHistorico, pdvId, podeTestar],
  );

  const renderCard = (componente: Componente) => {
    const item = status[componente];
    const config = item ? configStatus[item.status] ?? configStatus.erro : null;

    return (
      <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <CardContent sx={{ flexGrow: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 1, mb: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box
                sx={{
                  display: 'grid',
                  placeItems: 'center',
                  width: 40,
                  height: 40,
                  borderRadius: 2,
                  bgcolor: 'rgba(245,168,0,0.12)',
                  color: brand.amberDark,
                }}
              >
                {iconeComponente[componente]}
              </Box>
              <Box>
                <Typography variant="subtitle1" fontWeight={700} lineHeight={1.2}>
                  {tituloComponente[componente]}
                </Typography>
                {item?.destino && (
                  <Typography variant="caption" color="text.secondary" noWrap sx={{ display: 'block', maxWidth: 220 }}>
                    {item.destino}
                  </Typography>
                )}
              </Box>
            </Box>
            {config && (
              <Chip
                size="small"
                icon={config.icone}
                label={config.label}
                sx={{
                  bgcolor: `${config.cor}18`,
                  color: config.cor,
                  fontWeight: 700,
                  '& .MuiChip-icon': { color: config.cor },
                }}
              />
            )}
          </Box>

          <Divider sx={{ my: 1.5 }} />

          {item ? (
            <>
              <Alert severity={config!.severidade} sx={{ mb: 1 }}>
                {item.mensagem}
              </Alert>
              {item.detalhe && (
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1, whiteSpace: 'pre-wrap' }}>
                  {item.detalhe}
                </Typography>
              )}
              <Typography variant="caption" color="text.secondary">
                {item.tempoMs != null ? `${item.tempoMs} ms · ` : ''}
                {formatarHora(item.horaVerificacao)}
              </Typography>
            </>
          ) : (
            <Typography variant="body2" color="text.secondary">
              {carregandoInicial ? 'Verificando configuração…' : 'Ainda não verificado.'}
            </Typography>
          )}
        </CardContent>

        <Box sx={{ px: 2, pb: 2 }}>
          <Button
            fullWidth
            variant="outlined"
            startIcon={executando[componente] ? <CircularProgress size={16} /> : <PlayIcon />}
            onClick={() => executar(componente)}
            disabled={!podeTestar || algumExecutando || carregandoInicial}
          >
            {executando[componente] ? 'Testando…' : 'Testar agora'}
          </Button>
        </Box>
      </Card>
    );
  };

  return (
    <Box>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
            <Box>
              <Typography variant="h6" fontWeight={700}>
                Teste de Produção
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Confirma a comunicação real com o TEF, o PSP do PIX e a impressora fiscal.
                {pdvNome ? ` PDV selecionado: ${pdvNome}.` : ''}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              <Button
                variant="outlined"
                startIcon={<RefreshIcon />}
                onClick={carregarStatus}
                disabled={algumExecutando || carregandoInicial}
              >
                Atualizar status
              </Button>
              <Button
                variant="contained"
                startIcon={executando.tudo ? <CircularProgress size={16} color="inherit" /> : <PlayIcon />}
                onClick={() => executar('tudo')}
                disabled={!podeTestar || algumExecutando || carregandoInicial}
              >
                {executando.tudo ? 'Testando tudo…' : 'Testar tudo'}
              </Button>
            </Box>
          </Box>

          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12} sm={6} md={4}>
              <FormControlLabel
                control={
                  <Switch
                    checked={imprimirTeste}
                    onChange={(e) => setImprimirTeste(e.target.checked)}
                    disabled={algumExecutando}
                  />
                }
                label="Imprimir cupom de teste na impressora"
              />
            </Grid>
            {permitirDestinoManual && (
              <Grid item xs={12} sm={6} md={5}>
                <TextField
                  fullWidth
                  size="small"
                  label="Destino da impressora (opcional)"
                  placeholder="tcp://192.168.0.50:9100, COM3, \\servidor\impressora ou nome no Windows"
                  value={destinoManual}
                  onChange={(e) => setDestinoManual(e.target.value)}
                  disabled={algumExecutando}
                  helperText="Vazio = usa a impressora cadastrada no PDV ou a padrão do Windows"
                />
              </Grid>
            )}
            <Grid item xs={12} md={permitirDestinoManual ? 3 : 8} sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="caption" color="text.secondary">
                Última verificação: {formatarHora(ultimaVerificacao)}
              </Typography>
            </Grid>
          </Grid>

          {algumExecutando && <LinearProgress sx={{ mt: 2, borderRadius: 1 }} />}
        </CardContent>
      </Card>

      {!podeTestar && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Seu usuário não tem permissão para executar os testes. Peça a um supervisor ou gerente.
        </Alert>
      )}

      {erroGeral && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErroGeral(null)}>
          {erroGeral}
        </Alert>
      )}

      <Grid container spacing={2}>
        {COMPONENTES.map((componente) => (
          <Grid item xs={12} md={4} key={componente}>
            {renderCard(componente)}
          </Grid>
        ))}
      </Grid>

      {mostrarHistorico && historico.length > 0 && (
        <Card sx={{ mt: 3 }}>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={700} gutterBottom>
              Histórico desta sessão
            </Typography>
            {historico.map((item, indice) => (
              <Box
                key={`${item.ultimaVerificacao}-${indice}`}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: 2,
                  py: 1,
                  borderBottom: indice < historico.length - 1 ? 1 : 0,
                  borderColor: 'divider',
                }}
              >
                <Typography variant="caption" color="text.secondary">
                  {formatarHora(item.ultimaVerificacao)}
                </Typography>
                <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                  {COMPONENTES.map((componente) => {
                    const resultado = item[componente];
                    const cfg = resultado ? configStatus[resultado.status] ?? configStatus.erro : configStatus.nao_configurado;
                    return (
                      <Tooltip key={componente} title={resultado?.mensagem || 'Sem resultado'}>
                        <Chip
                          size="small"
                          label={tituloComponente[componente].split(' ')[0]}
                          sx={{ bgcolor: `${cfg.cor}18`, color: cfg.cor, fontWeight: 600 }}
                        />
                      </Tooltip>
                    );
                  })}
                </Box>
              </Box>
            ))}
          </CardContent>
        </Card>
      )}
    </Box>
  );
}

export default TesteProducaoPanel;
