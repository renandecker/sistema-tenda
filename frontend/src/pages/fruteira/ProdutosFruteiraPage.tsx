import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, IconButton, Tooltip, Typography } from '@mui/material';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  LocalGroceryStore as LocalGroceryStoreIcon,
} from '@mui/icons-material';
import { apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';

interface Produto {
  id: string;
  codigo: string;
  nome: string;
  categoria?: string;
  precoVenda?: number;
  estoqueAtual?: number;
  unidade?: string;
  ativo?: boolean;
}

export function ProdutosFruteiraPage() {
  const [produtos, setProdutos] = useState<Produto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);

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

  return (
    <Box>
      <PageHeader
        title="Produtos - Fruteira"
        subtitle="Cadastro e gestão de produtos do hortifrúti"
        icon={<LocalGroceryStoreIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar">
              <IconButton onClick={load} disabled={loading}>
                <RefreshIcon />
              </IconButton>
            </Tooltip>
            <Button variant="contained" startIcon={<AddIcon />}>
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
          { field: 'codigo', headerName: 'Código', width: 120 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          { field: 'categoria', headerName: 'Categoria', width: 160 },
          {
            field: 'precoVenda',
            headerName: 'Preço',
            width: 130,
            align: 'right',
            render: (value: number) => formatCurrency(value),
          },
          {
            field: 'estoqueAtual',
            headerName: 'Estoque',
            width: 120,
            align: 'right',
            render: (value: number, row: Produto) =>
              `${formatNumber(value, 3)} ${row.unidade ?? ''}`.trim(),
          },
          {
            field: 'ativo',
            headerName: 'Status',
            width: 120,
            align: 'center',
            render: (value: boolean) => (
              <Chip
                label={value ? 'Ativo' : 'Inativo'}
                size="small"
                color={value ? 'success' : 'default'}
              />
            ),
          },
        ]}
      />

      {!loading && produtos.length === 0 && (
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
          Dica: cadastre produtos para liberar vendas, etiquetas de balança e tabelas de preço.
        </Typography>
      )}
    </Box>
  );
}
