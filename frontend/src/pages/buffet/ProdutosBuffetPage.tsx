import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Edit as EditIcon, Refresh as RefreshIcon, Restaurant as RestaurantIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface Produto {
  id: string;
  codigo?: string;
  nome: string;
  categoria?: string;
  precoVenda?: number;
  estoqueAtual?: number;
  unidadeMedida?: string;
  disponivelBalcao?: boolean;
  ativo?: boolean;
  controlaEstoque?: boolean;
}

const emptyForm = { codigo: '', nome: '', categoria: 'PRATO_FEITO', unidadeMedida: 'KG', precoVenda: '', estoqueAtual: '', descricao: '', disponivelBalcao: true };

export function ProdutosBuffetPage() {
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Produto | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiBuffet.listProdutos({ page, size: rowsPerPage });
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setProdutos([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  useEffect(() => { load(); }, [load]);

  const openNew = () => { setEditing(null); setForm(emptyForm); setDialog(true); };
  const openEdit = (p: Produto) => {
    setEditing(p);
    setForm({ codigo: p.codigo ?? '', nome: p.nome, categoria: p.categoria ?? 'PRATO_FEITO', unidadeMedida: p.unidadeMedida ?? 'KG', precoVenda: String(p.precoVenda ?? ''), estoqueAtual: String(p.estoqueAtual ?? ''), descricao: '', disponivelBalcao: p.disponivelBalcao ?? true });
    setDialog(true);
  };

  const salvar = async () => {
    if (!form.nome || !form.precoVenda) { toast.warning('Informe nome e preço'); return; }
    try {
      const payload = { codigo: form.codigo || undefined, nome: form.nome, categoria: form.categoria, unidadeMedida: form.unidadeMedida, precoVenda: Number(form.precoVenda), estoqueAtual: form.estoqueAtual ? Number(form.estoqueAtual) : 0, descricao: form.descricao, disponivelBalcao: form.disponivelBalcao, ativo: true, controlaEstoque: true };
      if (editing) await apiBuffet.updateProduto(editing.id, payload);
      else await apiBuffet.createProduto(payload);
      toast.success('Produto salvo!');
      setDialog(false); load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Excluir produto?')) return;
    try {
      await apiBuffet.deleteProduto(id);
      toast.success('Produto excluído');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Produtos — Buffet"
        subtitle="Pratos por quilo e porção, disponibilidade no balcão e vínculo com fichas técnicas"
        icon={<RestaurantIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>Novo produto</Button>
          </Box>
        }
      />
      <DataTable<Produto>
        data={produtos} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar pratos e produtos..." emptyMessage="Nenhum produto cadastrado"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 110 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          { field: 'categoria', headerName: 'Categoria', width: 150 },
          { field: 'precoVenda', headerName: 'Preço', width: 120, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'estoqueAtual', headerName: 'Estoque', width: 110, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'disponivelBalcao', headerName: 'Balcão', width: 110, align: 'center', render: (v: boolean) => <Chip size="small" label={v ? 'Disponível' : 'Indisponível'} color={v ? 'success' : 'default'} /> },
          { field: 'ativo', headerName: 'Status', width: 100, align: 'center', render: (v: boolean) => <Chip size="small" label={v !== false ? 'Ativo' : 'Inativo'} color={v !== false ? 'success' : 'default'} /> },
          {
            field: 'acoes', headerName: 'Ações', width: 110, align: 'center',
            render: (_: any, row: Produto) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Editar"><IconButton size="small" onClick={() => openEdit(row)}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => excluir(row.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Box>
            ),
          },
        ]}
      />
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar produto' : 'Novo produto do buffet'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={4}><TextField fullWidth size="small" label="Código" value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} /></Grid>
            <Grid item xs={8}><TextField fullWidth size="small" label="Nome do prato/produto" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Categoria" value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })}>
                {['PRATO_FEITO', 'SELF_SERVICE', 'BEBIDA', 'SOBREMESA', 'PORCAO', 'SALADA', 'CHURRASCO', 'OUTROS'].map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Unidade" value={form.unidadeMedida} onChange={(e) => setForm({ ...form, unidadeMedida: e.target.value })}>
                {['KG', 'G', 'UN', 'PORCAO', 'L', 'ML'].map((u) => <MenuItem key={u} value={u}>{u}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Preço de venda" value={form.precoVenda} onChange={(e) => setForm({ ...form, precoVenda: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Estoque inicial" value={form.estoqueAtual} onChange={(e) => setForm({ ...form, estoqueAtual: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Descrição" value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} multiline rows={2} /></Grid>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="Disponível no balcão" value={form.disponivelBalcao ? 'S' : 'N'} onChange={(e) => setForm({ ...form, disponivelBalcao: e.target.value === 'S' })}>
                <MenuItem value="S">Sim — visível no balcão</MenuItem>
                <MenuItem value="N">Não — oculto do balcão</MenuItem>
              </TextField>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default ProdutosBuffetPage;
