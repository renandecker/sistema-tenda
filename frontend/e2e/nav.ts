import { Page, expect, APIResponse } from '@playwright/test';

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
