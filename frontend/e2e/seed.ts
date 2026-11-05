import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import path from 'node:path';

const container = process.env.E2E_DB_CONTAINER || 'sistematenda-db';
const dbName = process.env.E2E_DB_NAME || 'sistematenda';
const dbUser = process.env.E2E_DB_USER || 'sistematenda';

const seedFile = path.resolve(__dirname, 'seed.sql');

/**
 * Executa o seed idempotente de dados falsos (e2e/seed.sql) no banco local.
 * Usa `docker exec psql`, lendo o arquivo SQL pela entrada padrão.
 */
export function seedDatabase(): void {
  const sql = readFileSync(seedFile, 'utf8');

  execFileSync(
    'docker',
    [
      'exec',
      '-i',
      container,
      'psql',
      '-U',
      dbUser,
      '-d',
      dbName,
      '-v',
      'ON_ERROR_STOP=1',
      '-q',
      '-f',
      '-',
    ],
    { input: sql, encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] }
  );
}

if (require.main === module) {
  try {
    seedDatabase();
    console.log(`Seed aplicado em ${container}/${dbName}.`);
  } catch (error) {
    const err = error as { stderr?: string; message?: string };
    console.error('Falha ao aplicar o seed:', err.stderr || err.message);
    process.exitCode = 1;
  }
}
