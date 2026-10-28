import rollbar from '@rollbar/react';

let rollbarInstance: any = null;

export function initRollbar() {
  try {
    const accessToken = import.meta.env.VITE_ROLLBAR_ACCESS_KEY || 'your-rollbar-access-token';

    rollbarInstance = rollbar.init({
      accessToken: accessToken,
      captureUncaught: true,
      captureUnhandledRejections: true,
      payload: {
        environment: import.meta.env.MODE || 'production',
      },
    });

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
    return rb.getErrorBoundary()(props);
  }
  return null;
};