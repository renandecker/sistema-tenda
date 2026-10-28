import { scan } from 'react-scan';

export function initReactScan() {
  if (typeof window !== 'undefined' && (import.meta.env.DEV || import.meta.env.VITE_ENABLE_REACT_SCAN === 'true')) {
    scan({
      enabled: true,
      showToolbar: true,
    } as any);
    console.log('[React Scan] Initialized successfully');
  }
}
