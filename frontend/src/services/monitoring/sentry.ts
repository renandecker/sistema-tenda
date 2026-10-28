import * as Sentry from '@sentry/react';

export function initSentry() {
  try {
    Sentry.init({
      dsn: import.meta.env.VITE_SENTRY_DSN || 'https://examplePublicKey@o0.ingest.sentry.io/0',
      integrations: [
        Sentry.browserTracingIntegration(),
        Sentry.replayIntegration({
          maskAllText: false,
          blockAllMedia: false,
        }),
      ],
      tracesSampleRate: 1.0,
      replaysSessionSampleRate: 0.1,
      replaysOnErrorSampleRate: 1.0,
      environment: import.meta.env.MODE || 'production',
    });
    console.log('[Sentry] Initialized successfully');
  } catch (error) {
    console.error('[Sentry] Failed to initialize:', error);
  }
}

export const SentryErrorBoundary = Sentry.ErrorBoundary;
