import { initOpenTelemetry } from './opentelemetry';
import { initGrafanaFaro } from './faro';
import { initReactScan } from './reactScan';
import { initSentry } from './sentry';
import { initBugsnag } from './bugsnag';
import { initPostHog } from './posthog';
import { initRollbar } from './rollbar';

const isEnabled = (key: string) => import.meta.env[key] === 'true';

export function initializeMonitoring() {
  // 1. React Scan (render visualizer overlay) — off unless explicitly enabled
  if (isEnabled('VITE_ENABLE_REACT_SCAN')) {
    initReactScan();
  }

  // 2. OpenTelemetry (Tracing) - also forwards to Dynatrace via OTLP
  if (isEnabled('VITE_ENABLE_OPENTELEMETRY') || isEnabled('VITE_ENABLE_DYNATRACE_OTEL')) {
    initOpenTelemetry();
  }

  // 3. Grafana Faro (Frontend Observability)
  if (isEnabled('VITE_ENABLE_GRAFANA_FARO')) {
    initGrafanaFaro();
  }

  // 4. Sentry (Error Tracking & Replays)
  if (isEnabled('VITE_ENABLE_SENTRY')) {
    initSentry();
  }

  // 5. Bugsnag (Error Monitoring)
  if (isEnabled('VITE_ENABLE_BUGSNAG')) {
    initBugsnag();
  }

  // 6. PostHog (Product Analytics & Session Recording)
  if (isEnabled('VITE_ENABLE_POSTHOG')) {
    initPostHog();
  }

  // 7. Rollbar (Error Monitoring - alongside Bugsnag)
  if (isEnabled('VITE_ENABLE_ROLLBAR')) {
    initRollbar();
  }

  console.log('[Monitoring Suite] Monitoring initialization completed (respecting enable flags).');
}
