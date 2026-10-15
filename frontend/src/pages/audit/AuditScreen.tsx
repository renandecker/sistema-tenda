import { useEffect, useState } from 'react';
import {
  Box,
  Table,
  TableRow,
  TableCell,
  TableHead,
  Paper,
  Typography,
  Chip,
  Skeleton,
  Alert,
  InputLabel,
  Select,
  MenuItem as SelectMenuItem,
  Button,
  Toolbar,
  ToolbarTitle,
  Grid,
} from '@mui/material';
import AssessmentIcon from '@mui/icons-material/Assessment';
import { useAuth } from '@/hooks/useAuth';
import { apiPdv } from '@/services/api';
import { formatDateTime } from '@/utils/formatters';
import { PageHeader } from '@/components/PageHeader';

interface AuditLog {
  id: string;
  usuarioId: string;
  acao: string;
  entidade: string;
  registroId: string;
  descricao: string;
  ipAddress: string;
  userAgent: string;
  dataAcao: string;
  sucesso: boolean;
}

export function AuditScreen() {
  const { user, hasPermission } = useAuth();
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<{
    usuarioId: string;
    acao: string;
    entidade: string;
    dataInicio: string;
    dataFim: string;
  }>({
    usuarioId: '',
    acao: '',
    entidade: '',
    dataInicio: '',
    dataFim: '',
  });

  const fetchLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiPdv.get('/audit/logs', {
        params: {
          usuarioId: filters.usuarioId || undefined,
          acao: filters.acao || undefined,
          entidade: filters.entidade || undefined,
          dataInicio: filters.dataInicio || undefined,
          dataFim: filters.dataFim || undefined,
          page: 1,
          limit: 100,
        },
      });
      setLogs(response.data?.content || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao carregar logs de auditoria');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value as any }));
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  return (
    <Box sx={{ p: 3 }}>
      <PageHeader
        title="Auditoria do Sistema"
        subtitle="Registro de todas as ações do sistema"
        icon={<AssessmentIcon />}
      />

      <Paper sx={{ p: 3, mb: 3 }}>
        <Toolbar>
          <ToolbarTitle>Filtros</ToolbarTitle>
          <Box sx={{ flexGrow: 1 }} />
          <Button variant="outlined" onClick={fetchLogs}>
            <span style={{ mr: 1 }}>Atualizar</span>
          </Button>
        </Toolbar>

        <Box sx={{ pt: 2 }}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={3}>
              <InputLabel shrink>Usuário</InputLabel>
              <Select
                name="usuarioId"
                value={filters.usuarioId}
                onChange={handleFilterChange}
                sx={{ width: '100%' }}
              >
                <SelectMenuItem value="">Todos</SelectMenuItem>
              </Select>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <InputLabel shrink>Ação</InputLabel>
              <Select
                name="acao"
                value={filters.acao}
                onChange={handleFilterChange}
                sx={{ width: '100%' }}
              >
                <SelectMenuItem value="">Todas</SelectMenuItem>
                <SelectMenuItem value="login">Login</SelectMenuItem>
                <SelectMenuItem value="logout">Logout</SelectMenuItem>
                <SelectMenuItem value="abrir_caixa">Abrir Caixa</SelectMenuItem>
                <SelectMenuItem value="fechar_caixa">Fechar Caixa</SelectMenuItem>
              </Select>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <InputLabel shrink>Entidade</InputLabel>
              <Select
                name="entidade"
                value={filters.entidade}
                onChange={handleFilterChange}
                sx={{ width: '100%' }}
              >
                <SelectMenuItem value="">Todas</SelectMenuItem>
                <SelectMenuItem value="venda">Venda</SelectMenuItem>
                <SelectMenuItem value="comanda">Comanda</SelectMenuItem>
                <SelectMenuItem value="produto">Produto</SelectMenuItem>
              </Select>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <InputLabel shrink>Período</InputLabel>
              <input
                type="date"
                name="dataInicio"
                value={filters.dataInicio}
                onChange={handleFilterChange}
                style={{ width: '100%' }}
              />
              <InputLabel shrink>Ate</InputLabel>
              <input
                type="date"
                name="dataFim"
                value={filters.dataFim}
                onChange={handleFilterChange}
                style={{ width: '100%' }}
              />
            </Grid>
          </Grid>
        </Box>
      </Paper>

      {loading && (
        <Box sx={{ pt: 2 }}>
          <Grid container>
            <Grid item xs={12}>
              <Skeleton variant="text" paragraph={true} />
            </Grid>
          </Grid>
        </Box>
      )}

      {!loading && logs.length === 0 && !error && (
        <Alert variant="info">
          <Typography variant="body1">Nenhum log de auditoria encontrado com os filtros aplicados.</Typography>
        </Alert>
      )}

      {!loading && logs.length > 0 && !error && (
        <Box sx={{ pt: 2 }}>
          <Table size="small" aria-label="audit logs">
            <TableHead>
              <TableRow>
                <TableCell>Data/Hora</TableCell>
                <TableCell>Usuário</TableCell>
                <TableCell>Ação</TableCell>
                <TableCell>Entidade</TableCell>
                <TableCell>Registro</TableCell>
                <TableCell>Sucesso</TableCell>
                <TableCell>IP</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.map((log) => (
                <TableRow key={log.id} sx={{ bgcolor: log.sucesso ? 'background.success.light' : 'background.error.light' }}>
                  <TableCell component="td">{formatDateTime(log.dataAcao)}</TableCell>
                  <TableCell>{log.usuarioId ? 'Usuário ' + log.usuarioId.substring(0, 8) : 'System'}</TableCell>
                  <TableCell>{log.acao}</TableCell>
                  <TableCell>{log.entidade || '-'}</TableCell>
                  <TableCell>{log.registroId ? log.registroId.substring(0, 8) + '...' : '-'}</TableCell>
                  <TableCell>
                    <Chip
                      label={log.sucesso ? 'Sucesso' : 'Falha'}
                      color={log.sucesso ? 'success' : 'error'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{log.ipAddress || '-'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>
      )}
    </Box>
  );
}