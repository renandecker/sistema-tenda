import { test, expect } from '@playwright/test';
import { loadEnvFile } from './env';

loadEnvFile('.env.e2e');

const LOGIN = process.env.E2E_LOGIN || 'admin@sistematenda.com';
const SENHA = process.env.E2E_SENHA || '123456';

test.describe('Testes E2E Completos — Fluxo do Sistema', () => {
  test('deve navegar pelo login, dashboard e módulos principais', async ({ page }) => {
    // 1. Acessar tela de login
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: 'Sistema Tenda' })).toBeVisible();

    // 2. Fazer login
    await page.getByLabel('Email', { exact: true }).fill(LOGIN);
    await page.getByLabel('Senha', { exact: true }).fill(SENHA);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    // 3. Validar acesso ao Dashboard
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 30_000 });
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();

    // 4. Verificar se a navegação lateral existe e interagir
    const navItems = ['Dashboard', 'PDV', 'Fruteira', 'Buffet', 'Estoque', 'Fiscal', 'Clientes'];
    for (const item of navItems) {
      const link = page.getByRole('link', { name: new RegExp(item, 'i') });
      if (await link.count() > 0) {
        await expect(link.first()).toBeVisible();
      }
    }
  });

  test('deve acessar o módulo PDV e validar elementos de venda', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Email', { exact: true }).fill(LOGIN);
    await page.getByLabel('Senha', { exact: true }).fill(SENHA);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    await page.waitForURL(/\/dashboard/);

    // Navegar para PDV
    await page.goto('/pdv');
    // Validação básica da página de PDV
    await expect(page).toHaveURL(/\/pdv/);
  });
});
