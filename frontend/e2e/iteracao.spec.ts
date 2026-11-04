import { test, expect } from '@playwright/test';
import { PRODUTOS_FAKE, CLIENTES_FAKE, COMANDAS_FAKE } from './fakers';

test.describe('Testes de Iteração — Coleções e Listagens', () => {
  test('deve iterar sobre a lista de produtos falsos e validar propriedades', async () => {
    // Teste de iteração pura com os dados falsos criados
    expect(PRODUTOS_FAKE.length).toBeGreaterThan(0);
    
    let somaPrecos = 0;
    for (const produto of PRODUTOS_FAKE) {
      expect(produto.id).toBeTruthy();
      expect(produto.nome).toBeTruthy();
      expect(produto.preco).toBeGreaterThan(0);
      expect(typeof produto.ativo).toBe('boolean');
      somaPrecos += produto.preco;
    }

    expect(somaPrecos).toBeGreaterThan(0);
    
    // Teste de filtro (iteração com condição)
    const produtosAtivos = PRODUTOS_FAKE.filter(p => p.ativo);
    expect(produtosAtivos.length).toBe(PRODUTOS_FAKE.length);
  });

  test('deve iterar sobre a lista de clientes e validar CPFs', async () => {
    expect(CLIENTES_FAKE.length).toBeGreaterThan(0);
    
    for (const cliente of CLIENTES_FAKE) {
      expect(cliente.id).toBeTruthy();
      expect(cliente.nome).toBeTruthy();
      expect(cliente.email).toContain('@');
      if (cliente.cpf) {
        expect(cliente.cpf).toMatch(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/);
      }
    }

    // Iteração com reduce para contar clientes ativos
    const ativosCount = CLIENTES_FAKE.reduce((acc, c) => acc + (c.ativo ? 1 : 0), 0);
    expect(ativosCount).toBe(4);
  });

  test('deve iterar sobre comandas e itens calculando totais corretamente', async () => {
    expect(COMANDAS_FAKE.length).toBeGreaterThan(0);

    for (const comanda of COMANDAS_FAKE) {
      expect(comanda.id).toBeTruthy();
      expect(comanda.numero).toBeTruthy();
      expect(['ABERTA', 'FECHADA', 'PAGA']).toContain(comanda.status);

      if (comanda.itens && comanda.itens.length > 0) {
        let totalCalculado = 0;
        for (const item of comanda.itens) {
          expect(item.id).toBeTruthy();
          expect(item.quantidade).toBeGreaterThan(0);
          expect(item.precoUnitario).toBeGreaterThan(0);
          expect(item.total).toBe(item.quantidade * item.precoUnitario);
          totalCalculado += item.total;
        }
        expect(comanda.total).toBeCloseTo(totalCalculado, 2);
      }
    }
  });
});
