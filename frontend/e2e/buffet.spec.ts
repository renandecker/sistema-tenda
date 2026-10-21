import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk } from './nav';

test.describe('Buffet', () => {
  test('Produtos: lista do backend e abre cadastro', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/produtos');
    await irPara(page, '/buffet/produtos');
    await expect(page.getByRole('heading', { name: 'Produtos — Buffet' })).toBeVisible();
    expectOk(await lista);

    await page.getByRole('button', { name: 'Novo produto' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Novo produto do buffet' })).toBeVisible();
    await expect(dialog.getByLabel('Nome do prato/produto')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Mesas: mapa carrega e abre cadastro de mesa', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/mesas');
    await irPara(page, '/buffet/mesas');
    await expect(page.getByRole('heading', { name: 'Mesas — Buffet' })).toBeVisible();
    await expect(page.getByText(/Mapa em tempo real/)).toBeVisible();
    expectOk(await lista);

    await page.getByRole('button', { name: 'Nova mesa' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova mesa' })).toBeVisible();
    await expect(dialog.getByLabel('Número')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Comandas: lista e abre nova comanda', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/comandas');
    await irPara(page, '/buffet/comandas');
    await expect(page.getByRole('heading', { name: 'Comandas - Buffet' })).toBeVisible();
    expectOk(await lista);

    await page.getByRole('button', { name: 'Nova Comanda' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova Comanda' })).toBeVisible();
    await expect(dialog.getByLabel(/No da Comanda/)).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Clientes Buffet: carrega a tela com dados da API', async ({ page }) => {
    const resp = esperarApi(page, '/api/buffet/clientes');
    await irPara(page, '/buffet/clientes');
    expectOk(await resp);
  });
});
