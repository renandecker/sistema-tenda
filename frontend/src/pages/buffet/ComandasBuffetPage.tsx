import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, TextField, Tooltip } from '@mui/material';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  ReceiptLong as ReceiptLongIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDateTime, getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Comanda {
  id: string;
  numero: string | number;
  mesaNumero?: string | number;
  clienteNome?: string;
  status?: string;
  valorTotal?: number;
  dataAbertura?: string;
}

const emptyForm = { numero: '', mesaNumero: '', clienteNome: '', observacoes: '' };

export function ComandasBuffetPage() {
  const [comandas, setComandas] = useState<Comanda[]>([]);
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

  useEffect(() => {
    load();
  }, [load]);

  const abrirComanda = async () => {
    if (!form.numero) {
      toast.warning('Informe o número da comanda');
      return;
    }
    try {
      await apiBuffet.createComanda({
        numero: form.numero,
        mesaNumero: form.mesaNumero || undefined,
        clienteNome: form.clienteNome || 'Consumidor',
        observacoes: form.observacoes,
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
        subtitle="Gestão das comandas abertas no restaurante"
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
          { field: 'numero', headerName: 'Número', width: 110 },
          { field: 'mesaNumero', headerName: 'Mesa', width: 100, align: 'center' },
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
            headerName: 'Ações',
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

      {/* Modal Nova Comanda */}
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Nova Comanda</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Nº da Comanda"
                value={form.numero}
                onChange={(e) => setForm({ ...form, numero: e.target.value })}
                placeholder="Ex: 12"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Nº da Mesa"
                value={form.mesaNumero}
                onChange={(e) => setForm({ ...form, mesaNumero: e.target.value })}
                placeholder="Ex: 4"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Nome do Cliente"
                value={form.clienteNome}
                onChange={(e) => setForm({ ...form, clienteNome: e.target.value })}
                placeholder="Ex: João da Silva"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Observações"
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
