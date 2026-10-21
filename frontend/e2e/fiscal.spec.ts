import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk, clicarExportacao, confirmarDialogoNativo, selecionarOpcao } from './nav';

test.describe('Fiscal / Contábil', () => {
  test('NFC-e/Notas: carrega a tela e usa os filtros', async ({ page }) => {
    const resp = esperarApi(page, '/api/fiscal/nfe');
    await irPara(page, '/fiscal/nfe');
    await expect(page.getByRole('heading', { name: 'NFC-e / Notas Fiscais' })).toBeVisible();
    await resp;

    const main = page.getByRole('main');
    await expect(main.getByRole('button', { name: 'Atualizar' })).toBeVisible();
    await expect(main.getByLabel('Status')).toBeVisible();
    await expect(main.getByLabel('Modelo')).toBeVisible();
    await expect(main.getByLabel('Início')).toBeVisible();
    await expect(main.getByLabel('Fim')).toBeVisible();

    await main.getByLabel('Status').selectOption({ label: 'Autorizada' });
    await main.getByLabel('Modelo').selectOption({ label: 'NFC-e (65)' });
    await main.getByLabel('Início').fill('2024-01-01');
    await main.getByLabel('Fim').fill('2026-12-31');
    await main.getByRole('button', { name: 'Atualizar' }).click();
  });

  test('SPED Fiscal: valida período e gera arquivo', async ({ page }) => {
    await irPara(page, '/fiscal/sped-fiscal');
    await expect(page.getByRole('heading', { name: 'SPED Fiscal' })).toBeVisible();

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Validar registros' }).click();
    await expect(main.getByText('Informe o período de apuração')).toBeVisible();

    await selecionarOpcao(page, 'Tipo', 'SPED Fiscal — EFD ICMS/IPI');
    await main.getByLabel('Início').fill('2024-01-01');
    await main.getByLabel('Fim').fill('2026-12-31');

    await clicarExportacao(page, main.getByRole('button', { name: 'Gerar arquivo' }));
  });

  test('DRE: carrega, apura período e exporta CSV', async ({ page }) => {
    const resp = esperarApi(page, '/api/fiscal/dre');
    await irPara(page, '/fiscal/dre');
    await expect(page.getByRole('heading', { name: 'DRE — Resultado do Exercício' })).toBeVisible();
    expectOk(await resp);

    const main = page.getByRole('main');
    await main.getByLabel('Início').fill('2024-01-01');
    await main.getByLabel('Fim').fill('2026-12-31');
    await main.getByRole('button', { name: 'Apurar período' }).click();

    await clicarExportacao(page, main.getByRole('button', { name: 'Exportar CSV' }));
  });

  test('Ativos Imobilizados: cadastra, edita e baixa um bem', async ({ page }) => {
    const resp = esperarApi(page, '/api/fiscal/ativos');
    await irPara(page, '/fiscal/ativos');
    await expect(page.getByRole('heading', { name: 'Ativos Imobilizados' })).toBeVisible();
    expectOk(await resp);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Novo bem' }).click();
    let dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Novo bem do imobilizado/ })).toBeVisible();
    await expect(dialog.getByLabel('Descrição do bem')).toBeVisible();
    await expect(dialog.getByLabel('Categoria')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    if ((await main.getByRole('row').count()) > 1) {
      await page.getByRole('button', { name: 'Editar' }).first().click();
      dialog = page.getByRole('dialog');
      await expect(dialog.getByRole('heading', { name: 'Editar bem' })).toBeVisible();
      await dialog.getByRole('button', { name: 'Cancelar' }).click();

      confirmarDialogoNativo(page);
      await main.getByRole('button', { name: 'Baixar' }).first().click();
    }
  });

  test('Conciliação de Cartão: carrega e importa extrato', async ({ page }) => {
    const resp = esperarApi(page, '/api/fiscal/conciliacao-cartao');
    await irPara(page, '/fiscal/conciliacao-cartao');
    await expect(page.getByRole('heading', { name: 'Conciliação de Cartão' })).toBeVisible();
    expectOk(await resp);

    await page.getByRole('main').getByRole('button', { name: 'Importar extrato adquirente' }).click();
  });
});
