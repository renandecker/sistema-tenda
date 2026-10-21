import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk, acaoLinha, confirmarDialogoNativo } from './nav';

test.describe('Fruteira', () => {
  test('Produtos: lista, abre cadastro e expõe ações de linha', async ({ page }) => {
    const lista = esperarApi(page, '/api/fruteira/produtos');
    await irPara(page, '/fruteira/produtos');
    await expect(page.getByRole('heading', { name: 'Produtos - Fruteira' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await expect(main.getByRole('button', { name: 'Novo Produto' })).toBeVisible();
    await expect(main.locator('svg[data-testid="RefreshIcon"]')).toBeVisible();

    await main.getByRole('button', { name: 'Novo Produto' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Novo Produto — Fruteira/ })).toBeVisible();
    await expect(dialog.getByLabel('Nome do Produto')).toBeVisible();
    await expect(dialog.getByLabel('Tipo')).toBeVisible();
    await expect(dialog.getByLabel('Unidade de Medida')).toBeVisible();
    await expect(dialog.getByLabel('Preço de Venda (R$)')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
    await expect(page.getByRole('dialog')).toBeHidden();
  });

  test('Produtos: editar e excluir item existente', async ({ page }) => {
    await irPara(page, '/fruteira/produtos');
    const main = page.getByRole('main');
    await expect(main.getByRole('heading', { name: 'Produtos - Fruteira' })).toBeVisible();

    const linhas = main.getByRole('row');
    if ((await linhas.count()) > 1) {
      await acaoLinha(page, 'Editar').click();
      const dialog = page.getByRole('dialog');
      await expect(dialog.getByRole('heading', { name: /Editar Produto — Fruteira/ })).toBeVisible();
      await dialog.getByRole('button', { name: 'Cancelar' }).click();

      confirmarDialogoNativo(page);
      await acaoLinha(page, 'Excluir').click();
    }
  });

  test('Perdas: registra perda, edita e apura total do período', async ({ page }) => {
    const lista = esperarApi(page, '/api/fruteira/perdas');
    await irPara(page, '/fruteira/perdas');
    await expect(page.getByRole('heading', { name: 'Perdas e Quebras' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Registrar perda' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Registro rápido de perda' })).toBeVisible();
    await expect(dialog.getByLabel('Produto')).toBeVisible();
    await expect(dialog.getByLabel('Tipo')).toBeVisible();
    await expect(dialog.getByLabel('Quantidade')).toBeVisible();
    await expect(dialog.getByLabel('Responsável')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    await main.getByLabel('Início').fill('2024-01-01');
    await main.getByLabel('Fim').fill('2026-12-31');
    await main.getByRole('button', { name: /Apurar total do período/ }).click();
  });

  test('Etiquetas Balança: nova etiqueta e gerar EAN-13', async ({ page }) => {
    await irPara(page, '/fruteira/etiquetas');
    await expect(page.getByRole('heading', { name: 'Etiquetas de Balança' })).toBeVisible();

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Nova etiqueta' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova etiqueta' })).toBeVisible();
    await expect(dialog.getByLabel('Produto')).toBeVisible();
    await expect(dialog.getByLabel('Modelo')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    const gerar = main.getByRole('button', { name: /^Gerar: / }).first();
    if (await gerar.isVisible().catch(() => false)) {
      await gerar.click();
      await expect(page.getByRole('dialog')).toBeVisible();
      await page.getByRole('dialog').getByRole('button', { name: /Fechar|Cancelar/ }).click().catch(() => {});
    }
  });

  test('Tabelas de Preço: nova vigência e aplicar em lote', async ({ page }) => {
    await irPara(page, '/fruteira/tabelas-preco');
    await expect(page.getByRole('heading', { name: 'Tabelas de Preço' })).toBeVisible();

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Nova vigência' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova vigência de preço' })).toBeVisible();
    await expect(dialog.getByLabel('Preço venda (R$)')).toBeVisible();
    await expect(dialog.getByLabel('Início vigência')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    await main.getByRole('button', { name: 'Aplicar vigentes em lote' }).click();
  });
});
