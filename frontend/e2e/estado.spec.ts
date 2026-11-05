import { test, expect } from '@playwright/test';
import { COMANDAS_FAKE, ComandaFalso } from './fakers';

/**
 * Simula uma máquina de estados simples para comandas ou carrinhos de venda.
 */
class ComandaStateMachine {
  private estado: ComandaFalso;

  constructor(comandaInicial: ComandaFalso) {
    // Copia profunda dos itens: evita mutar as fixtures compartilhadas
    // (COMANDAS_FAKE) e contaminar outros testes do mesmo worker.
    this.estado = {
      ...comandaInicial,
      itens: comandaInicial.itens?.map((item) => ({ ...item })),
    };
  }

  public getStatus(): string {
    return this.estado.status;
  }

  public getTotal(): number {
    return this.estado.total;
  }

  public adicionarItem(quantidade: number, precoUnitario: number, produtoNome: string): void {
    if (this.estado.status !== 'ABERTA') {
      throw new Error('Não é possível adicionar itens a uma comanda fechada ou paga.');
    }
    const novoValor = quantidade * precoUnitario;
    this.estado.total += novoValor;
    if (!this.estado.itens) {
      this.estado.itens = [];
    }
    this.estado.itens.push({
      id: 'item-' + Math.random(),
      produtoId: 'prod-x',
      produtoNome,
      quantidade,
      precoUnitario,
      total: novoValor,
    });
  }

  public fecharComanda(): void {
    if (this.estado.status !== 'ABERTA') {
      throw new Error('A comanda já está fechada ou paga.');
    }
    this.estado.status = 'FECHADA';
  }

  public pagarComanda(): void {
    if (this.estado.status !== 'FECHADA') {
      throw new Error('A comanda precisa estar fechada para ser paga.');
    }
    this.estado.status = 'PAGA';
  }

  public cancelar(): void {
    if (this.estado.status === 'PAGA') {
      throw new Error('Não é possível cancelar uma comanda já paga.');
    }
    // apenas para teste de estado
  }
}

test.describe('Testes de Estado — Máquina de Estados de Comanda', () => {
  test('deve iniciar com status ABERTA e permitir adicionar itens', () => {
    const comandaBase = { ...COMANDAS_FAKE[0], status: 'ABERTA' as const, total: 10.0 };
    const maquina = new ComandaStateMachine(comandaBase);

    expect(maquina.getStatus()).toBe('ABERTA');
    expect(maquina.getTotal()).toBe(10.0);

    // Adiciona novo item
    maquina.adicionarItem(2, 5.0, 'Suco');
    expect(maquina.getTotal()).toBe(20.0);
  });

  test('deve transicionar corretamente de ABERTA para FECHADA e depois PAGA', () => {
    const comandaBase = { ...COMANDAS_FAKE[0], status: 'ABERTA' as const };
    const maquina = new ComandaStateMachine(comandaBase);

    expect(maquina.getStatus()).toBe('ABERTA');

    // Transição 1: ABERTA -> FECHADA
    maquina.fecharComanda();
    expect(maquina.getStatus()).toBe('FECHADA');

    // Tentar adicionar item após fechada deve falhar
    expect(() => {
      maquina.adicionarItem(1, 10.0, 'Erro');
    }).toThrow('Não é possível adicionar itens a uma comanda fechada ou paga.');

    // Transição 2: FECHADA -> PAGA
    maquina.pagarComanda();
    expect(maquina.getStatus()).toBe('PAGA');
  });

  test('deve rejeitar transições inválidas de estado', () => {
    const comandaBase = { ...COMANDAS_FAKE[2], status: 'PAGA' as const };
    const maquina = new ComandaStateMachine(comandaBase);

    expect(maquina.getStatus()).toBe('PAGA');

    // Tentar pagar comanda já paga ou fechada incorretamente
    expect(() => {
      maquina.fecharComanda();
    }).toThrow();
  });
});
