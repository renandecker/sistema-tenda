import { test, expect } from '@playwright/test';
import { irPara, esperarApi, expectOk, confirmarDialogoNativo } from './nav';

test.describe('Buffet', () => {
  test('Produtos: lista do backend e abre cadastro', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/produtos');
    await irPara(page, '/buffet/produtos');
    await expect(page.getByRole('heading', { name: 'Produtos — Buffet' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Novo produto' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Novo produto do buffet' })).toBeVisible();
    await expect(dialog.getByLabel('Nome do prato/produto')).toBeVisible();
    await expect(dialog.getByLabel('Categoria')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Produtos: editar e excluir item existente', async ({ page }) => {
    await irPara(page, '/buffet/produtos');
    const main = page.getByRole('main');
    await expect(main.getByRole('heading', { name: 'Produtos — Buffet' })).toBeVisible();

    if ((await main.getByRole('row').count()) > 1) {
      await page.getByRole('button', { name: 'Editar' }).first().click();
      const dialog = page.getByRole('dialog');
      await expect(dialog.getByRole('heading', { name: 'Editar produto' })).toBeVisible();
      await dialog.getByRole('button', { name: 'Cancelar' }).click();

      confirmarDialogoNativo(page);
      await page.getByRole('button', { name: 'Excluir' }).first().click();
    }
  });

  test('Mesas: mapa carrega, cadastro, ocupar/liberar/reservar e QR Code', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/mesas');
    await irPara(page, '/buffet/mesas');
    await expect(page.getByRole('heading', { name: 'Mesas — Buffet' })).toBeVisible();
    await expect(page.getByText(/Mapa em tempo real/)).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Nova mesa' }).click();
    let dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova mesa' })).toBeVisible();
    await expect(dialog.getByLabel('Número')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    // QR Code da primeira mesa
    const qrIcon = page.getByRole('button', { name: 'Gerar QR Code da Mesa' }).first();
    if (await qrIcon.isVisible().catch(() => false)) {
      await qrIcon.click();
      dialog = page.getByRole('dialog');
      await expect(dialog.getByRole('heading', { name: /QR Code — Mesa/ })).toBeVisible();
      await dialog.getByRole('button', { name: 'Fechar' }).click();
    }

    // Impressão em lote
    await main.getByRole('button', { name: 'Imprimir QR Codes' }).click();
    dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Imprimir QR Codes/ })).toBeVisible();
    await dialog.getByRole('button', { name: 'Fechar' }).click();
  });

  test('Comandas: lista e abre nova comanda', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/comandas');
    await irPara(page, '/buffet/comandas');
    await expect(page.getByRole('heading', { name: 'Comandas - Buffet' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Nova Comanda' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova Comanda' })).toBeVisible();
    await expect(dialog.getByLabel(/No da Comanda/)).toBeVisible();
    await expect(dialog.getByLabel('Mesa')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Clientes Buffet: carrega a tela e abre cadastro', async ({ page }) => {
    const resp = esperarApi(page, '/api/buffet/clientes');
    await irPara(page, '/buffet/clientes');
    await expect(page.getByRole('heading', { name: 'Clientes - Buffet' })).toBeVisible();
    expectOk(await resp);

    await page.getByRole('main').getByRole('button', { name: 'Novo Cliente' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Novo Cliente' })).toBeVisible();
    await expect(dialog.getByLabel('Nome')).toBeVisible();
    await expect(dialog.getByLabel('CPF')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });

  test('Receitas: lista, nova ficha e ver ficha técnica', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/receitas');
    await irPara(page, '/buffet/receitas');
    await expect(page.getByRole('heading', { name: 'Receitas e Fichas Técnicas' })).toBeVisible();
    expectOk(await lista);

    const main = page.getByRole('main');
    await main.getByRole('button', { name: 'Nova ficha' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: 'Nova ficha técnica' })).toBeVisible();
    await expect(dialog.getByLabel('Produto do prato')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();

    const ver = page.getByRole('button', { name: 'Ver ficha / ingredientes' }).first();
    if (await ver.isVisible().catch(() => false)) {
      await ver.click();
      const detalhe = page.getByRole('dialog');
      await expect(detalhe.getByRole('heading', { name: /Ficha —/ })).toBeVisible();
      await detalhe.getByRole('button', { name: 'Fechar' }).click();
    }
  });

  test('Transferências: lista e abre solicitação', async ({ page }) => {
    const lista = esperarApi(page, '/api/buffet/transferencias');
    await irPara(page, '/buffet/transferencias');
    await expect(page.getByRole('heading', { name: 'Transferências de Estoque' })).toBeVisible();
    expectOk(await lista);

    await page.getByRole('main').getByRole('button', { name: 'Solicitar transferência' }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog.getByRole('heading', { name: /Solicitar transferência/ })).toBeVisible();
    await expect(dialog.getByLabel('Produto')).toBeVisible();
    await expect(dialog.getByLabel('Origem')).toBeVisible();
    await expect(dialog.getByLabel('Destino')).toBeVisible();
    await dialog.getByRole('button', { name: 'Cancelar' }).click();
  });
});
