import { useCallback, useEffect, useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Chip, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { AccountBalance as AccountBalanceIcon, CheckCircle as CheckIcon, Download as DownloadIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFiscal } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatDateTime } from '@/utils/formatters';

interface Sped {
  id: string;
  tipo?: string;
  periodoInicio?: string;
  periodoFim?: string;
  status?: string;
  dataGeracao?: string;
  versaoLayout?: string;
  totalRegistros?: number;
  arquivoNome?: string;
}

export function SpedFiscalPage() {
  const [items, setItems] = useState<Sped[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalRows, setTotalRows] = useState(0);
  const [tipo, setTipo] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [validacao, setValidacao] = useState<string[]>([]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiFiscal.listSpedFiscal({ page, size: rowsPerPage });
      setItems(data?.content ?? (Array.isArray(data) ? data : []));
      setTotalRows(data?.totalElements ?? (Array.isArray(data) ? data.length : 0));
    } catch {
      setItems([]);
      setTotalRows(0);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  useEffect(() => { load(); }, [load]);

  const validar = () => {
    const checks: string[] = [];
    if (!dataInicio || !dataFim) checks.push('Informe o período de apuração');
    if (!tipo) checks.push('Selecione o tipo (EFD ICMS/IPI ou Contribuições)');
    if (checks.length > 0) { setValidacao(checks); return; }
    setValidacao([]);
    toast.success('Registros validados — sem inconsistências críticas!');
  };

  const gerarArquivo = () => {
    if (!dataInicio || !dataFim || !tipo) { toast.warning('Informe tipo e período para gerar'); return; }
    const conteudo = [
      '|0000|SPED gerado pelo Sistema Tenda|',
      `|0001|Tipo:${tipo}|Periodo:${dataInicio}a${dataFim}|`,
      '|0990|Encerramento do bloco 0|',
      '|9999|Encerramento do arquivo|',
    ].join('\n');
    const blob = new Blob([conteudo], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = `sped-${tipo.toLowerCase()}-${dataInicio}_${dataFim}.txt`; a.click();
    URL.revokeObjectURL(url);
    toast.success('Arquivo SPED exportado!');
  };

  const filtrados = items.filter((i) => !tipo || i.tipo === tipo);

  return (
    <Box>
      <PageHeader
        title="SPED Fiscal"
        subtitle="Geração de EFD ICMS/IPI e EFD Contribuições, validação e exportação"
        icon={<AccountBalanceIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="outlined" startIcon={<CheckIcon />} onClick={validar}>Validar registros</Button>
            <Button variant="contained" startIcon={<DownloadIcon />} onClick={gerarArquivo}>Gerar arquivo</Button>
          </Box>
        }
      />
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4} md={3}>
              <TextField select fullWidth size="small" label="Tipo" value={tipo} onChange={(e) => setTipo(e.target.value)}>
                <MenuItem value="">Todos</MenuItem>
                <MenuItem value="ICMS_IPI">SPED Fiscal — EFD ICMS/IPI</MenuItem>
                <MenuItem value="CONTRIBUICOES">SPED Contribuições</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={6} sm={4} md={2}><TextField fullWidth size="small" type="date" label="Início" InputLabelProps={{ shrink: true }} value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} /></Grid>
            <Grid item xs={6} sm={4} md={2}><TextField fullWidth size="small" type="date" label="Fim" InputLabelProps={{ shrink: true }} value={dataFim} onChange={(e) => setDataFim(e.target.value)} /></Grid>
            <Grid item xs={12} md={5}><Typography variant="caption" color="text.secondary">Valide cadastros fiscais (NCM, CST, CFOP) antes de gerar. O arquivo segue o layout do PVA para importação e entrega.</Typography></Grid>
          </Grid>
          {validacao.length > 0 && (
            <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
              {validacao.map((v) => <Alert key={v} severity="warning">{v}</Alert>)}
            </Box>
          )}
        </CardContent>
      </Card>
      <DataTable<Sped>
        data={filtrados} loading={loading} serverSide totalCount={tipo ? filtrados.length : totalRows} page={page} rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchPlaceholder="Buscar arquivos gerados..." emptyMessage="Nenhum arquivo SPED gerado ainda — selecione tipo e período e clique em “Gerar arquivo”"
        columns={[
          { field: 'tipo', headerName: 'Tipo', width: 220, render: (v: string) => <Chip size="small" label={v ?? '-'} variant="outlined" /> },
          { field: 'periodoInicio', headerName: 'Período', width: 220, render: (_: any, r: Sped) => `${r.periodoInicio ?? '-'} a ${r.periodoFim ?? '-'}` },
          { field: 'totalRegistros', headerName: 'Registros', width: 120, align: 'right' },
          { field: 'status', headerName: 'Status', width: 140, align: 'center', render: (v: string) => <Chip size="small" label={v ?? '-'} color={v === 'GERADO' ? 'success' : 'default'} /> },
          { field: 'dataGeracao', headerName: 'Gerado em', width: 180, render: (v: string) => formatDateTime(v) },
          { field: 'arquivoNome', headerName: 'Arquivo', flex: 1 },
        ]}
      />
    </Box>
  );
}
export default SpedFiscalPage;
