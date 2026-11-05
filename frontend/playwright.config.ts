import { defineConfig, devices } from '@playwright/test';
import { loadEnvFile } from './e2e/env';

loadEnvFile('.env.e2e');

const baseURL = process.env.E2E_BASE_URL || 'http://localhost:3000';
const apiURL = process.env.E2E_API_URL || 'http://localhost:8080/api';
const webCommand = process.env.E2E_WEB_COMMAND || 'npm run dev';

/** Origem do backend (sem /api) usada pelo proxy do Vite. */
const apiOrigin = apiURL.replace(/\/api\/?$/, '').replace('//localhost', '//127.0.0.1');
/** Porta em que o Vite deve subir durante os testes. */
const webPort = (() => {
  try {
    return new URL(baseURL).port || '3000';
  } catch {
    return '3000';
  }
})();

export default defineConfig({
  testDir: './e2e',
  globalSetup: './e2e/global-setup.ts',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  expect: { timeout: 15_000 },
  use: {
    baseURL,
    locale: 'pt-BR',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    navigationTimeout: 60_000,
    actionTimeout: 20_000,
  },
  projects: [
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['setup'],
      testIgnore: /auth\.setup\.ts/,
    },
  ],
  webServer: {
    command: webCommand,
    url: baseURL,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: {
      VITE_PORT: webPort,
      VITE_API_PROXY: apiOrigin,
    },
  },
  metadata: { apiURL },
});
