import React, { useState, useEffect } from 'react';
import { Box, Button, Typography, IconButton, Tooltip, Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Grid, Select, MenuItem, FormControl, InputLabel, Switch, FormControlLabel, Alert, Snackbar } from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Search as SearchIcon, FilterList as FilterIcon, Download as DownloadIcon, Visibility as VisibilityIcon, Inventory as InventoryIcon, PriceChange as PriceChangeIcon, Print as PrintIcon } from '@mui/icons-material';
import { DataTable } from '@/components/DataTable';
import { apiFruteira } from '@/services/api';
import { useApiQuery, useApiMutation, useInvalidateQuery } from '@/hooks/useApi';
import { formatCurrency, formatNumber, getStatusColor, getStatusLabel } from '@/utils/formatters';
import { ProdutoFormDialog } from './components/ProdutoFormDialog';
import { PerdaRapidaDialog } from './components/PerdaRapidaDialog';
import { EtiquetaImpressaoDialog } from './components/EtiquetaImpressaoDialog';

interface Produto {
  id: string;
  codigo: string;
  codigoBarras: string;
  nome: string;
  tipo: string;
  unidadeMedida: string;
  precoVenda: number;
  precoAtacado?: number;
  custoMedio: number;
  estoqueAtual: number;
  estoqueMinimo: number;
  ativo: boolean;
  pesavelBalanca: boolean;
  controlaValidade: boolean;
}

const columns = [
  { field: 'codigo', headerName: 'Código', width: 100, sortable: true },
  { field: 'codigoBarras', headerName: 'Cód. Barras', width: 130, sortable: true },
  { field: 'nome', headerName: 'Nome', flex: 1, minWidth: 200, sortable: true },
  { field: 'tipo', headerName: 'Tipo', width: 110, sortable: true, render: (v) => <Chip label={v} size="small" /> },
  { field: 'unidadeMedida', headerName: 'Un.', width: 70, align: 'center' },
  { field: 'precoVenda', headerName: 'Preço Venda', width: 120, align: 'right', type: 'currency', sortable: true },
  { field: 'custoMedio', headerName: 'Custo Médio', width: 110, align: 'right', type: 'currency' },
  { field: 'estoqueAtual', headerName: 'Estoque', width: 100, align: 'right', type: 'number', sortable: true,
    render: (v, row) => <span style={{ color: v <= row.estoqueMinimo ? 'red' : 'inherit', fontWeight: v <= row.estoqueMinimo ? 'bold' : 'normal' }}>{formatNumber(v, 3)}</span>
  },
  { field: 'estoqueMinimo', headerName: 'Mínimo', width: 80, align: 'right', type: 'number' },
  { field: 'ativo', headerName: 'Status', width: 100, align: 'center', type: 'status', sortable: true },
  { field: 'pesavelBalanca', headerName: 'Balança', width: 80, align: 'center', type: 'boolean' },
];

