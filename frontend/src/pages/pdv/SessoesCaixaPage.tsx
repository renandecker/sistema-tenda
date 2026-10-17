import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, AttachMoney as AttachMoneyIcon, Refresh as RefreshIcon, Remove as RemoveIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiPdv } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { useAuth } from '@/hooks/useAuth';
import { formatCurrency, formatDateTime, getStatusColor, getStatusLabel } from '@/utils/formatters';
import { maskCurrency, parseCurrency } from '@/utils/masks';

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
  const { user } = useAuth();
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [fecharId, setFecharId] = useState<string | null>(null);
  const [formFechar, setFormFechar] = useState({ operadorFechamento: '', valorFinalContado: '', observacoesFechamento: '' });
  const [movId, setMovId] = useState<string | null>(null);
  const [movTipo, setMovTipo] = useState<'sangria' | 'reforco'>('sangria');
  const [movForm, setMovForm] = useState({ valor: '', motivo: '', operador: '' });
  const [detalhe, setDetalhe] = useState<Sessao | null>(null);
  const [vinculoOk, setVinculoOk] = useState<boolean | null>(null);

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

  // Abertura automática do caixa para o usuário logado (PDV vinculado)
  const autoAbrir = useCallback(async () => {
    const pdvId = user?.pdvId;
    if (!pdvId) return;
    try {
      await apiPdv.abrirCaixaAutomatico({ pdvId, operadorAbertura: user?.nome ?? 'SISTEMA', valorInicial: 0 });
      // Verifica vínculo PDV x caixa
      try {
        const { data } = await apiPdv.listSessoes({ page: 0, size: 1 });
        const aberta = (data?.content ?? []).find((s: any) => s?.status === 'ABERTA' && s?.pdv?.id === pdvId);
        if (aberta) {
          const { data: v } = await apiPdv.validarVinculoPdv(aberta.id, pdvId);
          setVinculoOk(!!v?.vinculado);
        }
      } catch { setVinculoOk(null); }
      load();
    } catch (e: any) {
      // silencioso se já existe
    }
  }, [user, load]);

  useEffect(() => { load(); autoAbrir(); }, [load, autoAbrir]);

  const confirmarFechamento = async () => {
    if (!fecharId) return;
    try {
      await apiPdv.fecharCaixa(fecharId, { operadorFechamento: formFechar.operadorFechamento, valorFinalContado: parseCurrency(formFechar.valorFinalContado), observacoesFechamento: formFechar.observacoesFechamento });
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
      const payload = { valor: parseCurrency(movForm.valor), motivo: movForm.motivo, operador: movForm.operador };
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
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            {vinculoOk !== null && (
              <Chip size="small" color={vinculoOk ? 'success' : 'error'} label={vinculoOk ? 'PDV vinculado ao caixa' : 'PDV não vinculado'} />
            )}
            <Tooltip title="Fechamento automático às 23:30 (configurável via CAIXA_AUTO_CLOSE_HORA)"><span><Typography variant="caption" color="text.secondary">Auto-fechamento 23:30</Typography></span></Tooltip>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
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

      <Dialog open={!!fecharId} onClose={() => setFecharId(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Fechar caixa — conferência</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}><TextField fullWidth size="small" label="Operador de fechamento" value={formFechar.operadorFechamento} onChange={(e) => setFormFechar({ ...formFechar, operadorFechamento: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Valor contado no gavetão (R$)" value={maskCurrency(formFechar.valorFinalContado)} onChange={(e) => setFormFechar({ ...formFechar, valorFinalContado: maskCurrency(e.target.value) })} placeholder="0,00" inputMode="decimal" /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Observações / divergências" value={formFechar.observacoesFechamento} onChange={(e) => setFormFechar({ ...formFechar, observacoesFechamento: e.target.value })} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setFecharId(null)}>Voltar</Button><Button variant="contained" onClick={confirmarFechamento}>Fechar caixa</Button></DialogActions>
      </Dialog>

      <Dialog open={!!movId} onClose={() => setMovId(null)} maxWidth="xs" fullWidth>
        <DialogTitle>{movTipo === 'sangria' ? 'Sangria' : 'Reforço'} de caixa</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}><TextField fullWidth size="small" label="Valor (R$)" value={maskCurrency(movForm.valor)} onChange={(e) => setMovForm({ ...movForm, valor: maskCurrency(e.target.value) })} placeholder="0,00" inputMode="decimal" /></Grid>
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
