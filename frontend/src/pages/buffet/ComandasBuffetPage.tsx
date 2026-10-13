import React, { useState, useEffect } from 'react';
import {
  Box, Button, Typography, Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Grid, Select, MenuItem, FormControl, InputLabel, Switch, FormControlLabel, Alert, Snackbar,
  Paper, TableContainer, Table, TableHead, TableBody, TableRow, IconButton,
  AddIcon, EditIcon, DeleteIcon, VisibilityIcon as Vis, VisibilityIcon as VisOff, RefreshIcon,
  FormatListNumbered as FormatIcon, Settings as SettingsIcon
} from '@mui/material';
import { apiBuffet, apiPdv, apiFruteira } from '@/services/api';
import { useApiQuery, useApiMutation, useInvalidateQuery } from '@/hooks/useApi';
import { useAuth } from '@/hooks/useAuth';
import { formatCurrency, formatNumber } from '@/utils/formatters';
import { PerdaRapidaDialog } from '@/components/PerdaRapidaDialog';
import { ComandaFormDialog } from '@/components/ComandaFormDialog';

export function ComandasBuffetPage() {
  const { user, hasPermission } = useAuth();
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ABERTA,EM_USO');
  const [tipoFilter, setTipoFilter] = useState('PRATO_BUFFET');
  const [openDialog, setOpenDialog] = useState(false);
  const [editingComanda, setEditingComanda] = useState<any | null>(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as const });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [itemDelete, setItemDelete] = useState<string | null>(null);
  const [fecharDialogOpen, setFecharDialogOpen] = useState(false);
  const [fecharRequest, setFecharRequest] = useState({ valorFinalContado: 0, motivo: '' });
  const [iniciarBalancaDialog, setIniciarBalancaDialog] = useState(false);
  const [aplicarDescontoDialog, setAplicarDescontoDialog] = useState(false);
  const [descontoRequest, setDescontoRequest] = useState({ valor: 0 });

  const invalidateQuery = useInvalidateQuery(['buffet', 'comandas']);
  const { data, isLoading, error } = useApiQuery(
    ['buffet', 'comandas', { page, size: rowsPerPage, sortBy: 'dataAbertura', sortDirection: 'desc', status: statusFilter, tipo: tipoFilter }],
    () => apiBuffet.listComandas({ page, size: rowsPerPage, sortBy: 'dataAbertura', sortDirection: 'desc', status: statusFilter, tipo: tipoFilter }),
    { enabled: true }
  );

  const createMutation = useApiMutation(['buffet', 'comandas'], (d: any) => apiBuffet.createComanda(d).then(r => r.data), { onSuccess: () => showSnackbar('Comanda criada com sucesso!', 'success') });
  const updateMutation = useApiMutation(['buffet', 'comandas'], (d: any) => apiBuffet.updateComanda(editingComanda!.id, d).then(r => r.data), { onSuccess: () => showSnackbar('Comanda atualizada com sucesso!', 'success') });
  const cancelMutation = useApiMutation(['buffet', 'comandas'], (id: string) => apiBuffet.cancelarComanda(id).then(r => r.data), { onSuccess: () => showSnackbar('Comanda cancelada!', 'success') });
  const deleteMutation = useApiMutation(['buffet', 'comandas'], (id: string) => apiBuffet.deleteComanda(id).then(r => r.data), { onSuccess: () => showSnackbar('Comanda excluída!', 'success') });
  const closeDialog = () => { setOpenDialog(false); setEditingComanda(null); };

  const handleSort = (field: string, direction: 'asc' | 'desc') => {
    // Handle sort
  };

  const handlePageChange = (newPage: number, newRowsPerPage: number) => { setPage(newPage); setRowsPerPage(newRowsPerPage); };

  const handleDelete = (id: string) => { setItemDelete(id); setDeleteDialogOpen(true); };

  // Função para processar pagamento/fechamento
  const processarPagamento = async () => {
    if (!editingComanda) return;
    setFecharDialogOpen(true);
  };

  const formatStatus = (status: string) => {
    const labels: Record<string, string> = {
      ABERTA: 'Aberta',
      EM_USO: 'Em Uso',
      FECHADA: 'Fechada',
      PAGA: 'Paga',
      CANCELADA: 'Cancelada',
    };
    return labels[status] || status;
  };

  const formatTipo = (tipo: string) => {
    const labels: Record<string, string> = {
      PRATO_BUFFET: 'Prato Buffet',
      LIVRE: 'Buffet Livre',
      PRATO_FEITO: 'Prato Feito',
      EXECUTIVO: 'Executivo',
    };
    return labels[tipo] || tipo;
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => setSnackbar({ open: true, message, severity });

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={600}>Comandas - Buffet</Typography>
          <Typography variant="body1" color="text.secondary">
            Gestão de comandas do restaurante por quilo/livre
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Nova Comanda">
            <Button variant="contained" startIcon={<AddIcon />}>
              Nova Comanda
            </Button>
          </Tooltip>
          <Tooltip title="Fechamento do Caixa">
            <Button variant="outlined" startIcon={<AttachMoneyIcon />}>Fechamento</Button>
          </Tooltip>
        </Box>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <Box sx={{ flexGrow: 1, minWidth: 250 }}>
          <TextField
            placeholder="Buscar por cliente, número da mesa ou status..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            size="small"
            InputProps={{ startAdornment: <SearchIcon color="action" sx={{ mr: 1 }} /> }}
          />
        </Box>
        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel>Status</InputLabel>
          <Select value={statusFilter} label="Status" onChange={(e) => setStatusFilter(e.target.value)}>
            <MenuItem value="ABERTA,EM_USO">Abertas/Em Uso</MenuItem>
            <MenuItem value="FECHADA,PAGA">Fechadas/Pagas</MenuItem>
            <MenuItem value="CANCELADA">Canceladas</MenuItem>
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel>Tipo</InputLabel>
          <Select value={tipoFilter} label="Tipo" onChange={(e) => setTipoFilter(e.target.value)}>
            <MenuItem value="">Todos</MenuItem>
            <MenuItem value="PRATO_BUFFET">Prato Buffet</MenuItem>
            <MenuItem value="LIVRE">Buffet Livre</MenuItem>
            <MenuItem value="PRATO_FEITO">Prato Feito</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <DataTable<any>
        data={data?.content || data || []}
        columns={[
          { field: 'numero', headerName: 'N° Comanda', width: 100, sortable: true },
          { field: 'clienteNome', headerName: 'Cliente', width: 130, sortable: true },
          { field: 'mesaNumero', headerName: 'Mesa', width: 70, align: 'center', sortable: true },
          { field: 'modalidade', headerName: 'Modalidade', width: 110, align: 'center', type: 'text', sortable: true, render: (v) => formatTipo(v) },
          { field: 'status', headerName: 'Status', width: 100, align: 'center', type: 'status', sortable: true, render: (v) => formatStatus(v) },
          { field: 'subtotal', headerName: 'Subtotal', width: 110, align: 'right', type: 'currency', sortable: true },
          { field: 'actions', headerName: 'Ações', width: 160, align: 'center',
            render: (value, row: any) => (
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button size="small" variant="outlined" onClick={() => openEditDialog(row)}>
                  <EditIcon /> Editar
                </Button>
                <Button size="small" variant="outlined" color="error" onClick={() => handleDelete(row.id)}>
                  <DeleteIcon /> Excluir
                </Button>
                {row.status !== 'FECHADA' && row.status !== 'PAGA' && (
                  <>
                    <Button size="small" variant="outlined" color="secondary" onClick={() => processarPagamento(row.id)}>
                      <Paperclip /> Fechar
                    </Button>
                    <Button size="small" variant="outlined" color="warning" onClick={() => abrirFecharDialog(row)}>
                      <RefreshIcon /> Reabrir
                    </Button>
                    <Button size="small" variant="outlined" color="error" onClick={() => cancelarComanda(row.id, 'Excluir via PDV')}>
                      <DeleteIcon /> Cancelar
                    </Button>
                  </>
                )}
                {row.status === 'ABERTA' || row.status === 'EM_USO' && (
                  <>
                    <Button size="small" variant="outlined" color="primary" onClick={() => iniciarBalancaDialog(row.id)}>
                      <ScaleIcon /> Balança
                    </Button>
                    <Button size="small" variant="outlined" color="secondary" onClick={() => setAplicarDescontoDialog(true)}>
                      <FormatPercentIcon /> Desconto
                    </Button>
                  </>
                )}
              </Box>
            )
          },
        ]}
        loading={isLoading}
        pagination
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[10, 20, 50, 100]}
        onPageChange={handlePageChange}
        onSortChange={handleSort}
        emptyMessage="Nenhuma comanda encontrada"
      />

      <ComandaFormDialog open={openDialog && !editingComanda} onClose={closeDialog} title="Nova Comanda" onSubmit={createMutation.mutate} />
      <ComandaFormDialog open={openDialog && !!editingComanda} onClose={closeDialog} title="Editar Comanda" initialData={editingComanda} onSubmit={updateMutation.mutate} />

      <DeleteDialog open={deleteDialogOpen} onConfirm={() => deleteMutation.mutate(deleteDialogOpen && deleteDialogOpen.target?.dataset?.id)} onClose={() => setDeleteDialogOpen(false)} />

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar({...snackbar, open: false})}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({...snackbar, open: false})}>
          {snackbar.message}
        </Alert>
      </Snackbar>

      {/* Diálogo de Fechar Caixa */}
      <Dialog open={fecharDialogOpen} onClose={() => setFecharDialogOpen(false)} aria-labelledby="alert-dialog-title" maxWidth="sm">
        <DialogTitle id="alert-dialog-title">Fechar Caixa</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1, pb: 2 }}>
            <Typography variant="h6">Informe o valor final do caixa</Typography>
            <Divider />
            <Grid container spacing={1}>
              <Grid item xs>
                <TextField
                  fullWidth
                  label="Valor Contado"
                  type="number"
                  inputProps={{ min: 0 }}
                  onChange={(e) => setFecharRequest(prev => ({ ...prev, valorFinalContado: Number(e.target.value) }))}
                />
              </Grid>
              <Grid item xs>
                <TextField
                  fullWidth
                  label="Motivo"
                  multiline
                  rowsMaxify
                  rows={3}
                  onChange={(e) => setFecharRequest(prev => ({ ...prev, motivo: e.target.value }))}
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFecharDialogOpen(false)} variant="outlined">Cancelar</Button>
          <Button variant="contained" color="primary" onClick={() => {
            if (editingComanda) {
              // Fechar comanda com pagamento unificado
              apiBuffet.fecharComanda(editingComanda.id).then(r => {
                showSnackbar('Comanda fechada com sucesso!', 'success');
                closeDialog();
                setFecharDialogOpen(false);
                invalidateQuery();
              }).catch(err => showSnackbar(err.response?.data?.message || 'Erro ao fechar', 'error'));
            }
          }}>Fechar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Diálogo de Iniciar Balança */}
      <Dialog open={iniciarBalancaDialog} onClose={() => setIniciarBalancaDialog(false)} aria-labelledby="alert-dialog-title" maxWidth="sm">
        <DialogTitle id="alert-dialog-title">Iniciar Pesagem</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1, pb: 2 }}>
            <Typography variant="h6">Comanda: {editingComanda?.numero}</Typography>
            <Divider />
            <TextField fullWidth label="Peso Bruto (kg)" type="number" inputProps={{ min: 0 }} />
            <TextField fullWidth label="Tara (kg)" type="number" inputProps={{ min: 0 }} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIniciarBalancaDialog(false)} variant="outlined">Cancelar</Button>
          <Button variant="contained" color="primary" onClick={() => setIniciarBalancaDialog(false)}>
            <RefreshIcon /> Iniciar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}