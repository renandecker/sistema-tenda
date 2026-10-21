import { test, expect } from '@playwright/test';
import { loadEnvFile } from './env';

loadEnvFile('.env.e2e');

const LOGIN = process.env.E2E_LOGIN || 'admin@sistematenda.com';
const SENHA = process.env.E2E_SENHA || '123456';

test.describe('Tela de Login', () => {
  test.use({ storageState: { cookies: [], origins: [] } });

  test('deve exibir os elementos principais', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByRole('heading', { name: 'Sistema Tenda' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Entrar no sistema' })).toBeVisible();
    await expect(page.getByLabel('Email', { exact: true })).toBeVisible();
    await expect(page.getByLabel('Senha', { exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Entrar', exact: true })).toBeVisible();
  });

  test('deve exibir erros de validação com campos vazios', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    await expect(page.getByText('Email é obrigatório')).toBeVisible();
    await expect(page.getByText('Senha deve ter no mínimo 6 caracteres')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('deve rejeitar email em formato inválido', async ({ page }) => {
    await page.goto('/login');

    const email = page.getByLabel('Email', { exact: true });
    await email.fill('nao-e-email');
    await page.getByLabel('Senha', { exact: true }).fill(SENHA);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    await expect(email).toHaveJSProperty('validity.valid', false);
    await expect(page).toHaveURL(/\/login/);
  });

  test('deve autenticar com credenciais reais e acessar o dashboard', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Email', { exact: true }).fill(LOGIN);
    await page.getByLabel('Senha', { exact: true }).fill(SENHA);
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    await expect(page).toHaveURL(/\/dashboard/, { timeout: 30_000 });
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();

    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
  });

  test('deve exibir erro com senha incorreta (backend real)', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Email', { exact: true }).fill(LOGIN);
    await page.getByLabel('Senha', { exact: true }).fill('senha-invalida-123');
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();

    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('deve alternar a visibilidade da senha', async ({ page }) => {
    await page.goto('/login');

    const senha = page.getByLabel('Senha', { exact: true });
    await expect(senha).toHaveAttribute('type', 'password');

    await page.getByRole('button', { name: 'Mostrar senha' }).click();
    await expect(senha).toHaveAttribute('type', 'text');

    await page.getByRole('button', { name: 'Mostrar senha' }).click();
    await expect(senha).toHaveAttribute('type', 'password');
  });
});
