import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  LocalGroceryStore as LocalGroceryStoreIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface Produto {
  id: string;
  codigo?: string;
  codigoBarras?: string;
  nome: string;
  categoria?: string;
  precoVenda?: number;
  custoMedio?: number;
  estoqueAtual?: number;
  estoqueMinimo?: number;
  unidade?: string;
  unidadeMedida?: string;
  ativo?: boolean;
}

const emptyForm = {
  codigo: '',
  codigoBarras: '',
  nome: '',
  categoria: 'HORTIFRUTI',
  unidadeMedida: 'KG',
  precoVenda: '',
  custoMedio: '',
  estoqueAtual: '',
  estoqueMinimo: '5',
};

export function ProdutosFruteiraPage() {
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Produto | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiFruteira.listProdutos({ page, size: rowsPerPage });
      const data = response.data;
      setProdutos(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setProdutos([]);
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

  const openEdit = (p: Produto) => {
    setEditing(p);
    setForm({
      codigo: p.codigo ?? '',
      codigoBarras: p.codigoBarras ?? '',
      nome: p.nome,
      categoria: p.categoria ?? 'HORTIFRUTI',
      unidadeMedida: p.unidadeMedida ?? p.unidade ?? 'KG',
      precoVenda: String(p.precoVenda ?? ''),
      custoMedio: String(p.custoMedio ?? ''),
      estoqueAtual: String(p.estoqueAtual ?? ''),
      estoqueMinimo: String(p.estoqueMinimo ?? 5),
    });
    setDialog(true);
  };

  const salvar = async () => {
    if (!form.nome || !form.precoVenda) {
      toast.warning('Informe o nome e o preço de venda do produto');
      return;
    }
    try {
      const payload = {
        codigo: form.codigo || undefined,
        codigoBarras: form.codigoBarras || undefined,
        nome: form.nome,
        categoria: form.categoria,
        unidadeMedida: form.unidadeMedida,
        precoVenda: Number(form.precoVenda) || 0,
        custoMedio: form.custoMedio ? Number(form.custoMedio) : 0,
        estoqueAtual: form.estoqueAtual ? Number(form.estoqueAtual) : 0,
        estoqueMinimo: Number(form.estoqueMinimo) || 5,
        ativo: true,
      };

      if (editing) {
        await apiFruteira.updateProduto(editing.id, payload);
        toast.success('Produto atualizado com sucesso!');
      } else {
        await apiFruteira.createProduto(payload);
        toast.success('Produto cadastrado com sucesso!');
      }

      setDialog(false);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar produto');
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Tem certeza que deseja excluir este produto?')) return;
    try {
      await apiFruteira.deleteProduto(id);
      toast.success('Produto excluído');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir produto');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Produtos - Fruteira"
        subtitle="Cadastro e gestão de produtos do hortifrúti"
        icon={<LocalGroceryStoreIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openNew}>
              Novo Produto
            </Button>
          </Box>
        }
      />

      <DataTable<Produto>
        data={produtos}
        loading={loading}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={(newPage, newRowsPerPage) => {
          setPage(newPage);
          setRowsPerPage(newRowsPerPage);
        }}
        searchPlaceholder="Buscar produtos..."
        emptyMessage="Nenhum produto cadastrado"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 110 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          { field: 'categoria', headerName: 'Categoria', width: 150 },
          {
            field: 'precoVenda',
            headerName: 'Preço',
            width: 120,
            align: 'right',
            render: (value: number) => formatCurrency(value),
          },
          {
            field: 'estoqueAtual',
            headerName: 'Estoque',
            width: 120,
            align: 'right',
            render: (value: number, row: Produto) =>
              `${formatNumber(value, 3)} ${row.unidadeMedida ?? row.unidade ?? ''}`.trim(),
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
            render: (_: any, row: Produto) => (
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
          Dica: cadastre produtos para liberar vendas, etiquetas de balança e tabelas de preço.
        </Typography>
      )}

      {/* Modal de Cadastro / Edição */}
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Editar Produto — Fruteira' : 'Novo Produto — Fruteira'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={4}>
              <TextField
                fullWidth
                size="small"
                label="Código"
                value={form.codigo}
                onChange={(e) => setForm({ ...form, codigo: e.target.value })}
                placeholder="Ex: 101"
              />
            </Grid>
            <Grid item xs={8}>
              <TextField
                fullWidth
                size="small"
                label="Nome do Produto"
                value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
                placeholder="Ex: Tomate Italiano"
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
                {['HORTIFRUTI', 'FRUTAS', 'VERDURAS', 'LEGUMES', 'PERCIVEIS', 'OUTROS'].map((c) => (
                  <MenuItem key={c} value={c}>{c}</MenuItem>
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
                {['KG', 'G', 'UN', 'PCT', 'CX', 'L'].map((u) => (
                  <MenuItem key={u} value={u}>{u}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Preço de Venda (R$)"
                value={form.precoVenda}
                onChange={(e) => setForm({ ...form, precoVenda: e.target.value })}
                placeholder="0,00"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Custo Médio (R$)"
                value={form.custoMedio}
                onChange={(e) => setForm({ ...form, custoMedio: e.target.value })}
                placeholder="0,00"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Estoque Atual"
                value={form.estoqueAtual}
                onChange={(e) => setForm({ ...form, estoqueAtual: e.target.value })}
                placeholder="0"
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                size="small"
                label="Estoque Mínimo"
                value={form.estoqueMinimo}
                onChange={(e) => setForm({ ...form, estoqueMinimo: e.target.value })}
                placeholder="5"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                size="small"
                label="Código de Barras (opcional)"
                value={form.codigoBarras}
                onChange={(e) => setForm({ ...form, codigoBarras: e.target.value })}
                placeholder="Ex: 7891020304050"
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
export default ProdutosFruteiraPage;
