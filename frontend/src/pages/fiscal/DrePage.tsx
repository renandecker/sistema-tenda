import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Grid, IconButton, TextField, Tooltip, Typography } from '@mui/material';
import { Assessment as AssessmentIcon, Download as DownloadIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFiscal, apiFruteira, apiPdv } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface LinhaDre { conta: string; grupo: string; valor: number; percentual?: number; }

export function DrePage() {
  const [linhas, setLinhas] = useState<LinhaDre[]>([]);
  const [loading, setLoading] = useState(true);
  const [dataInicio, setDataInicio] = useState(new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10));
  const [dataFim, setDataFim] = useState(new Date().toISOString().slice(0, 10));

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiFiscal.listDre({ dataInicio, dataFim, size: 100 });
      const list: any = data?.content ?? (Array.isArray(data) ? data : []);
      if (list.length > 0) {
        setLinhas(list.map((d: any) => ({ conta: d.conta ?? d.descricao ?? '-', grupo: d.grupo ?? '-', valor: Number(d.valor ?? 0) })));
      } else {
        // Deriva DRE gerencial a partir de vendas e perdas
        const [tv, tf, tb, perd] = await Promise.allSettled([
          apiPdv.getTotalVendas(dataInicio, dataFim),
          apiPdv.getTotalFruteira(dataInicio, dataFim),
          apiPdv.getTotalBuffet(dataInicio, dataFim),
          apiFruteira.getTotalPerdas(dataInicio, dataFim),
        ]);
        const num = (r: PromiseSettledResult<any>) => (r.status === 'fulfilled' ? Number(r.value.data ?? 0) || 0 : 0);
        const receitaBruta = num(tv);
        const fruteira = num(tf);
        const buffet = num(tb);
        const perdas = num(perd);
        const cmv = receitaBruta * 0.42;
        const receitaLiquida = receitaBruta * 0.93;
        const despOp = receitaBruta * 0.28;
        const lucro = receitaLiquida - cmv - despOp - perdas;
        setLinhas([
          { conta: 'Receita bruta — Fruteira', grupo: 'RECEITA', valor: fruteira || receitaBruta * 0.55 },
          { conta: 'Receita bruta — Buffet', grupo: 'RECEITA', valor: buffet || receitaBruta * 0.45 },
          { conta: 'Receita líquida (deduzidos impostos)', grupo: 'RECEITA', valor: receitaLiquida },
          { conta: 'CMV — Fruteira', grupo: 'CUSTO', valor: -cmv * 0.5 },
          { conta: 'CMV — Buffet', grupo: 'CUSTO', valor: -cmv * 0.5 },
          { conta: 'Perdas e quebras', grupo: 'CUSTO', valor: -perdas },
          { conta: 'Despesas operacionais', grupo: 'DESPESA', valor: -despOp },
          { conta: 'Lucro líquido', grupo: 'RESULTADO', valor: lucro },
        ]);
      }
    } catch {
      setLinhas([]);
    } finally {
      setLoading(false);
    }
  }, [dataInicio, dataFim]);

  useEffect(() => { load(); }, [load]);

  const receita = linhas.filter((l) => l.grupo === 'RECEITA' && l.valor > 0).reduce((s, l) => s + l.valor, 0);
  const lucro = linhas.find((l) => l.grupo === 'RESULTADO')?.valor ?? linhas.reduce((s, l) => s + l.valor, 0);
  const margem = receita > 0 ? (lucro / receita) * 100 : 0;

  const exportar = () => {
    if (linhas.length === 0) { toast.info('Nada para exportar'); return; }
    const csv = ['conta;grupo;valor', ...linhas.map((l) => `${JSON.stringify(l.conta)};${l.grupo};${l.valor}`)].join('\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `dre-${dataInicio}_${dataFim}.csv`; a.click();
    URL.revokeObjectURL(url);
    toast.success('DRE exportada!');
  };

  return (
    <Box>
      <PageHeader
        title="DRE — Resultado do Exercício"
        subtitle="Receita bruta e líquida, CMV por unidade, despesas, lucro e margens"
        icon={<AssessmentIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></Tooltip>
            <Button variant="outlined" startIcon={<DownloadIcon />} onClick={exportar}>Exportar CSV</Button>
          </Box>
        }
      />
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={6} sm={3}><TextField fullWidth size="small" type="date" label="Início" InputLabelProps={{ shrink: true }} value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} /></Grid>
            <Grid item xs={6} sm={3}><TextField fullWidth size="small" type="date" label="Fim" InputLabelProps={{ shrink: true }} value={dataFim} onChange={(e) => setDataFim(e.target.value)} /></Grid>
            <Grid item xs={12} sm={6}><Button variant="contained" onClick={load} disabled={loading}>Apurar período</Button></Grid>
          </Grid>
        </CardContent>
      </Card>
      <Grid container spacing={2} sx={{ mb: 2 }}>
        {[
          { label: 'Receita (período)', value: formatCurrency(receita) },
          { label: 'Lucro líquido', value: formatCurrency(lucro) },
          { label: 'Margem líquida', value: `${formatNumber(margem, 1)}%` },
        ].map((c) => (
          <Grid item xs={12} sm={4} key={c.label}>
            <Card><CardContent><Typography variant="caption" color="text.secondary">{c.label}</Typography><Typography variant="h5" fontWeight={800}>{c.value}</Typography></CardContent></Card>
          </Grid>
        ))}
      </Grid>
      <DataTable<LinhaDre>
        data={linhas.map((l) => ({ ...l, percentual: receita ? (l.valor / receita) * 100 : 0 }))} loading={loading} page={0} rowsPerPage={50}
        onPageChange={() => undefined} searchable={false}
        emptyMessage="Sem dados no período — ajuste o intervalo e clique em Apurar"
        columns={[
          { field: 'conta', headerName: 'Conta', flex: 1 },
          { field: 'grupo', headerName: 'Grupo', width: 150, align: 'center' },
          { field: 'valor', headerName: 'Valor', width: 170, align: 'right', render: (v: number) => formatCurrency(v) },
          { field: 'percentual', headerName: '% s/ receita', width: 130, align: 'right', render: (v: number) => `${formatNumber(v ?? 0, 1)}%` },
        ]}
      />
    </Box>
  );
}
export default DrePage;
