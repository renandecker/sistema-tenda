import posthog from 'posthog-js';

export function initPostHog() {
  try {
    const apiKey = import.meta.env.VITE_POSTHOG_API_KEY || 'phc_your_posthog_api_key_here';
    const apiHost = import.meta.env.VITE_POSTHOG_HOST || 'https://app.posthog.com';

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
