import React from 'react';

let rollbarInstance: any = null;

export function initRollbar() {
  try {
    const accessToken = import.meta.env.VITE_ROLLBAR_ACCESS_KEY || 'your-rollbar-access-token';
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