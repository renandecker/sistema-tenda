-- V1__create_estoque_schema.sql
-- Estoque module database schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Estoque movimentos table
CREATE TABLE IF NOT EXISTS estoque_movimento (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    produto_fruteira_id UUID REFERENCES fruteira_produto(id) ON DELETE SET NULL,
    produto_buffet_id UUID REFERENCES buffet_produto(id) ON DELETE SET NULL,

    tipo VARCHAR(30) NOT NULL,
    origem VARCHAR(20),
    destino VARCHAR(20),

    quantidade DECIMAL(15,4) NOT NULL,
    custo_unitario DECIMAL(15,4),
    valor_total DECIMAL(15,4),
    estoque_anterior DECIMAL(15,4),
    estoque_posterior DECIMAL(15,4),

    lote VARCHAR(50),
    data_validade TIMESTAMP WITH TIME ZONE,
    documento_referencia VARCHAR(100),
    numero_documento VARCHAR(50),
    observacoes VARCHAR(1000),

    motivo_defeito VARCHAR(500),
    tipo_defeito VARCHAR(50),
    lote_defeituoso VARCHAR(50),
    quantidade_recuperavel DECIMAL(15,4),

    responsavel VARCHAR(100),
    data TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Indexes for estoque_movimento
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_produto_fruteira ON estoque_movimento(produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_produto_buffet ON estoque_movimento(produto_buffet_id);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_tipo ON estoque_movimento(tipo);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_data ON estoque_movimento(data);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_origem_destino ON estoque_movimento(origem, destino);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_defeito ON estoque_movimento(tipo) WHERE tipo LIKE '%DEFEITO%';

-- Estoque alertas table
CREATE TABLE IF NOT EXISTS estoque_alerta (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    produto_fruteira_id UUID REFERENCES fruteira_produto(id) ON DELETE SET NULL,
    produto_buffet_id UUID REFERENCES buffet_produto(id) ON DELETE SET NULL,

    tipo VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    prioridade VARCHAR(20) DEFAULT 'MEDIA',

    mensagem VARCHAR(500),
    valor_referencia DECIMAL(15,4),
    valor_atual DECIMAL(15,4),

    data_vencimento DATE,
    dias_para_vencimento INTEGER,
    data_alerta TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_visualizacao TIMESTAMP WITH TIME ZONE,
    data_resolucao TIMESTAMP WITH TIME ZONE,

    visualizado_por VARCHAR(100),
    resolvido_por VARCHAR(100),
    observacoes_resolucao VARCHAR(500)
);

-- Indexes for estoque_alerta
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_tipo ON estoque_alerta(tipo);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_status ON estoque_alerta(status);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_produto_fruteira ON estoque_alerta(produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_produto_buffet ON estoque_alerta(produto_buffet_id);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_data ON estoque_alerta(data_alerta);

-- Inventario table
CREATE TABLE IF NOT EXISTS estoque_inventario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'PLANEJADO',
    responsavel VARCHAR(100),
    descricao VARCHAR(200),
    observacoes VARCHAR(1000),

    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fim TIMESTAMP WITH TIME ZONE,

    total_itens INTEGER DEFAULT 0,
    itens_contados INTEGER DEFAULT 0,
    itens_divergentes INTEGER DEFAULT 0,
    valor_divergencia DECIMAL(15,4)
);

-- Item Inventario table
CREATE TABLE IF NOT EXISTS estoque_item_inventario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    inventario_id UUID NOT NULL REFERENCES estoque_inventario(id) ON DELETE CASCADE,
    produto_fruteira_id UUID REFERENCES fruteira_produto(id) ON DELETE SET NULL,
    produto_buffet_id UUID REFERENCES buffet_produto(id) ON DELETE SET NULL,

    contado BOOLEAN NOT NULL DEFAULT false,
    quantidade_sistema DECIMAL(15,4) NOT NULL,
    quantidade_contada DECIMAL(15,4),
    quantidade_divergencia DECIMAL(15,4),
    custo_unitario DECIMAL(15,4),
    valor_divergencia DECIMAL(15,4),
    data_contagem TIMESTAMP WITH TIME ZONE,
    contado_por VARCHAR(100),
    observacoes VARCHAR(500)
);

-- Indexes for estoque_item_inventario
CREATE INDEX IF NOT EXISTS idx_estoque_item_inventario_inventario ON estoque_item_inventario(inventario_id);
CREATE INDEX IF NOT EXISTS idx_estoque_item_inventario_produto_fruteira ON estoque_item_inventario(produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_estoque_item_inventario_produto_buffet ON estoque_item_inventario(produto_buffet_id);
