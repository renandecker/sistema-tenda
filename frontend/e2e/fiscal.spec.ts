import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk } from './nav';

test.describe('Fiscal / Contábil', () => {
  test('NFC-e/Notas: carrega do backend e expõe ações', async ({ page }) => {
    const resp = esperarApi(page, '/api/fiscal/nfe');
    await irPara(page, '/fiscal/nfe');
    await expect(page.getByRole('heading', { name: 'NFC-e / Notas Fiscais' })).toBeVisible();
    expectOk(await resp);
    await expect(page.getByRole('button', { name: 'Atualizar' })).toBeVisible();
  });

  test('SPED Fiscal: carrega e valida período', async ({ page }) => {
    await irPara(page, '/fiscal/sped-fiscal');
    await expect(page.getByRole('heading', { name: 'SPED Fiscal' })).toBeVisible();

    await page.getByRole('button', { name: 'Validar registros' }).click();
    await expect(page.getByText('Informe o período de apuração')).toBeVisible();
  });

  test('DRE: carrega e apura período', async ({ page }) => {
    const resp = esperarApi(page, '/api/fiscal/dre');
    await irPara(page, '/fiscal/dre');
    await expect(page.getByRole('heading', { name: 'DRE — Resultado do Exercício' })).toBeVisible();

    await page.getByRole('button', { name: 'Apurar período' }).click();
    expectOk(await resp);
  });

  test('Ativos Imobilizados: abre o cadastro de bem', async ({ page }) => {
    await irPara(page, '/fiscal/ativos');
    await expect(page.getByRole('heading', { name: 'Ativos Imobilizados' })).toBeVisible();

    await page.getByRole('button', { name: 'Novo bem' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Novo bem do imobilizado/ })).toBeVisible();
    await expect(dialog.getByLabel('Descrição do bem')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Conciliação de Cartão: carrega e importa extrato', async ({ page }) => {
    await irPara(page, '/fiscal/conciliacao-cartao');
    await expect(page.getByRole('heading', { name: 'Conciliação de Cartão' })).toBeVisible();

    await page.getByRole('button', { name: 'Importar extrato adquirente' }).click();
  });
});
