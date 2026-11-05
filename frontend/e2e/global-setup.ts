import { loadEnvFile } from './env';
import { seedDatabase } from './seed';

loadEnvFile('.env.e2e');

/**
 * Roda uma vez antes de toda a suíte: garante que o banco local tenha os
 * dados falsos deterministicos usados pelos testes de iteracao/estado/E2E.
 */
export default async function globalSetup(): Promise<void> {
  if (process.env.E2E_SKIP_SEED === 'true') {
    console.log('E2E_SKIP_SEED=true: seed ignorado.');
    return;
  }

  seedDatabase();
  console.log('Seed de dados falsos aplicado.');
}
