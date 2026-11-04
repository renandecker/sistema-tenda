import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  Inventory2 as Inventory2Icon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiEstoque } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';
import { maskCurrency, maskQuantity, parseCurrency, parseDecimal } from '@/utils/masks';

interface ProdutoEstoque {
  id: string;
  codigo?: string;
  codigoBarras?: string;
  nome: string;
  descricao?: string;
  categoria?: string;
  unidadeMedida?: string;
  custoMedio?: number;
  estoqueAtual?: number;
  estoqueMinimo?: number;
  estoqueMaximo?: number;
  ativo?: boolean;
  observacoes?: string;
}

const CATEGORIAS: { value: string; label: string }[] = [
  { value: 'MATERIA_PRIMA', label: 'Matéria-prima' },
  { value: 'EMBALAGEM', label: 'Embalagem' },
  { value: 'DESCARTAVEL', label: 'Descartável' },
  { value: 'LIMPEZA', label: 'Limpeza' },
  { value: 'OUTROS', label: 'Outros' },
];

const UNIDADES_MEDIDA = ['KG', 'G', 'UN', 'L', 'ML', 'PC', 'CX', 'FD', 'KT', 'MT', 'M2', 'M3'];

const emptyForm = {
  codigoBarras: '',
  nome: '',
  descricao: '',
  categoria: 'MATERIA_PRIMA',
  unidadeMedida: 'UN',
  custoMedio: '',
  estoqueAtual: '',
  estoqueMinimo: '',
  estoqueMaximo: '',
  observacoes: '',
};

