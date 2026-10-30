import { useCallback, useEffect, useState } from 'react';
import { Box, Card, CardContent, Chip, Grid, Typography, Button, IconButton, Tooltip, Dialog, DialogActions, DialogContent, DialogTitle, TextField } from '@mui/material';
import { Add as AddIcon, Refresh as RefreshIcon, People as PeopleIcon, EmojiEvents as TrophyIcon, AttachMoney as MoneyIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet, apiCaderno } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDateTime } from '@/utils/formatters';

interface Cliente {
  id: string;
  nome?: string;
  cpf?: string;
  telefone?: string;
  totalVisitas?: number;
  totalConsumido?: number;
  primeiraVisita?: string;
  ultimaVisita?: string;
  observacoes?: string;
}

const emptyForm = { nome: '', cpf: '', telefone: '', observacoes: '' };

const docKey = (doc?: string) => (doc ?? '').replace(/\D/g, '');

export function ClientesBuffetPage() {
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [frequentes, setFrequentes] = useState<Cliente[]>([]);
  const [consumo, setConsumo] = useState<Cliente[]>([]);
  const [resumo, setResumo] = useState<{ totalClientes: number; totalConsumido: number }>({ totalClientes: 0, totalConsumido: 0 });
  const [dividas, setDividas] = useState<Record<string, { total: number; atraso: boolean }>>({});
  const [totalReceber, setTotalReceber] = useState(0);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalRows, setTotalRows] = useState(0);
  const [dialog, setDialog] = useState(false);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [listRes, freqRes, consRes, resumoRes] = await Promise.all([
        apiBuffet.listClientes({ page, size: rowsPerPage }),
        apiBuffet.rankingFrequentes(5),
        apiBuffet.rankingConsumo(5),
        apiBuffet.resumoClientes(),
      ]);
      setClientes(Array.isArray(listRes.data) ? listRes.data : (listRes.data?.content ?? []));
      setTotalRows(listRes.data?.totalElements ?? (Array.isArray(listRes.data) ? listRes.data.length : 0));
      setFrequentes(Array.isArray(freqRes.data) ? freqRes.data : (freqRes.data?.content ?? []));
      setConsumo(Array.isArray(consRes.data) ? consRes.data : (consRes.data?.content ?? []));
      setResumo(resumoRes.data ?? { totalClientes: 0, totalConsumido: 0 });
    } catch {
      setClientes([]);
      setTotalRows(0);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadDividas = useCallback(async () => {
    try {
      const { data } = await apiCaderno.clientesComDivida({ page: 0, size: 1000 });
      const map: Record<string, { total: number; atraso: boolean }> = {};
      let total = 0;
      (Array.isArray(data) ? data : (data?.content ?? [])).forEach((d: any) => {
        const key = docKey(d.clienteDocumento);
        const valor = Number(d.totalDivida ?? 0);
        if (!key || valor <= 0) return;
        map[key] = { total: valor, atraso: Boolean(d.possuiAtraso) };
        total += valor;
      });
      setDividas(map);
      setTotalReceber(total);
    } catch {
      setDividas({});
      setTotalReceber(0);
    }
  }, []);

  const atualizar = () => { load(); loadDividas(); };

  useEffect(() => { load(); loadDividas(); }, [load, loadDividas]);

  const salvar = async () => {
    if (!form.nome || !form.cpf) { toast.warning('Informe nome e CPF'); return; }
    try {
      await apiBuffet.createCliente(form);
      toast.success('Cliente cadastrado!');
      setDialog(false);
      setForm(emptyForm);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao cadastrar');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Clientes - Buffet"
        subtitle="Cadastro e analise de clientes"
        icon={<PeopleIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={atualizar} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setForm(emptyForm); setDialog(true); }}>
              Novo Cliente
            </Button>
          </Box>
        }
      />

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">Total de Clientes</Typography>
              <Typography variant="h4">{resumo.totalClientes ?? clientes.length}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">Total Consumido</Typography>
              <Typography variant="h4">{formatCurrency(resumo.totalConsumido ?? 0)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">Ticket Medio por Cliente</Typography>
              <Typography variant="h4">{formatCurrency((resumo.totalClientes ?? 0) > 0 ? (resumo.totalConsumido ?? 0) / (resumo.totalClientes ?? 1) : 0)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={3}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">Total a Receber (Caderno)</Typography>
              <Typography variant="h4" color={totalReceber > 0 ? 'warning.main' : 'text.primary'}>
                {formatCurrency(totalReceber)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <TrophyIcon color="primary" />
                <Typography variant="h6">Clientes Mais Frequentes</Typography>
              </Box>
              {frequentes.map((c, i) => (
                <Box key={c.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="body2">{i + 1}. {c.nome}</Typography>
                  <Typography variant="body2" fontWeight={600}>{c.totalVisitas ?? 0} visitas</Typography>
                </Box>
              ))}
              {frequentes.length === 0 && <Typography variant="body2" color="text.secondary">Sem dados</Typography>}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <MoneyIcon color="primary" />
                <Typography variant="h6">Maiores Consumidores</Typography>
              </Box>
              {consumo.map((c, i) => (
                <Box key={c.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="body2">{i + 1}. {c.nome}</Typography>
                  <Typography variant="body2" fontWeight={600}>{formatCurrency(c.totalConsumido ?? 0)}</Typography>
                </Box>
              ))}
              {consumo.length === 0 && <Typography variant="body2" color="text.secondary">Sem dados</Typography>}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <DataTable<Cliente>
        data={clientes}
        loading={loading}
        serverSide
        totalCount={totalRows}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage, newRowsPerPage) => {
          setPage(newPage);
          setRowsPerPage(newRowsPerPage);
        }}
        searchPlaceholder="Buscar clientes..."
        emptyMessage="Nenhum cliente cadastrado"
        columns={[
          { field: 'nome', headerName: 'Nome', flex: 1 },
          { field: 'cpf', headerName: 'CPF', width: 140 },
          { field: 'telefone', headerName: 'Telefone', width: 140 },
          { field: 'totalVisitas', headerName: 'Visitas', width: 90, align: 'center' },
          { field: 'totalConsumido', headerName: 'Total Consumido', width: 150, align: 'right', render: (v: number) => formatCurrency(v) },
          {
            field: 'divida',
            headerName: 'Devendo',
            width: 150,
            align: 'right',
            render: (_: any, row: Cliente) => {
              const d = dividas[docKey(row.cpf)];
              if (!d || d.total <= 0) {
                return <Typography variant="body2" color="text.secondary">—</Typography>;
              }
              return <Chip size="small" color={d.atraso ? 'error' : 'warning'} label={formatCurrency(d.total)} />;
            },
          },
          { field: 'ultimaVisita', headerName: 'Ultima Visita', width: 170, render: (v: string) => (v ? formatDateTime(v) : '-') },
        ]}
      />

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Novo Cliente</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField fullWidth size="small" label="Nome" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth size="small" label="CPF" value={form.cpf} onChange={(e) => setForm({ ...form, cpf: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth size="small" label="Telefone" value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth size="small" label="Observacoes" value={form.observacoes} onChange={(e) => setForm({ ...form, observacoes: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={salvar}>Salvar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
export default ClientesBuffetPage;
