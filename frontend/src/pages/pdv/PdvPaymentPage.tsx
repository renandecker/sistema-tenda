import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Alert,
  Divider,
  InputAdornment,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import {
  Add as AddIcon,
  PointOfSale as PosIcon,
  ReceiptLong as ReceiptIcon,
  Payments as PaymentIcon,
  QrCode as QrCodeIcon,
  Close as CloseIcon,
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Receipt as ReceiptPrintIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiPdv } from '@/services/api';
import { PixPaymentModal, PixQrCode } from '@/components/pix';
import { TefPaymentModal } from '@/components/tef';
import { BandeiraCartao } from '@/constants/bandeiras';

interface Venda {
  id: string;
  numero: string;
  status: string;
  valorTotal: number;
  valorPago: number;
  cliente?: { nome: string; documento: string };
  dataCriacao: string;
  pagamentos?: Pagamento[];
}

interface Pagamento {
  id: string;
  formaPagamento: string;
  valor: number;
  status: string;
  pixTxId?: string;
  pixQrCode?: string;
  dataPagamento: string;
}

const FORMA_PAGAMENTO_LABELS: Record<string, string> = {
  DINHEIRO: 'Dinheiro',
  CARTAO_CREDITO: 'Cartão Crédito',
  CARTAO_DEBITO: 'Cartão Débito',
  PIX: 'PIX',
  VALE_ALIMENTACAO: 'Vale Alimentação',
  VALE_REFEICAO: 'Vale Refeição',
  VALE_CULTURA: 'Vale Cultura',
};

const STATUS_LABELS: Record<string, string> = {
  ABERTA: 'Aberta',
  PAGA: 'Paga',
  CANCELADA: 'Cancelada',
  PENDENTE: 'Pendente',
  AUTORIZADO: 'Autorizado',
  NEGADO: 'Negado',
};

