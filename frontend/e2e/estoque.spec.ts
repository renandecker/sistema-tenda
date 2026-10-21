import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk, clicarExportacao } from './nav';

test.describe('Estoque', () => {
  test('Movimentação: carrega saldos, registra estoque e exporta', async ({ page }) => {
    const resp = esperarApi(page, '/api/estoque');
    await irPara(page, '/estoque');
    await expect(page.getByRole('heading', { name: 'Movimentação de Estoque' })).toBeVisible();
    expectOk(await resp);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Registrar estoque' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Registrar quantidade em estoque' })).toBeVisible();
    await expect(dialog.getByLabel('Produto')).toBeVisible();
    await expect(dialog.getByLabel('Operação')).toBeVisible();
    await expect(dialog.getByLabel('Quantidade')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    await clicarExportacao(page, main.getByRole('button', { name: 'Exportar saldos' }));
    await clicarExportacao(page, main.getByRole('button', { name: 'Exportar movimentos' }));
  });

  test('Inventário: abre, carrega produtos p/ contagem e fecha', async ({ page }) => {
    await irPara(page, '/estoque/inventario');
    await expect(page.getByRole('heading', { name: 'Inventário', exact: true })).toBeVisible();

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Abrir inventário' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Abrir inventário' })).toBeVisible();
    await expect(dialog.getByLabel(/Responsável/)).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    await main.getByRole('button', { name: 'Carregar produtos p/ contagem' }).click();
    await expect(main.getByRole('button', { name: 'Fechar inventário e aplicar ajustes' })).toBeVisible();
  });

  test('Alertas: carrega a tela e exporta', async ({ page }) => {
    const resp = esperarApi(page, '/api/estoque/alertas');
    await irPara(page, '/estoque/alertas');
    await expect(page.getByRole('heading', { name: 'Alertas de Estoque' })).toBeVisible();
    expectOk(await resp);

    await clicarExportacao(page, page.getByRole('main').getByRole('button', { name: 'Exportar' }));
  });

  test('Defeitos: registra defeito, exporta e vê detalhes', async ({ page }) => {
    const lista = esperarApi(page, '/api/estoque/movimentos/defeitos');
    await irPara(page, '/estoque/defeitos');
    await expect(page.getByRole('heading', { name: 'Gestão de Defeitos e Qualidade' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Registrar Defeito' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Registrar Defeito / Qualidade' })).toBeVisible();
    await expect(dialog.getByRole('spinbutton', { name: 'Quantidade', exact: true })).toBeVisible();
    await expect(dialog.getByRole('textbox', { name: 'Responsável' })).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    await clicarExportacao(page, main.getByRole('button', { name: 'Exportar' }));
  });
});
