import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Chip, IconButton, Tooltip } from '@mui/material';
import {
  Add as AddIcon,
  Refresh as RefreshIcon,
  ReceiptLong as ReceiptLongIcon,
} from '@mui/icons-material';
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

export function ComandasBuffetPage() {
  const [comandas, setComandas] = useState<Comanda[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);

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

  return (
    <Box>
      <PageHeader
        title="Comandas - Buffet"
        subtitle="Gestão das comandas abertas no restaurante"
        icon={<ReceiptLongIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar">
              <IconButton onClick={load} disabled={loading}>
                <RefreshIcon />
              </IconButton>
            </Tooltip>
            <Button variant="contained" startIcon={<AddIcon />}>
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
          { field: 'numero', headerName: 'Número', width: 120 },
          { field: 'mesaNumero', headerName: 'Mesa', width: 100, align: 'center' },
          { field: 'clienteNome', headerName: 'Cliente', flex: 1 },
          {
            field: 'valorTotal',
            headerName: 'Total',
            width: 140,
            align: 'right',
            render: (value: number) => formatCurrency(value),
          },
          {
            field: 'status',
            headerName: 'Status',
            width: 140,
            align: 'center',
            render: (value: string) => (
              <Chip label={getStatusLabel(value)} size="small" color={getStatusColor(value)} />
            ),
          },
          {
            field: 'dataAbertura',
            headerName: 'Abertura',
            width: 180,
            render: (value: string) => formatDateTime(value),
          },
        ]}
      />
    </Box>
  );
}
