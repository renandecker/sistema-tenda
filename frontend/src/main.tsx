import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { theme } from './theme';
import { App } from './App';
import { AuthProvider } from './contexts/AuthContext';
import { PdvProvider } from './contexts/PdvContext';
import { setupIndexedDBCleanup } from './utils/indexedDBCleanup';
import { initializeMonitoring } from './services/monitoring';
import { SentryErrorBoundary } from './services/monitoring/sentry';
import { getBugsnagErrorBoundary } from './services/monitoring/bugsnag';
import { RollbarErrorBoundary } from './services/monitoring/rollbar';

// Initialize all observability and monitoring suites
initializeMonitoring();

setupIndexedDBCleanup();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

const BugsnagBoundary = getBugsnagErrorBoundary();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <SentryErrorBoundary fallback={() => <div>Ocorreu um erro inesperado. O reporte foi enviado para a equipe de suporte.</div>}>
      <RollbarErrorBoundary>
        <BugsnagBoundary>
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <ThemeProvider theme={theme}>
              <CssBaseline />
              <AuthProvider>
                <PdvProvider>
                  <App />
                  <ToastContainer
                    position="top-right"
                    autoClose={5000}
                    hideProgressBar={false}
                    newestOnTop
                    closeOnClick
                    rtl={false}
                    pauseOnFocusLoss
                    draggable
                    pauseOnHover
                  />
                </PdvProvider>
              </AuthProvider>
            </ThemeProvider>
          </BrowserRouter>
        </QueryClientProvider>
        </BugsnagBoundary>
      </RollbarErrorBoundary>
    </SentryErrorBoundary>
  </React.StrictMode>
);