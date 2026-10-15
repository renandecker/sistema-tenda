import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box } from '@mui/material';

import { Layout } from './components/Layout';
import { LoginPage } from './pages/auth/LoginPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { PdvPage } from './pages/pdv/PdvPage';
import { ProdutosFruteiraPage } from './pages/fruteira/ProdutosFruteiraPage';
import { PerdasFruteiraPage } from './pages/fruteira/PerdasFruteiraPage';
import { EtiquetasBalancaPage } from './pages/fruteira/EtiquetasBalancaPage';
import { TabelasPrecoPage } from './pages/fruteira/TabelasPrecoPage';
import { ComandasBuffetPage } from './pages/buffet/ComandasBuffetPage';
import { MesasBuffetPage } from './pages/buffet/MesasBuffetPage';
import { ProdutosBuffetPage } from './pages/buffet/ProdutosBuffetPage';
import { ReceitasBuffetPage } from './pages/buffet/ReceitasBuffetPage';
import { TransferenciasEstoquePage } from './pages/buffet/TransferenciasEstoquePage';
import { EstoquePage } from './pages/estoque/EstoquePage';
import { InventarioPage } from './pages/estoque/InventarioPage';
import { AlertasEstoquePage } from './pages/estoque/AlertasEstoquePage';
import { VendasPage } from './pages/pdv/VendasPage';
import { SessoesCaixaPage } from './pages/pdv/SessoesCaixaPage';
import { NfePage } from './pages/fiscal/NfePage';
import { SpedFiscalPage } from './pages/fiscal/SpedFiscalPage';
import { DrePage } from './pages/fiscal/DrePage';
import { AtivosImobilizadosPage } from './pages/fiscal/AtivosImobilizadosPage';
import { AuditScreen } from './pages/audit/AuditScreen';
import { ConciliacaoCartaoPage } from './pages/fiscal/ConciliacaoCartaoPage';
import { PerfilPage } from './pages/auth/PerfilPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { useAuth } from './hooks/useAuth';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        Carregando...
      </Box>
    );
  }
  
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/*"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        
        {/* PDV */}
        <Route path="pdv" element={<PdvPage />} />
        <Route path="vendas" element={<VendasPage />} />
        <Route path="sessoes-caixa" element={<SessoesCaixaPage />} />
        
        {/* Fruteira */}
        <Route path="fruteira/produtos" element={<ProdutosFruteiraPage />} />
        <Route path="fruteira/perdas" element={<PerdasFruteiraPage />} />
        <Route path="fruteira/etiquetas" element={<EtiquetasBalancaPage />} />
        <Route path="fruteira/tabelas-preco" element={<TabelasPrecoPage />} />
        
        {/* Buffet */}
        <Route path="buffet/comandas" element={<ComandasBuffetPage />} />
        <Route path="buffet/mesas" element={<MesasBuffetPage />} />
        <Route path="buffet/produtos" element={<ProdutosBuffetPage />} />
        <Route path="buffet/receitas" element={<ReceitasBuffetPage />} />
        <Route path="buffet/transferencias" element={<TransferenciasEstoquePage />} />
        
        {/* Estoque */}
        <Route path="estoque" element={<EstoquePage />} />
        <Route path="estoque/inventario" element={<InventarioPage />} />
        <Route path="estoque/alertas" element={<AlertasEstoquePage />} />
        
        {/* Fiscal */}
        <Route path="fiscal/nfe" element={<NfePage />} />
        <Route path="fiscal/sped-fiscal" element={<SpedFiscalPage />} />
        <Route path="fiscal/dre" element={<DrePage />} />
        <Route path="fiscal/ativos" element={<AtivosImobilizadosPage />} />
        <Route path="fiscal/conciliacao-cartao" element={<ConciliacaoCartaoPage />} />
        <Route path="perfil" element={<PerfilPage />} />
        <Route path="audit" element={<AuditScreen />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}