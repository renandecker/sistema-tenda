import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk, acaoPorIcone } from './nav';

test.describe('PDV e Vendas', () => {
  test('PDV: frente de caixa carrega e oferece as ações principais', async ({ page }) => {
    await irPara(page, '/pdv');
    await expect(page.getByRole('heading', { name: 'PDV — Frente de Caixa' })).toBeVisible();

    const main = page.getByRole('main');
    await expect(main.getByRole('button', { name: 'Nova venda' })).toBeVisible();
    await expect(main.getByRole('button', { name: /Buscar/ })).toBeVisible();
    await expect(main.getByRole('button', { name: 'Caderno (fiado)' })).toBeVisible();
    await expect(main.getByRole('button', { name: 'Pagamento' })).toBeDisabled();
    await expect(main.getByRole('button', { name: 'Finalizar' })).toBeDisabled();

    for (const aba of ['Buffet', 'Fruteira', 'Mesas']) {
      await expect(main.getByRole('tab', { name: aba })).toBeVisible();
    }
  });

  test('PDV: controles de venda presentes e fiado exige cliente', async ({ page }) => {
    await irPara(page, '/pdv');
    const main = page.getByRole('main');
    await expect(main.getByRole('heading', { name: 'PDV — Frente de Caixa' })).toBeVisible();

    await expect(main.getByRole('button', { name: 'Nova venda' })).toBeEnabled();
    await expect(main.getByRole('button', { name: 'Caderno (fiado)' })).toBeEnabled();
    await expect(main.getByRole('button', { name: 'Lançar fiado' })).toBeEnabled();
    await expect(main.getByRole('button', { name: 'Aplicar' })).toBeDisabled();
    await expect(main.getByRole('button', { name: 'Pagamento' })).toBeDisabled();

    // Sem cliente selecionado, "Caderno (fiado)" não abre diálogo (exibe aviso)
    await main.getByRole('button', { name: 'Caderno (fiado)' }).click();
    await expect(page.getByRole('dialog')).toBeHidden();
    await expect(page.getByText('Selecione o cliente para lançar a dívida')).toBeVisible();
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

  test('Vendas: abre comprovante e cancelamento de uma venda', async ({ page }) => {
    await irPara(page, '/vendas');
    await expect(page.getByRole('heading', { name: 'Vendas', exact: true })).toBeVisible();

    await page.getByRole('button', { name: 'Detalhes / comprovante' }).first().click();
    let dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Venda .* comprovante não fiscal/ })).toBeVisible();
    await dialog.getByRole('button', { name: 'Fechar' }).click();
    await expect(page.getByRole('dialog')).toBeHidden();

    // O ícone de cancelar não tem aria-label (Tooltip envolto por <span>)
    await acaoPorIcone(page, 'CancelIcon').first().click();
    dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Cancelar venda' })).toBeVisible();
    await expect(dialog.getByLabel(/Motivo do cancelamento/)).toBeVisible();
    await dialog.getByRole('button', { name: 'Voltar' }).click();
  });

  test('Sessões de Caixa: lista e abre o diálogo de abrir caixa', async ({ page }) => {
    const lista = esperarApi(page, '/api/pdv/sessoes-caixa');
    await irPara(page, '/sessoes-caixa');
    await expect(page.getByRole('heading', { name: 'Sessões de Caixa' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Abrir caixa' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Abrir caixa' })).toBeVisible();
    await expect(dialog.getByLabel('Operador')).toBeVisible();
    await expect(dialog.getByLabel('Fundo inicial (R$)')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Caderno: lista devedores e abre novo lançamento', async ({ page }) => {
    const clientes = esperarApi(page, '/api/pdv/caderno/clientes');
    await irPara(page, '/caderno');
    await expect(page.getByRole('heading', { name: 'Caderno de Dívidas' })).toBeVisible();
    expectOk(await clientes);

    await page.getByRole('main').getByRole('button', { name: 'Novo lançamento' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Novo lançamento no caderno' })).toBeVisible();
    await expect(dialog.getByLabel('Valor da dívida (R$)')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });
});
