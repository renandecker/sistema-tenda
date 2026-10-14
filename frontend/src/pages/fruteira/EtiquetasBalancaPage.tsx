import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Print as PrintIcon, Refresh as RefreshIcon, Edit as EditIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDate } from '@/utils/formatters';

interface Etiqueta {
  id: string;
  codigo?: string;
  produto?: { id?: string; codigo?: string; nome?: string };
  preco?: number;
  validadeDias?: number;
  ativa?: boolean;
  ultimaImpressao?: string;
  quantidadeImpressa?: number;
  modeloEtiqueta?: string;
}

const emptyForm = { produtoId: '', preco: '', validadeDias: '3', modeloEtiqueta: 'PADRAO', textoLivre1: '', textoLivre2: '' };

export function EtiquetasBalancaPage() {
  const [etiquetas, setEtiquetas] = useState<Etiqueta[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [produtos, setProdutos] = useState<{ id: string; nome: string; codigo?: string; precoVenda?: number }[]>([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Etiqueta | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [balanca, setBalanca] = useState<any>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiFruteira.listEtiquetas({ page, size: rowsPerPage });
      setEtiquetas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setEtiquetas([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadAux = useCallback(async () => {
    try {
      const { data } = await apiFruteira.listProdutos({ size: 200 });
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
    } catch { setProdutos([]); }
    try {
      const { data } = await apiFruteira.getBalancaStatus();
      setBalanca(data);
    } catch { setBalanca(null); }
  }, []);

  useEffect(() => { load(); loadAux(); }, [load, loadAux]);

  const openNew = () => { setEditing(null); setForm(emptyForm); setDialog(true); };
  const openEdit = (e: Etiqueta) => {
    setEditing(e);
    setForm({ produtoId: e.produto?.id ?? '', preco: String(e.preco ?? ''), validadeDias: String(e.validadeDias ?? 3), modeloEtiqueta: e.modeloEtiqueta ?? 'PADRAO', textoLivre1: '', textoLivre2: '' });
    setDialog(true);
  };

  const salvar = async () => {
    if (!form.produtoId) { toast.warning('Selecione o produto'); return; }
    try {
      const payload = { produtoId: form.produtoId, preco: Number(form.preco) || 0, validadeDias: Number(form.validadeDias) || 3, modeloEtiqueta: form.modeloEtiqueta, textoLivre1: form.textoLivre1, textoLivre2: form.textoLivre2, imprimirCodigoBarras: true, ativa: true };
      if (editing) await apiFruteira.updateEtiqueta(editing.id, payload);
      else await apiFruteira.createEtiqueta(payload);
      toast.success('Etiqueta salva!');
      setDialog(false); load();
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Erro ao salvar');
    }
  };

  const gerarParaProduto = async (produtoId: string) => {
    try {
      await apiFruteira.gerarParaProduto(produtoId);
      toast.success('Etiqueta EAN-13 gerada!');
      load();
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Erro ao gerar etiqueta');
    }
  };

  const imprimir = async (codigo?: string) => {
    if (!codigo) return;
    try {
      await apiFruteira.imprimir(codigo);
      toast.success(`Etiqueta ${codigo} enviada para impressão!`);
      load();
    } catch {
      toast.error('Erro ao imprimir');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Excluir etiqueta?')) return;
    try {
      await apiFruteira.deleteEtiqueta(id);
      toast.success('Etiqueta excluída');
      load();
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Erro ao excluir');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Etiquetas de Balança"
        subtitle={`Geração EAN-13 e impressão em lote ${balanca ? `· Balança: ${balanca.conectada ? 'conectada' : 'desconectada'}` : ''}`}
        icon={<PrintIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>Nova etiqueta</Button>
          </Box>
        }
      />
      <DataTable<Etiqueta>
        data={etiquetas} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por código ou produto..." emptyMessage="Nenhuma etiqueta cadastrada"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 140 },
          { field: 'produto.nome', headerName: 'Produto', flex: 1, render: (_: any, r: Etiqueta) => r.produto?.nome ?? '-' },
          { field: 'preco', headerName: 'Preço', width: 120, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'validadeDias', headerName: 'Validade (dias)', width: 130, align: 'center' },
          { field: 'quantidadeImpressa', headerName: 'Impressas', width: 110, align: 'center' },
          { field: 'ultimaImpressao', headerName: 'Últ. impressão', width: 150, render: (v: string) => v ? formatDate(v) : '-' },
          { field: 'ativa', headerName: 'Status', width: 110, align: 'center', render: (v: boolean) => <Chip size="small" label={v ? 'Ativa' : 'Inativa'} color={v ? 'success' : 'default'} /> },
          {
            field: 'acoes', headerName: 'Ações', width: 150, align: 'center',
            render: (_: any, row: Etiqueta) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Imprimir"><IconButton size="small" color="primary" onClick={() => imprimir(row.codigo)}><PrintIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Editar"><IconButton size="small" onClick={() => openEdit(row)}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => excluir(row.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Box>
            ),
          },
        ]}
      />
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
        Gere etiquetas EAN-13 por produto, imprima em lote e use layouts personalizados. Produtos sem etiqueta: selecione abaixo para gerar.
      </Typography>
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 1 }}>
        {produtos.slice(0, 8).map((p) => (
          <Chip key={p.id} label={`Gerar: ${p.nome}`} variant="outlined" onClick={() => gerarParaProduto(p.id)} clickable />
        ))}
      </Box>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar etiqueta' : 'Nova etiqueta'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="Produto" value={form.produtoId} onChange={(e) => {
                const prod = produtos.find((p) => p.id === e.target.value);
                setForm({ ...form, produtoId: e.target.value, preco: prod?.precoVenda ? String(prod.precoVenda) : form.preco });
              }}>
                {produtos.map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo ? `${p.codigo} — ` : ''}{p.nome}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Preço" value={form.preco} onChange={(e) => setForm({ ...form, preco: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Validade (dias)" value={form.validadeDias} onChange={(e) => setForm({ ...form, validadeDias: e.target.value })} /></Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Modelo" value={form.modeloEtiqueta} onChange={(e) => setForm({ ...form, modeloEtiqueta: e.target.value })}>
                {['PADRAO', 'GONDOLA', 'PROMOCAO', 'BALANCA_TOLEDO', 'BALANCA_FILIZOLA'].map((m) => <MenuItem key={m} value={m}>{m}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Texto livre 1" value={form.textoLivre1} onChange={(e) => setForm({ ...form, textoLivre1: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Texto livre 2" value={form.textoLivre2} onChange={(e) => setForm({ ...form, textoLivre2: e.target.value })} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default EtiquetasBalancaPage;
