import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip } from '@mui/material';
import { Add as AddIcon, Refresh as RefreshIcon, ReceiptLong as ReceiptLongIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDateTime, getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Mesa {
  id: string;
  numero?: string | number;
  nome?: string;
  status?: string;
}

interface Comanda {
  id: string;
  numero: string | number;
  mesa?: { id?: string; numero?: string | number; nome?: string };
  clienteNome?: string;
  clienteDocumento?: string;
  status?: string;
  valorTotal?: number;
  dataAbertura?: string;
}

const emptyForm = { numero: '', mesaId: '', clienteNome: '', clienteDocumento: '', observacoes: '' };

export function ComandasBuffetPage() {
  const [comandas, setComandas] = useState<Comanda[]>([]);
  const [mesas, setMesas] = useState<Mesa[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [dialog, setDialog] = useState(false);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiBuffet.listComandas({ page, size: rowsPerPage });
      const data = response.data;
      setComandas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setComandas([]);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  const loadMesas = useCallback(async () => {
    try {
      const { data } = await apiBuffet.listMesas({ size: 200 });
      setMesas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setMesas([]);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    loadMesas();
  }, [loadMesas]);

  const abrirComanda = async () => {
    if (!form.numero) {
      toast.warning('Informe o numero da comanda');
      return;
    }
    try {
      await apiBuffet.createComanda({
        numero: form.numero,
        mesa: form.mesaId ? { id: form.mesaId } : undefined,
        clienteNome: form.clienteNome || 'Consumidor',
        clienteDocumento: form.clienteDocumento || undefined,
        observacoes: form.observacoes,
        modalidade: 'POR_PESO',
      });
      toast.success('Comanda aberta com sucesso!');
      setDialog(false);
      setForm(emptyForm);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao abrir comanda');
    }
  };

  const fechar = async (id: string) => {
    try {
      await apiBuffet.fecharComanda(id);
      toast.success('Comanda fechada!');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao fechar');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Comandas - Buffet"
        subtitle="Gestao das comandas abertas no restaurante"
        icon={<ReceiptLongIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialog(true)}>
              Nova Comanda
            </Button>
          </Box>
        }
      />

      <DataTable<Comanda>
        data={comandas}
        loading={loading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage, newRowsPerPage) => {
          setPage(newPage);
          setRowsPerPage(newRowsPerPage);
        }}
        searchPlaceholder="Buscar comandas..."
        emptyMessage="Nenhuma comanda encontrada"
        columns={[
          { field: 'numero', headerName: 'Numero', width: 110 },
          { field: 'mesa', headerName: 'Mesa', width: 100, align: 'center', render: (value: any) => value?.numero ?? '-' },
          { field: 'clienteNome', headerName: 'Cliente', flex: 1 },
          {
            field: 'valorTotal',
            headerName: 'Total',
            width: 130,
            align: 'right',
            render: (value: number) => formatCurrency(value),
          },
          {
            field: 'status',
            headerName: 'Status',
            width: 130,
            align: 'center',
            render: (value: string) => (
              <Chip label={getStatusLabel(value)} size="small" color={getStatusColor(value)} />
            ),
          },
          {
            field: 'dataAbertura',
            headerName: 'Abertura',
            width: 170,
            render: (value: string) => formatDateTime(value),
          },
          {
            field: 'acoes',
            headerName: 'Acoes',
            width: 130,
            align: 'center',
            render: (_: any, row: Comanda) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Fechar comanda">
                  <span>
                    <Button
                      size="small"
                      variant="outlined"
                      color="success"
                      disabled={row.status === 'FECHADA' || row.status === 'PAGA'}
                      onClick={() => fechar(row.id)}
                    >
                      Fechar
                    </Button>
                  </span>
                </Tooltip>
              </Box>
            ),
          },
        ]}
      />

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Nova Comanda</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="No da Comanda"
                value={form.numero}
                onChange={(e) => setForm({ ...form, numero: e.target.value })}
                placeholder="Ex: 12"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                size="small"
                label="Mesa"
                value={form.mesaId}
                onChange={(e) => setForm({ ...form, mesaId: e.target.value })}
              >
                <MenuItem value="">Sem mesa</MenuItem>
                {mesas.map((m) => (
                  <MenuItem key={m.id} value={m.id}>
                    Mesa {m.numero} {m.nome ? `- ${m.nome}` : ''}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Nome do Cliente"
                value={form.clienteNome}
                onChange={(e) => setForm({ ...form, clienteNome: e.target.value })}
                placeholder="Ex: Joao da Silva"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="CPF do Cliente"
                value={form.clienteDocumento}
                onChange={(e) => setForm({ ...form, clienteDocumento: e.target.value })}
                placeholder="Ex: 000.000.000-00"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Observacoes"
                value={form.observacoes}
                onChange={(e) => setForm({ ...form, observacoes: e.target.value })}
                multiline
                rows={2}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={abrirComanda}>Abrir Comanda</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
export default ComandasBuffetPage;
