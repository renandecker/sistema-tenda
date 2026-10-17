import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Edit as EditIcon, Refresh as RefreshIcon, Warning as WarningIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDate, formatNumber } from '@/utils/formatters';

interface Perda {
  id: string;
  produto?: { id?: string; codigo?: string; nome?: string };
  tipo?: string;
  quantidade?: number;
  custoUnitario?: number;
  valorTotal?: number;
  data?: string;
  motivo?: string;
  responsavel?: string;
  baixaEstoqueRealizada?: boolean;
}

const emptyForm = { produtoId: '', tipo: 'AVARIA', quantidade: '', motivo: '', responsavel: '', data: new Date().toISOString().slice(0, 10), observacoes: '' };

export function PerdasFruteiraPage() {
  const [perdas, setPerdas] = useState<Perda[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [produtos, setProdutos] = useState<{ id: string; nome: string; codigo?: string }[]>([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Perda | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [total, setTotal] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiFruteira.listPerda({ page, size: rowsPerPage });
      setPerdas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setPerdas([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadProdutos = useCallback(async () => {
    try {
      const { data } = await apiFruteira.listProdutos({ size: 200 });
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setProdutos([]);
    }
  }, []);

  useEffect(() => { load(); loadProdutos(); }, [load, loadProdutos]);

  const openNew = () => { setEditing(null); setForm(emptyForm); setDialog(true); };
  const openEdit = (p: Perda) => {
    setEditing(p);
    setForm({ produtoId: p.produto?.id ?? '', tipo: p.tipo ?? 'AVARIA', quantidade: String(p.quantidade ?? ''), motivo: p.motivo ?? '', responsavel: p.responsavel ?? '', data: (p.data ?? '').slice(0, 10), observacoes: '' });
    setDialog(true);
  };

  const salvar = async () => {
    if (!form.produtoId || !form.quantidade) { toast.warning('Informe produto e quantidade'); return; }
    try {
      const payload = { produtoId: form.produtoId, tipo: form.tipo, quantidade: Number(form.quantidade), motivo: form.motivo, responsavel: form.responsavel, data: form.data, observacoes: form.observacoes };
      if (editing) await apiFruteira.updatePerda(editing.id, payload);
      else await apiFruteira.createPerdaRapida(payload);
      toast.success('Perda registrada com baixa de estoque!');
      setDialog(false);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar perda');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Excluir este registro de perda?')) return;
    try {
      await apiFruteira.deletePerda(id);
      toast.success('Registro excluído');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir');
    }
  };

  const apurarTotal = async () => {
    if (!dataInicio || !dataFim) { toast.info('Informe o período'); return; }
    try {
      const { data } = await apiFruteira.getTotalPerdas(dataInicio, dataFim);
      setTotal(typeof data === 'number' ? data : Number(data) || 0);
    } catch {
      toast.error('Erro ao apurar total');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Perdas e Quebras"
        subtitle="Registro rápido de perdas do hortifrúti com baixa automática de estoque"
        icon={<WarningIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>Registrar perda</Button>
          </Box>
        }
      />
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={6} sm={3} md={2}><TextField fullWidth size="small" type="date" label="Início" InputLabelProps={{ shrink: true }} value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} /></Grid>
            <Grid item xs={6} sm={3} md={2}><TextField fullWidth size="small" type="date" label="Fim" InputLabelProps={{ shrink: true }} value={dataFim} onChange={(e) => setDataFim(e.target.value)} /></Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Button variant="outlined" onClick={apurarTotal}>Apurar total do período{total !== null ? `: ${formatCurrency(total)}` : ''}</Button>
            </Grid>
            <Grid item xs={12} md={4}><Typography variant="caption" color="text.secondary">Motivos, responsáveis e baixa automática de estoque por período.</Typography></Grid>
          </Grid>
        </CardContent>
      </Card>
      <DataTable<Perda>
        data={perdas} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por produto, motivo, responsável..." emptyMessage="Nenhuma perda registrada"
        columns={[
          { field: 'data', headerName: 'Data', width: 120, render: (v: string) => formatDate(v) },
          { field: 'produto.nome', headerName: 'Produto', flex: 1, render: (_: any, r: Perda) => `${r.produto?.codigo ? `${r.produto.codigo} — ` : ''}${r.produto?.nome ?? '-'}` },
          { field: 'tipo', headerName: 'Tipo', width: 130, align: 'center', render: (v: string) => <Chip size="small" label={v ?? '-'} variant="outlined" /> },
          { field: 'quantidade', headerName: 'Qtd', width: 100, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'valorTotal', headerName: 'Valor', width: 120, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'motivo', headerName: 'Motivo', width: 180 },
          { field: 'responsavel', headerName: 'Responsável', width: 140 },
          { field: 'baixaEstoqueRealizada', headerName: 'Baixa', width: 110, align: 'center', render: (v: boolean) => <Chip size="small" label={v ? 'Baixado' : 'Pendente'} color={v ? 'success' : 'warning'} /> },
          {
            field: 'acoes', headerName: 'Ações', width: 110, align: 'center',
            render: (_: any, row: Perda) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Editar"><IconButton size="small" onClick={() => openEdit(row)}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => excluir(row.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Box>
            ),
          },
        ]}
      />
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar perda' : 'Registro rápido de perda'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="Produto" value={form.produtoId} onChange={(e) => setForm({ ...form, produtoId: e.target.value })}>
                {produtos.map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo ? `${p.codigo} — ` : ''}{p.nome}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Tipo" value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
                {['DETERIORACAO', 'AVARIA', 'VENCIMENTO', 'FURTO', 'DOACAO', 'OUTROS'].map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Quantidade" value={form.quantidade} onChange={(e) => setForm({ ...form, quantidade: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" type="date" label="Data" InputLabelProps={{ shrink: true }} value={form.data} onChange={(e) => setForm({ ...form, data: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Responsável" value={form.responsavel} onChange={(e) => setForm({ ...form, responsavel: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Motivo" value={form.motivo} onChange={(e) => setForm({ ...form, motivo: e.target.value })} multiline rows={2} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default PerdasFruteiraPage;
