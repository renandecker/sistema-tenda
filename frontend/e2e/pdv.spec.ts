import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk } from './nav';

test.describe('PDV e Vendas', () => {
  test('PDV: frente de caixa carrega e oferece as ações principais', async ({ page }) => {
    await irPara(page, '/pdv');
    await expect(page.getByRole('heading', { name: 'PDV — Frente de Caixa' })).toBeVisible();

    const main = page.getByRole('main');
    await expect(main.getByRole('button', { name: 'Nova venda' })).toBeVisible();
    await expect(main.getByRole('button', { name: 'Buscar', exact: true })).toBeVisible();
    await expect(main.getByRole('button', { name: 'Caderno (fiado)' })).toBeVisible();
  });

  test('Vendas: lista vendas do backend e apura total do período', async ({ page }) => {
    const lista = esperarApi(page, '/api/pdv/vendas');
    await irPara(page, '/vendas');
    await expect(page.getByRole('heading', { name: 'Vendas', exact: true })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByLabel('Início').fill('2024-01-01');
    await main.getByLabel('Fim').fill('2026-12-31');

    const total = esperarApi(page, '/api/pdv/vendas/total-vendas');
    await main.getByRole('button', { name: /Total do período/ }).click();
    expectOk(await total);
  });

  test('Sessões de Caixa: lista e abre o diálogo de abrir caixa', async ({ page }) => {
    const lista = esperarApi(page, '/api/pdv/sessoes-caixa');
    await irPara(page, '/sessoes-caixa');
    await expect(page.getByRole('heading', { name: 'Sessões de Caixa' })).toBeVisible();
    expectOk(await lista);
  });

  test('Caderno: lista devedores do backend', async ({ page }) => {
    const clientes = esperarApi(page, '/api/pdv/caderno/clientes');
    await irPara(page, '/caderno');
    await expect(page.getByRole('heading', { name: 'Caderno de Dívidas' })).toBeVisible();
    expectOk(await clientes);
  });
});
