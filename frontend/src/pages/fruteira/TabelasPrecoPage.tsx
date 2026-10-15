import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip } from '@mui/material';
import { Add as AddIcon, Check as CheckIcon, Delete as DeleteIcon, Edit as EditIcon, PriceChange as PriceChangeIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDate } from '@/utils/formatters';

interface Tabela {
  id: string;
  produto?: { id?: string; codigo?: string; nome?: string };
  precoVenda?: number;
  precoAtacado?: number;
  dataInicio?: string;
  dataFim?: string;
  ativa?: boolean;
  aplicadaEm?: string;
  origem?: string;
}

const emptyForm = { produtoId: '', precoVenda: '', precoAtacado: '', quantidadeMinimaAtacado: '', dataInicio: '', dataFim: '', origem: 'MANUAL', observacoes: '' };

export function TabelasPrecoPage() {
  const [tabelas, setTabelas] = useState<Tabela[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [produtos, setProdutos] = useState<{ id: string; nome: string; codigo?: string }[]>([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Tabela | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiFruteira.listTabelas({ page, size: rowsPerPage });
      setTabelas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setTabelas([]);
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

  const openNew = () => { setEditing(null); setForm(emptyForm); setDialog(true); };
  const openEdit = (t: Tabela) => {
    setEditing(t);
    setForm({ produtoId: t.produto?.id ?? '', precoVenda: String(t.precoVenda ?? ''), precoAtacado: String(t.precoAtacado ?? ''), quantidadeMinimaAtacado: '', dataInicio: (t.dataInicio ?? '').slice(0, 10), dataFim: (t.dataFim ?? '').slice(0, 10), origem: t.origem ?? 'MANUAL', observacoes: '' });
    setDialog(true);
  };

  const salvar = async () => {
    if (!form.produtoId || !form.precoVenda || !form.dataInicio) { toast.warning('Informe produto, preço e início da vigência'); return; }
    try {
      const payload = { produtoId: form.produtoId, precoVenda: Number(form.precoVenda), precoAtacado: form.precoAtacado ? Number(form.precoAtacado) : undefined, dataInicio: form.dataInicio, dataFim: form.dataFim || undefined, origem: form.origem, observacoes: form.observacoes, ativa: true };
      if (editing) await apiFruteira.updateTabela(editing.id, payload);
      else await apiFruteira.createTabela(payload);
      toast.success('Tabela de preço salva!');
      setDialog(false); load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Excluir esta vigência de preço?')) return;
    try {
      await apiFruteira.deleteTabela(id);
      toast.success('Excluída');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir');
    }
  };

  const aplicarVigente = async (produtoId?: string) => {
    if (!produtoId) return;
    try {
      await apiFruteira.aplicarVigente(produtoId);
      toast.success('Preço vigente aplicado ao produto!');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao aplicar');
    }
  };

  const aplicarTodas = async () => {
    try {
      await apiFruteira.aplicarTodasVigentes();
      toast.success('Todas as vigências aplicadas em lote!');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao aplicar em lote');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Tabelas de Preço"
        subtitle="Preços com vigência por data, aplicação em lote e histórico"
        icon={<PriceChangeIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="outlined" startIcon={<CheckIcon />} onClick={aplicarTodas}>Aplicar vigentes em lote</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>Nova vigência</Button>
          </Box>
        }
      />
      <DataTable<Tabela>
        data={tabelas} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por produto..." emptyMessage="Nenhuma tabela de preço cadastrada"
        columns={[
          { field: 'produto.nome', headerName: 'Produto', flex: 1, render: (_: any, r: Tabela) => r.produto?.nome ?? '-' },
          { field: 'precoVenda', headerName: 'Preço venda', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'precoAtacado', headerName: 'Atacado', width: 120, align: 'right', render: (v: number) => (v ? formatCurrency(v) : '-') },
          { field: 'dataInicio', headerName: 'Início', width: 120, render: (v: string) => formatDate(v) },
          { field: 'dataFim', headerName: 'Fim', width: 120, render: (v: string) => (v ? formatDate(v) : '—') },
          { field: 'aplicadaEm', headerName: 'Aplicada em', width: 150, render: (v: string) => (v ? formatDate(v) : '-') },
          { field: 'ativa', headerName: 'Status', width: 110, align: 'center', render: (v: boolean) => <Chip size="small" label={v ? 'Ativa' : 'Inativa'} color={v ? 'success' : 'default'} /> },
          {
            field: 'acoes', headerName: 'Ações', width: 150, align: 'center',
            render: (_: any, row: Tabela) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Aplicar vigente"><IconButton size="small" color="success" onClick={() => aplicarVigente(row.produto?.id)}><CheckIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Editar"><IconButton size="small" onClick={() => openEdit(row)}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => excluir(row.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Box>
            ),
          },
        ]}
      />
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar vigência' : 'Nova vigência de preço'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="Produto" value={form.produtoId} onChange={(e) => setForm({ ...form, produtoId: e.target.value })}>
                {produtos.map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo ? `${p.codigo} — ` : ''}{p.nome}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Preço venda" value={form.precoVenda} onChange={(e) => setForm({ ...form, precoVenda: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Preço atacado (opcional)" value={form.precoAtacado} onChange={(e) => setForm({ ...form, precoAtacado: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" type="date" label="Início vigência" InputLabelProps={{ shrink: true }} value={form.dataInicio} onChange={(e) => setForm({ ...form, dataInicio: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" type="date" label="Fim vigência (opcional)" InputLabelProps={{ shrink: true }} value={form.dataFim} onChange={(e) => setForm({ ...form, dataFim: e.target.value })} /></Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Canal/origem" value={form.origem} onChange={(e) => setForm({ ...form, origem: e.target.value })}>
                {['MANUAL', 'BALCAO', 'DELIVERY', 'ATACADO', 'PROMOCAO'].map((o) => <MenuItem key={o} value={o}>{o}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Observações" value={form.observacoes} onChange={(e) => setForm({ ...form, observacoes: e.target.value })} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default TabelasPrecoPage;
