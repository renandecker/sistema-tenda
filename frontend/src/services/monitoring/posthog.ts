import posthog from 'posthog-js';

export function initPostHog() {
  try {
    if (import.meta.env.VITE_ENABLE_POSTHOG !== 'true') {
      return;
    }
    const apiKey = import.meta.env.VITE_POSTHOG_API_KEY;
    const apiHost = import.meta.env.VITE_POSTHOG_HOST || 'https://app.posthog.com';

    if (!apiKey) {
      console.warn('[PostHog] VITE_POSTHOG_API_KEY not set, skipping initialization');
      return;
    }

    posthog.init(apiKey, {
      api_host: apiHost,
      loaded: () => {},
      capture_pageview: true,
      capture_pageleave: true,
    });
    console.log('[PostHog] Initialized successfully');
  } catch (error) {
    console.error('[PostHog] Failed to initialize:', error);
  }
}

export { posthog };
