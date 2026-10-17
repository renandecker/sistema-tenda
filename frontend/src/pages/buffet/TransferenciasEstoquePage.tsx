import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Refresh as RefreshIcon, SwapHoriz as SwapHorizIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet, apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatDateTime, formatNumber, getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Transferencia {
  id: string;
  produto?: { nome?: string; codigo?: string };
  quantidade?: number;
  origem?: string;
  destino?: string;
  status?: string;
  data?: string;
  solicitadoPor?: string;
  observacoes?: string;
}

export function TransferenciasEstoquePage() {
  const [items, setItems] = useState<Transferencia[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [produtos, setProdutos] = useState<{ id: string; nome: string }[]>([]);
  const [dialog, setDialog] = useState(false);
  const [form, setForm] = useState({ produtoId: '', quantidade: '', origem: 'FRUTEIRA', destino: 'BUFFET', solicitadoPor: '', observacoes: '' });
  const [motivo, setMotivo] = useState('');
  const [alvo, setAlvo] = useState<Transferencia | null>(null);
  const [acao, setAcao] = useState<'aprovar' | 'rejeitar' | 'concluir' | 'cancelar' | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiBuffet.listTransferencias({ page, size: rowsPerPage });
      setItems(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadProdutos = useCallback(async () => {
    try {
      const { data } = await apiFruteira.listProdutos({ size: 200 });
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
    } catch { setProdutos([]); }
  }, []);

  useEffect(() => { load(); loadProdutos(); }, [load, loadProdutos]);

  const solicitar = async () => {
    if (!form.produtoId || !form.quantidade || !form.solicitadoPor) { toast.warning('Informe produto, quantidade e solicitante'); return; }
    try {
      await apiBuffet.createTransferencia({ produtoId: form.produtoId, quantidade: Number(form.quantidade), origem: form.origem, destino: form.destino, solicitadoPor: form.solicitadoPor, observacoes: form.observacoes });
      toast.success('Transferência solicitada!');
      setDialog(false);
      setForm({ produtoId: '', quantidade: '', origem: 'FRUTEIRA', destino: 'BUFFET', solicitadoPor: '', observacoes: '' });
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao solicitar');
    }
  };

  const executarAcao = async () => {
    if (!alvo || !acao) return;
    try {
      const operador = motivo || 'operador';
      if (acao === 'aprovar') await apiBuffet.aprovar(alvo.id, operador);
      if (acao === 'rejeitar') {
        if (!motivo) { toast.warning('Informe o motivo da rejeição'); return; }
        await apiBuffet.rejeitar(alvo.id, motivo, operador);
      }
      if (acao === 'concluir') await apiBuffet.concluir(alvo.id, operador);
      if (acao === 'cancelar') {
        if (!motivo) { toast.warning('Informe o motivo do cancelamento'); return; }
        await apiBuffet.cancelar(alvo.id, motivo);
      }
      toast.success('Operação concluída!');
      setAlvo(null); setAcao(null); setMotivo('');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro na operação');
    }
  };

  const pendentes = items.filter((i) => i.status === 'PENDENTE').length;

  return (
    <Box>
      <PageHeader
        title="Transferências de Estoque"
        subtitle={`Solicitação e aprovação entre fruteira, buffet e estoque · ${pendentes} pendentes`}
        icon={<SwapHorizIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialog(true)}>Solicitar transferência</Button>
          </Box>
        }
      />
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="caption" color="text.secondary">Fluxo: solicitação → aprovação → conferência de recebimento (concluir). Rejeição e cancelamento exigem motivo para rastreabilidade.</Typography>
        </CardContent>
      </Card>
      <DataTable<Transferencia>
        data={items} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por produto ou solicitante..." emptyMessage="Nenhuma transferência encontrada"
        columns={[
          { field: 'data', headerName: 'Data', width: 160, render: (v: string) => formatDateTime(v) },
          { field: 'produto.nome', headerName: 'Produto', flex: 1, render: (_: any, r: Transferencia) => r.produto?.nome ?? '-' },
          { field: 'quantidade', headerName: 'Qtd', width: 100, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'origem', headerName: 'Origem', width: 120, align: 'center' },
          { field: 'destino', headerName: 'Destino', width: 120, align: 'center' },
          { field: 'status', headerName: 'Status', width: 140, align: 'center', render: (v: string) => <Chip size="small" label={getStatusLabel(v ?? '')} color={getStatusColor(v ?? '')} /> },
          { field: 'solicitadoPor', headerName: 'Solicitante', width: 140 },
          {
            field: 'acoes', headerName: 'Ações', width: 230, align: 'center',
            render: (_: any, row: Transferencia) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center', flexWrap: 'wrap' }}>
                <Button size="small" variant="outlined" color="success" disabled={row.status !== 'PENDENTE'} onClick={() => { setAlvo(row); setAcao('aprovar'); }}>Aprovar</Button>
                <Button size="small" variant="outlined" color="info" disabled={row.status !== 'APROVADA'} onClick={() => { setAlvo(row); setAcao('concluir'); }}>Concluir</Button>
                <Button size="small" variant="outlined" color="error" disabled={row.status === 'CONCLUIDA' || row.status === 'CANCELADA'} onClick={() => { setAlvo(row); setAcao('rejeitar'); }}>Rejeitar</Button>
              </Box>
            ),
          },
        ]}
      />

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Solicitar transferência</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="Produto" value={form.produtoId} onChange={(e) => setForm({ ...form, produtoId: e.target.value })}>
                {produtos.map((p) => <MenuItem key={p.id} value={p.id}>{p.nome}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Quantidade" value={form.quantidade} onChange={(e) => setForm({ ...form, quantidade: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Solicitado por" value={form.solicitadoPor} onChange={(e) => setForm({ ...form, solicitadoPor: e.target.value })} /></Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Origem" value={form.origem} onChange={(e) => setForm({ ...form, origem: e.target.value })}>
                {['FRUTEIRA', 'BUFFET', 'FORNECEDOR', 'TRANSFERENCIA'].map((o) => <MenuItem key={o} value={o}>{o}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Destino" value={form.destino} onChange={(e) => setForm({ ...form, destino: e.target.value })}>
                {['FRUTEIRA', 'BUFFET', 'TRANSFERENCIA'].map((o) => <MenuItem key={o} value={o}>{o}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Observações" value={form.observacoes} onChange={(e) => setForm({ ...form, observacoes: e.target.value })} multiline rows={2} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={solicitar}>Solicitar</Button></DialogActions>
      </Dialog>

      <Dialog open={!!alvo} onClose={() => { setAlvo(null); setAcao(null); }} maxWidth="xs" fullWidth>
        <DialogTitle>{acao === 'aprovar' ? 'Aprovar' : acao === 'concluir' ? 'Confirmar recebimento' : acao === 'rejeitar' ? 'Rejeitar' : 'Cancelar'} — {alvo?.produto?.nome}</DialogTitle>
        <DialogContent>
          <TextField fullWidth size="small" label={acao === 'aprovar' || acao === 'concluir' ? 'Responsável (aprovado/recebido por)' : 'Motivo'} value={motivo} onChange={(e) => setMotivo(e.target.value)} sx={{ mt: 1 }} multiline rows={2} />
        </DialogContent>
        <DialogActions><Button onClick={() => { setAlvo(null); setAcao(null); }}>Voltar</Button><Button variant="contained" onClick={executarAcao}>Confirmar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default TransferenciasEstoquePage;
