import bugsnag from '@bugsnag/js';
import React from 'react';

let bugsnagPlugin: any = null;

export function initBugsnag() {
  try {
    if (import.meta.env.VITE_ENABLE_BUGSNAG !== 'true') {
      return null;
    }
    const apiKey = import.meta.env.VITE_BUGSNAG_API_KEY;
    if (!apiKey) {
      console.warn('[Bugsnag] VITE_BUGSNAG_API_KEY not set, skipping initialization');
      return null;
    }
    const bugsnagClient = bugsnag.start({
      apiKey,
      releaseStage: import.meta.env.MODE || 'production',
    });

    bugsnagPlugin = bugsnagClient.getPlugin('react');
    console.log('[Bugsnag] Initialized successfully');
    return bugsnagClient;
  } catch (error) {
    console.error('[Bugsnag] Failed to initialize:', error);
    return null;
  }
}

export function getBugsnagErrorBoundary() {
  if (bugsnagPlugin) {
    return bugsnagPlugin.createErrorBoundary(React);
  }
  return class FallbackBugsnagBoundary extends React.Component<{ children: React.ReactNode }> {
    render() {
      return this.props.children;
    }
  };
}
