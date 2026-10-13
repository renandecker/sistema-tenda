import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  Tooltip,
  Chip,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  PointOfSale as PointOfSaleIcon,
  Inventory as InventoryIcon,
  Restaurant as RestaurantIcon,
  LocalGroceryStore as LocalGroceryStoreIcon,
  ReceiptLong as ReceiptLongIcon,
  TableRestaurant as TableRestaurantIcon,
  Kitchen as KitchenIcon,
  SwapHoriz as SwapHorizIcon,
  Assessment as AssessmentIcon,
  AccountBalance as AccountBalanceIcon,
  CreditCard as CreditCardIcon,
  Logout as LogoutIcon,
  Person as PersonIcon,
  Store as StoreIcon,
  Print as PrintIcon,
  PriceChange as PriceChangeIcon,
  Warning as WarningIcon,
  AttachMoney as AttachMoneyIcon,
} from '@mui/icons-material';
import { useAuth } from '@/hooks/useAuth';
import { usePdv } from '@/contexts/PdvContext';
import { brand } from '@/theme';

const drawerWidth = 260;

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { text: 'PDV', icon: <PointOfSaleIcon />, path: '/pdv', roles: ['PDV', 'ADMIN', 'CAIXA'] },
  { text: 'Vendas', icon: <ReceiptLongIcon />, path: '/vendas', roles: ['PDV', 'ADMIN', 'CAIXA', 'GERENTE'] },
  { text: 'Sessões de Caixa', icon: <AttachMoneyIcon />, path: '/sessoes-caixa', roles: ['ADMIN', 'GERENTE', 'CAIXA'] },
  { divider: true, section: 'Fruteira' },
  { text: 'Produtos', icon: <LocalGroceryStoreIcon />, path: '/fruteira/produtos', roles: ['FRUTEIRA', 'ADMIN', 'GERENTE', 'ESTOQUE'] },
  { text: 'Perdas/Quebras', icon: <WarningIcon />, path: '/fruteira/perdas', roles: ['FRUTEIRA', 'ADMIN', 'GERENTE', 'ESTOQUE'] },
  { text: 'Etiquetas Balança', icon: <PrintIcon />, path: '/fruteira/etiquetas', roles: ['FRUTEIRA', 'ADMIN', 'GERENTE'] },
  { text: 'Tabelas de Preço', icon: <PriceChangeIcon />, path: '/fruteira/tabelas-preco', roles: ['FRUTEIRA', 'ADMIN', 'GERENTE'] },
  { divider: true, section: 'Buffet' },
  { text: 'Comandas', icon: <ReceiptLongIcon />, path: '/buffet/comandas', roles: ['BUFFET', 'ADMIN', 'GERENTE', 'CAIXA'] },
  { text: 'Mesas', icon: <TableRestaurantIcon />, path: '/buffet/mesas', roles: ['BUFFET', 'ADMIN', 'GERENTE'] },
  { text: 'Produtos', icon: <RestaurantIcon />, path: '/buffet/produtos', roles: ['BUFFET', 'ADMIN', 'GERENTE', 'ESTOQUE'] },
  { text: 'Receitas/Fichas Técnicas', icon: <KitchenIcon />, path: '/buffet/receitas', roles: ['BUFFET', 'ADMIN', 'GERENTE'] },
  { text: 'Transferências Estoque', icon: <SwapHorizIcon />, path: '/buffet/transferencias', roles: ['BUFFET', 'ADMIN', 'GERENTE', 'ESTOQUE'] },
  { divider: true, section: 'Estoque' },
  { text: 'Movimentação', icon: <InventoryIcon />, path: '/estoque', roles: ['ESTOQUE', 'ADMIN', 'GERENTE'] },
  { text: 'Inventário', icon: <AssessmentIcon />, path: '/estoque/inventario', roles: ['ESTOQUE', 'ADMIN', 'GERENTE'] },
  { text: 'Alertas', icon: <WarningIcon />, path: '/estoque/alertas', roles: ['ESTOQUE', 'ADMIN', 'GERENTE'] },
  { divider: true, section: 'Fiscal/Contábil' },
  { text: 'NFC-e/Notas', icon: <ReceiptLongIcon />, path: '/fiscal/nfe', roles: ['FISCAL', 'ADMIN', 'GERENTE', 'CONTABIL'] },
  { text: 'SPED Fiscal', icon: <AccountBalanceIcon />, path: '/fiscal/sped-fiscal', roles: ['FISCAL', 'ADMIN', 'CONTABIL'] },
  { text: 'DRE', icon: <AssessmentIcon />, path: '/fiscal/dre', roles: ['FISCAL', 'ADMIN', 'CONTABIL', 'GERENTE'] },
  { text: 'Ativos Imobilizados', icon: <StoreIcon />, path: '/fiscal/ativos', roles: ['FISCAL', 'ADMIN', 'CONTABIL'] },
  { text: 'Conciliação Cartão', icon: <CreditCardIcon />, path: '/fiscal/conciliacao-cartao', roles: ['FISCAL', 'ADMIN', 'CONTABIL', 'GERENTE'] },
];

