import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Alert,
  LinearProgress,
  Chip,
  IconButton,
  Divider,
} from '@mui/material';
import {
  CreditCard as CreditCardIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  HourglassEmpty as HourglassIcon,
  Print as PrintIcon,
  Cancel as CancelIcon,
  Refresh as RefreshIcon,
  SignalCellularConnectedNoInternet0Bar as TefForteIcon,
  SignalCellularOff as TefFracoIcon,
  Wifi as PixDentroIcon,
  WifiOff as PixForteIcon,
} from '@mui/icons-material';
import { useTef } from '@/services/tef';
import { toast } from 'react-toastify';

interface TefPaymentModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: (comprovante: { cliente: string; estabelecimento: string }) => void;
  valor: number;
  formaPagamento: string;
  bandeira?: string;
  parcelas?: number;
  cpfConsumidor?: string;
  vendaNumero?: string;
}

const STATUS_ICONS: Record<string, React.ReactNode> = {
  AGUARDANDO_CARTAO: <CreditCardIcon color="action" />,
  PROCESSANDO: <HourglassIcon color="action" />,
  AGUARDANDO_SENHA: <InfoIcon color="primary" />,
  AUTORIZANDO: <HourglassIcon color="primary" />,
  AUTORIZADA: <CheckCircleIcon color="success" />,
  NEGADA: <ErrorIcon color="error" />,
  CANCELADA: <CancelIcon color="warning" />,
  CONFIRMADA: <CheckCircleIcon color="success" />,
  DESFEITA: <CancelIcon color="warning" />,
  ERRO: <ErrorIcon color="error" />,
};

const STATUS_COLORS: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
  AGUARDANDO_CARTAO: 'info',
  PROCESSANDO: 'info',
  AGUARDANDO_SENHA: 'info',
  AUTORIZANDO: 'info',
  AUTORIZADA: 'success',
  NEGADA: 'error',
  CANCELADA: 'warning',
  CONFIRMADA: 'success',
  DESFEITA: 'warning',
  ERRO: 'error',
};

