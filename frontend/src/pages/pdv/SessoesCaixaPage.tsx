import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, AttachMoney as AttachMoneyIcon, Refresh as RefreshIcon, Remove as RemoveIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiPdv } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDateTime, getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Sessao {
  id: string;
  pdv?: { id?: string; codigo?: string; nome?: string };
  operadorAbertura?: string;
  valorInicial?: number;
  totalVendas?: number;
  diferenca?: number;
  status?: string;
  dataAbertura?: string;
  dataFechamento?: string;
}

export function SessoesCaixaPage() {
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [pdvs, setPdvs] = useState<{ id: string; codigo: string; nome?: string }[]>([]);
  const [abrirOpen, setAbrirOpen] = useState(false);
  const [formAbrir, setFormAbrir] = useState({ pdvId: '', operadorAbertura: '', valorInicial: '', observacoesAbertura: '' });
  const [fecharId, setFecharId] = useState<string | null>(null);
  const [formFechar, setFormFechar] = useState({ operadorFechamento: '', valorFinalContado: '', observacoesFechamento: '' });
  const [movId, setMovId] = useState<string | null>(null);
  const [movTipo, setMovTipo] = useState<'sangria' | 'reforco'>('sangria');
  const [movForm, setMovForm] = useState({ valor: '', motivo: '', operador: '' });
  const [detalhe, setDetalhe] = useState<Sessao | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiPdv.listSessoes({ page, size: rowsPerPage });
      setSessoes(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setSessoes([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadPdvs = useCallback(async () => {
    try {
      const { data } = await apiPdv.listPdvs({ size: 100 });
      setPdvs(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setPdvs([]);
    }
  }, []);

  useEffect(() => { load(); loadPdvs(); }, [load, loadPdvs]);

  const confirmarAbertura = async () => {
    if (!formAbrir.pdvId || !formAbrir.operadorAbertura) { toast.warning('Informe PDV e operador'); return; }
    try {
      await apiPdv.abrirCaixa({ pdvId: formAbrir.pdvId, operadorAbertura: formAbrir.operadorAbertura, valorInicial: Number(formAbrir.valorInicial) || 0, observacoesAbertura: formAbrir.observacoesAbertura });
      toast.success('Caixa aberto!');
      setAbrirOpen(false);
      setFormAbrir({ pdvId: '', operadorAbertura: '', valorInicial: '', observacoesAbertura: '' });
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao abrir caixa');
    }
  };

  const confirmarFechamento = async () => {
    if (!fecharId) return;
    try {
      await apiPdv.fecharCaixa(fecharId, { operadorFechamento: formFechar.operadorFechamento, valorFinalContado: Number(formFechar.valorFinalContado) || 0, observacoesFechamento: formFechar.observacoesFechamento });
      toast.success('Caixa fechado!');
      setFecharId(null);
      setFormFechar({ operadorFechamento: '', valorFinalContado: '', observacoesFechamento: '' });
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao fechar caixa');
    }
  };

  const confirmarMov = async () => {
    if (!movId || !movForm.valor || !movForm.motivo) { toast.warning('Informe valor e motivo'); return; }
    try {
      const payload = { valor: Number(movForm.valor), motivo: movForm.motivo, operador: movForm.operador };
      if (movTipo === 'sangria') await apiPdv.sangria(movId, payload);
      else await apiPdv.reforco(movId, payload);
      toast.success(`${movTipo === 'sangria' ? 'Sangria' : 'Reforço'} registrada!`);
      setMovId(null); setMovForm({ valor: '', motivo: '', operador: '' });
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao registrar movimentação');
    }
  };

  const verDetalhe = async (s: Sessao) => {
    try {
      const { data } = await apiPdv.getSessao(s.id);
      setDetalhe(data);
    } catch {
      setDetalhe(s);
    }
  };

  return (
    <Box>
      <PageHeader
        title="Sessões de Caixa"
        subtitle="Abertura, fechamento, sangria e reforço por operador"
        icon={<AttachMoneyIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAbrirOpen(true)}>Abrir caixa</Button>
          </Box>
        }
      />
      <DataTable<Sessao>
        data={sessoes} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar por PDV ou operador..." emptyMessage="Nenhuma sessão encontrada"
        columns={[
          { field: 'pdv.codigo', headerName: 'PDV', width: 110, render: (_: any, r: Sessao) => r.pdv?.codigo ?? '-' },
          { field: 'operadorAbertura', headerName: 'Operador', width: 150 },
          { field: 'valorInicial', headerName: 'Fundo inicial', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'totalVendas', headerName: 'Vendas', width: 130, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'diferenca', headerName: 'Diferença', width: 120, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'status', headerName: 'Status', width: 120, align: 'center', render: (v: string) => <Chip size="small" label={getStatusLabel(v ?? '')} color={getStatusColor(v ?? '')} /> },
          { field: 'dataAbertura', headerName: 'Abertura', width: 170, render: (v: string) => formatDateTime(v) },
          {
            field: 'acoes', headerName: 'Ações', width: 190, align: 'center',
            render: (_: any, row: Sessao) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Detalhes / conferência"><IconButton size="small" onClick={() => verDetalhe(row)}><ViewIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Sangria"><span><IconButton size="small" color="warning" disabled={row.status !== 'ABERTA'} onClick={() => { setMovId(row.id); setMovTipo('sangria'); }}><RemoveIcon fontSize="small" /></IconButton></span></Tooltip>
                <Tooltip title="Reforço"><span><IconButton size="small" color="success" disabled={row.status !== 'ABERTA'} onClick={() => { setMovId(row.id); setMovTipo('reforco'); }}><AddIcon fontSize="small" /></IconButton></span></Tooltip>
                <Tooltip title="Fechar caixa"><span><Button size="small" variant="outlined" disabled={row.status !== 'ABERTA'} onClick={() => setFecharId(row.id)}>Fechar</Button></span></Tooltip>
              </Box>
            ),
          },
        ]}
      />

      <Dialog open={abrirOpen} onClose={() => setAbrirOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Abrir caixa</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="PDV" value={formAbrir.pdvId} onChange={(e) => setFormAbrir({ ...formAbrir, pdvId: e.target.value })}>
                {pdvs.map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo} — {p.nome ?? ''}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Operador de abertura" value={formAbrir.operadorAbertura} onChange={(e) => setFormAbrir({ ...formAbrir, operadorAbertura: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Valor inicial (fundo de troco)" value={formAbrir.valorInicial} onChange={(e) => setFormAbrir({ ...formAbrir, valorInicial: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Observações" value={formAbrir.observacoesAbertura} onChange={(e) => setFormAbrir({ ...formAbrir, observacoesAbertura: e.target.value })} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setAbrirOpen(false)}>Cancelar</Button><Button variant="contained" onClick={confirmarAbertura}>Abrir</Button></DialogActions>
      </Dialog>

      <Dialog open={!!fecharId} onClose={() => setFecharId(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Fechar caixa — conferência</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}><TextField fullWidth size="small" label="Operador de fechamento" value={formFechar.operadorFechamento} onChange={(e) => setFormFechar({ ...formFechar, operadorFechamento: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Valor contado no gavetão" value={formFechar.valorFinalContado} onChange={(e) => setFormFechar({ ...formFechar, valorFinalContado: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Observações / divergências" value={formFechar.observacoesFechamento} onChange={(e) => setFormFechar({ ...formFechar, observacoesFechamento: e.target.value })} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setFecharId(null)}>Voltar</Button><Button variant="contained" onClick={confirmarFechamento}>Fechar caixa</Button></DialogActions>
      </Dialog>

      <Dialog open={!!movId} onClose={() => setMovId(null)} maxWidth="xs" fullWidth>
        <DialogTitle>{movTipo === 'sangria' ? 'Sangria' : 'Reforço'} de caixa</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}><TextField fullWidth size="small" label="Valor" value={movForm.valor} onChange={(e) => setMovForm({ ...movForm, valor: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Operador" value={movForm.operador} onChange={(e) => setMovForm({ ...movForm, operador: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Justificativa" value={movForm.motivo} onChange={(e) => setMovForm({ ...movForm, motivo: e.target.value })} multiline rows={2} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setMovId(null)}>Voltar</Button><Button variant="contained" onClick={confirmarMov}>Confirmar</Button></DialogActions>
      </Dialog>

      <Dialog open={!!detalhe} onClose={() => setDetalhe(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Conferência — {detalhe?.pdv?.codigo}</DialogTitle>
        <DialogContent>
          <Typography variant="body2">Abertura: {formatDateTime(detalhe?.dataAbertura)} por {detalhe?.operadorAbertura}</Typography>
          <Typography variant="body2">Fechamento: {detalhe?.dataFechamento ? formatDateTime(detalhe.dataFechamento) : 'em aberto'}</Typography>
          <Typography variant="body2">Fundo inicial: {formatCurrency(detalhe?.valorInicial ?? 0)}</Typography>
          <Typography variant="body2">Total vendas: {formatCurrency(detalhe?.totalVendas ?? 0)}</Typography>
          <Typography variant="body2">Diferença: {formatCurrency(detalhe?.diferenca ?? 0)}</Typography>
          <Typography variant="caption" color="text.secondary">Divergências entre sistema e contado aparecem aqui para conferência do operador.</Typography>
        </DialogContent>
        <DialogActions><Button variant="contained" onClick={() => setDetalhe(null)}>Fechar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default SessoesCaixaPage;