export function Layout() {
  const { user, logout, hasPermission } = useAuth();
  const { selectedPdv, sessaoAtiva } = usePdv();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleDrawerToggle = () => setMobileOpen(!mobileOpen);
  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => setAnchorEl(event.currentTarget);
  const handleMenuClose = () => setAnchorEl(null);
  const handleLogout = () => { handleMenuClose(); logout(); navigate('/login'); };

  const filteredMenuItems = menuItems.filter(item => {
    if (item.divider) return true;
    if (!item.roles) return true;
    return item.roles.some(role => hasPermission(role));
  });

  const drawer = (
    <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', p: 2 }}>
      <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1.5, px: 0.5 }}>
        <Box
          component="img"
          src="/logo.png"
          alt="Sistema Tenda"
          sx={{
            height: 52,
            width: 52,
            objectFit: 'contain',
            borderRadius: 2,
            bgcolor: brand.cream,
            p: 0.5,
            flexShrink: 0,
          }}
        />
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h6" fontWeight={700} sx={{ color: 'text.primary', lineHeight: 1.1 }}>
            Sistema Tenda
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Fruteira &amp; Buffet
          </Typography>
        </Box>
      </Box>
      <Divider />
      <Box sx={{ mb: 2, p: 1.5, bgcolor: 'rgba(245,168,0,0.08)', borderRadius: 2, border: '1px solid rgba(245,168,0,0.18)' }}>
        <Typography variant="caption" color="text.secondary" fontWeight={600}>PDV Ativo</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {selectedPdv ? (
            <>
              <Chip label={selectedPdv.codigo} size="small" color="primary" variant="outlined" />
              <Chip label={sessaoAtiva ? 'CAIXA ABERTO' : 'CAIXA FECHADO'} size="small" color={sessaoAtiva ? 'success' : 'default'} variant="outlined" />
            </>
          ) : (
            <Typography variant="body2" color="text.secondary">Nenhum PDV selecionado</Typography>
          )}
        </Box>
      </Box>
      <List component="nav" aria-label="Main navigation">
        {filteredMenuItems.map((item, index) => {
          if (item.divider) {
            return (
              <React.Fragment key={index}>
                <Divider sx={{ my: 1 }} />
                <Typography variant="caption" sx={{ px: 2, py: 1, fontWeight: 700, letterSpacing: 0.6, color: 'text.secondary', textTransform: 'uppercase' }}>
                  {item.section}
                </Typography>
              </React.Fragment>
            );
          }
          const active = location.pathname === item.path;
          return (
            <ListItemButton
              key={item.path}
              selected={active}
              onClick={() => item.path && navigate(item.path)}
              sx={{ borderRadius: 2, mx: 1, my: 0.25 }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{ fontSize: '0.9rem', fontWeight: active ? 600 : 400 }}
              />
            </ListItemButton>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
          bgcolor: 'background.paper',
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { md: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Box sx={{ flexGrow: 1 }} />
          <Tooltip title={selectedPdv ? `${selectedPdv.nome} - ${sessaoAtiva ? 'Caixa Aberto' : 'Caixa Fechado'}` : 'Nenhum PDV selecionado'}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mr: 2, cursor: 'pointer', px: 1.5, py: 0.5, borderRadius: 2, bgcolor: 'rgba(245,168,0,0.10)', border: '1px solid rgba(245,168,0,0.20)' }}>
              <PointOfSaleIcon color="primary" fontSize="small" />
              <Typography variant="body2" sx={{ whiteSpace: 'nowrap' }}>
                {selectedPdv ? selectedPdv.codigo : 'Selecionar PDV'}
              </Typography>
            </Box>
          </Tooltip>
          <IconButton onClick={handleMenuOpen} sx={{ ml: 1 }}>
            <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', color: 'primary.contrastText', fontWeight: 700 }}>
              {user?.nome?.charAt(0).toUpperCase() || 'U'}
            </Avatar>
          </IconButton>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
      >
        {drawer}
      </Drawer>

      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, borderRight: 1, borderColor: 'divider' },
        }}
        open
      >
        {drawer}
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          mt: '64px',
          bgcolor: 'background.default',
          minHeight: 'calc(100vh - 64px)',
        }}
      >
        <Outlet />
      </Box>

      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleMenuClose} transformOrigin={{ horizontal: 'right', vertical: 'top' }} anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}>
        <MenuItem onClick={() => { handleMenuClose(); navigate('/perfil'); }}>
          <ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon>
          Perfil
        </MenuItem>
        <MenuItem onClick={handleLogout}>
          <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
          Sair
        </MenuItem>
      </Menu>
    </Box>
  );
}