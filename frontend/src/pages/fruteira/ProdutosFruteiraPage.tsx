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
import { formatCurrency } from '@/utils/formatters';
import { maskCurrency, parseCurrency } from '@/utils/masks';

interface Produto {
  id: string;
  codigo?: string;
  codigoBarras?: string;
  nome: string;
  tipo?: string;
  precoVenda?: number;
  custoMedio?: number;
  unidade?: string;
  unidadeMedida?: string;
  ativo?: boolean;
}

const TIPOS_PRODUTO: { value: string; label: string }[] = [
  { value: 'FRUTA', label: 'Fruta' },
  { value: 'VERDURA', label: 'Verdura' },
  { value: 'LEGUME', label: 'Legume' },
  { value: 'SECO', label: 'Seco/Mercearia' },
  { value: 'BEBIDA', label: 'Bebida' },
  { value: 'LIMPEZA', label: 'Limpeza' },
  { value: 'HIGIENE', label: 'Higiene' },
  { value: 'CONGELADO', label: 'Congelado' },
  { value: 'RESFRIADO', label: 'Resfriado' },
  { value: 'PAES', label: 'Pães/Padaria' },
  { value: 'OUTROS', label: 'Outros' },
];

const UNIDADES_MEDIDA = ['KG', 'G', 'UN', 'L', 'ML', 'PC', 'CX', 'FD', 'KT', 'MT', 'M2', 'M3'];

const emptyForm = {
  codigoBarras: '',
  nome: '',
  tipo: 'FRUTA',
  unidadeMedida: 'KG',
  precoVenda: '',
  custoMedio: '',
};

export function ProdutosFruteiraPage() {
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [total, setTotal] = useState(0);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Produto | null>(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiFruteira.listProdutos({ page, size: rowsPerPage });
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

const openEdit = (p: Produto) => {
    setEditing(p);
    setForm({
      codigoBarras: p.codigoBarras ?? '',
      nome: p.nome,
      tipo: p.tipo ?? 'FRUTA',
      unidadeMedida: p.unidadeMedida ?? p.unidade ?? 'KG',
      precoVenda: String(p.precoVenda ?? ''),
      custoMedio: String(p.custoMedio ?? ''),
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
        codigoBarras: form.codigoBarras || undefined,
        nome: form.nome,
        tipo: form.tipo,
        unidadeMedida: form.unidadeMedida,
        precoVenda: parseCurrency(form.precoVenda),
        custoMedio: form.custoMedio ? parseCurrency(form.custoMedio) : 0,
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
        serverSide
        totalCount={total}
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
          { field: 'tipo', headerName: 'Tipo', width: 150 },
          {
            field: 'precoVenda',
            headerName: 'Preço',
            width: 120,
            align: 'right',
            render: (value: number) => formatCurrency(value),
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
            <Grid item xs={12}>
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
                label="Tipo"
                value={form.tipo}
                onChange={(e) => setForm({ ...form, tipo: e.target.value })}
              >
                {TIPOS_PRODUTO.map((t) => (
                  <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>
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
                label="Preço de Venda (R$)"
                value={maskCurrency(form.precoVenda)}
                onChange={(e) => setForm({ ...form, precoVenda: maskCurrency(e.target.value) })}
                placeholder="0,00"
                inputMode="decimal"
              />
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
