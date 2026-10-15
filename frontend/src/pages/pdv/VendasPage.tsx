import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Cancel as CancelIcon, ReceiptLong as ReceiptLongIcon, Refresh as RefreshIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiPdv } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDateTime, getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Venda {
  id: string;
  numero?: string;
  clienteNome?: string;
  status?: string;
  subtotal?: number;
  desconto?: number;
  valorTotal?: number;
  valorPago?: number;
  data?: string;
  dataFinalizacao?: string;
  operador?: string;
  itens?: { nomeProduto?: string; quantidade?: number; valorTotal?: number }[];
  pagamentos?: { formaPagamento?: string; valor?: number }[];
}

export function VendasPage() {
  const [vendas, setVendas] = useState<Venda[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [status, setStatus] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [detalhe, setDetalhe] = useState<Venda | null>(null);
  const [cancelId, setCancelId] = useState<string | null>(null);
  const [motivo, setMotivo] = useState('');
  const [totalPeriodo, setTotalPeriodo] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params: any = { page, size: rowsPerPage };
      if (status) params.status = status;
      if (dataInicio) params.dataInicio = dataInicio;
      if (dataFim) params.dataFim = dataFim;
      const { data } = await apiPdv.listVendas(params);
      setVendas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setVendas([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage, status, dataInicio, dataFim]);

  useEffect(() => { load(); }, [load]);

  const carregarTotais = async () => {
    if (!dataInicio || !dataFim) { toast.info('Informe o período para apurar o total'); return; }
    try {
      const { data } = await apiPdv.getTotalVendas(dataInicio, dataFim);
      setTotalPeriodo(typeof data === 'number' ? data : Number(data) || 0);
    } catch {
      toast.error('Erro ao apurar totais');
    }
  };

  const verDetalhe = async (v: Venda) => {
    try {
      const { data } = await apiPdv.getVenda(v.id);
      setDetalhe(data);
    } catch {
      setDetalhe(v);
    }
  };

  const confirmarCancelamento = async () => {
    if (!cancelId || !motivo.trim()) { toast.warning('Informe o motivo'); return; }
    try {
      await apiPdv.cancelarVenda(cancelId, motivo);
      toast.success('Venda cancelada');
      setCancelId(null); setMotivo('');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao cancelar');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Vendas"
        subtitle="Consulta e gestão de vendas realizadas no PDV"
        icon={<ReceiptLongIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="outlined" onClick={carregarTotais}>Total do período{totalPeriodo !== null ? `: ${formatCurrency(totalPeriodo)}` : ''}</Button>
          </Box>
        }
      />
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4} md={2}>
              <TextField select fullWidth size="small" label="Status" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <MenuItem value="">Todos</MenuItem>
                {['ABERTA', 'FINALIZADA', 'PAGA', 'CANCELADA'].map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6} sm={4} md={2}>
              <TextField fullWidth size="small" type="date" label="Início" InputLabelProps={{ shrink: true }} value={dataInicio} onChange={(e) => { setDataInicio(e.target.value); setPage(0); }} />
            </Grid>
            <Grid item xs={6} sm={4} md={2}>
              <TextField fullWidth size="small" type="date" label="Fim" InputLabelProps={{ shrink: true }} value={dataFim} onChange={(e) => { setDataFim(e.target.value); setPage(0); }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="caption" color="text.secondary">Filtre por período, PDV e status. O cancelamento exige motivo e gera estorno. Use o detalhe para reimprimir o comprovante não fiscal.</Typography>
            </Grid>
          </Grid>
        </CardContent>
      </Card>
      <DataTable<Venda>
        data={vendas} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por número, cliente, operador..." emptyMessage="Nenhuma venda encontrada"
        columns={[
          { field: 'numero', headerName: 'Número', width: 110 },
          { field: 'clienteNome', headerName: 'Cliente', flex: 1 },
          { field: 'operador', headerName: 'Operador', width: 140 },
          { field: 'valorTotal', headerName: 'Total', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'valorPago', headerName: 'Pago', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'status', headerName: 'Status', width: 130, align: 'center', render: (v: string) => <Chip size="small" label={getStatusLabel(v)} color={getStatusColor(v)} /> },
          { field: 'data', headerName: 'Data', width: 170, render: (v: string) => formatDateTime(v) },
          {
            field: 'acoes', headerName: 'Ações', width: 120, align: 'center',
            render: (_: any, row: Venda) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Detalhes / comprovante"><IconButton size="small" onClick={() => verDetalhe(row)}><ViewIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Cancelar"><span><IconButton size="small" color="error" disabled={row.status === 'CANCELADA'} onClick={() => setCancelId(row.id)}><CancelIcon fontSize="small" /></IconButton></span></Tooltip>
              </Box>
            ),
          },
        ]}
      />

      <Dialog open={!!detalhe} onClose={() => setDetalhe(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Venda {detalhe?.numero} — comprovante não fiscal</DialogTitle>
        <DialogContent>
          <Typography variant="body2">Cliente: {detalhe?.clienteNome ?? 'Consumidor final'}</Typography>
          <Typography variant="body2">Data: {formatDateTime(detalhe?.data)}</Typography>
          <Typography variant="body2">Status: {getStatusLabel(detalhe?.status ?? '')}</Typography>
          <Box sx={{ mt: 2 }}>
            {(detalhe?.itens ?? []).map((it, i) => (
              <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">{it.quantidade}x {it.nomeProduto}</Typography>
                <Typography variant="body2">{formatCurrency(it.valorTotal ?? 0)}</Typography>
              </Box>
            ))}
          </Box>
          <Box sx={{ mt: 1 }}>
            {(detalhe?.pagamentos ?? []).map((p, i) => (
              <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="caption">{p.formaPagamento}</Typography>
                <Typography variant="caption">{formatCurrency(p.valor ?? 0)}</Typography>
              </Box>
            ))}
          </Box>
          <Typography variant="h6" sx={{ mt: 2 }}>Total: {formatCurrency(detalhe?.valorTotal ?? 0)}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => window.print()}>Reimprimir</Button>
          <Button variant="contained" onClick={() => setDetalhe(null)}>Fechar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!cancelId} onClose={() => setCancelId(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Cancelar venda</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Motivo do cancelamento/estorno" value={motivo} onChange={(e) => setMotivo(e.target.value)} sx={{ mt: 1 }} multiline rows={3} />
        </DialogContent>
        <DialogActions><Button onClick={() => setCancelId(null)}>Voltar</Button><Button color="error" variant="contained" onClick={confirmarCancelamento}>Confirmar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default VendasPage;
