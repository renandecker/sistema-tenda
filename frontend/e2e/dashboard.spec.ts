import { test, expect } from '@playwright/test';
import { loadEnvFile } from './env';

loadEnvFile('.env.e2e');

test.describe('Dashboard', () => {
  test('deve exibir o cabeçalho e os cartões de métricas', async ({ page }) => {
    await page.goto('/dashboard');

    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    await expect(page.getByText('Vendas Hoje')).toBeVisible();
    await expect(page.getByText('Comandas Abertas')).toBeVisible();
    await expect(page.getByText('Estoque Baixo')).toBeVisible();
    await expect(page.getByText('Alertas Ativos')).toBeVisible();
  });

  test('deve carregar os totais de venda do backend real', async ({ page }) => {
    const resposta = page.waitForResponse(
      (r) => r.url().includes('/api/pdv/vendas/total-vendas') && r.status() === 200
    );

    await page.goto('/dashboard');
    await resposta;

    const cards = page.locator('.MuiCard-root');
    await expect(cards.getByText(/R\$\s*[\d.]+,\d{2}/).first()).toBeVisible();
  });
});
