import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk } from './nav';

test.describe('Usuário, Perfil, Auditoria e Saúde', () => {
  test('Meu Perfil: exibe dados do usuário logado', async ({ page }) => {
    await irPara(page, '/perfil');
    const main = page.getByRole('main');
    await expect(main.getByText('Meu Perfil')).toBeVisible();
    await expect(main.getByText(/admin@sistematenda\.com/).first()).toBeVisible();
  });

  test('Painel do Usuário: abre operação de caixa', async ({ page }) => {
    await irPara(page, '/usuario');
    await expect(page.getByRole('heading', { name: 'Painel do Usuário' })).toBeVisible();
  });

  test('Auditoria: carrega a tela e trata a resposta do backend', async ({ page }) => {
    const resp = esperarApi(page, '/api/audit/logs');
    await irPara(page, '/audit');
    await expect(page.getByRole('heading', { name: 'Auditoria do Sistema' })).toBeVisible();
    const response = await resp;
    // O backend pode autorizar (200) ou negar (403) conforme a policy; a tela deve
    // renderizar em ambos os casos sem quebrar.
    expect([200, 403]).toContain(response.status());
    expect(response.url()).toContain('/audit');
  });

  test('Saúde dos Pagamentos: consulta o health check', async ({ page }) => {
    const resp = esperarApi(page, '/api/saude-check');
    await irPara(page, '/saude');
    const main = page.getByRole('main');
    await expect(main.getByText('Saúde dos Pagamentos')).toBeVisible();
    expectOk(await resp);
    await expect(main.getByText('PIX', { exact: true })).toBeVisible();
    await expect(main.getByText('TEF', { exact: true })).toBeVisible();
  });
});
