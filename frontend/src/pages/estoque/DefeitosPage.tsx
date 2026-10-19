import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Refresh as RefreshIcon, Download as DownloadIcon, Warning as WarningIcon, Visibility as VisibilityIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiEstoque, apiFruteira, apiBuffet } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatDateTime, formatNumber } from '@/utils/formatters';

interface Defeito {
  id: string;
  produtoNome?: string;
  produtoCodigo?: string;
  tipo?: string;
  quantidade?: number;
  custoUnitario?: number;
  valorTotal?: number;
  motivoDefeito?: string;
  tipoDefeito?: string;
  loteDefeituoso?: string;
  quantidadeRecuperavel?: number;
  origem?: string;
  destino?: string;
  data?: string;
  responsavel?: string;
}

interface ProdutoSimple {
  id: string;
  codigo?: string;
  nome: string;
  estoqueAtual?: number;
  custoMedio?: number;
}

const TIPO_DEFEITO_OPCOES = [
  { value: 'SAIDA_DEFEITO_AVARIA', label: 'Avaria/Quebra' },
  { value: 'SAIDA_DEFEITO_DETERIORACAO', label: 'Deterioração/Perecível' },
  { value: 'SAIDA_DEFEITO_VENCIMENTO', label: 'Vencimento' },
  { value: 'SAIDA_DEFEITO_QUALIDADE', label: 'Qualidade/Reprovação' },
  { value: 'SAIDA_DEFEITO_OUTROS', label: 'Outros' },
];

