-- V1__create_estoque_schema.sql
-- Estoque module database schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Estoque movimentos table
CREATE TABLE estoque_movimento (
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
CREATE INDEX idx_estoque_movimento_produto_fruteira ON estoque_movimento(produto_fruteira_id);
CREATE INDEX idx_estoque_movimento_produto_buffet ON estoque_movimento(produto_buffet_id);
CREATE INDEX idx_estoque_movimento_tipo ON estoque_movimento(tipo);
CREATE INDEX idx_estoque_movimento_data ON estoque_movimento(data);
CREATE INDEX idx_estoque_movimento_origem_destino ON estoque_movimento(origem, destino);
CREATE INDEX idx_estoque_movimento_defeito ON estoque_movimento(tipo) WHERE tipo LIKE '%DEFEITO%';

-- Estoque alertas table
CREATE TABLE estoque_alerta (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    tipo VARCHAR(30) NOT NULL,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',

    produto_fruteira_id UUID REFERENCES fruteira_produto(id) ON DELETE SET NULL,
    produto_buffet_id UUID REFERENCES buffet_produto(id) ON DELETE SET NULL,

    mensagem VARCHAR(500) NOT NULL,
    valor_referencia DECIMAL(15,4),
    valor_atual DECIMAL(15,4),

    data_detecao TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_resolucao TIMESTAMP WITH TIME ZONE,
    responsavel_deteccao VARCHAR(100),
    responsavel_resolucao VARCHAR(100),
    observacoes_resolucao VARCHAR(1000)
);

-- Indexes for estoque_alerta
CREATE INDEX idx_estoque_alerta_tipo ON estoque_alerta(tipo);
CREATE INDEX idx_estoque_alerta_status ON estoque_alerta(status);
CREATE INDEX idx_estoque_alerta_produto_fruteira ON estoque_alerta(produto_fruteira_id);
CREATE INDEX idx_estoque_alerta_produto_buffet ON estoque_alerta(produto_buffet_id);
CREATE INDEX idx_estoque_alerta_data ON estoque_alerta(data_detecao);

-- Inventario table
CREATE TABLE estoque_inventario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    descricao VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTO',

    data_abertura TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITH TIME ZONE,

    responsavel_abertura VARCHAR(100),
    responsavel_fechamento VARCHAR(100),

    observacoes VARCHAR(2000),
    total_divergencias INTEGER DEFAULT 0
);

-- Item Inventario table
CREATE TABLE estoque_item_inventario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    inventario_id UUID NOT NULL REFERENCES estoque_inventario(id) ON DELETE CASCADE,
    produto_fruteira_id UUID REFERENCES fruteira_produto(id) ON DELETE SET NULL,
    produto_buffet_id UUID REFERENCES buffet_produto(id) ON DELETE SET NULL,

    estoque_sistema DECIMAL(15,4) NOT NULL,
    contagem_fisica DECIMAL(15,4),
    divergencia DECIMAL(15,4),

    lote VARCHAR(50),
    data_validade TIMESTAMP WITH TIME ZONE,
    observacoes VARCHAR(1000)
);

-- Indexes for estoque_item_inventario
CREATE INDEX idx_estoque_item_inventario_inventario ON estoque_item_inventario(inventario_id);
CREATE INDEX idx_estoque_item_inventario_produto_fruteira ON estoque_item_inventario(produto_fruteira_id);
CREATE INDEX idx_estoque_item_inventario_produto_buffet ON estoque_item_inventario(produto_buffet_id);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_estoque_movimento_updated_at BEFORE UPDATE ON estoque_movimento FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_estoque_alerta_updated_at BEFORE UPDATE ON estoque_alerta FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_estoque_inventario_updated_at BEFORE UPDATE ON estoque_inventario FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_estoque_item_inventario_updated_at BEFORE UPDATE ON estoque_item_inventario FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();