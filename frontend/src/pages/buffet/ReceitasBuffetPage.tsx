import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Edit as EditIcon, Kitchen as KitchenIcon, Refresh as RefreshIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface Receita {
  id: string;
  produto?: { nome?: string; codigo?: string };
  rendimento?: number;
  custoTotal?: number;
  precoSugerido?: number;
  margemDesejada?: number;
  ativa?: boolean;
  ingredientes?: { produto?: { nome?: string }; quantidade?: number; custoTotal?: number }[];
  instrucoes?: string;
}

const emptyForm = { produtoId: '', rendimento: '', custoTotal: '', margemDesejada: '30', precoSugerido: '', instrucoes: '' };

export function ReceitasBuffetPage() {
  const [receitas, setReceitas] = useState<Receita[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [produtos, setProdutos] = useState<{ id: string; nome: string }[]>([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Receita | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [detalhe, setDetalhe] = useState<Receita | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiBuffet.listReceitas({ page, size: rowsPerPage });
      setReceitas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setReceitas([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadProdutos = useCallback(async () => {
    try {
      const { data } = await apiBuffet.listProdutos({ size: 200 });
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
    } catch { setProdutos([]); }
  }, []);

  useEffect(() => { load(); loadProdutos(); }, [load, loadProdutos]);

  const openNew = () => { setEditing(null); setForm(emptyForm); setDialog(true); };
  const openEdit = (r: Receita) => {
    setEditing(r);
    setForm({ produtoId: '', rendimento: String(r.rendimento ?? ''), custoTotal: String(r.custoTotal ?? ''), margemDesejada: String(r.margemDesejada ?? 30), precoSugerido: String(r.precoSugerido ?? ''), instrucoes: r.instrucoes ?? '' });
    setDialog(true);
  };

  const salvar = async () => {
    if (!editing && !form.produtoId) { toast.warning('Selecione o produto do prato'); return; }
    try {
      const payload = { produtoId: form.produtoId || undefined, rendimento: Number(form.rendimento) || 1, custoTotal: Number(form.custoTotal) || 0, margemDesejada: Number(form.margemDesejada) || 0, precoSugerido: Number(form.precoSugerido) || 0, instrucoes: form.instrucoes, ativa: true, ingredientes: [] };
      if (editing) await apiBuffet.updateReceita(editing.id, payload);
      else await apiBuffet.createReceita({ produto: { id: form.produtoId }, ...payload });
      toast.success('Ficha técnica salva!');
      setDialog(false); load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Excluir ficha técnica?')) return;
    try {
      await apiBuffet.deleteReceita(id);
      toast.success('Excluída');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir');
    }
  };

  const verDetalhe = async (r: Receita) => {
    try {
      const { data } = await apiBuffet.getReceita(r.id);
      setDetalhe(data);
    } catch {
      setDetalhe(r);
    }
  };

  return (
    <Box>
      <PageHeader
        title="Receitas e Fichas Técnicas"
        subtitle="Composição de pratos, custo de produção e CMV"
        icon={<KitchenIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>Nova ficha</Button>
          </Box>
        }
      />
      <DataTable<Receita>
        data={receitas} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por prato..." emptyMessage="Nenhuma ficha técnica cadastrada"
        columns={[
          { field: 'produto.nome', headerName: 'Prato', flex: 1, render: (_: any, r: Receita) => r.produto?.nome ?? '-' },
          { field: 'rendimento', headerName: 'Rendimento', width: 120, align: 'right', render: (v: number) => formatNumber(v, 2) },
          { field: 'custoTotal', headerName: 'Custo total', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'margemDesejada', headerName: 'Margem %', width: 110, align: 'right', render: (v: number) => `${formatNumber(v, 1)}%` },
          { field: 'precoSugerido', headerName: 'Preço sugerido', width: 140, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'ativa', headerName: 'Status', width: 100, align: 'center', render: (v: boolean) => <Chip size="small" label={v !== false ? 'Ativa' : 'Inativa'} color={v !== false ? 'success' : 'default'} /> },
          {
            field: 'acoes', headerName: 'Ações', width: 140, align: 'center',
            render: (_: any, row: Receita) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Ver ficha / ingredientes"><IconButton size="small" onClick={() => verDetalhe(row)}><ViewIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Editar"><IconButton size="small" onClick={() => openEdit(row)}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => excluir(row.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Box>
            ),
          },
        ]}
      />
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar ficha técnica' : 'Nova ficha técnica'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            {!editing && (
              <Grid item xs={12}>
                <TextField select fullWidth size="small" label="Produto do prato" value={form.produtoId} onChange={(e) => setForm({ ...form, produtoId: e.target.value })}>
                  {produtos.map((p) => <MenuItem key={p.id} value={p.id}>{p.nome}</MenuItem>)}
                </TextField>
              </Grid>
            )}
            <Grid item xs={6}><TextField fullWidth size="small" label="Rendimento (porções/kg)" value={form.rendimento} onChange={(e) => setForm({ ...form, rendimento: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Custo total" value={form.custoTotal} onChange={(e) => setForm({ ...form, custoTotal: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Margem desejada %" value={form.margemDesejada} onChange={(e) => setForm({ ...form, margemDesejada: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Preço sugerido" value={form.precoSugerido} onChange={(e) => setForm({ ...form, precoSugerido: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Modo de preparo / instruções" value={form.instrucoes} onChange={(e) => setForm({ ...form, instrucoes: e.target.value })} multiline rows={3} /></Grid>
          </Grid>
          <Typography variant="caption" color="text.secondary">Ingredientes detalhados podem ser lançados na edição da receita (ingredientes, quantidades e baixa de insumos por produção).</Typography>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>

      <Dialog open={!!detalhe} onClose={() => setDetalhe(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Ficha — {detalhe?.produto?.nome}</DialogTitle>
        <DialogContent>
          <Typography variant="body2">Rendimento: {formatNumber(detalhe?.rendimento ?? 0, 2)} · Custo: {formatCurrency(detalhe?.custoTotal ?? 0)} · Preço sugerido: {formatCurrency(detalhe?.precoSugerido ?? 0)}</Typography>
          <Typography variant="body2" sx={{ mt: 1, whiteSpace: 'pre-wrap' }}>{detalhe?.instrucoes || 'Sem instruções cadastradas.'}</Typography>
          <Box sx={{ mt: 2 }}>
            <Typography variant="caption" fontWeight={700}>INGREDIENTES</Typography>
            {(detalhe?.ingredientes ?? []).map((ing, i) => (
              <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">{ing.produto?.nome} — {formatNumber(ing.quantidade ?? 0, 3)}</Typography>
                <Typography variant="body2">{formatCurrency(ing.custoTotal ?? 0)}</Typography>
              </Box>
            ))}
            {(detalhe?.ingredientes ?? []).length === 0 && <Typography variant="caption" color="text.secondary">Nenhum ingrediente detalhado.</Typography>}
          </Box>
        </DialogContent>
        <DialogActions><Button variant="contained" onClick={() => setDetalhe(null)}>Fechar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default ReceitasBuffetPage;
