import { useCallback, useEffect, useMemo, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Download as DownloadIcon, Inventory as InventoryIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet, apiEstoque, apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatDateTime, formatNumber } from '@/utils/formatters';
import { maskCurrency, maskQuantity, parseCurrency, parseDecimal } from '@/utils/masks';

interface Movimento {
  id: string;
  produtoNome?: string;
  produtoCodigo?: string;
  tipo?: string;
  quantidade?: number;
  saldoAnterior?: number;
  saldoAtual?: number;
  origem?: string;
  data?: string;
  responsavel?: string;
}

interface Saldo {
  id: string;
  produtoId: string;
  codigo?: string;
  nome: string;
  local: string;
  estoque: number;
  minimo?: number;
}

export function EstoquePage() {
  const [movimentos, setMovimentos] = useState<Movimento[]>([]);
  const [saldos, setSaldos] = useState<Saldo[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [movPage, setMovPage] = useState(0);
  const [movRowsPerPage, setMovRowsPerPage] = useState(10);
  const [movTotal, setMovTotal] = useState(0);
  const [tipo, setTipo] = useState('');
  const [local, setLocal] = useState('');
  const [movDialog, setMovDialog] = useState(false);
  const [registrando, setRegistrando] = useState(false);
  const [movForm, setMovForm] = useState({ produtoId: '', operacao: 'ENTRADA_COMPRA', quantidade: '', custo: '', numeroDocumento: '', observacoes: '', responsavel: '' });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [m, f, b] = await Promise.allSettled([
        apiEstoque.listMovimentos({ page: movPage, size: movRowsPerPage }),
        apiFruteira.listProdutos({ size: 200 }),
        apiBuffet.listProdutos({ size: 200 }),
      ]);
      const movData = m.status === 'fulfilled' ? m.value.data : null;
      setMovimentos(movData?.content ?? (Array.isArray(movData) ? movData : []));
      setMovTotal(movData?.totalElements ?? (Array.isArray(movData) ? movData.length : 0));
      const listF: any[] = f.status === 'fulfilled' ? (f.value.data?.content ?? (Array.isArray(f.value.data) ? f.value.data : [])) : [];
      const listB: any[] = b.status === 'fulfilled' ? (b.value.data?.content ?? (Array.isArray(b.value.data) ? b.value.data : [])) : [];
      setSaldos([
        ...listF.map((p) => ({ id: `F-${p.id}`, produtoId: p.id, codigo: p.codigo, nome: p.nome, local: 'FRUTEIRA', estoque: Number(p.estoqueAtual ?? 0), minimo: Number(p.estoqueMinimo ?? 0) })),
        ...listB.map((p) => ({ id: `B-${p.id}`, produtoId: p.id, codigo: p.codigo, nome: p.nome, local: 'BUFFET', estoque: Number(p.estoqueAtual ?? 0), minimo: Number(p.estoqueMinimo ?? 0) })),
      ]);
    } catch {
      setMovimentos([]);
      setMovTotal(0);
    } finally {
      setLoading(false);
    }
  }, [movPage, movRowsPerPage]);

  useEffect(() => { load(); }, [load]);

  const abrirMovDialog = () => {
    setMovForm({ produtoId: '', operacao: 'ENTRADA_COMPRA', quantidade: '', custo: '', numeroDocumento: '', observacoes: '', responsavel: '' });
    setMovDialog(true);
  };

  const registrarMovimento = async () => {
    const saldo = saldos.find((s) => s.id === movForm.produtoId);
    if (!saldo) { toast.warning('Selecione o produto'); return; }
    const qtd = parseDecimal(movForm.quantidade, 3);
    if (qtd <= 0) { toast.warning('Informe a quantidade'); return; }
    const isFruteira = saldo.local === 'FRUTEIRA';
    const payload: any = {
      quantidade: qtd,
      custoUnitario: movForm.custo ? parseCurrency(movForm.custo) : undefined,
      responsavel: movForm.responsavel || undefined,
    };
    if (isFruteira) payload.produtoFruteiraId = saldo.produtoId;
    else payload.produtoBuffetId = saldo.produtoId;
    setRegistrando(true);
    try {
      if (movForm.operacao === 'ENTRADA_COMPRA') {
        await apiEstoque.registrarEntradaCompra({
          ...payload,
          numeroDocumento: movForm.numeroDocumento || undefined,
          documentoReferencia: movForm.observacoes || undefined,
        });
      } else {
        await apiEstoque.registrarAjuste({
          ...payload,
          tipo: movForm.operacao,
          observacoes: movForm.observacoes || undefined,
        });
      }
      toast.success('Movimento registrado e saldo atualizado!');
      setMovDialog(false);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao registrar movimento');
    } finally {
      setRegistrando(false);
    }
  };

  const filtrados = useMemo(() => saldos.filter((s) => (!local || s.local === local)), [saldos, local]);
  const totalItens = filtrados.length;
  const totalUnidades = filtrados.reduce((s, i) => s + i.estoque, 0);
  const abaixoMinimo = filtrados.filter((i) => (i.minimo ?? 0) > 0 && i.estoque < (i.minimo ?? 0)).length;

  const exportar = (dados: unknown[], nome: string) => {
    const rows = dados as Record<string, unknown>[];
    if (rows.length === 0) { toast.info('Nada para exportar'); return; }
    const headers = Object.keys(rows[0]);
    const csv = [headers.join(';'), ...rows.map((r) => headers.map((h) => JSON.stringify(r[h] ?? '')).join(';'))].join('\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `${nome}.csv`; a.click();
    URL.revokeObjectURL(url);
    toast.success('Arquivo CSV exportado!');
  };

  return (
    <Box>
      <PageHeader
        title="Movimentação de Estoque"
        subtitle="Entradas, saídas, saldos por produto e local, histórico integrado com compras e vendas"
        icon={<InventoryIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={abrirMovDialog}>Registrar estoque</Button>
            <Button variant="outlined" startIcon={<DownloadIcon />} onClick={() => exportar(filtrados, 'saldos-estoque')}>Exportar saldos</Button>
            <Button variant="outlined" startIcon={<DownloadIcon />} onClick={() => exportar(movimentos, 'movimentos-estoque')}>Exportar movimentos</Button>
          </Box>
        }
      />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        {[
          { label: 'Itens em estoque', value: String(totalItens) },
          { label: 'Unidades totais', value: formatNumber(totalUnidades, 2) },
          { label: 'Abaixo do mínimo', value: String(abaixoMinimo) },
        ].map((c) => (
          <Grid item xs={12} sm={4} key={c.label}>
            <Card><CardContent><Typography variant="caption" color="text.secondary">{c.label}</Typography><Typography variant="h5" fontWeight={800}>{c.value}</Typography></CardContent></Card>
          </Grid>
        ))}
      </Grid>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={6} sm={3}>
              <TextField select fullWidth size="small" label="Local" value={local} onChange={(e) => setLocal(e.target.value)}>
                <MenuItem value="">Todos</MenuItem>{['FRUTEIRA', 'BUFFET', 'ESTOQUE'].map((l) => <MenuItem key={l} value={l}>{l}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6} sm={3}>
              <TextField select fullWidth size="small" label="Tipo de movimento" value={tipo} onChange={(e) => setTipo(e.target.value)}>
                <MenuItem value="">Todos</MenuItem>{['ENTRADA', 'SAIDA', 'AJUSTE', 'TRANSFERENCIA', 'PERDA', 'VENDA', 'COMPRA'].map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}><Typography variant="caption" color="text.secondary">Saldos consolidados de fruteira + buffet. Movimentos de compras, vendas, perdas e transferências aparecem no histórico abaixo.</Typography></Grid>
          </Grid>
        </CardContent>
      </Card>
      <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1 }}>Saldos por produto e local</Typography>
      <DataTable<Saldo>
        data={filtrados} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar saldo por produto..." emptyMessage="Nenhum saldo encontrado"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 120 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          { field: 'local', headerName: 'Local', width: 120, align: 'center', render: (v: string) => <Chip size="small" label={v} variant="outlined" /> },
          { field: 'estoque', headerName: 'Saldo', width: 130, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'minimo', headerName: 'Mínimo', width: 110, align: 'right', render: (v: number) => formatNumber(v ?? 0, 3) },
        ]}
      />
      <Typography variant="subtitle1" fontWeight={700} sx={{ mt: 3, mb: 1 }}>Histórico de movimentações</Typography>
      <DataTable<Movimento>
        data={movimentos.filter((m) => !tipo || m.tipo === tipo)} loading={loading} serverSide totalCount={tipo ? movimentos.filter((m) => m.tipo === tipo).length : movTotal} page={movPage} rowsPerPage={movRowsPerPage}
        onPageChange={(p, r) => { setMovPage(p); setMovRowsPerPage(r); }}
        searchPlaceholder="Buscar no histórico..." emptyMessage="Sem histórico — os movimentos de compras, vendas, perdas e transferências passarão a aparecer aqui"
        columns={[
          { field: 'data', headerName: 'Data', width: 160, render: (v: string) => formatDateTime(v) },
          { field: 'produtoNome', headerName: 'Produto', flex: 1 },
          { field: 'tipo', headerName: 'Tipo', width: 140, align: 'center', render: (v: string) => <Chip size="small" label={v ?? '-'} variant="outlined" /> },
          { field: 'quantidade', headerName: 'Qtd', width: 110, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'origem', headerName: 'Origem', width: 140 },
          { field: 'responsavel', headerName: 'Responsável', width: 150 },
        ]}
      />

      <Dialog open={movDialog} onClose={() => setMovDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Registrar quantidade em estoque</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12} sm={7}>
              <TextField select fullWidth size="small" label="Produto" value={movForm.produtoId}
                onChange={(e) => setMovForm({ ...movForm, produtoId: e.target.value })}>
                <MenuItem value=""><em>Selecione</em></MenuItem>
                {saldos.map((s) => (
                  <MenuItem key={s.id} value={s.id}>{s.nome}{s.codigo ? ` (${s.codigo})` : ''} — {s.local} · saldo {formatNumber(s.estoque, 3)}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={5}>
              <TextField select fullWidth size="small" label="Operação" value={movForm.operacao}
                onChange={(e) => setMovForm({ ...movForm, operacao: e.target.value })}>
                <MenuItem value="ENTRADA_COMPRA">Entrada (compra)</MenuItem>
                <MenuItem value="ENTRADA_AJUSTE">Ajuste de entrada</MenuItem>
                <MenuItem value="SAIDA_AJUSTE">Ajuste de saída</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField fullWidth size="small" label="Quantidade" value={maskQuantity(movForm.quantidade)}
                onChange={(e) => setMovForm({ ...movForm, quantidade: maskQuantity(e.target.value) })}
                placeholder="0,000" inputMode="decimal" />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField fullWidth size="small" label="Custo unitário (R$)" value={maskCurrency(movForm.custo)}
                onChange={(e) => setMovForm({ ...movForm, custo: maskCurrency(e.target.value) })}
                placeholder="0,00" inputMode="decimal" />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField fullWidth size="small" label="Nº do documento/NF" value={movForm.numeroDocumento}
                onChange={(e) => setMovForm({ ...movForm, numeroDocumento: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="small" label="Responsável" value={movForm.responsavel}
                onChange={(e) => setMovForm({ ...movForm, responsavel: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="small" label="Observações" value={movForm.observacoes}
                onChange={(e) => setMovForm({ ...movForm, observacoes: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMovDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={registrarMovimento} disabled={registrando}>Registrar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
export default EstoquePage;
