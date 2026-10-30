import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid,
  IconButton, MenuItem, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Tooltip, Typography
} from '@mui/material';
import {
  Add as AddIcon, AttachMoney as AttachMoneyIcon, LockOpen as LockOpenIcon,
  Refresh as RefreshIcon, Remove as RemoveIcon, Visibility as ViewIcon, ReceiptLong as ReceiptLongIcon
} from '@mui/icons-material';
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
  operadorFechamento?: string;
  valorInicial?: number;
  totalVendas?: number;
  vendasDinheiro?: number;
  vendasCartao?: number;
  vendasPix?: number;
  vendasVale?: number;
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
  const [totalRows, setTotalRows] = useState(0);
  const [fecharId, setFecharId] = useState<string | null>(null);
  const [formFechar, setFormFechar] = useState({ operadorFechamento: '', valorFinalContado: '', observacoesFechamento: '' });
  const [movId, setMovId] = useState<string | null>(null);
  const [movTipo, setMovTipo] = useState<'sangria' | 'reforco'>('sangria');
  const [movForm, setMovForm] = useState({ valor: '', motivo: '', operador: '' });
  const [detalhe, setDetalhe] = useState<Sessao | null>(null);
  const [vinculoOk, setVinculoOk] = useState<boolean | null>(null);
  const [abrirDialog, setAbrirDialog] = useState(false);
  const [pdvs, setPdvs] = useState<{ id: string; codigo?: string; nome?: string }[]>([]);
  const [formAbrir, setFormAbrir] = useState({ pdvId: '', operador: '', valorInicial: '' });

  // Modal para Itens Vendidos e Formas de Pagamento
  const [vendasModalOpen, setVendasModalOpen] = useState(false);
  const [sessaoSelecionadaModal, setSessaoSelecionadaModal] = useState<Sessao | null>(null);
  const [vendasSessao, setVendasSessao] = useState<any[]>([]);
  const [loadingVendasModal, setLoadingVendasModal] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiPdv.listSessoes({ page, size: rowsPerPage });
      setSessoes(data?.content ?? (Array.isArray(data) ? data : []));
      setTotalRows(data?.totalElements ?? (Array.isArray(data) ? data.length : 0));
    } catch {
      setSessoes([]);
      setTotalRows(0);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  // Resolve o PDV do usuário logado de forma focada e robusta
  const resolverPdvId = useCallback(async (): Promise<string | null> => {
    if (user?.pdvId) return user.pdvId;
    try {
      const { data } = await apiPdv.listPdvs({ size: 200, ativo: true });
      const list = data?.content ?? (Array.isArray(data) ? data : []);
      if (list.length === 0) return null;
      const match = list.find((p: any) => p.codigo?.toLowerCase().includes(user?.nome?.toLowerCase() || ''));
      return match?.id ?? list[0]?.id ?? null;
    } catch {
      return null;
    }
  }, [user?.pdvId, user?.nome]);

  // Abertura automática focada no caixa do usuário logado (1 caixa por usuário por dia)
  const autoAbrir = useCallback(async () => {
    const pdvId = await resolverPdvId();
    try {
      await apiPdv.abrirCaixaUsuario({ pdvId, operador: user?.nome ?? 'SISTEMA', valorInicial: 0 });
      if (pdvId) {
        try {
          const { data: aberta } = await apiPdv.getAbertaByPdv(pdvId);
          if (aberta?.id) {
            const { data: v } = await apiPdv.validarVinculoPdv(aberta.id, pdvId);
            setVinculoOk(!!v?.vinculado);
          }
        } catch { setVinculoOk(null); }
      }
    } catch {
      // já criado ou ignorado
    } finally {
      load();
    }
  }, [user, resolverPdvId, load]);

  useEffect(() => {
    (async () => {
      await autoAbrir();
      load();
    })();
  }, [load, autoAbrir]);

  const abrirDialogManual = async () => {
    try {
      const { data } = await apiPdv.listPdvs({ size: 200, ativo: true });
      const list = data?.content ?? (Array.isArray(data) ? data : []);
      setPdvs(list);
      const pdvId = await resolverPdvId();
      setFormAbrir({ pdvId: pdvId ?? list[0]?.id ?? '', operador: user?.nome ?? '', valorInicial: '' });
      setAbrirDialog(true);
    } catch {
      toast.error('Erro ao listar PDVs');
    }
  };

  const confirmarAbertura = async () => {
    if (!formAbrir.pdvId) { toast.warning('Selecione o PDV'); return; }
    try {
      await apiPdv.abrirCaixa({
        pdvId: formAbrir.pdvId,
        operador: formAbrir.operador || user?.nome || 'SISTEMA',
        valorInicial: parseCurrency(formAbrir.valorInicial),
      });
      toast.success('Caixa aberto!');
      setAbrirDialog(false);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao abrir caixa');
    }
  };

  const confirmarFechamento = async () => {
    if (!fecharId) return;
    try {
      await apiPdv.fecharCaixa(fecharId, { operador: formFechar.operadorFechamento, valorFinalContado: parseCurrency(formFechar.valorFinalContado) });
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
      const payload = { valor: parseCurrency(movForm.valor), descricao: movForm.motivo, operador: movForm.operador };
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

  const abrirModalVendasPagamentos = async (s: Sessao) => {
    setSessaoSelecionadaModal(s);
    setVendasModalOpen(true);
    setLoadingVendasModal(true);
    try {
      const pdvId = s.pdv?.id;
      if (!pdvId) {
        setVendasSessao([]);
        return;
      }
      const dataInicio = s.dataAbertura ? s.dataAbertura.split('T')[0] : undefined;
      const dataFim = s.dataFechamento ? s.dataFechamento.split('T')[0] : new Date().toISOString().split('T')[0];
      const { data } = await apiPdv.listVendas({ pdvId, dataInicio, dataFim, size: 200 });
      const list = data?.content ?? (Array.isArray(data) ? data : []);
      setVendasSessao(list);
    } catch {
      setVendasSessao([]);
    } finally {
      setLoadingVendasModal(false);
    }
  };

  const itensAgregados = vendasSessao.flatMap((v: any) => (v.itens ?? []).map((it: any) => ({ ...it, vendaNumero: v.numero })));
  const pagamentosAgregados = vendasSessao.flatMap((v: any) => (v.pagamentos ?? []).map((p: any) => ({ ...p, vendaNumero: v.numero })));

  return (
    <Box>
      <PageHeader
        title="Sessões de Caixa"
        subtitle="Abertura focada no usuário logado, gestão de PDVs e conferência de vendas e pagamentos"
        icon={<AttachMoneyIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            {vinculoOk !== null && (
              <Chip size="small" color={vinculoOk ? 'success' : 'error'} label={vinculoOk ? 'PDV vinculado ao caixa' : 'PDV não vinculado'} />
            )}
            <Tooltip title="Fechamento automático às 23:30"><span><Typography variant="caption" color="text.secondary">Auto 23:30</Typography></span></Tooltip>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" size="small" startIcon={<LockOpenIcon />} onClick={abrirDialogManual}>Abrir caixa</Button>
          </Box>
        }
      />
      <DataTable<Sessao>
        data={sessoes} loading={loading} serverSide totalCount={totalRows} page={page} rowsPerPage={rowsPerPage}
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
            field: 'acoes', headerName: 'Ações', width: 230, align: 'center',
            render: (_: any, row: Sessao) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Itens vendidos e Formas de pagamento">
                  <IconButton size="small" color="primary" onClick={() => abrirModalVendasPagamentos(row)}>
                    <ReceiptLongIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Detalhes / conferência"><IconButton size="small" onClick={() => verDetalhe(row)}><ViewIcon fontSize="small" /></IconButton></Tooltip>
                <Tooltip title="Sangria"><span><IconButton size="small" color="warning" disabled={row.status !== 'ABERTA'} onClick={() => { setMovId(row.id); setMovTipo('sangria'); }}><RemoveIcon fontSize="small" /></IconButton></span></Tooltip>
                <Tooltip title="Reforço"><span><IconButton size="small" color="success" disabled={row.status !== 'ABERTA'} onClick={() => { setMovId(row.id); setMovTipo('reforco'); }}><AddIcon fontSize="small" /></IconButton></span></Tooltip>
                <Tooltip title="Fechar caixa"><span><Button size="small" variant="outlined" disabled={row.status !== 'ABERTA'} onClick={() => setFecharId(row.id)}>Fechar</Button></span></Tooltip>
              </Box>
            ),
          },
        ]}
      />

      {/* Modal de Itens Vendidos e Formas de Pagamento por PDV */}
      <Dialog open={vendasModalOpen} onClose={() => setVendasModalOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Detalhes do PDV {sessaoSelecionadaModal?.pdv?.codigo} — Itens Vendidos e Formas de Pagamento
        </DialogTitle>
        <DialogContent dividers>
          {loadingVendasModal ? (
            <Typography variant="body2" sx={{ py: 3, textAlign: 'center' }}>Carregando vendas...</Typography>
          ) : (
            <Box>
              <Box sx={{ mb: 2, p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                <Typography variant="subtitle2" gutterBottom>Resumo de Pagamentos Registrados</Typography>
                <Grid container spacing={2}>
                  <Grid item xs={3}><Typography variant="caption">Dinheiro</Typography><Typography variant="body2" fontWeight={700}>{formatCurrency(sessaoSelecionadaModal?.vendasDinheiro ?? 0)}</Typography></Grid>
                  <Grid item xs={3}><Typography variant="caption">Cartão</Typography><Typography variant="body2" fontWeight={700}>{formatCurrency(sessaoSelecionadaModal?.vendasCartao ?? 0)}</Typography></Grid>
                  <Grid item xs={3}><Typography variant="caption">PIX</Typography><Typography variant="body2" fontWeight={700}>{formatCurrency(sessaoSelecionadaModal?.vendasPix ?? 0)}</Typography></Grid>
                  <Grid item xs={3}><Typography variant="caption">Vales</Typography><Typography variant="body2" fontWeight={700}>{formatCurrency(sessaoSelecionadaModal?.vendasVale ?? 0)}</Typography></Grid>
                </Grid>
              </Box>

              <Typography variant="subtitle1" fontWeight={700} gutterBottom sx={{ mt: 2 }}>Itens Vendidos ({itensAgregados.length})</Typography>
              <TableContainer component={Paper} variant="outlined" sx={{ mb: 3, maxHeight: 220 }}>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell>Venda</TableCell>
                      <TableCell>Produto</TableCell>
                      <TableCell align="right">Qtd</TableCell>
                      <TableCell align="right">Preço Un.</TableCell>
                      <TableCell align="right">Total</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {itensAgregados.map((item: any, idx: number) => (
                      <TableRow key={idx}>
                        <TableCell>#{item.vendaNumero || '—'}</TableCell>
                        <TableCell>{item.nomeProduto || item.produtoNome || 'Produto'}</TableCell>
                        <TableCell align="right">{item.quantidade ?? 1}</TableCell>
                        <TableCell align="right">{formatCurrency(item.precoUnitario ?? 0)}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>{formatCurrency(item.valorTotal ?? 0)}</TableCell>
                      </TableRow>
                    ))}
                    {itensAgregados.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} align="center">Nenhum item vendido nesta sessão.</TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Formas de Pagamento Utilizadas ({pagamentosAgregados.length})</Typography>
              <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 200 }}>
                <Table size="small" stickyHeader>
                  <TableHead>
                    <TableRow>
                      <TableCell>Venda</TableCell>
                      <TableCell>Forma de Pagamento</TableCell>
                      <TableCell align="right">Valor</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pagamentosAgregados.map((pag: any, idx: number) => (
                      <TableRow key={idx}>
                        <TableCell>#{pag.vendaNumero || '—'}</TableCell>
                        <TableCell>{pag.formaPagamento ?? '—'}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600 }}>{formatCurrency(pag.valor ?? 0)}</TableCell>
                      </TableRow>
                    ))}
                    {pagamentosAgregados.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={3} align="center">Nenhum pagamento registrado nesta sessão.</TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVendasModalOpen(false)} variant="contained">Fechar</Button>
        </DialogActions>
      </Dialog>

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

      <Dialog open={abrirDialog} onClose={() => setAbrirDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Abrir caixa</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="PDV / Terminal" value={formAbrir.pdvId}
                onChange={(e) => setFormAbrir({ ...formAbrir, pdvId: e.target.value })}>
                {pdvs.map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo} — {p.nome}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Operador" value={formAbrir.operador} onChange={(e) => setFormAbrir({ ...formAbrir, operador: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Fundo inicial (R$)" value={maskCurrency(formAbrir.valorInicial)} onChange={(e) => setFormAbrir({ ...formAbrir, valorInicial: maskCurrency(e.target.value) })} placeholder="0,00" inputMode="decimal" /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setAbrirDialog(false)}>Cancelar</Button><Button variant="contained" onClick={confirmarAbertura}>Abrir caixa</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default SessoesCaixaPage;
