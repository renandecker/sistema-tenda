import { Page, Locator, expect, APIResponse } from '@playwright/test';

/**
 * Navega direto pela URL da tela (com a sessão já autenticada via storageState).
 * Evita depender do hover do menu lateral, que é inconsistente em headless.
 */
export async function irPara(page: Page, path: string) {
  await page.goto(path);
  await expect(page).toHaveURL(new RegExp(`${path.replace(/\//g, '\\/')}(\\b|$)`));
}

/** Abre o menu lateral (desktop) posicionando o mouse na borda esquerda. */
export async function abrirMenu(page: Page) {
  await page.mouse.move(0, 0);
  await page.mouse.move(6, 60);
  const nav = page.getByRole('navigation', { name: 'Main navigation' });
  await expect(nav.getByText('Dashboard', { exact: true })).toBeVisible();
  await expect
    .poll(async () => {
      const box = await nav.boundingBox();
      return box ? box.x : -999;
    }, { message: 'menu lateral deve deslizar para dentro da viewport' })
    .toBeGreaterThanOrEqual(-1);
  return nav;
}

/** Aguarda uma resposta de API por substring de URL + método opcional. */
export function esperarApi(page: Page, urlPart: string, method = 'GET') {
  return page.waitForResponse(
    (r) => r.url().includes(urlPart) && r.request().method() === method
  );
}

/** Verifica que a resposta veio 2xx. */
export function expectOk(response: APIResponse) {
  expect(response.ok(), `API ${response.url()} retornou ${response.status()}`).toBeTruthy();
}

/**
 * Botões de ação por linha são IconButtons envolvidos por Tooltip do MUI. O texto
 * do tooltip vira o nome acessível do botão. Retorna o primeiro (ou `index`-ésimo).
 */
export function acaoLinha(page: Page, tooltip: string, index = 0) {
  return page.getByRole('button', { name: tooltip }).nth(index);
}

/**
 * IconButton sem nome acessível (Tooltip envolvido por <span>, o que impede o MUI
 * de clonar o título para a aria-label). Localiza pelo ícone via data-testid do SVG.
 */
export function acaoPorIcone(page: Page, icone: string, index = 0) {
  return page.locator(`svg[data-testid="${icone}"]`).nth(index);
}

/** Aceita (OK) o próximo window.confirm nativo. */
export function confirmarDialogoNativo(page: Page) {
  page.once('dialog', (d) => d.accept());
}

/** Recusa (Cancelar) o próximo window.confirm nativo. */
export function recusarDialogoNativo(page: Page) {
  page.once('dialog', (d) => d.dismiss());
}

/** Intercepta exportações/CSV disparadas por download e devolve a sugestão de nome. */
export function esperarDownload(page: Page) {
  return page.waitForEvent('download');
}

/**
 * Seleciona uma opção num <TextField select> do MUI (renderiza combobox + listbox,
 * não um <select> nativo). `label` é o label do campo; `opcao` é o texto da opção.
 */
export async function selecionarOpcao(page: Page, label: string, opcao: string) {
  await page.getByRole('combobox', { name: label }).click();
  await page.getByRole('option', { name: opcao, exact: true }).click();
}

/**
 * Clica num botão de exportação tolerando telas sem dados (algumas só geram o
 * arquivo quando há linhas). Espera um download por até `timeout` ms; se não vier,
 * apenas confirma que o clique foi aceito sem erro.
 */
export async function clicarExportacao(page: Page, botao: Locator, timeout = 3000) {
  const dl = page.waitForEvent('download', { timeout }).catch(() => null);
  await botao.click();
  const download = await dl;
  if (download) expect(download.suggestedFilename()).toMatch(/\.(csv|xlsx|txt|pdf)$/i);
  return download;
}
