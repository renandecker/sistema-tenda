import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk } from './nav';

test.describe('Fruteira', () => {
  test('Produtos: lista produtos do backend e abre o cadastro', async ({ page }) => {
    const lista = page.waitForResponse((r) =>
      r.url().includes('/api/fruteira/produtos') &&
      r.request().method() === 'GET' &&
      !r.url().includes('/codigo')
    );

    await irPara(page, '/fruteira/produtos');
    await expect(page.getByRole('heading', { name: 'Produtos - Fruteira' })).toBeVisible();
    expectOk(await lista);

    await page.getByRole('button', { name: 'Novo Produto' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Novo Produto — Fruteira/ })).toBeVisible();
    await expect(dialog.getByLabel('Nome do Produto')).toBeVisible();
    await expect(dialog.getByLabel('Preço de Venda (R$)')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
    await expect(page.getByRole('dialog')).toBeHidden();
  });

  test('Perdas: abre o registro rápido de perda', async ({ page }) => {
    await irPara(page, '/fruteira/perdas');
    await expect(page.getByRole('heading', { name: 'Perdas e Quebras' })).toBeVisible();

    await page.getByRole('button', { name: 'Registrar perda' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Registro rápido de perda' })).toBeVisible();
    await expect(dialog.getByLabel('Produto')).toBeVisible();
    await expect(dialog.getByLabel('Quantidade')).toBeVisible();
    await expect(dialog.getByLabel('Tipo')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Tabelas de Preço: abre nova vigência', async ({ page }) => {
    await irPara(page, '/fruteira/tabelas-preco');
    await expect(page.getByRole('heading', { name: 'Tabelas de Preço' })).toBeVisible();

    await page.getByRole('button', { name: 'Nova vigência' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova vigência de preço' })).toBeVisible();
    await expect(dialog.getByLabel('Preço venda (R$)')).toBeVisible();
    await expect(dialog.getByLabel('Início vigência')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Etiquetas Balança: carrega a tela com dados da API', async ({ page }) => {
    const resp = esperarApi(page, '/api/fruteira');
    await irPara(page, '/fruteira/etiquetas');
    await expect(page.getByRole('heading', { name: 'Etiquetas de Balança' })).toBeVisible();
    expectOk(await resp);
  });
});
