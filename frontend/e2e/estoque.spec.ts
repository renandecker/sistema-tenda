import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk } from './nav';

test.describe('Estoque', () => {
  test('Movimentação: carrega saldos e histórico do backend', async ({ page }) => {
    const resp = esperarApi(page, '/api/estoque');
    await irPara(page, '/estoque');
    await expect(page.getByRole('heading', { name: 'Movimentação de Estoque' })).toBeVisible();
    expectOk(await resp);

    await page.getByRole('button', { name: 'Registrar estoque' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Registrar quantidade em estoque' })).toBeVisible();
    await expect(dialog.getByLabel('Produto')).toBeVisible();
    await expect(dialog.getByLabel('Quantidade')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Inventário: abre o diálogo de inventário', async ({ page }) => {
    await irPara(page, '/estoque/inventario');
    await expect(page.getByRole('heading', { name: 'Inventário', exact: true })).toBeVisible();

    await page.getByRole('button', { name: 'Abrir inventário' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Abrir inventário' })).toBeVisible();
    await expect(dialog.getByLabel(/Responsável/)).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Alertas: carrega a tela', async ({ page }) => {
    const resp = esperarApi(page, '/api/estoque');
    await irPara(page, '/estoque/alertas');
    await expect(page.getByRole('heading', { name: 'Alertas de Estoque' })).toBeVisible();
    expectOk(await resp);
  });

  test('Defeitos: abre o registro de defeito', async ({ page }) => {
    await irPara(page, '/estoque/defeitos');
    await expect(page.getByRole('heading', { name: 'Gestão de Defeitos e Qualidade' })).toBeVisible();

    await page.getByRole('button', { name: 'Registrar Defeito' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Registrar Defeito / Qualidade' })).toBeVisible();
    await expect(dialog.getByRole('spinbutton', { name: 'Quantidade', exact: true })).toBeVisible();
    await expect(dialog.getByRole('textbox', { name: 'Responsável' })).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });
});