export function ProdutosEstoquePage() {
  const [produtos, setProdutos] = useState<ProdutoEstoque[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [total, setTotal] = useState(0);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<ProdutoEstoque | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiEstoque.listProdutos({ page, size: rowsPerPage });
      const data = response.data;
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
      setTotal(data?.totalElements ?? (Array.isArray(data) ? data.length : 0));
    } catch {
      setProdutos([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  useEffect(() => {
    load();
  }, [load]);

  const openNew = () => {
    setEditing(null);
    setForm(emptyForm);
    setDialog(true);
  };

  const openEdit = (p: ProdutoEstoque) => {
    setEditing(p);
    setForm({
      codigoBarras: p.codigoBarras ?? '',
      nome: p.nome,
      descricao: p.descricao ?? '',
      categoria: p.categoria ?? 'MATERIA_PRIMA',
      unidadeMedida: p.unidadeMedida ?? 'UN',
      custoMedio: String(p.custoMedio ?? ''),
      estoqueAtual: String(p.estoqueAtual ?? ''),
      estoqueMinimo: String(p.estoqueMinimo ?? ''),
      estoqueMaximo: String(p.estoqueMaximo ?? ''),
      observacoes: p.observacoes ?? '',
    });
    setDialog(true);
  };

  const salvar = async () => {
    if (!form.nome) {
      toast.warning('Informe o nome do produto');
      return;
    }
    try {
      const payload = {
        codigoBarras: form.codigoBarras || undefined,
        nome: form.nome,
        descricao: form.descricao || undefined,
        categoria: form.categoria,
        unidadeMedida: form.unidadeMedida,
        custoMedio: form.custoMedio ? parseCurrency(form.custoMedio) : 0,
        estoqueAtual: form.estoqueAtual ? parseDecimal(form.estoqueAtual, 3) : 0,
        estoqueMinimo: form.estoqueMinimo ? parseDecimal(form.estoqueMinimo, 3) : undefined,
        estoqueMaximo: form.estoqueMaximo ? parseDecimal(form.estoqueMaximo, 3) : undefined,
        ativo: true,
        observacoes: form.observacoes || undefined,
      };

      if (editing) {
        await apiEstoque.updateProduto(editing.id, payload);
        toast.success('Produto de estoque atualizado com sucesso!');
      } else {
        await apiEstoque.createProduto(payload);
        toast.success('Produto de estoque cadastrado com sucesso!');
      }

      setDialog(false);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar produto de estoque');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Tem certeza que deseja excluir este produto de estoque?')) return;
    try {
      await apiEstoque.removeProduto(id);
      toast.success('Produto de estoque excluído');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir produto de estoque');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Produtos de Estoque"
        subtitle="Cadastro de matéria-prima e insumos utilizados no buffet e na operação"
        icon={<Inventory2Icon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>
              Novo Produto
            </Button>
          </Box>
        }
      />

      <DataTable<ProdutoEstoque>
        data={produtos}
        loading={loading}
        serverSide
        totalCount={total}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage, newRowsPerPage) => {
          setPage(newPage);
          setRowsPerPage(newRowsPerPage);
        }}
        searchPlaceholder="Buscar produtos de estoque..."
        emptyMessage="Nenhum produto de estoque cadastrado"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 110 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          {
            field: 'categoria',
            headerName: 'Categoria',
            width: 150,
            render: (value: string) => CATEGORIAS.find((c) => c.value === value)?.label ?? value ?? '-',
          },
          {
            field: 'custoMedio',
            headerName: 'Custo médio',
            width: 130,
            align: 'right',
            render: (value: number) => formatCurrency(value ?? 0),
          },
          {
            field: 'estoqueAtual',
            headerName: 'Estoque',
            width: 120,
            align: 'right',
            render: (value: number, row: ProdutoEstoque) => {
              const baixo = (row.estoqueMinimo ?? 0) > 0 && (value ?? 0) <= (row.estoqueMinimo ?? 0);
              return (
                <Typography component="span" fontWeight={baixo ? 700 : 400} color={baixo ? 'error.main' : 'inherit'}>
                  {formatNumber(value ?? 0, 3)}
                </Typography>
              );
            },
          },
          {
            field: 'ativo',
            headerName: 'Status',
            width: 100,
            align: 'center',
            render: (value: boolean) => (
              <Chip
                label={value !== false ? 'Ativo' : 'Inativo'}
                size="small"
                color={value !== false ? 'success' : 'default'}
              />
            ),
          },
          {
            field: 'acoes',
            headerName: 'Ações',
            width: 120,
            align: 'center',
            render: (_: any, row: ProdutoEstoque) => (
              <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                <Tooltip title="Editar">
                  <IconButton size="small" onClick={() => openEdit(row)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Excluir">
                  <IconButton size="small" color="error" onClick={() => excluir(row.id)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>
            ),
          },
        ]}
      />

      {!loading && produtos.length === 0 && (
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
          Cadastre aqui as matérias-primas e insumos. Os produtos do buffet não controlam mais estoque.
        </Typography>
      )}

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar Produto de Estoque' : 'Novo Produto de Estoque'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Nome do Produto"
                value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
                placeholder="Ex: Arroz tipo 1"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                size="small"
                label="Categoria"
                value={form.categoria}
                onChange={(e) => setForm({ ...form, categoria: e.target.value })}
              >
                {CATEGORIAS.map((c) => (
                  <MenuItem key={c.value} value={c.value}>{c.label}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField
                select
                fullWidth
                size="small"
                label="Unidade de Medida"
                value={form.unidadeMedida}
                onChange={(e) => setForm({ ...form, unidadeMedida: e.target.value })}
              >
                {UNIDADES_MEDIDA.map((u) => (
                  <MenuItem key={u} value={u}>{u}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Custo Médio (R$)"
                value={maskCurrency(form.custoMedio)}
                onChange={(e) => setForm({ ...form, custoMedio: maskCurrency(e.target.value) })}
                placeholder="0,00"
                inputMode="decimal"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Código de Barras (opcional)"
                value={form.codigoBarras}
                onChange={(e) => setForm({ ...form, codigoBarras: e.target.value })}
                placeholder="Ex: 7891020304050"
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                fullWidth
                size="small"
                label="Estoque Atual"
                value={maskQuantity(form.estoqueAtual)}
                onChange={(e) => setForm({ ...form, estoqueAtual: maskQuantity(e.target.value) })}
                placeholder="0,000"
                inputMode="decimal"
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                fullWidth
                size="small"
                label="Estoque Mínimo"
                value={maskQuantity(form.estoqueMinimo)}
                onChange={(e) => setForm({ ...form, estoqueMinimo: maskQuantity(e.target.value) })}
                placeholder="0,000"
                inputMode="decimal"
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                fullWidth
                size="small"
                label="Estoque Máximo"
                value={maskQuantity(form.estoqueMaximo)}
                onChange={(e) => setForm({ ...form, estoqueMaximo: maskQuantity(e.target.value) })}
                placeholder="0,000"
                inputMode="decimal"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Descrição (opcional)"
                value={form.descricao}
                onChange={(e) => setForm({ ...form, descricao: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Observações (opcional)"
                value={form.observacoes}
                onChange={(e) => setForm({ ...form, observacoes: e.target.value })}
              />
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

export default ProdutosEstoquePage;
