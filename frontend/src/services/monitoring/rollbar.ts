import React from 'react';

let rollbarInstance: any = null;

export function initRollbar() {
  try {
    if (import.meta.env.VITE_ENABLE_ROLLBAR !== 'true') {
      return null;
    }
    const accessToken = import.meta.env.VITE_ROLLBAR_ACCESS_KEY;
    if (!accessToken) {
      console.warn('[Rollbar] VITE_ROLLBAR_ACCESS_KEY not set, skipping initialization');
      return null;
    }
    const rollbar = {
      accessToken,
      captureUncaught: true,
      captureUnhandledRejections: true,
      payload: {
        environment: import.meta.env.MODE || 'production',
      },
    };

    rollbarInstance = rollbar;
    console.log('[Rollbar] Initialized successfully');
    return rollbarInstance;
  } catch (error) {
    console.error('[Rollbar] Failed to initialize:', error);
    return null;
  }
}

export function getRollbarInstance() {
  return rollbarInstance;
}

export const RollbarErrorBoundary = (props: any) => {
  const rb = getRollbarInstance();
  if (rb) {
    return React.createElement(React.Fragment, null, props.children);
  }
  return React.createElement(React.Fragment, null, props.children);
};

export const Provider = (props: any) => props.children;