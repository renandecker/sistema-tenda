import { scan } from 'react-scan';

export function initReactScan() {
  if (typeof window === 'undefined') {
    return;
  }
  if (import.meta.env.VITE_ENABLE_REACT_SCAN !== 'true' && !import.meta.env.DEV) {
    return;
  }
  scan({
    enabled: true,
    showToolbar: true,
  } as any);
  console.log('[React Scan] Initialized successfully');
}
