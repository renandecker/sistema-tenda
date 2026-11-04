/**
 * Dados falsos (fixtures) para testes.
 * Estes dados simulam respostas do banco e podem ser usados
 * em testes unitários, de integração e E2E.
 */

export interface ProdutoFalso {
  id: string;
  nome: string;
  preco: number;
  descricao?: string;
  codigoBarras?: string;
  ativo: boolean;
}

export interface ClienteFalso {
  id: string;
  nome: string;
  email: string;
  cpf?: string;
  ativo: boolean;
}

export interface ComandaFalso {
  id: string;
  numero: string;
  status: 'ABERTA' | 'FECHADA' | 'PAGA';
  total: number;
  itens?: ItemComandaFalso[];
}

export interface ItemComandaFalso {
  id: string;
  produtoId: string;
  produtoNome: string;
  quantidade: number;
  precoUnitario: number;
  total: number;
}

export const PRODUTOS_FAKE: ProdutoFalso[] = [
  { id: 'prod-001', nome: 'Maçã', preco: 5.0, descricao: 'Fruta vermelha', codigoBarras: '7891234001234', ativo: true },
  { id: 'prod-002', nome: 'Banana', preco: 3.5, descricao: 'Fruta amarela', codigoBarras: '7891234001235', ativo: true },
  { id: 'prod-003', nome: 'Laranja', preco: 4.2, descricao: 'Fruta cítrica', codigoBarras: '7891234001236', ativo: true },
  { id: 'prod-004', nome: 'Uva', preco: 12.0, descricao: 'Fruta verde', codigoBarras: '7891234001237', ativo: true },
  { id: 'prod-005', nome: 'Pêra', preco: 7.8, descricao: 'Fruta', codigoBarras: '7891234001238', ativo: true },
];

export const CLIENTES_FAKE: ClienteFalso[] = [
  { id: 'cli-001', nome: 'João Silva', email: 'joao.silva@email.com', cpf: '123.456.789-00', ativo: true },
  { id: 'cli-002', nome: 'Maria Oliveira', email: 'maria.oliveira@email.com', cpf: '987.654.321-00', ativo: true },
  { id: 'cli-003', nome: 'Pedro Santos', email: 'pedro.santos@email.com', cpf: '111.222.333-44', ativo: true },
  { id: 'cli-004', nome: 'Ana Costa', email: 'ana.costa@email.com', cpf: '444.555.666-77', ativo: true },
  { id: 'cli-005', nome: 'Carlos Lima', email: 'carlos.lima@email.com', cpf: '555.666.777-88', ativo: false },
];

export const COMANDAS_FAKE: ComandaFalso[] = [
  {
    id: 'cmd-001',
    numero: '001',
    status: 'ABERTA',
    total: 13.5,
    itens: [
      { id: 'item-001', produtoId: 'prod-001', produtoNome: 'Maçã', quantidade: 2, precoUnitario: 5.0, total: 10.0 },
      { id: 'item-002', produtoId: 'prod-002', produtoNome: 'Banana', quantidade: 1, precoUnitario: 3.5, total: 3.5 },
    ],
  },
  {
    id: 'cmd-002',
    numero: '002',
    status: 'FECHADA',
    total: 22.0,
    itens: [
      { id: 'item-003', produtoId: 'prod-003', produtoNome: 'Laranja', quantidade: 3, precoUnitario: 4.0, total: 12.0 },
      { id: 'item-004', produtoId: 'prod-005', produtoNome: 'Pêra', quantidade: 2, precoUnitario: 5.0, total: 10.0 },
    ],
  },
  {
    id: 'cmd-003',
    numero: '003',
    status: 'PAGA',
    total: 20.5,
    itens: [
      { id: 'item-005', produtoId: 'prod-004', produtoNome: 'Uva', quantidade: 1, precoUnitario: 12.0, total: 12.0 },
      { id: 'item-006', produtoId: 'prod-001', produtoNome: 'Maçã', quantidade: 1, precoUnitario: 5.0, total: 5.0 },
      { id: 'item-007', produtoId: 'prod-002', produtoNome: 'Banana', quantidade: 1, precoUnitario: 3.5, total: 3.5 },
    ],
  },
];

/**
 * Gera um ID aleatório (para uso em testes que precisam de IDs únicos)
 */
export function gerarId(): string {
  return 'tmp-' + Math.random().toString(36).substring(2, 11);
}

/**
 * Aguarda um tempo determinado (útil para simular atrasos em testes)
 */
export function aguatar(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}