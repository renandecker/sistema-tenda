import { test as setup, expect } from '@playwright/test';
import { loadEnvFile } from './env';

loadEnvFile('.env.e2e');

const authFile = 'e2e/.auth/user.json';
const LOGIN = process.env.E2E_LOGIN || 'admin@sistematenda.com';
const SENHA = process.env.E2E_SENHA || '123456';

setup('autenticar via UI e salvar sessão', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Email', { exact: true }).fill(LOGIN);
  await page.getByLabel('Senha', { exact: true }).fill(SENHA);
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();

  await expect(page).toHaveURL(/\/dashboard/, { timeout: 30_000 });

  await page.context().storageState({ path: authFile });
});
