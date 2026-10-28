import { initOpenTelemetry } from './opentelemetry';
import { initGrafanaFaro } from './faro';
import { initReactScan } from './reactScan';
import { initSentry } from './sentry';
import { initBugsnag } from './bugsnag';
import { initPostHog } from './posthog';
import { initRollbar } from './rollbar';

export function initializeMonitoring() {
  // 1. React Scan (Development render visualizer)
  initReactScan();

  // 2. OpenTelemetry (Tracing) - also forwards to Dynatrace via OTLP
  initOpenTelemetry();

  // 3. Grafana Faro (Frontend Observability)
  initGrafanaFaro();

  // 4. Sentry (Error Tracking & Replays)
  initSentry();

  // 5. Bugsnag (Error Monitoring)
  initBugsnag();

  // 6. PostHog (Product Analytics & Session Recording)
  initPostHog();

  // 7. Rollbar (Error Monitoring - alongside Bugsnag)
  initRollbar();

  console.log('[Monitoring Suite] All observability services initialized.');
}
