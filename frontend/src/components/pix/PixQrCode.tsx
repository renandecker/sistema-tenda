import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  IconButton,
  Alert,
  CircularProgress,
  Divider,
  Snackbar,
  Chip,
  Grid,
  Paper,
} from '@mui/material';
import {
  QrCode as QrCodeIcon,
  ContentCopy as CopyIcon,
  CheckCircle as CheckIcon,
  Error as ErrorIcon,
  Refresh as RefreshIcon,
  Timer as TimerIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import QRCode from 'qrcode.react';
import { toast } from 'react-toastify';

export interface PixChargeResponse {
  txid: string;
  qrCode?: string;
  qrCodeBase64?: string;
  pixCopiaECola: string;
  status: string;
  valor: number;
  expiracao?: string;
}

interface PixQrCodeProps {
  txid: string;
  valor: number;
  pixCopiaECola: string;
  qrCodeBase64?: string;
  status: string;
  expiracao?: string;
  onStatusChange?: (status: string) => void;
  onClose?: () => void;
  pollingInterval?: number;
  autoPolling?: boolean;
}

export const PixQrCode: React.FC<PixQrCodeProps> = ({
  txid,
  valor,
  pixCopiaECola,
  qrCodeBase64,
  status,
  expiracao,
  onStatusChange,
  onClose,
  pollingInterval = 5000,
  autoPolling = true,
}) => {
  const [currentStatus, setCurrentStatus] = useState(status);
  const [copied, setCopied] = useState(false);
  const [polling, setPolling] = useState(autoPolling);
  const [timeRemaining, setTimeRemaining] = useState<number | null>(null);
  const [expired, setExpired] = useState(false);

  const fetchStatus = useCallback(async () => {
    try {
      const response = await fetch(`/api/pdv/vendas/${txid}/pix/status?txid=${txid}`);
      if (response.ok) {
        const data: PixChargeResponse = await response.json();
        if (data.status !== currentStatus) {
          setCurrentStatus(data.status);
          onStatusChange?.(data.status);
        }
        
        if (data.status === 'CONCLUIDA' || data.status === 'CONCLUDED' || data.status === 'PAID') {
          setPolling(false);
          toast.success('Pagamento PIX confirmado!');
        } else if (data.status === 'EXPIRADA' || data.status === 'EXPIRED') {
          setPolling(false);
          setExpired(true);
          toast.warning('PIX expirado');
        }
      }
    } catch (error) {
      console.error('Erro ao consultar status PIX:', error);
    }
  }, [txid, currentStatus, onStatusChange]);

  useEffect(() => {
    if (!polling || currentStatus === 'CONCLUIDA' || currentStatus === 'EXPIRADA') {
      return;
    }

    const interval = setInterval(fetchStatus, pollingInterval);
    return () => clearInterval(interval);
  }, [polling, currentStatus, pollingInterval, fetchStatus]);

  useEffect(() => {
    if (expiracao) {
      const calculateTimeRemaining = () => {
        const exp = new Date(expiracao).getTime();
        const now = Date.now();
        const diff = exp - now;
        if (diff <= 0) {
          setTimeRemaining(0);
          setExpired(true);
          setPolling(false);
        } else {
          setTimeRemaining(Math.floor(diff / 1000));
        }
      };

      calculateTimeRemaining();
      const timer = setInterval(calculateTimeRemaining, 1000);
      return () => clearInterval(timer);
    }
  }, [expiracao]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(pixCopiaECola);
      setCopied(true);
      toast.success('Código PIX copiado!');
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      toast.error('Erro ao copiar código');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case 'CONCLUIDA':
      case 'CONCLUDED':
      case 'PAID':
        return 'success';
      case 'EXPIRADA':
      case 'EXPIRED':
        return 'warning';
      case 'REMOVIDA_PELO_USUARIO_RECEBEDOR':
      case 'CANCELLED':
        return 'error';
      default:
        return 'info';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status.toUpperCase()) {
      case 'CONCLUIDA':
      case 'CONCLUDED':
      case 'PAID':
        return 'Pago';
      case 'EXPIRADA':
      case 'EXPIRED':
        return 'Expirado';
      case 'REMOVIDA_PELO_USUARIO_RECEBEDOR':
      case 'CANCELLED':
        return 'Cancelado';
      default:
        return 'Aguardando pagamento';
    }
  };

  return (
    <Box sx={{ maxWidth: 400, mx: 'auto', p: 2 }}>
      {onClose && (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1 }}>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      )}

      <Card elevation={3}>
        <CardContent sx={{ textAlign: 'center' }}>
          <Box sx={{ mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              Pagamento PIX
            </Typography>
            <Typography variant="body2" color="text.secondary">
              TXID: {txid}
            </Typography>
          </Box>

          <Divider sx={{ my: 2 }} />

          <Box sx={{ mb: 3 }}>
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main' }}>
              R$ {valor.toFixed(2).replace('.', ',')}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Valor a pagar
            </Typography>
          </Box>

          <Chip
            label={getStatusLabel(currentStatus)}
            icon={
              currentStatus.toUpperCase() === 'CONCLUIDA' || currentStatus.toUpperCase() === 'CONCLUDED' ? (
                <CheckIcon fontSize="small" />
              ) : currentStatus.toUpperCase() === 'EXPIRADA' ? (
                <ErrorIcon fontSize="small" />
              ) : (
                <TimerIcon fontSize="small" />
              )
            }
            color={getStatusColor(currentStatus)}
            size="large"
            sx={{ mb: 2, width: '100%', justifyContent: 'center', fontWeight: 600 }}
          />

          {expired && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              Este PIX expirou. Gere um novo código para continuar.
            </Alert>
          )}

          {currentStatus.toUpperCase() === 'CONCLUIDA' && (
            <Alert severity="success" sx={{ mb: 2 }}>
              Pagamento confirmado com sucesso!
            </Alert>
          )}

          <Paper
            variant="outlined"
            sx={{ p: 2, mb: 2, bg: 'grey.50', minHeight: 200, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}
          >
            {qrCodeBase64 ? (
              <img
                src={`data:image/png;base64,${qrCodeBase64}`}
                alt="QR Code PIX"
                style={{ maxWidth: '100%', height: 'auto' }}
              />
            ) : (
              <QRCode
                value={pixCopiaECola}
                size={180}
                level="M"
                includeMargin={true}
                imageSettings={{
                  src: require('@/assets/logo.png'),
                  height: 40,
                  width: 40,
                  excavate: true,
                }}
              />
            )}
          </Paper>

          {timeRemaining !== null && !expired && currentStatus.toUpperCase() !== 'CONCLUIDA' && (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1, mb: 2, color: timeRemaining < 60 ? 'error.main' : 'text.secondary' }}>
              <TimerIcon fontSize="small" />
              <Typography variant="h6" sx={{ fontVariant: 'tabular-nums' }}>
                Expira em: {formatTime(timeRemaining)}
              </Typography>
            </Box>
          )}

          <Divider sx={{ my: 2 }} />

          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Pix Copia e Cola
            </Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Box component="textarea" 
                readOnly
                rows={3}
                sx={{ 
                  flex: 1, 
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem',
                  p: 1,
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: 'divider',
                  backgroundColor: 'grey.100',
                }}
                value={pixCopiaECola}
              />
              <Button
                variant="outlined"
                startIcon={copied ? <CheckIcon /> : <CopyIcon />}
                onClick={handleCopy}
                disabled={copied}
                sx={{ whiteSpace: 'nowrap', height: 'fit-content' }}
              >
                {copied ? 'Copiado!' : 'Copiar'}
              </Button>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={fetchStatus}
              disabled={currentStatus === 'CONCLUIDA' || expired}
            >
              Atualizar
            </Button>
            
            <Button
              variant={polling ? 'contained' : 'outlined'}
              startIcon={polling ? <TimerIcon /> : <TimerIcon />}
              onClick={() => setPolling(!polling)}
              disabled={currentStatus === 'CONCLUIDA' || expired}
            >
              {polling ? 'Parar verificação' : 'Iniciar verificação'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

interface PixPaymentModalProps {
  open: boolean;
  onClose: () => void;
  vendaId: string;
  valor: number;
  onPaymentConfirmed?: () => void;
  onPixGenerated?: (response: PixChargeResponse) => void;
}

export const PixPaymentModal: React.FC<PixPaymentModalProps> = ({
  open,
  onClose,
  vendaId,
  valor,
  onPaymentConfirmed,
  onPixGenerated,
}) => {
  const [pixData, setPixData] = useState<PixChargeResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const generatePix = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`/api/pdv/vendas/${vendaId}/pix/gerar?valor=${valor}`, {
        method: 'POST',
      });
      
      if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Erro ao gerar PIX');
      }
      
      const data: PixChargeResponse = await response.json();
      setPixData(data);
      onPixGenerated?.(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro desconhecido');
      toast.error('Erro ao gerar PIX');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      generatePix();
    } else {
      setPixData(null);
      setError(null);
    }
  }, [open]);

  if (!open) return null;

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 300 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" onClose={onClose}>
        {error}
      </Alert>
    );
  }

  if (!pixData) return null;

  return (
    <PixQrCode
      txid={pixData.txid}
      valor={pixData.valor}
      pixCopiaECola={pixData.pixCopiaECola}
      qrCodeBase64={pixData.qrCodeBase64}
      status={pixData.status}
      expiracao={pixData.expiracao}
      onStatusChange={(status) => {
        if (status === 'CONCLUIDA' || status === 'CONCLUDED' || status === 'PAID') {
          onPaymentConfirmed?.();
          setTimeout(onClose, 2000);
        }
      }}
      onClose={onClose}
    />
  );
};

export default PixQrCode;