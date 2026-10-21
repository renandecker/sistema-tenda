import { request, APIRequestContext } from '@playwright/test';

/** Origem do backend, sem o sufixo /api (ex.: http://localhost:8080). */
export const API_ORIGIN = (process.env.E2E_API_URL || 'http://localhost:8080/api').replace(
  /\/api\/?$/,
  ''
);
/** Prefixo base das rotas REST. */
export const API_PREFIX = '/api';

export const LOGIN = process.env.E2E_LOGIN || 'admin@sistematenda.com';
export const SENHA = process.env.E2E_SENHA || '123456';

export interface ApiSession {
  context: APIRequestContext;
  token: string;
  user: {
    id: string;
    nome: string;
    email: string;
    perfis: string[];
  };
}

export function newApiContext(): Promise<APIRequestContext> {
  return request.newContext({ baseURL: API_ORIGIN });
}

/** Faz login real na API e devolve um contexto autenticado. */
export async function loginApi(
  credentials: { email: string; senha: string } = { email: LOGIN, senha: SENHA }
): Promise<ApiSession> {
  const context = await newApiContext();
  const response = await context.post(`${API_PREFIX}/auth/login`, { data: credentials });
  if (!response.ok()) {
    throw new Error(
      `Falha no login da API (${response.status()}): ${await response.text()}`
    );
  }
  const body = await response.json();
  return { context, token: body.token, user: body.user };
}

export function authHeaders(token: string) {
  return { Authorization: `Bearer ${token}` };
}
