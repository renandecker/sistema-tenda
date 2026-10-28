import { initializeFaro } from '@grafana/faro-web-sdk';
import { TracingInstrumentation } from '@grafana/faro-web-tracing';

export function initGrafanaFaro() {
  try {
    if (import.meta.env.VITE_ENABLE_GRAFANA_FARO !== 'true') {
      return;
    }
    const url = import.meta.env.VITE_GRAFANA_FARO_URL;
    if (!url) {
      console.warn('[Grafana Faro] VITE_GRAFANA_FARO_URL not set, skipping initialization');
      return;
    }
    initializeFaro({
      url,
      app: {
        name: 'sistema-tenda-frontend',
        version: '1.0.0',
        environment: import.meta.env.MODE || 'production',
      },
      instrumentations: [
        new TracingInstrumentation(),
      ],
      paused: false,
    });
    console.log('[Grafana Faro] Initialized successfully');
  } catch (error) {
    console.error('[Grafana Faro] Failed to initialize:', error);
  }
}
