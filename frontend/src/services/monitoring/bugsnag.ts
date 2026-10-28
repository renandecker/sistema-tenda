import bugsnag from '@bugsnag/js';
import React from 'react';

let bugsnagPlugin: any = null;

export function initBugsnag() {
  try {
    const bugsnagClient = bugsnag.start({
      apiKey: import.meta.env.VITE_BUGSNAG_API_KEY || 'your-bugsnag-api-key',
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
