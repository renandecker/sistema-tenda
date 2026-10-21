import { test, expect } from '@playwright/test';
import { loginApi, newApiContext, authHeaders, API_PREFIX, LOGIN } from './api';

test.describe('API do backend (integração real)', () => {
  test('login retorna token, usuário e perfis', async () => {
    const session = await loginApi();
    expect(session.token).toBeTruthy();
    expect(session.user.email).toBe(LOGIN);
    expect(session.user.perfis).toContain('ADMIN');
    await session.context.dispose();
  });

  test('rejeita credenciais inválidas', async () => {
    const context = await newApiContext();
    const response = await context.post(`${API_PREFIX}/auth/login`, {
      data: { email: LOGIN, senha: 'errada-123' },
    });
    expect([400, 401]).toContain(response.status());
    await context.dispose();
  });

  test('endpoints protegidos respondem com token válido', async () => {
    const session = await loginApi();
    const response = await session.context.get(`${API_PREFIX}/fruteira/produtos`, {
      headers: authHeaders(session.token),
    });
    expect(response.ok()).toBeTruthy();
    await session.context.dispose();
  });

  test('lista de produtos da fruteira responde com paginação', async () => {
    const session = await loginApi();
    const response = await session.context.get(`${API_PREFIX}/fruteira/produtos`, {
      headers: authHeaders(session.token),
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body).toHaveProperty('content');
    expect(Array.isArray(body.content)).toBeTruthy();
    await session.context.dispose();
  });

  test('health check responde ok', async () => {
    const session = await loginApi();
    const response = await session.context.get(`${API_PREFIX}/saude-check`, {
      headers: authHeaders(session.token),
    });
    expect(response.ok()).toBeTruthy();
    await session.context.dispose();
  });
});
