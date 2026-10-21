import { test, expect } from '@playwright/test';

test.describe('Autenticação e navegação', () => {
  test('rota privada sem sessão deve redirecionar para /login', async ({ browser }) => {
    const context = await browser.newContext({ storageState: { cookies: [], origins: [] } });
    const page = await context.newPage();

    await page.goto('/dashboard');

    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole('heading', { name: 'Entrar no sistema' })).toBeVisible();
    await context.close();
  });

  test('deve navegar pelo menu lateral para as telas principais', async ({ page }) => {
    const telas: { menu: string; url: RegExp; heading: string }[] = [
      { menu: 'Vendas', url: /\/vendas/, heading: 'Vendas' },
      { menu: 'Sessões de Caixa', url: /\/sessoes-caixa/, heading: 'Sessões de Caixa' },
      { menu: 'Caderno (Fiado)', url: /\/caderno/, heading: 'Caderno de Dívidas' },
    ];

    await page.goto('/dashboard');
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();

    for (const { menu, url, heading } of telas) {
      await page.mouse.move(6, 60);
      const nav = page.getByRole('navigation', { name: 'Main navigation' });
      await expect(nav.getByText(menu, { exact: true })).toBeVisible();
      await nav.getByText(menu, { exact: true }).click();

      await expect(page).toHaveURL(url);
      await expect(page.getByRole('heading', { name: heading, exact: true })).toBeVisible();
      await page.goto('/dashboard');
    }
  });

  test('deve sair do sistema e voltar para o login', async ({ page }) => {
    await page.goto('/dashboard');

    await page
      .locator('.MuiToolbar-root button')
      .filter({ has: page.locator('.MuiAvatar-root') })
      .click();
    await page.getByRole('menuitem', { name: 'Sair' }).click();

    await expect(page).toHaveURL(/\/login/);
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeNull();
  });
});
