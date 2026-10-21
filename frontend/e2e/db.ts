import { execFileSync } from 'node:child_process';

const container = process.env.E2E_DB_CONTAINER || 'sistematenda-db';
const dbName = process.env.E2E_DB_NAME || 'sistematenda';
const dbUser = process.env.E2E_DB_USER || 'sistematenda';

/**
 * Executa SQL no banco de teste via `docker exec psql`.
 * Retorna o stdout como texto.
 */
export function runSql(sql: string): string {
  return execFileSync(
    'docker',
    ['exec', '-i', container, 'psql', '-U', dbUser, '-d', dbName, '-tA', '-c', sql],
    { encoding: 'utf8' }
  ).trim();
}

/** Um único valor de uma query (primeira coluna da primeira linha). */
export function queryOne(sql: string): string {
  return runSql(sql).split('\n')[0].trim();
}

/**
 * Garante que exista um PDV ativo (o PDV da tela depende de um terminal
 * selecionado). Retorna o id do terminal.
 */
export function ensurePdvTerminal(codigo = 'PDV01', nome = 'PDV Teste E2E'): string {
  const existente = runSql(
    `SELECT id FROM pdv_terminal WHERE codigo = '${codigo}' AND deleted = false LIMIT 1;`
  );
  if (existente) return existente.split('\n')[0].trim();

  const id = queryOne(
    `INSERT INTO pdv_terminal (id, codigo, nome, ativo, deleted, status, numero_ultima_venda, created_at, updated_at, version)
     VALUES (gen_random_uuid(), '${codigo}', '${nome}', true, false, 'ATIVO', 0, now(), now(), 0)
     RETURNING id;`
  );
  return id;
}