export function PdvPaymentPage() {
  const [vendas, setVendas] = useState<Venda[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedVenda, setSelectedVenda] = useState<Venda | null>(null);
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [paymentForm, setPaymentForm] = useState({
    formaPagamento: 'PIX',
    valor: 0,
    parcelas: 1,
    cpfConsumidor: '',
    emitirNfce: false,
  });
  const [pixDialogOpen, setPixDialogOpen] = useState(false);
  const [pixGenerating, setPixGenerating] = useState(false);
  const [finalizing, setFinalizing] = useState(false);
  const [tefDialogOpen, setTefDialogOpen] = useState(false);
  const [tefBandeira, setTefBandeira] = useState<BandeiraCartao>(BandeiraCartao.ALELO_ALIMENTACAO);

  const loadVendas = async () => {
    setLoading(true);
    try {
      const response = await apiPdv.listVendas({ status: 'ABERTA', size: 50 });
      setVendas(response.data.content || response.data);
    } catch (error) {
      toast.error('Erro ao carregar vendas');
    } finally {
      setLoading(false);
    }
  };

  const handlePayment = async () => {
    if (!selectedVenda) return;

    // Para cartões e vales, usar fluxo TEF
    const formasTef = ['CARTAO_CREDITO', 'CARTAO_DEBITO', 'VALE_ALIMENTACAO', 'VALE_REFEICAO', 'VALE_CULTURA'];
    if (formasTef.includes(paymentForm.formaPagamento)) {
      handleTefPayment();
      return;
    }

    // Para dinheiro e PIX, fluxo direto
    try {
      const response = await apiPdv.processarPagamento(selectedVenda.id, paymentForm);
      toast.success('Pagamento processado com sucesso!');
      setPaymentDialogOpen(false);
      loadVendas();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao processar pagamento');
    }
  };

  const handlePixPayment = async () => {
    if (!selectedVenda) return;

    setPixGenerating(true);
    try {
      const saldoDevedor = selectedVenda.valorTotal - selectedVenda.valorPago;
      const response = await apiPdv.gerarPix(selectedVenda.id, saldoDevedor);
      setSelectedVenda({ ...selectedVenda, pagamentos: [...(selectedVenda.pagamentos || []), {
        id: 'temp',
        formaPagamento: 'PIX',
        valor: saldoDevedor,
        status: 'PENDENTE',
        pixTxId: response.data.txid,
        pixQrCode: response.data.pixCopiaECola,
        dataPagamento: new Date().toISOString(),
      }] });
      setPixDialogOpen(true);
      toast.success('PIX gerado com sucesso!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao gerar PIX');
    } finally {
      setPixGenerating(false);
    }
  };

  const handlePixConfirmed = () => {
    setPixDialogOpen(false);
    loadVendas();
  };

  const handleTefPayment = async () => {
    if (!selectedVenda) return;

    const formasTef = ['CARTAO_CREDITO', 'CARTAO_DEBITO', 'VALE_ALIMENTACAO', 'VALE_REFEICAO', 'VALE_CULTURA'];
    if (!formasTef.includes(paymentForm.formaPagamento)) {
      toast.error('Forma de pagamento não suportada para TEF');
      return;
    }

    setTefBandeira(
      paymentForm.formaPagamento === 'VALE_ALIMENTACAO' ? BandeiraCartao.ALELO_ALIMENTACAO :
      paymentForm.formaPagamento === 'VALE_REFEICAO' ? BandeiraCartao.ALELO_REFEICAO :
      BandeiraCartao.ALELO_TUDO
    );
    
    setPaymentDialogOpen(false);
    setTefDialogOpen(true);
  };

  const handleTefSuccess = async (comprovante: { cliente: string; estabelecimento: string }) => {
    if (!selectedVenda) return;

    try {
      // O backend já processou a confirmação via webhook, só precisamos registrar o pagamento
      // O pagamento já foi persistido no backend quando a transação foi confirmada
      // Aqui apenas fechamos o modal e recarregamos
      setTefDialogOpen(false);
      toast.success('Pagamento TEF confirmado com sucesso!');
      loadVendas();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao registrar pagamento TEF');
    }
  };

  const handleFinalizarVenda = async () => {
    if (!selectedVenda) return;

    setFinalizing(true);
    try {
      const cpfConsumidor = paymentForm.emitirNfce ? paymentForm.cpfConsumidor : '';
      await apiPdv.finalizarVenda(selectedVenda.id, cpfConsumidor);
      toast.success('Venda finalizada com sucesso!');
      setPaymentDialogOpen(false);
      loadVendas();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao finalizar venda');
    } finally {
      setFinalizing(false);
    }
  };

  const getSaldoDevedor = (venda: Venda) => {
    return venda.valorTotal - venda.valorPago;
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('pt-BR');
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" component="h1">
            <PosIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            PDV - Pagamentos
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Gerencie pagamentos de vendas em aberto
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={loadVendas}
          disabled={loading}
        >
          Atualizar
        </Button>
      </Box>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Venda</TableCell>
                  <TableCell>Cliente</TableCell>
                  <TableCell align="right">Total</TableCell>
                  <TableCell align="right">Pago</TableCell>
                  <TableCell align="right">Saldo</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="center">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {vendas.map((venda) => (
                  <TableRow key={venda.id} hover>
                    <TableCell>
                      <Typography variant="body2" component="span">
                        #{venda.numero}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatDate(venda.dataCriacao)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {venda.cliente ? (
                        <>
                          <Typography variant="body2">{venda.cliente.nome}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            {venda.cliente.documento}
                          </Typography>
                        </>
                      ) : (
                        <Typography variant="body2" color="text.secondary">Consumidor final</Typography>
                      )}
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {formatCurrency(venda.valorTotal)}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2">{formatCurrency(venda.valorPago)}</Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography 
                        variant="body2" 
                        sx={{ 
                          fontWeight: 600, 
                          color: getSaldoDevedor(venda) > 0 ? 'error.main' : 'success.main' 
                        }}
                      >
                        {formatCurrency(getSaldoDevedor(venda))}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={STATUS_LABELS[venda.status] || venda.status}
                        size="small"
                        color={venda.status === 'PAGA' ? 'success' : venda.status === 'CANCELADA' ? 'error' : 'warning'}
                      />
                    </TableCell>
                    <TableCell align="center">
                      <IconButton
                        size="small"
                        onClick={() => {
                          setSelectedVenda(venda);
                          setPaymentForm({ formaPagamento: 'PIX', valor: getSaldoDevedor(venda), parcelas: 1 });
                          setPaymentDialogOpen(true);
                        }}
                        disabled={getSaldoDevedor(venda) <= 0}
                        aria-label="Processar pagamento"
                      >
                        <PaymentIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => {
                          setSelectedVenda(venda);
                          handlePixPayment();
                        }}
                        disabled={getSaldoDevedor(venda) <= 0 || pixGenerating}
                        aria-label="Gerar PIX"
                      >
                        <QrCodeIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          
          {vendas.length === 0 && !loading && (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" color="text.secondary">
                Nenhuma venda em aberto
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Dialog Pagamento */}
      <Dialog open={paymentDialogOpen} onClose={() => setPaymentDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          <Typography variant="h6">
            Processar Pagamento - Venda #{selectedVenda?.numero}
          </Typography>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Saldo devedor: <strong>{formatCurrency(selectedVenda ? getSaldoDevedor(selectedVenda) : 0)}</strong>
            </Typography>
            <Divider sx={{ mb: 2 }} />
            
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField
                  select
                  label="Forma de Pagamento"
                  value={paymentForm.formaPagamento}
                  onChange={(e) => setPaymentForm({ ...paymentForm, formaPagamento: e.target.value })}
                  fullWidth
                  SelectProps={{ native: true }}
                >
                  <option value="DINHEIRO">Dinheiro</option>
                  <option value="CARTAO_CREDITO">Cartão Crédito</option>
                  <option value="CARTAO_DEBITO">Cartão Débito</option>
                  <option value="PIX">PIX</option>
                  <option value="VALE_ALIMENTACAO">Vale Alimentação</option>
                  <option value="VALE_REFEICAO">Vale Refeição</option>
                  <option value="VALE_CULTURA">Vale Cultura</option>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  type="number"
                  label="Valor"
                  value={paymentForm.valor}
                  onChange={(e) => setPaymentForm({ ...paymentForm, valor: parseFloat(e.target.value) || 0 })}
                  InputProps={{
                    startAdornment: <InputAdornment position="start">R$</InputAdornment>,
                  }}
                  fullWidth
                  inputProps={{ step: 0.01, min: 0.01 }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  type="number"
                  label="Parcelas"
                  value={paymentForm.parcelas}
                  onChange={(e) => setPaymentForm({ ...paymentForm, parcelas: parseInt(e.target.value) || 1 })}
                  fullWidth
                  inputProps={{ min: 1, max: 12 }}
                />
              </Grid>
              {['CARTAO_CREDITO', 'CARTAO_DEBITO', 'VALE_ALIMENTACAO', 'VALE_REFEICAO', 'VALE_CULTURA'].includes(paymentForm.formaPagamento) && (
                <Grid item xs={12}>
                  <TextField
                    select
                    label="Bandeira do Cartão/Vale"
                    value={tefBandeira}
                    onChange={(e) => setTefBandeira(e.target.value as BandeiraCartao)}
                    fullWidth
                    SelectProps={{ native: true }}
                  >
                    <option value="ALELO_ALIMENTACAO">Alelo Alimentação</option>
                    <option value="ALELO_REFEICAO">Alelo Refeição</option>
                    <option value="ALELO_TUDO">Alelo Tudo (Flexível)</option>
                    <option value="BANRICOMPRAS_BANRICARD">Banricompras & Banricard</option>
                    <option value="TICKET_EDENRED">Ticket (Edenred)</option>
                    <option value="PLUXEE">Pluxee (antiga Sodexo)</option>
                    <option value="VR_BENEFICIOS">VR Benefícios</option>
                    <option value="GREEN_CARD">Green Card</option>
                    <option value="VALECARD_VOLUS">ValeCard / Vólus</option>
                  </TextField>
                </Grid>
              )}
              <Grid item xs={12}>
                <Divider sx={{ my: 1 }} />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={paymentForm.emitirNfce}
                      onChange={(e) => setPaymentForm({ ...paymentForm, emitirNfce: e.target.checked })}
                      color="primary"
                    />
                  }
                  label="Emitir NFC-e (Nota Fiscal de Consumidor Eletrônica)"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="CPF do Consumidor (NFG)"
                  value={paymentForm.cpfConsumidor}
                  onChange={(e) => setPaymentForm({ ...paymentForm, cpfConsumidor: e.target.value })}
                  fullWidth
                  placeholder="000.000.000-00"
                  inputProps={{ maxLength: 14 }}
                  helperText="Obrigatório para NFC-e com CPF na nota. Deixe em branco para consumidor final."
                  disabled={!paymentForm.emitirNfce}
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPaymentDialogOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handlePayment} disabled={paymentForm.valor <= 0}>
            Confirmar Pagamento
          </Button>
          <Button 
            variant="contained" 
            color="secondary"
            startIcon={<ReceiptPrintIcon />}
            onClick={handleFinalizarVenda} 
            disabled={finalizing || getSaldoDevedor(selectedVenda!) <= 0}
          >
            {finalizing ? 'Finalizando...' : 'Finalizar Venda e Emitir NFC-e'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal PIX */}
      <PixPaymentModal
        open={pixDialogOpen}
        onClose={() => setPixDialogOpen(false)}
        vendaId={selectedVenda?.id || ''}
        valor={selectedVenda ? getSaldoDevedor(selectedVenda) : 0}
        onPaymentConfirmed={handlePixConfirmed}
      />

      {/* Modal TEF */}
      <TefPaymentModal
        open={tefDialogOpen}
        onClose={() => setTefDialogOpen(false)}
        onSuccess={handleTefSuccess}
        valor={selectedVenda ? getSaldoDevedor(selectedVenda) : 0}
        formaPagamento={paymentForm.formaPagamento}
        bandeira={tefBandeira}
        parcelas={paymentForm.parcelas}
        cpfConsumidor={paymentForm.cpfConsumidor}
        vendaNumero={selectedVenda?.numero}
      />
    </Box>
  );
}

export default PdvPaymentPage;