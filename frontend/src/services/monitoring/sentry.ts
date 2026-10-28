import * as Sentry from '@sentry/react';

export function initSentry() {
  try {
    if (import.meta.env.VITE_ENABLE_SENTRY !== 'true') {
      return;
    }
    const dsn = import.meta.env.VITE_SENTRY_DSN;
    if (!dsn) {
      console.warn('[Sentry] VITE_SENTRY_DSN not set, skipping initialization');
      return;
    }
    Sentry.init({
      dsn,
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
