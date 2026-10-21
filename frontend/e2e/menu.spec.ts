import { test, expect } from '@playwright/test';
import { abrirMenu } from './nav';

test.describe('Menu lateral', () => {
  test('revela o menu ao passar o mouse na borda e navega pelas seções', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();

    const nav = await abrirMenu(page);

    for (const secao of ['Fruteira', 'Buffet', 'Estoque', 'Fiscal/Contábil']) {
      await expect(nav.getByText(secao, { exact: true })).toBeVisible();
    }

    await nav.getByText('NFC-e/Notas', { exact: true }).click();
    await expect(page).toHaveURL(/\/fiscal\/nfe/);
  });
});