export function TefPaymentModal({
  open,
  onClose,
  onSuccess,
  valor,
  formaPagamento,
  bandeira,
  parcelas = 1,
  cpfConsumidor,
  vendaNumero,
}: TefPaymentModalProps) {
  const {
    isConnected,
    transacaoAtual,
    eventoAtual,
    tefStatus,
    pixStatus,
    iniciarTransacao,
    confirmarTransacao,
    desfazerTransacao,
    reimprimirComprovante,
    cancelarTransacao,
    limparTransacao,
  } = useTef();

  const [confirmando, setConfirmando] = useState(false);
  const [desfazendo, setDesfazendo] = useState(false);
  const [imprimindo, setImprimindo] = useState<'CLIENTE' | 'ESTABELECIMENTO' | null>(null);

  useEffect(() => {
    if (!open) {
      limparTransacao();
    }
  }, [open, limparTransacao]);

  const handleIniciar = async () => {
    try {
      const tipoTransacaoMap: Record<string, string> = {
        CARTAO_CREDITO: parcelas > 1 ? 'CREDITO_PARCELADO' : 'CREDITO_A_VISTA',
        CARTAO_DEBITO: 'DEBITO',
        VALE_ALIMENTACAO: 'VALE_ALIMENTACAO',
        VALE_REFEICAO: 'VALE_REFEICAO',
        VALE_CULTURA: 'VALE_CULTURA',
      };

      await iniciarTransacao({
        valor: Math.round(valor * 100), // converter para centavos
        tipoTransacao: tipoTransacaoMap[formaPagamento] as any,
        parcelas,
        bandeira,
        documento: vendaNumero,
        cpfCnpjConsumidor: cpfConsumidor,
      });
    } catch (error: any) {
      toast.error(error.message || 'Erro ao iniciar transação TEF');
    }
  };

  const handleConfirmar = async () => {
    if (!transacaoAtual) return;
    setConfirmando(true);
    try {
      const comprovante = await confirmarTransacao();
      onSuccess(comprovante);
      toast.success('Transação confirmada com sucesso!');
    } catch (error: any) {
      toast.error(error.message || 'Erro ao confirmar transação');
    } finally {
      setConfirmando(false);
    }
  };

  const handleDesfazer = async () => {
    if (!transacaoAtual) return;
    setDesfazendo(true);
    try {
      await desfazerTransacao();
      toast.success('Transação desfeita');
      onClose();
    } catch (error: any) {
      toast.error(error.message || 'Erro ao desfazer transação');
    } finally {
      setDesfazendo(false);
    }
  };

  const handleCancelar = async () => {
    if (!transacaoAtual) return;
    try {
      await cancelarTransacao();
      toast.info('Transação cancelada pelo operador');
      onClose();
    } catch (error: any) {
      toast.error(error.message || 'Erro ao cancelar transação');
    }
  };

  const handleImprimir = async (via: 'CLIENTE' | 'ESTABELECIMENTO') => {
    if (!transacaoAtual) return;
    setImprimindo(via);
    try {
      const comprovante = await reimprimirComprovante(via);
      // Abrir em nova janela para impressão
      const printWindow = window.open('', '_blank');
      if (printWindow) {
        printWindow.document.write(`
          <html>
            <head><title>Comprovante ${via === 'CLIENTE' ? 'Cliente' : 'Estabelecimento'}</title></head>
            <body onload="window.print()">
              <pre style="font-family: monospace; white-space: pre-wrap;">${comprovante}</pre>
            </body>
          </html>
        `);
        printWindow.document.close();
      }
      toast.success(`Comprovante ${via === 'CLIENTE' ? 'do cliente' : 'do estabelecimento'} enviado para impressão`);
    } catch (error: any) {
      toast.error(error.message || 'Erro ao imprimir comprovante');
    } finally {
      setImprimindo(null);
    }
  };

  const canConfirm = transacaoAtual?.status === 'AUTORIZADA';
  const canDesfazer = transacaoAtual?.status === 'AUTORIZADA' || transacaoAtual?.status === 'NEGADA';
  const isFinalizado = transacaoAtual && ['AUTORIZADA', 'NEGADA', 'CANCELADA', 'CONFIRMADA', 'DESFEITA', 'ERRO'].includes(transacaoAtual.status);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth disableEscapeKeyDown={!!transacaoAtual && !isFinalizado}>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
          <Typography variant="h6">
            <CreditCardIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            Pagamento TEF - Venda #{vendaNumero}
          </Typography>
<Chip
            label={isConnected ? 'Conectado' : 'Desconectado'}
            color={isConnected ? 'success' : 'error'}
            size="small"
            icon={isConnected ? <CheckCircleIcon fontSize="small" /> : <ErrorIcon fontSize="small" />}
          />
          <Chip
            label={tefStatus.status === 'dentro' ? 'TEF Dentro' : 'TEF Fora'}
            color={tefStatus.status === 'dentro' ? 'success' : 'error'}
            size="small"
            icon={tefStatus.status === 'dentro' ? <CheckCircleIcon fontSize="small" /> : <ErrorIcon fontSize="small" />}
          />
          <Chip
            label={pixStatus.status === 'dentro' ? 'PIX Dentro' : 'PIX Fora'}
            color={pixStatus.status === 'dentro' ? 'success' : 'error'}
            size="small"
            icon={pixStatus.status === 'dentro' ? <CheckCircleIcon fontSize="small" /> : <ErrorIcon fontSize="small" />}
          />
        </Box>
      </DialogTitle>
    
    <DialogContent>
        <Box sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Valor: <strong>{new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor)}</strong>
            {' | Forma: '}{formaPagamento.replace('_', ' ')}
            {parcelas > 1 && ` | ${parcelas}x`}
            {bandeira && ` | Bandeira: ${bandeira}`}
          </Typography>
          <Divider sx={{ mb: 2 }} />

          {!transacaoAtual ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Alert severity="info" icon={<InfoIcon />}>
                Clique em "Iniciar Transação" para comunicar com o pinpad
              </Alert>
            </Box>
          ) : (
            <>
              <Box sx={{ mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  {STATUS_ICONS[eventoAtual?.event || transacaoAtual.status] || <HourglassIcon />}
                  <Typography variant="h6" color={STATUS_COLORS[eventoAtual?.event || transacaoAtual.status] as any}>
                    {eventoAtual?.message || transacaoAtual.status}
                  </Typography>
                </Box>
                {eventoAtual && (
                  <Typography variant="body2" color="text.secondary">
                    {new Date(eventoAtual.timestamp).toLocaleTimeString('pt-BR')}
                  </Typography>
                )}
                
                {transacaoAtual.nsu && (
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    <strong>NSU:</strong> {transacaoAtual.nsu}
                  </Typography>
                )}
                {transacaoAtual.codigoAutorizacao && (
                  <Typography variant="body2">
                    <strong>Cód. Autorização:</strong> {transacaoAtual.codigoAutorizacao}
                  </Typography>
                )}
                {transacaoAtual.motivoNegativa && (
                  <Alert severity="error" sx={{ mt: 1 }}>
                    Motivo: {transacaoAtual.motivoNegativa}
                  </Alert>
                )}
              </Box>

              {eventoAtual && eventoAtual.event !== 'AUTORIZADA' && eventoAtual.event !== 'NEGADA' && (
                <LinearProgress variant="indeterminate" sx={{ mb: 2 }} />
              )}

              {transacaoAtual.comprovanteCliente && (
                <Alert severity="success" icon={<CheckCircleIcon />} sx={{ mb: 2 }}>
                  Transação autorizada! Aguardando confirmação.
                </Alert>
              )}

              {transacaoAtual.status === 'NEGADA' && (
                <Alert severity="error" icon={<ErrorIcon />} sx={{ mb: 2 }}>
                  Transação negada: {transacaoAtual.motivoNegativa || 'Motivo não informado'}
                </Alert>
              )}
            </>
          )}
        </Box>
      </DialogContent>

      <DialogActions>
        {!transacaoAtual ? (
          <Button variant="contained" startIcon={<CreditCardIcon />} onClick={handleIniciar} disabled={!isConnected}>
            Iniciar Transação TEF
          </Button>
        ) : (
          <>
            {transacaoAtual.status === 'AUTORIZADA' && (
              <>
                <Button 
                  variant="contained" 
                  color="secondary"
                  startIcon={<PrintIcon />}
                  onClick={() => handleImprimir('CLIENTE')}
                  disabled={imprimindo === 'CLIENTE'}
                >
                  {imprimindo === 'CLIENTE' ? 'Imprimindo...' : 'Imprimir Via Cliente'}
                </Button>
                <Button 
                  variant="outlined" 
                  startIcon={<PrintIcon />}
                  onClick={() => handleImprimir('ESTABELECIMENTO')}
                  disabled={imprimindo === 'ESTABELECIMENTO'}
                >
                  {imprimindo === 'ESTABELECIMENTO' ? 'Imprimindo...' : 'Imprimir Via Estabelecimento'}
                </Button>
                <Button 
                  variant="contained" 
                  startIcon={<CheckCircleIcon />}
                  onClick={handleConfirmar}
                  disabled={confirmando}
                >
                  {confirmando ? 'Confirmando...' : 'Confirmar Transação'}
                </Button>
                <Button 
                  variant="outlined" 
                  color="error"
                  startIcon={<CancelIcon />}
                  onClick={handleDesfazer}
                  disabled={desfazendo}
                >
                  {desfazendo ? 'Desfazendo...' : 'Desfazer/Cancelar'}
                </Button>
              </>
            )}
            
            {(transacaoAtual.status === 'NEGADA' || transacaoAtual.status === 'ERRO') && (
              <Button variant="contained" onClick={onClose}>
                Fechar
              </Button>
            )}

            {['AGUARDANDO_CARTAO', 'PROCESSANDO', 'AGUARDANDO_SENHA', 'AUTORIZANDO'].includes(transacaoAtual?.status || '') && (
              <Button 
                variant="outlined" 
                color="warning"
                startIcon={<CancelIcon />}
                onClick={handleCancelar}
              >
                Cancelar Transação
              </Button>
            )}

            {transacaoAtual?.status === 'CONFIRMADA' && (
              <Button variant="contained" onClick={onClose}>
                Fechar
              </Button>
            )}
          </>
        )}
      </DialogActions>
    </Dialog>
  );
}