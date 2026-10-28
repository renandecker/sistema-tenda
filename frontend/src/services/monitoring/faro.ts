import { initializeFaro } from '@grafana/faro-web-sdk';
import { TracingInstrumentation } from '@grafana/faro-web-tracing';

export function initGrafanaFaro() {
  try {
    initializeFaro({
      url: import.meta.env.VITE_GRAFANA_FARO_URL || 'https://faro-collector-prod-us-east-0.grafana.net/collect/your-faro-key',
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