export function ProdutosFruteiraPage() {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [searchTerm, setSearchTerm] = useState('');
  const [tipoFilter, setTipoFilter] = useState('');
  const [ativoFilter, setAtivoFilter] = useState('');
  const [sortConfig, setSortConfig] = useState({ field: 'nome', direction: 'asc' });
  const [openDialog, setOpenDialog] = useState(false);
  const [editingProduto, setEditingProduto] = useState<Produto | null>(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as const });

  const invalidateQuery = useInvalidateQuery(['fruteira', 'produtos']);

  const { data, isLoading, error } = useApiQuery(
    ['fruteira', 'produtos', { page, size: rowsPerPage, sortBy: sortConfig.field, sortDirection: sortConfig.direction, tipo: tipoFilter, ativo: ativoFilter, search: searchTerm }],
    () => apiFruteira.listProdutos({ page, size: rowsPerPage, sortBy: sortConfig.field, sortDirection: sortConfig.direction, tipo: tipoFilter || undefined, ativo: ativoFilter !== '' ? ativoFilter === 'true' : undefined }).then(r => r.data),
    { enabled: true }
  );

  const createMutation = useApiMutation(['fruteira', 'produtos'], (d: any) => apiFruteira.createProduto(d).then(r => r.data), { onSuccess: () => { closeDialog(); showSnackbar('Produto criado com sucesso!', 'success'); } });
  const updateMutation = useApiMutation(['fruteira', 'produtos'], (d: any) => apiFruteira.updateProduto(editingProduto!.id, d).then(r => r.data), { onSuccess: () => { closeDialog(); showSnackbar('Produto atualizado com sucesso!', 'success'); } });
  const deleteMutation = useApiMutation(['fruteira', 'produtos'], (id: string) => apiFruteira.deleteProduto(id).then(r => r.data), { onSuccess: () => showSnackbar('Produto excluído com sucesso!', 'success') });

  const handleSort = (field: string, direction: 'asc' | 'desc') => setSortConfig({ field, direction });
  const handlePageChange = (newPage: number, newRowsPerPage: number) => { setPage(newPage); setRowsPerPage(newRowsPerPage); };
  const handleDelete = (id: string) => { if (window.confirm('Tem certeza que deseja excluir este produto?')) deleteMutation.mutate(id); };

  const openCreateDialog = () => { setEditingProduto(null); setOpenDialog(true); };
  const openEditDialog = (produto: Produto) => { setEditingProduto(produto); setOpenDialog(true); };
  const closeDialog = () => { setOpenDialog(false); setEditingProduto(null); };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info') => setSnackbar({ open: true, message, severity });

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={600}>Produtos - Fruteira</Typography>
          <Typography variant="body1" color="text.secondary">Cadastro e gestão de produtos do hortifrúti</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Importar"><IconButton><DownloadIcon /></IconButton></Tooltip>
          <Tooltip title="Etiquetas Balança"><IconButton onClick={() => setOpenDialog(true)}><PrintIcon /></IconButton></Tooltip>
          <Tooltip title="Perda Rápida"><IconButton onClick={() => setOpenDialog(true)}><InventoryIcon color="error" /></IconButton></Tooltip>
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreateDialog}>Novo Produto</Button>
        </Box>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <Box sx={{ flexGrow: 1, minWidth: 250 }}>
          <TextField
            placeholder="Buscar por nome, código ou código de barras..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            size="small"
            InputProps={{ startAdornment: <SearchIcon color="action" sx={{ mr: 1 }} /> }}
          />
        </Box>
        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel>Tipo</InputLabel>
          <Select value={tipoFilter} label="Tipo" onChange={(e) => setTipoFilter(e.target.value)}>
            <MenuItem value="">Todos</MenuItem>
            <MenuItem value="FRUTA">Fruta</MenuItem>
            <MenuItem value="VERDURA">Verdura</MenuItem>
            <MenuItem value="LEGUME">Legume</MenuItem>
            <MenuItem value="SECO">Seco/Mercearia</MenuItem>
            <MenuItem value="BEBIDA">Bebida</MenuItem>
            <MenuItem value="OUTROS">Outros</MenuItem>
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 130 }}>
          <InputLabel>Status</InputLabel>
          <Select value={ativoFilter} label="Status" onChange={(e) => setAtivoFilter(e.target.value)}>
            <MenuItem value="">Todos</MenuItem>
            <MenuItem value="true">Ativos</MenuItem>
            <MenuItem value="false">Inativos</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <DataTable<Produto>
        data={data?.content || data || []}
        columns={columns}
        loading={isLoading}
        pagination
        page={page}
        rowsPerPage={rowsPerPage}
        rowsPerPageOptions={[10, 20, 50, 100]}
        onPageChange={handlePageChange}
        onSortChange={handleSort}
        onRowClick={(row) => openEditDialog(row)}
        searchable={false}
        emptyMessage="Nenhum produto encontrado"
      />

      <ProdutoFormDialog open={openDialog && !editingProduto} onClose={closeDialog} onSubmit={createMutation.mutate} loading={createMutation.isPending} />
      <ProdutoFormDialog open={openDialog && !!editingProduto} onClose={closeDialog} initialData={editingProduto} onSubmit={updateMutation.mutate} loading={updateMutation.isPending} />
      
      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar({...snackbar, open: false})}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({...snackbar, open: false})}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}