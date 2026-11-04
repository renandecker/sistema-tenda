-- V10__create_estoque_produto.sql
-- Cadastro de produtos de estoque (matéria-prima/insumos) e integração
-- com a movimentação de estoque. Produtos do buffet deixam de controlar estoque.

-- Nao cria extensoes: gen_random_uuid() e nativo no PostgreSQL 13+ e funciona
-- com usuario comum, owner do banco (mesma abordagem da V8).
-- ---------------------------------------------------------------------------
-- Cadastro de produtos de estoque
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estoque_produto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    codigo VARCHAR(50) NOT NULL,
    codigo_barras VARCHAR(100),
    nome VARCHAR(200) NOT NULL,
    descricao VARCHAR(1000),
    categoria VARCHAR(30) NOT NULL DEFAULT 'MATERIA_PRIMA',
    unidade_medida VARCHAR(10) NOT NULL DEFAULT 'UN',
    custo_medio DECIMAL(15,4) NOT NULL DEFAULT 0,
    estoque_atual DECIMAL(15,4) NOT NULL DEFAULT 0,
    estoque_minimo DECIMAL(15,4),
    estoque_maximo DECIMAL(15,4),
    ativo BOOLEAN NOT NULL DEFAULT true,
    observacoes VARCHAR(2000),

    CONSTRAINT estoque_produto_categoria_check CHECK (
        categoria IN ('MATERIA_PRIMA', 'EMBALAGEM', 'DESCARTAVEL', 'LIMPEZA', 'OUTROS')
    ),
    CONSTRAINT estoque_produto_unidade_medida_check CHECK (
        unidade_medida IN ('KG', 'UN', 'L', 'ML', 'G', 'PC', 'CX', 'FD', 'KT', 'MT', 'M2', 'M3')
    ),
    CONSTRAINT estoque_produto_custo_medio_check CHECK (custo_medio >= 0),
    CONSTRAINT estoque_produto_estoque_atual_check CHECK (estoque_atual >= 0),
    CONSTRAINT estoque_produto_estoque_minimo_check CHECK (estoque_minimo IS NULL OR estoque_minimo >= 0),
    CONSTRAINT estoque_produto_estoque_maximo_check CHECK (estoque_maximo IS NULL OR estoque_maximo >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_estoque_produto_codigo ON estoque_produto(codigo);
CREATE UNIQUE INDEX IF NOT EXISTS idx_estoque_produto_codigo_barras ON estoque_produto(codigo_barras) WHERE codigo_barras IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_estoque_produto_nome ON estoque_produto(nome);
CREATE INDEX IF NOT EXISTS idx_estoque_produto_categoria ON estoque_produto(categoria);
CREATE INDEX IF NOT EXISTS idx_estoque_produto_ativo ON estoque_produto(ativo);

-- ---------------------------------------------------------------------------
-- Movimentação de estoque: vínculo com produto de estoque
-- ---------------------------------------------------------------------------
ALTER TABLE estoque_movimento ADD COLUMN IF NOT EXISTS produto_estoque_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'estoque_movimento_produto_estoque_fkey'
    ) THEN
        ALTER TABLE estoque_movimento
            ADD CONSTRAINT estoque_movimento_produto_estoque_fkey
            FOREIGN KEY (produto_estoque_id) REFERENCES estoque_produto(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_estoque_movimento_produto_estoque ON estoque_movimento(produto_estoque_id);

-- ---------------------------------------------------------------------------
-- Atualiza as restrições de origem/destino/tipo para suportar o novo cadastro
-- ---------------------------------------------------------------------------
ALTER TABLE estoque_movimento DROP CONSTRAINT IF EXISTS estoque_movimento_origem_check;
ALTER TABLE estoque_movimento DROP CONSTRAINT IF EXISTS estoque_movimento_destino_check;
ALTER TABLE estoque_movimento DROP CONSTRAINT IF EXISTS estoque_movimento_tipo_check;

ALTER TABLE estoque_movimento
    ADD CONSTRAINT estoque_movimento_origem_check CHECK (
        origem IS NULL OR origem IN (
            'FRUTEIRA', 'BUFFET', 'ESTOQUE', 'FORNECEDOR', 'CLIENTE',
            'PERDA', 'AJUSTE', 'DEFEITO', 'REPROCESSO', 'OUTROS'
        )
    );

ALTER TABLE estoque_movimento
    ADD CONSTRAINT estoque_movimento_destino_check CHECK (
        destino IS NULL OR destino IN (
            'FRUTEIRA', 'BUFFET', 'ESTOQUE', 'FORNECEDOR', 'CLIENTE',
            'PERDA', 'AJUSTE', 'DEFEITO', 'REPROCESSO', 'OUTROS'
        )
    );

ALTER TABLE estoque_movimento
    ADD CONSTRAINT estoque_movimento_tipo_check CHECK (
        tipo IN (
            'ENTRADA_COMPRA', 'ENTRADA_TRANSFERENCIA', 'ENTRADA_AJUSTE', 'ENTRADA_DEVOLUCAO',
            'SAIDA_VENDA', 'SAIDA_TRANSFERENCIA', 'SAIDA_PERDA', 'SAIDA_AJUSTE', 'SAIDA_CONSUMO_INTERNO',
            'SAIDA_DEFEITO_AVARIA', 'SAIDA_DEFEITO_DETERIORACAO', 'SAIDA_DEFEITO_VENCIMENTO',
            'SAIDA_DEFEITO_QUALIDADE', 'SAIDA_DEFEITO_OUTROS', 'ENTRADA_DEFEITO_REPROCESSO',
            'INVENTARIO'
        )
    );

-- ---------------------------------------------------------------------------
-- Produtos do buffet não controlam mais estoque
-- ---------------------------------------------------------------------------
UPDATE buffet_produto SET controla_estoque = false WHERE controla_estoque = true;
