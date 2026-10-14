import { useEffect, useState } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Chip,
  Divider,
  Paper,
} from '@mui/material';
import {
  LocalGroceryStore as LocalGroceryStoreIcon,
  Restaurant as RestaurantIcon,
  PointOfSale as PointOfSaleIcon,
  AttachMoney as AttachMoneyIcon,
  Inventory as InventoryIcon,
  Warning as WarningIcon,
  Dashboard as DashboardIcon,
} from '@mui/icons-material';
import { usePdv } from '@/contexts/PdvContext';
import { apiPdv } from '@/services/api';
import { formatCurrency } from '@/utils/formatters';
import { PageHeader } from '@/components/PageHeader';

interface MetricCard {
  title: string;
  value: string;
  icon: React.ReactNode;
  color: 'primary' | 'success' | 'warning' | 'info' | 'error';
  subtitle?: string;
}

export function DashboardPage() {
  const { selectedPdv, sessaoAtiva } = usePdv();
  const [vendasHoje, setVendasHoje] = useState<{ total: number; fruteira: number; buffet: number }>({
    total: 0,
    fruteira: 0,
    buffet: 0,
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let active = true;
    const hoje = new Date().toISOString().split('T')[0];
    setLoading(true);
    Promise.all([
      apiPdv.getTotalVendas(hoje, hoje).catch(() => ({ data: 0 })),
      apiPdv.getTotalFruteira(hoje, hoje).catch(() => ({ data: 0 })),
      apiPdv.getTotalBuffet(hoje, hoje).catch(() => ({ data: 0 })),
    ])
      .then(([total, fruteira, buffet]) => {
        if (!active) return;
        setVendasHoje({
          total: Number(total.data) || 0,
          fruteira: Number(fruteira.data) || 0,
          buffet: Number(buffet.data) || 0,
        });
      })
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, []);

  const metricCards: MetricCard[] = [
    {
      title: 'Vendas Hoje',
      value: formatCurrency(vendasHoje.total),
      icon: <PointOfSaleIcon />,
      color: 'primary',
    },
    {
      title: 'Fruteira',
      value: formatCurrency(vendasHoje.fruteira),
      icon: <LocalGroceryStoreIcon />,
      color: 'success',
      subtitle: 'Varejo / Hortifrúti',
    },
    {
      title: 'Buffet',
      value: formatCurrency(vendasHoje.buffet),
      icon: <RestaurantIcon />,
      color: 'warning',
      subtitle: 'Restaurante',
    },
    { title: 'Comandas Abertas', value: '0', icon: <AttachMoneyIcon />, color: 'info' },
    { title: 'Estoque Baixo', value: '0', icon: <InventoryIcon />, color: 'success' },
    { title: 'Alertas Ativos', value: '0', icon: <WarningIcon />, color: 'success' },
  ];

  return (
    <Box>
      <PageHeader
        title="Dashboard"
        subtitle={
          selectedPdv
            ? `Visão geral do PDV ${selectedPdv.codigo} - ${selectedPdv.nome}`
            : 'Visão geral do sistema'
        }
        icon={<DashboardIcon />}
      />

      <Grid container spacing={3} sx={{ mb: 3 }}>
        {metricCards.map((card) => (
          <Grid item xs={12} sm={6} md={4} lg={2} key={card.title}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    {card.title}
                  </Typography>
                  <Box sx={{ p: 0.75, borderRadius: 1.5, bgcolor: `${card.color}.light`, color: `${card.color}.contrastText` }}>
                    {card.icon}
                  </Box>
                </Box>
                <Typography variant="h5" fontWeight={700}>
                  {card.value}
                </Typography>
                {card.subtitle && (
                  <Typography variant="caption" color="text.secondary">
                    {card.subtitle}
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} lg={7}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Bem-vindo ao Sistema Tenda
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Typography variant="body2" color="text.secondary" paragraph>
              Gestão integrada da <strong>Fruteira</strong> e do <strong>Buffet</strong>: PDV, estoque,
              produção, fiscal e gestão financeira em um único sistema.
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Use o menu lateral para navegar entre os módulos.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} lg={5}>
          <Paper sx={{ p: 3, height: '100%' }}>
            <Typography variant="h6" gutterBottom>
              Status do Caixa
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <Chip
                label={sessaoAtiva ? 'CAIXA ABERTO' : 'CAIXA FECHADO'}
                color={sessaoAtiva ? 'success' : 'default'}
              />
              {selectedPdv && (
                <Chip label={selectedPdv.codigo} variant="outlined" color="primary" />
              )}
            </Box>
            {sessaoAtiva ? (
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">Abertura</Typography>
                  <Typography variant="body1">{formatCurrency(sessaoAtiva.valorInicial)}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Operador</Typography>
                  <Typography variant="body1">{sessaoAtiva.operadorAbertura}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Desde</Typography>
                  <Typography variant="body1">
                    {new Date(sessaoAtiva.dataAbertura).toLocaleString('pt-BR')}
                  </Typography>
                </Box>
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary">
                Nenhuma sessão de caixa aberta. Selecione um PDV e abra o caixa para iniciar as vendas.
              </Typography>
            )}
          </Paper>
        </Grid>
      </Grid>

      {loading && (
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
          Atualizando indicadores...
        </Typography>
      )}
    </Box>
  );
}
