import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Chip, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Download as DownloadIcon, Refresh as RefreshIcon, Warning as WarningIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet, apiEstoque, apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatNumber } from '@/utils/formatters';

interface Alerta {
  id: string;
  codigo?: string;
  nome: string;
  local?: string;
  estoque?: number;
  minimo?: number;
  tipo?: string;
  sugestaoReposicao?: number;
}

export function AlertasEstoquePage() {
  const [alertas, setAlertas] = useState<Alerta[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [tipo, setTipo] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [a, fb, bb] = await Promise.allSettled([
        apiEstoque.listAlertas({ size: 200 }),
        apiFruteira.getEstoqueBaixo(),
        apiBuffet.getEstoqueBaixo(),
      ]);
      const listA: any[] = a.status === 'fulfilled' ? (a.value.data?.content ?? (Array.isArray(a.value.data) ? a.value.data : [])) : [];
      const listF: any[] = fb.status === 'fulfilled' ? (Array.isArray(fb.value.data) ? fb.value.data : fb.value.data?.content ?? []) : [];
      const listB: any[] = bb.status === 'fulfilled' ? (Array.isArray(bb.value.data) ? bb.value.data : bb.value.data?.content ?? []) : [];
      const derivados: Alerta[] = [
        ...listF.map((p: any, i: number) => ({ id: `F-${p.id ?? i}`, codigo: p.codigo, nome: p.nome, local: 'FRUTEIRA', estoque: Number(p.estoqueAtual ?? 0), minimo: Number(p.estoqueMinimo ?? 0), tipo: 'ESTOQUE_BAIXO', sugestaoReposicao: Math.max(0, Number(p.estoqueMinimo ?? 0) * 2 - Number(p.estoqueAtual ?? 0)) })),
        ...listB.map((p: any, i: number) => ({ id: `B-${p.id ?? i}`, codigo: p.codigo, nome: p.nome, local: 'BUFFET', estoque: Number(p.estoqueAtual ?? 0), minimo: Number(p.estoqueMinimo ?? 0), tipo: 'ESTOQUE_BAIXO', sugestaoReposicao: Math.max(0, Number(p.estoqueMinimo ?? 0) * 2 - Number(p.estoqueAtual ?? 0)) })),
      ];
      setAlertas([...listA, ...derivados]);
    } catch {
      setAlertas([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filtrados = useMemo(() => alertas.filter((x) => !tipo || x.tipo === tipo), [alertas, tipo]);
  const criticos = filtrados.filter((x) => (x.estoque ?? 0) <= 0).length;

  const exportar = () => {
    if (filtrados.length === 0) { toast.info('Nada para exportar'); return; }
    const headers = ['codigo', 'nome', 'local', 'estoque', 'minimo', 'tipo', 'sugestaoReposicao'];
    const csv = [headers.join(';'), ...filtrados.map((r) => headers.map((h) => JSON.stringify((r as unknown as Record<string, unknown>)[h] ?? '')).join(';'))].join('\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = 'alertas-estoque.csv'; a.click();
    URL.revokeObjectURL(url);
    toast.success('Alertas exportados!');
  };

  return (
    <Box>
      <PageHeader
        title="Alertas de Estoque"
        subtitle="Estoque mínimo, validade, sugestão de reposição e notificações"
        icon={<WarningIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField select size="small" label="Tipo" value={tipo} onChange={(e) => setTipo(e.target.value)} sx={{ minWidth: 170 }}>
              <MenuItem value="">Todos</MenuItem>
              {['ESTOQUE_BAIXO', 'VALIDADE', 'RUPTURA', 'EXCESSO'].map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
            </TextField>
            <Tooltip title="Atualizar"><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></Tooltip>
            <Button variant="outlined" startIcon={<DownloadIcon />} onClick={exportar}>Exportar</Button>
          </Box>
        }
      />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} sm={6}><Card><CardContent><Typography variant="caption">Alertas ativos</Typography><Typography variant="h5" fontWeight={800}>{filtrados.length}</Typography></CardContent></Card></Grid>
        <Grid item xs={12} sm={6}><Card><CardContent><Typography variant="caption">Itens zerados / ruptura</Typography><Typography variant="h5" fontWeight={800} color="error.main">{criticos}</Typography></CardContent></Card></Grid>
      </Grid>
      {criticos > 0 && <Alert severity="error" sx={{ mb: 2 }}>{criticos} produto(s) zerado(s) — providencie reposição imediata para não romper vendas no PDV.</Alert>}
      <DataTable<Alerta>
        data={filtrados} loading={loading} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar alertas por produto..." emptyMessage="Nenhum alerta — estoque dentro dos mínimos"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 120 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          { field: 'local', headerName: 'Local', width: 120, align: 'center', render: (v: string) => <Chip size="small" label={v ?? '-'} variant="outlined" /> },
          { field: 'tipo', headerName: 'Tipo', width: 150, align: 'center', render: (v: string) => <Chip size="small" label={v ?? '-'} color="warning" /> },
          { field: 'estoque', headerName: 'Estoque', width: 110, align: 'right', render: (v: number) => formatNumber(v, 3) },
          { field: 'minimo', headerName: 'Mínimo', width: 110, align: 'right', render: (v: number) => formatNumber(v ?? 0, 3) },
          { field: 'sugestaoReposicao', headerName: 'Sugestão compra', width: 150, align: 'right', render: (v: number) => formatNumber(v ?? 0, 3) },
        ]}
      />
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
        A sugestão de reposição considera o dobro do estoque mínimo menos o saldo atual. Ajuste o mínimo no cadastro do produto para calibrar os alertas em tempo real.
      </Typography>
    </Box>
  );
}
export default AlertasEstoquePage;
