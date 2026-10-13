import React, { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Chip,
  LinearProgress,
  IconButton,
  Tooltip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Divider,
  Skeleton,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  LocalGroceryStore as LocalGroceryStoreIcon,
  Restaurant as RestaurantIcon,
  PointOfSale as PointOfSaleIcon,
  AttachMoney as AttachMoneyIcon,
  Inventory as InventoryIcon,
  Warning as WarningIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { usePdv } from '@/contexts/PdvContext';
import { apiPdv, apiFruteira, apiBuffet, apiEstoque } from '@/services/api';
import { formatCurrency } from '@/utils/formatters';

interface DashboardMetrics {
  vendasHoje: { total: number; fruteira: number; buffet: number };
  caixa: { aberto: boolean; valorInicial: number; valorFinal: number; diferenca: number };
  estoque: { totalProdutos: number; estoqueBaixo: number; alertasAtivos: number };
  comandas: { abertas: number; valorTotal: number };
}

export function DashboardPage() {
  const { selectedPdv, sessaoAtiva, fetchSessaoAtiva } = usePdv();
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [vendasRecentes, setVendasRecentes] = useState<any[]>([]);

  const loadData = async () => {
    if (!selectedPdv) return;
    setLoading(true);
    try {
      const hoje = new Date().toISOString().split('T')[0];
      
      // Vendas do dia
      const [vendasResponse, estoqueResponse, alertasResponse, comandasResponse] = await Promise.all([
        apiPdv.getTotalVendas(hoje, hoje),
        apiFruteira.getEstoqueBaixo().then(r => r.data.content || r.data).catch(() => []),
        apiEstoque.listAlertas({ status: 'ATIVO', page: 0, size: 1 }).catch(() => ({ data: { totalElements: 0 } })),
        apiBuffet.listComandas({ status: 'ABERTA,EM_USO', page: 0, size: 1 }).catch(() => ({ data: { totalElements: 0 } })),
      ]);

      const vendasHoje = vendasResponse.data || 0;
      const vendasFruteiraResponse = await apiPdv.getTotalFruteira(hoje, hoje);
      const vendasBuffetResponse = await apiPdv.getTotalBuffet(hoje, hoje);

      setMetrics({
        vendasHoje: {
          total: vendasHoje,
          fruteira: vendasFruteiraResponse.data || 0,
          buffet: vendasBuffetResponse.data || 0,
        },
        caixa: {
          aberto: !!sessaoAtiva,
          valorInicial: sessaoAtiva?.valorInicial || 0,
          valorFinal: sessaoAtiva?.valorFinalSistema || 0,
          diferenca: sessaoAtiva?.diferenca || 0,
        },
        estoque: {
          totalProdutos: 0, // TODO: buscar total
          estoqueBaixo: (estoqueResponse.length || 0),
          alertasAtivos: alertasResponse.data?.totalElements || 0,
        },
        comandas: {
          abertas: comandasResponse.data?.totalElements || 0,
          valorTotal: 0, // TODO: calcular
        },
      });

      // Vendas recentes
      const vendasRecentesResponse = await apiPdv.listVendas({ page: 0, size: 5, sortBy: 'data', sortDirection: 'desc' });
      setVendasRecentes(vendasRecentesResponse.data?.content || vendasRecentesResponse.data || []);
    } catch (error) {
      console.error('Erro ao carregar dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedPdv, sessaoAtiva]);

  useEffect(() => {
    if (selectedPdv) {
      fetchSessaoAtiva();
    }
  }, [selectedPdv, fetchSessaoAtiva]);

  if (!selectedPdv) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', textAlign: 'center', p: 4 }}>
        <PointOfSaleIcon sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
        <Typography variant="h5" gutterBottom>Selecione um PDV para começar</Typography>
        <Typography variant="body1" color="text.secondary" paragraph>
          Clique no ícone do PDV na barra superior para selecionar o terminal e abrir o caixa.
        </Typography>
      </Box>
    );
  }

  const metricCards = [
    { title: 'Vendas Hoje', value: metrics?.vendasHoje.total ? formatCurrency(metrics.vendasHoje.total) : 'R$ 0,00', icon: <PointOfSaleIcon />, color: 'primary', trend: '+12%', trendIcon: <TrendingUpIcon fontSize="small" /> },
    { title: 'Fruteira', value: metrics?.vendasHoje.fruteira ? formatCurrency(metrics.vendasHoje.fruteira) : 'R$ 0,00', icon: <LocalGroceryStoreIcon />, color: 'success', subtitle: 'Varejo' },
    { title: 'Buffet', value: metrics?.vendasHoje.buffet ? formatCurrency(metrics.vendasHoje.buffet) : 'R$ 0,00', icon: <RestaurantIcon />, color: 'warning', subtitle: 'Restaurante' },
    { title: 'Comandas Abertas', value: metrics?.comandas.abertas?.toString() || '0', icon: <AttachMoneyIcon />, color: 'info', trend: metrics?.comandas.abertas && metrics.comandas.abertas > 0 ? `${metrics.comandas.abertas} ativas` : '', trendIcon: <WarningIcon fontSize="small" /> },
    { title: 'Estoque Baixo', value: metrics?.estoque.estoqueBaixo?.toString() || '0', icon: <InventoryIcon />, color: metrics?.estoque.estoqueBaixo && metrics.estoque.estoqueBaixo > 0 ? 'error' : 'success', trend: metrics?.estoque.estoqueBaixo && metrics.estoque.estoqueBaixo > 0 ? 'Atenção necessária' : 'OK', trendIcon: metrics?.estoque.estoqueBaixo && metrics.estoque.estoqueBaixo > 0 ? <WarningIcon fontSize="small" color="error" /> : <TrendingUpIcon fontSize="small" color="success" /> },
    { title: 'Alertas Ativos', value: metrics?.estoque.alertasAtivos?.toString() || '0', icon: <WarningIcon />, color: metrics?.estoque.alertasAtivos && metrics.estoque.alertasAtivos > 0 ? 'warning' : 'success' },
  ];

  return (
    <Box sx={{ flexGrow: 1 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" fontWeight={600}>Dashboard</Typography>
          <Typography variant="body1" color="text.secondary">Visão geral do PDV {selectedPdv.codigo} - {selectedPdv.nome}</Typography>
        </Box>
        <Tooltip title="Atualizar dados">
          <IconButton onClick={loadData} disabled={loading}>
            <RefreshIcon sx={{ spin: loading }} />
          </IconButton>
        </Tooltip>
      </Box>

      {loading && metrics === null ? (
        <Grid container spacing={3}>
          {metricCards.map((_, i) => (
            <Grid item xs={12} sm={6} md={4} lg={2} key={i}>
              <Skeleton variant="rectangular" height={120} />
            </Grid>
          ))}
        </Grid>
      ) : (
        <>
          <Grid container spacing={3} sx={{ mb: 3 }}>
            {metricCards.map((card, index) => (
              <Grid item xs={12} sm={6} md={4} lg={2} key={index}>
                <Card sx={{ height: '100%' }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                      <Typography variant="body2" color="text.secondary">{card.title}</Typography>
                      <Box sx={{ p: 0.5, borderRadius: 1, bgcolor: `${card.color}.light`, color: `${card.color}.main` }}>
                        {card.icon}
                      </Box>
                    </Box>
                    <Typography variant="h4" fontWeight={700} sx={{ mb: 0.5 }}>{card.value}</Typography>
                    {card.subtitle && <Typography variant="caption" color="text.secondary">{card.subtitle}</Typography>}
                    {card.trend && (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 1 }}>
                        {card.trendIcon}
                        <Typography variant="caption" color={card.trend.includes('Atenção') ? 'error' : card.trend.includes('OK') ? 'success' : 'text.secondary'}>
                          {card.trend}
                        </Typography>
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>

          <Divider sx={{ mb: 3 }} />

          <Grid container spacing={3}>
            <Grid item xs={12} lg={7}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">Vendas Recentes</Typography>
                </Box>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Número</TableCell>
                        <TableCell>Cliente</TableCell>
                        <TableCell align="right">Total</TableCell>
                        <TableCell align="center">Status</TableCell>
                        <TableCell align="right">Hora</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {vendasRecentes.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={5} align="center" sx={{ py: 3 }}>Nenhuma venda recente</TableCell>
                        </TableRow>
                      ) : (
                        vendasRecentes.map((venda) => (
                          <TableRow key={venda.id} hover>
                            <TableCell>{venda.numero}</TableCell>
                            <TableCell>{venda.clienteNome || 'Consumidor Final'}</TableCell>
                            <TableCell align="right">{formatCurrency(venda.valorTotal)}</TableCell>
                            <TableCell align="center">
                              <Chip
                                label={venda.status}
                                size="small"
                                color={
                                  venda.status === 'PAGA' || venda.status === 'FINALIZADA' ? 'success' :
                                  venda.status === 'CANCELADA' ? 'error' :
                                  venda.status === 'ABERTA' ? 'info' : 'warning'
                                }
                              />
                            </TableCell>
                            <TableCell align="right">{new Date(venda.data).toLocaleTimeString('pt-BR')}</TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            </Grid>

            <Grid item xs={12} lg={5}>
              <Paper sx={{ p: 2, height: '100%' }}>
                <Typography variant="h6" gutterBottom>Status do Caixa</Typography>
                <Divider sx={{ mb: 2 }} />
                {sessaoAtiva ? (
                  <Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                      <Chip label="CAIXA ABERTO" color="success" icon={<TrendingUpIcon fontSize="small" />} />
                    </Box>
                    <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                      <Box>
                        <Typography variant="caption" color="text.secondary">Abertura</Typography>
                        <Typography variant="body1">{formatCurrency(sessaoAtiva.valorInicial)}</Typography>
                      </Box>
                      <Box>
                        <Typography variant="caption" color="text.secondary">Esperado (Sistema)</Typography>
                        <Typography variant="body1">{formatCurrency(sessaoAtiva.valorFinalSistema || 0)}</Typography>
                      </Box>
                      <Box>
                        <Typography variant="caption" color="text.secondary">Operador</Typography>
                        <Typography variant="body1">{sessaoAtiva.operadorAbertura}</Typography>
                      </Box>
                      <Box>
                        <Typography variant="caption" color="text.secondary">Desde</Typography>
                        <Typography variant="body1">{new Date(sessaoAtiva.dataAbertura).toLocaleString('pt-BR')}</Typography>
                      </Box>
                    </Box>
                    <Divider sx={{ my: 2 }} />
                    <Button variant="contained" color="warning" fullWidth startIcon={<AttachMoneyIcon fontSize="small" />}>
                      Fechar Caixa
                    </Button>
                  </Box>
                ) : (
                  <Box sx={{ textAlign: 'center', py: 4 }}>
                    <AttachMoneyIcon sx={{ fontSize: 48, color: 'grey.400', mb: 2 }} />
                    <Typography variant="h6" color="text.secondary" gutterBottom>Caixa Fechado</Typography>
                    <Typography variant="body2" color="text.secondary" paragraph>
                      Clique em "Abrir Caixa" para iniciar uma nova sessão.
                    </Typography>
                    <Button variant="contained" color="primary" size="large" fullWidth startIcon={<AttachMoneyIcon fontSize="small" />}>
                      Abrir Caixa
                    </Button>
                  </Box>
                )}
              </Paper>
            </Grid>
          </Grid>
        </>
      )}
    </Box>
  );
}