export function DefeitosPage() {
  const [defeitos, setDefeitos] = useState<Defeito[]>([]);
  const [produtos, setProdutos] = useState<ProdutoSimple[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [dialog, setDialog] = useState(false);
  const [viewDialog, setViewDialog] = useState(false);
  const [selectedDefeito, setSelectedDefeito] = useState<Defeito | null>(null);
  const [form, setForm] = useState({
    produtoFruteiraId: '',
    produtoBuffetId: '',
    tipo: 'SAIDA_DEFEITO_AVARIA',
    quantidade: '',
    custoUnitario: '',
    motivoDefeito: '',
    tipoDefeito: '',
    loteDefeituoso: '',
    quantidadeRecuperavel: '',
    responsavel: '',
  });
  const [tipoFiltro, setTipoFiltro] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [d, f, b] = await Promise.allSettled([
        apiEstoque.listDefeitos({
          page,
          size: rowsPerPage,
          ...(dataInicio ? { dataInicio } : {}),
          ...(dataFim ? { dataFim } : {}),
        }),
        apiFruteira.listProdutos({ size: 500 }),
        apiBuffet.listProdutos({ size: 500 }),
      ]);
      const defeitoData = d.status === 'fulfilled' ? d.value.data : null;
      setDefeitos(defeitoData?.content ?? (Array.isArray(defeitoData) ? defeitoData : []));

      const listF: any[] = f.status === 'fulfilled' ? (f.value.data?.content ?? (Array.isArray(f.value.data) ? f.value.data : [])) : [];
      const listB: any[] = b.status === 'fulfilled' ? (b.value.data?.content ?? (Array.isArray(b.value.data) ? b.value.data : [])) : [];
      setProdutos([
        ...listF.map((p) => ({ id: p.id, codigo: p.codigo, nome: `[F] ${p.nome}`, estoqueAtual: Number(p.estoqueAtual ?? 0), custoMedio: Number(p.custoMedio ?? 0) })),
        ...listB.map((p) => ({ id: p.id, codigo: p.codigo, nome: `[B] ${p.nome}`, estoqueAtual: Number(p.estoqueAtual ?? 0), custoMedio: Number(p.custoMedio ?? 0) })),
      ]);
    } catch (e) {
      console.error(e);
      setDefeitos([]);
      setProdutos([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage, dataInicio, dataFim]);

  useEffect(() => { load(); }, [load]);

  const handleProdutoChange = (id: string) => {
    const produto = produtos.find(p => p.id === id);
    if (produto) {
      setForm(prev => ({
        ...prev,
        custoUnitario: String(prev.tipo.startsWith('ENTRADA') ? produto.custoMedio : ''),
      }));
    }
  };

  const submit = async () => {
    if (!form.produtoFruteiraId && !form.produtoBuffetId) { toast.warning('Selecione um produto'); return; }
    if (!form.quantidade || Number(form.quantidade) <= 0) { toast.warning('Informe a quantidade'); return; }
    if (!form.responsavel) { toast.warning('Informe o responsável'); return; }
    if (!form.motivoDefeito) { toast.warning('Informe o motivo do defeito'); return; }

    const payload = {
      produtoFruteiraId: form.produtoFruteiraId || undefined,
      produtoBuffetId: form.produtoBuffetId || undefined,
      tipo: form.tipo,
      quantidade: Number(form.quantidade),
      custoUnitario: form.custoUnitario ? Number(form.custoUnitario) : undefined,
      motivoDefeito: form.motivoDefeito,
      tipoDefeito: form.tipoDefeito,
      loteDefeituoso: form.loteDefeituoso || undefined,
      quantidadeRecuperavel: form.quantidadeRecuperavel ? Number(form.quantidadeRecuperavel) : undefined,
      responsavel: form.responsavel,
    };

    try {
      await apiEstoque.registrarDefeito(payload);
      toast.success('Defeito registrado com sucesso!');
      setDialog(false);
      setForm({
        produtoFruteiraId: '', produtoBuffetId: '', tipo: 'SAIDA_DEFEITO_AVARIA',
        quantidade: '', custoUnitario: '', motivoDefeito: '', tipoDefeito: '',
        loteDefeituoso: '', quantidadeRecuperavel: '', responsavel: '',
      });
      load();
    } catch (e) {
      console.error(e);
      toast.error('Erro ao registrar defeito');
    }
  };

  const exportar = (dados: Defeito[], nome: string) => {
    if (dados.length === 0) { toast.info('Nada para exportar'); return; }
    const headers = Object.keys(dados[0] as object);
    const csv = [headers.join(';'), ...dados.map((r) => headers.map((h) => JSON.stringify((r as any)[h] ?? '')).join(';'))].join('\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `${nome}.csv`; a.click();
    URL.revokeObjectURL(url);
    toast.success('Arquivo CSV exportado!');
  };

  const getTipoChipColor = (tipo?: string) => {
    if (!tipo) return 'default';
    if (tipo.includes('AVARIA')) return 'error';
    if (tipo.includes('DETERIORACAO')) return 'warning';
    if (tipo.includes('VENCIMENTO')) return 'error';
    if (tipo.includes('QUALIDADE')) return 'info';
    if (tipo.includes('REPROCESSO')) return 'success';
    return 'default';
  };

  const filtrados = useMemo(() => defeitos.filter(d => !tipoFiltro || d.tipo === tipoFiltro), [defeitos, tipoFiltro]);

  return (
    <Box>
      <PageHeader
        title="Gestão de Defeitos e Qualidade"
        subtitle="Registro de perdas por avaria, deterioração, vencimento, qualidade e reprocessamento"
        icon={<WarningIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="outlined" startIcon={<DownloadIcon />} onClick={() => exportar(filtrados, 'defeitos-estoque')}>Exportar</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialog(true)}>Registrar Defeito</Button>
          </Box>
        }
      />

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <TextField select fullWidth size="small" label="Tipo de Defeito" value={tipoFiltro} onChange={(e) => setTipoFiltro(e.target.value)}>
                <MenuItem value="">Todos</MenuItem>
                {TIPO_DEFEITO_OPCOES.map((t) => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
                <MenuItem value="ENTRADA_DEFEITO_REPROCESSO">Entrada por Reprocessamento</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField type="date" fullWidth size="small" label="Data Início" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField type="date" fullWidth size="small" label="Data Fim" value={dataFim} onChange={(e) => setDataFim(e.target.value)} InputLabelProps={{ shrink: true }} />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <DataTable<Defeito>
        data={filtrados} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar defeitos..." emptyMessage="Nenhum defeito registrado"
        columns={[
          { field: 'data', headerName: 'Data', width: 160, render: (v: string) => formatDateTime(v) },
          { field: 'produtoNome', headerName: 'Produto', flex: 1 },
          { field: 'produtoCodigo', headerName: 'Código', width: 120 },
          { field: 'tipo', headerName: 'Tipo', width: 200, align: 'center', render: (v: string) => <Chip size="small" label={TIPO_DEFEITO_OPCOES.find(t => t.value === v)?.label ?? v} color={getTipoChipColor(v)} /> },
          { field: 'quantidade', headerName: 'Qtd', width: 100, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'valorTotal', headerName: 'Valor Total', width: 140, align: 'right', render: (v: number) => formatNumber(v ?? 0, 2) },
          { field: 'motivoDefeito', headerName: 'Motivo', width: 200 },
          { field: 'tipoDefeito', headerName: 'Tipo Defeito', width: 150 },
          { field: 'loteDefeituoso', headerName: 'Lote', width: 120 },
          { field: 'quantidadeRecuperavel', headerName: 'Recuperável', width: 120, align: 'right', render: (v: number) => formatNumber(v ?? 0, 3) },
          { field: 'responsavel', headerName: 'Responsável', width: 150 },
          {
            field: 'actions', headerName: '', width: 60, align: 'center',
            render: (_: any, row: Defeito) => (
              <IconButton size="small" onClick={() => { setSelectedDefeito(row); setViewDialog(true); }}><VisibilityIcon fontSize="small" /></IconButton>
            ),
          },
        ]}
      />

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Registrar Defeito / Qualidade</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth size="small" label="Produto (Fruteira)" value={form.produtoFruteiraId} onChange={(e) => { setForm({ ...form, produtoFruteiraId: e.target.value, produtoBuffetId: '' }); handleProdutoChange(e.target.value); }}>
                <MenuItem value="">Selecione...</MenuItem>
                {produtos.filter(p => p.id.startsWith('F-') || !p.id.startsWith('B-')).map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo} - {p.nome} (Est: {formatNumber(p.estoqueAtual ?? 0, 3)})</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth size="small" label="Produto (Buffet)" value={form.produtoBuffetId} onChange={(e) => { setForm({ ...form, produtoBuffetId: e.target.value, produtoFruteiraId: '' }); handleProdutoChange(e.target.value); }}>
                <MenuItem value="">Selecione...</MenuItem>
                {produtos.filter(p => p.id.startsWith('B-')).map((p) => <MenuItem key={p.id} value={p.id}>{p.codigo} - {p.nome} (Est: {formatNumber(p.estoqueAtual ?? 0, 3)})</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField select fullWidth size="small" label="Tipo de Defeito" value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
                {TIPO_DEFEITO_OPCOES.map((t) => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
                <MenuItem value="ENTRADA_DEFEITO_REPROCESSO">Entrada por Reprocessamento</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField type="number" fullWidth size="small" label="Quantidade" value={form.quantidade} onChange={(e) => setForm({ ...form, quantidade: e.target.value })} inputProps={{ step: '0.001', min: '0.001' }} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField type="number" fullWidth size="small" label="Custo Unitário" value={form.custoUnitario} onChange={(e) => setForm({ ...form, custoUnitario: e.target.value })} inputProps={{ step: '0.01', min: '0' }} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="small" label="Motivo do Defeito" value={form.motivoDefeito} onChange={(e) => setForm({ ...form, motivoDefeito: e.target.value })} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="small" label="Tipo de Defeito (detalhe)" value={form.tipoDefeito} onChange={(e) => setForm({ ...form, tipoDefeito: e.target.value })} placeholder="Ex: Embalagem rasgada, Fruta podre, etc." />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="small" label="Lote Defeituoso" value={form.loteDefeituoso} onChange={(e) => setForm({ ...form, loteDefeituoso: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField type="number" fullWidth size="small" label="Quantidade Recuperável (reprocesso)" value={form.quantidadeRecuperavel} onChange={(e) => setForm({ ...form, quantidadeRecuperavel: e.target.value })} inputProps={{ step: '0.001', min: '0' }} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth size="small" label="Responsável" value={form.responsavel} onChange={(e) => setForm({ ...form, responsavel: e.target.value })} required />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={submit} disabled={loading}>Registrar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={viewDialog} onClose={() => setViewDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Detalhes do Defeito</DialogTitle>
        {selectedDefeito && (
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Data</Typography><Typography>{formatDateTime(selectedDefeito.data ?? '')}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Produto</Typography><Typography>{selectedDefeito.produtoNome} ({selectedDefeito.produtoCodigo})</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Tipo</Typography><Typography><Chip size="small" label={TIPO_DEFEITO_OPCOES.find(t => t.value === selectedDefeito.tipo)?.label ?? selectedDefeito.tipo} color={getTipoChipColor(selectedDefeito.tipo)} /></Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Quantidade</Typography><Typography>{formatNumber(selectedDefeito.quantidade ?? 0, 3)}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Custo Unitário</Typography><Typography>{formatNumber(selectedDefeito.custoUnitario ?? 0, 2)}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Valor Total</Typography><Typography>{formatNumber(selectedDefeito.valorTotal ?? 0, 2)}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Motivo</Typography><Typography>{selectedDefeito.motivoDefeito}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Tipo Defeito</Typography><Typography>{selectedDefeito.tipoDefeito}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Lote</Typography><Typography>{selectedDefeito.loteDefeituoso}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Recuperável</Typography><Typography>{formatNumber(selectedDefeito.quantidadeRecuperavel ?? 0, 3)}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Responsável</Typography><Typography>{selectedDefeito.responsavel}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Origem</Typography><Typography>{selectedDefeito.origem}</Typography></Grid>
              <Grid item xs={12} sm={6}><Typography variant="body2" color="text.secondary">Destino</Typography><Typography>{selectedDefeito.destino}</Typography></Grid>
            </Grid>
          </DialogContent>
        )}
        <DialogActions><Button onClick={() => setViewDialog(false)}>Fechar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}

import { useMemo } from 'react';

export default DefeitosPage;