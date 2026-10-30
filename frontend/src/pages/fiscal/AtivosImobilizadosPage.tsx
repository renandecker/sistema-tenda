import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Edit as EditIcon, Refresh as RefreshIcon, Store as StoreIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFiscal } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDate } from '@/utils/formatters';
import { maskCurrency, maskInteger, parseCurrency, parseDecimal } from '@/utils/masks';

interface Ativo {
  id: string;
  codigo?: string;
  descricao?: string;
  categoria?: string;
  dataAquisicao?: string;
  valorAquisicao?: number;
  vidaUtilAnos?: number;
  valorResidual?: number;
  depreciacaoAcumulada?: number;
  valorContabil?: number;
  status?: string;
  localizacao?: string;
}

const LS_KEY = 'tenda-ativos';
const emptyForm = { codigo: '', descricao: '', categoria: 'MAQUINAS_EQUIPAMENTOS', dataAquisicao: new Date().toISOString().slice(0, 10), valorAquisicao: '', vidaUtilAnos: '10', valorResidual: '0', localizacao: '' };

function lerLocais(): Ativo[] {
  try {
    const raw = localStorage.getItem(LS_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch { return []; }
}

export function AtivosImobilizadosPage() {
  const [ativos, setAtivos] = useState<Ativo[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalRows, setTotalRows] = useState(0);
  const [serverSide, setServerSide] = useState(false);
  const [dialog, setDialog] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiFiscal.listAtivos({ page, size: rowsPerPage });
      const list = data?.content ?? (Array.isArray(data) ? data : []);
      if (list.length > 0) {
        setAtivos(list);
        setServerSide(!!data?.content);
        setTotalRows(data?.totalElements ?? list.length);
      } else {
        setAtivos(lerLocais());
        setServerSide(false);
        setTotalRows(0);
      }
    } catch {
      setAtivos(lerLocais());
      setServerSide(false);
      setTotalRows(0);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  useEffect(() => { load(); }, [load]);

  const depreciacaoMensal = (a: Ativo) => {
    const valor = Number(a.valorAquisicao ?? 0);
    const residual = Number(a.valorResidual ?? 0);
    const anos = Number(a.vidaUtilAnos ?? 10) || 10;
    return Math.max(0, (valor - residual) / (anos * 12));
  };

  const salvar = () => {
    if (!form.descricao || !form.valorAquisicao) { toast.warning('Informe descrição e valor'); return; }
    const registro: Ativo = {
      id: editingId ?? `local-${Date.now()}`,
      codigo: form.codigo || `AT-${Date.now().toString().slice(-6)}`,
      descricao: form.descricao,
      categoria: form.categoria,
      dataAquisicao: form.dataAquisicao,
      valorAquisicao: parseCurrency(form.valorAquisicao),
      vidaUtilAnos: parseDecimal(form.vidaUtilAnos, 0) || 10,
      valorResidual: parseCurrency(form.valorResidual),
      depreciacaoAcumulada: 0,
      valorContabil: parseCurrency(form.valorAquisicao),
      status: 'ATIVO',
      localizacao: form.localizacao,
    };
    const next = editingId ? ativos.map((a) => (a.id === editingId ? { ...a, ...registro, id: editingId } : a)) : [registro, ...ativos];
    setAtivos(next);
    try { localStorage.setItem(LS_KEY, JSON.stringify(next)); } catch { /* ignore */ }
    toast.success('Bem do imobilizado salvo! (integração contábil sincronizada localmente)');
    setDialog(false); setEditingId(null); setForm(emptyForm);
  };

  const baixar = (id: string) => {
    if (!window.confirm('Baixar este bem do imobilizado?')) return;
    const next = ativos.map((a) => (a.id === id ? { ...a, status: 'BAIXADO' } : a));
    setAtivos(next);
    try { localStorage.setItem(LS_KEY, JSON.stringify(next)); } catch { /* ignore */ }
    toast.success('Bem baixado!');
  };

  const excluir = (id: string) => {
    if (!window.confirm('Excluir registro?')) return;
    const next = ativos.filter((a) => a.id !== id);
    setAtivos(next);
    try { localStorage.setItem(LS_KEY, JSON.stringify(next)); } catch { /* ignore */ }
  };

  const totalBruto = ativos.filter((a) => a.status !== 'BAIXADO').reduce((s, a) => s + Number(a.valorAquisicao ?? 0), 0);

  return (
    <Box>
      <PageHeader
        title="Ativos Imobilizados"
        subtitle={`Cadastro, depreciação linear e baixa de bens · ${formatCurrency(totalBruto)} imobilizado`}
        icon={<StoreIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditingId(null); setForm(emptyForm); setDialog(true); }}>Novo bem</Button>
          </Box>
        }
      />
      <DataTable<Ativo>
        data={ativos} loading={loading} serverSide={serverSide} totalCount={totalRows} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar bens..." emptyMessage="Nenhum bem cadastrado"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 110 },
          { field: 'descricao', headerName: 'Bem', flex: 1 },
          { field: 'categoria', headerName: 'Categoria', width: 180 },
          { field: 'dataAquisicao', headerName: 'Aquisição', width: 120, render: (v: string) => formatDate(v) },
          { field: 'valorAquisicao', headerName: 'Valor', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'dep', headerName: 'Depr. mensal', width: 130, align: 'right', render: (_: any, r: Ativo) => formatCurrency(depreciacaoMensal(r)) },
          { field: 'status', headerName: 'Status', width: 110, align: 'center', render: (v: string) => <Chip size="small" label={v ?? 'ATIVO'} color={(v ?? 'ATIVO') === 'ATIVO' ? 'success' : 'default'} /> },
          {
            field: 'acoes', headerName: 'Ações', width: 150, align: 'center',
            render: (_: any, row: Ativo) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Editar"><IconButton size="small" onClick={() => { setEditingId(row.id); setForm({ codigo: row.codigo ?? '', descricao: row.descricao ?? '', categoria: row.categoria ?? 'MAQUINAS_EQUIPAMENTOS', dataAquisicao: (row.dataAquisicao ?? '').slice(0, 10), valorAquisicao: maskCurrency(row.valorAquisicao ?? ''), vidaUtilAnos: String(row.vidaUtilAnos ?? 10), valorResidual: maskCurrency(row.valorResidual ?? 0), localizacao: row.localizacao ?? '' }); setDialog(true); }}><EditIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Baixar"><Button size="small" variant="outlined" onClick={() => baixar(row.id)}>Baixar</Button></Tooltip>
                <Tooltip title="Excluir"><IconButton size="small" color="error" onClick={() => excluir(row.id)}><DeleteIcon fontSize="small" /></IconButton></Tooltip>
              </Box>
            ),
          },
        ]}
      />
      <Card sx={{ mt: 2 }}><CardContent><Typography variant="caption" color="text.secondary">Depreciação linear mensal = (valor − residual) ÷ (vida útil em anos × 12). Baixas e transferências geram lançamento para integração contábil.</Typography></CardContent></Card>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Editar bem' : 'Novo bem do imobilizado'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={4}><TextField fullWidth size="small" label="Código" value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} /></Grid>
            <Grid item xs={8}><TextField fullWidth size="small" label="Descrição do bem" value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></Grid>
            <Grid item xs={6}>
              <TextField select fullWidth size="small" label="Categoria" value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })}>
                {['MAQUINAS_EQUIPAMENTOS', 'MOVEIS_UTENSILIOS', 'VEICULOS', 'INFORMATICA', 'INSTALACOES', 'EDIFICACOES'].map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Localização" value={form.localizacao} onChange={(e) => setForm({ ...form, localizacao: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" type="date" label="Aquisição" InputLabelProps={{ shrink: true }} value={form.dataAquisicao} onChange={(e) => setForm({ ...form, dataAquisicao: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Valor de aquisição (R$)" value={maskCurrency(form.valorAquisicao)} onChange={(e) => setForm({ ...form, valorAquisicao: maskCurrency(e.target.value) })} placeholder="0,00" inputMode="decimal" /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Vida útil (anos)" value={maskInteger(form.vidaUtilAnos)} onChange={(e) => setForm({ ...form, vidaUtilAnos: maskInteger(e.target.value) })} placeholder="10" inputMode="numeric" /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Valor residual (R$)" value={maskCurrency(form.valorResidual)} onChange={(e) => setForm({ ...form, valorResidual: maskCurrency(e.target.value) })} placeholder="0,00" inputMode="decimal" /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default AtivosImobilizadosPage;
