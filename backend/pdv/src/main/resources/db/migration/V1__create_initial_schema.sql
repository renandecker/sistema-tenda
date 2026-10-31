-- V1__create_initial_schema.sql
-- Initial database schema for Sistema Tenda

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'OPERADOR',
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE produtos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_venda DECIMAL(10,2) NOT NULL,
    preco_custo DECIMAL(10,2),
    unidade_medida VARCHAR(20) NOT NULL DEFAULT 'UN',
    codigo_barras VARCHAR(100),
    ncm VARCHAR(20),
    cfop VARCHAR(10),
    cest VARCHAR(10),
    origem_mercadoria INTEGER DEFAULT 0,
    icms_cst VARCHAR(3),
    pis_cst VARCHAR(2),
    cofins_cst VARCHAR(2),
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product-Category relationship
CREATE TABLE produto_categorias (
    produto_id UUID NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    categoria_id UUID NOT NULL REFERENCES categorias(id) ON DELETE CASCADE,
    PRIMARY KEY (produto_id, categoria_id)
);

-- Customers table
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    documento VARCHAR(20) UNIQUE,
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco TEXT,
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf CHAR(2),
    cep VARCHAR(10),
    observacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sales table
CREATE TABLE vendas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_venda BIGSERIAL NOT NULL UNIQUE,
    cliente_id UUID REFERENCES clientes(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTA',
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    desconto DECIMAL(10,2) NOT NULL DEFAULT 0,
    acrescimo DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    forma_pagamento VARCHAR(50),
    observacoes TEXT,
    data_venda TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sale items table
CREATE TABLE venda_itens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venda_id UUID NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produtos(id),
    quantidade DECIMAL(10,3) NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    desconto DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Payment table
CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venda_id UUID NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    forma_pagamento VARCHAR(50) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    referencia_externa VARCHAR(255),
    data_pagamento TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inventory table
CREATE TABLE estoque (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    produto_id UUID NOT NULL UNIQUE REFERENCES produtos(id) ON DELETE CASCADE,
    quantidade_atual DECIMAL(10,3) NOT NULL DEFAULT 0,
    quantidade_minima DECIMAL(10,3) NOT NULL DEFAULT 0,
    quantidade_maxima DECIMAL(10,3),
    localizacao VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Stock movements table
CREATE TABLE estoque_movimentacoes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    produto_id UUID NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL,
    quantidade_anterior DECIMAL(10,3) NOT NULL,
    quantidade_nova DECIMAL(10,3) NOT NULL,
    documento_referencia VARCHAR(100),
    observacoes TEXT,
    usuario_id UUID REFERENCES usuarios(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_produtos_codigo ON produtos(codigo);
CREATE INDEX idx_produtos_codigo_barras ON produtos(codigo_barras);
CREATE INDEX idx_produtos_ativo ON produtos(ativo);
CREATE INDEX idx_clientes_documento ON clientes(documento);
CREATE INDEX idx_clientes_nome ON clientes(nome);
CREATE INDEX idx_vendas_numero ON vendas(numero_venda);
CREATE INDEX idx_vendas_cliente ON vendas(cliente_id);
CREATE INDEX idx_vendas_data ON vendas(data_venda);
CREATE INDEX idx_vendas_status ON vendas(status);
CREATE INDEX idx_venda_itens_venda ON venda_itens(venda_id);
CREATE INDEX idx_venda_itens_produto ON venda_itens(produto_id);
CREATE INDEX idx_pagamentos_venda ON pagamentos(venda_id);
CREATE INDEX idx_estoque_movimentacoes_produto ON estoque_movimentacoes(produto_id);
CREATE INDEX idx_estoque_movimentacoes_data ON estoque_movimentacoes(created_at);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $func$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

CREATE TRIGGER update_usuarios_updated_at BEFORE UPDATE ON usuarios FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_produtos_updated_at BEFORE UPDATE ON produtos FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_categorias_updated_at BEFORE UPDATE ON categorias FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_clientes_updated_at BEFORE UPDATE ON clientes FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_vendas_updated_at BEFORE UPDATE ON vendas FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_pagamentos_updated_at BEFORE UPDATE ON pagamentos FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_estoque_updated_at BEFORE UPDATE ON estoque FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- Insert default admin user (password: admin123 - should be changed in production)
INSERT INTO usuarios (id, nome, email, senha_hash, role, ativo)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrador',
    'admin@sistematenda.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa',
    'ADMIN',
    true
) ON CONFLICT (id) DO NOTHING;

--
-- Name: fruteira_produto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fruteira_produto (
    ativo boolean NOT NULL,
    controla_lote boolean NOT NULL,
    controla_validade boolean NOT NULL,
    cst_cofins character varying(2),
    cst_icms character varying(3),
    cst_pis character varying(2),
    custo_medio numeric(15,4) NOT NULL,
    custo_ultimo numeric(15,4),
    deleted boolean NOT NULL,
    dias_validade_alerta integer,
    estoque_atual numeric(15,4) NOT NULL,
    estoque_maximo numeric(15,4),
    estoque_minimo numeric(15,4),
    margem_desejada numeric(5,2),
    origem_mercadoria integer,
    permite_fracao boolean NOT NULL,
    pesavel_balanca boolean NOT NULL,
    peso_liquido_padrao numeric(10,4),
    preco_atacado numeric(15,4),
    preco_venda numeric(15,4) NOT NULL,
    quantidade_minima_atacado integer,
    tara_padrao numeric(10,4),
    cest character varying(7),
    created_at timestamp(6) without time zone NOT NULL,
    ncm character varying(8),
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade_medida character varying(10) NOT NULL,
    id uuid NOT NULL,
    codigo_balanca character varying(20),
    tipo character varying(30) NOT NULL,
    codigo character varying(50) NOT NULL,
    codigo_barras character varying(100),
    nome character varying(200) NOT NULL,
    imagem_url character varying(500),
    descricao character varying(1000),
    observacoes character varying(2000),
    CONSTRAINT fruteira_produto_pkey PRIMARY KEY (id),
    CONSTRAINT fruteira_produto_codigo_key UNIQUE (codigo),
    CONSTRAINT fruteira_produto_codigo_barras_key UNIQUE (codigo_barras),
    CONSTRAINT fruteira_produto_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying])::text[])),
    CONSTRAINT fruteira_produto_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[])),
    CONSTRAINT fruteira_produto_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying])::text[])),
    CONSTRAINT fruteira_produto_custo_medio_check CHECK ((custo_medio >= (0)::numeric)),
    CONSTRAINT fruteira_produto_estoque_atual_check CHECK ((estoque_atual >= (0)::numeric)),
    CONSTRAINT fruteira_produto_preco_venda_check CHECK ((preco_venda >= (0)::numeric)),
    CONSTRAINT fruteira_produto_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FRUTA'::character varying, 'VERDURA'::character varying, 'LEGUME'::character varying, 'SECO'::character varying, 'BEBIDA'::character varying, 'LIMPEZA'::character varying, 'HIGIENE'::character varying, 'CONGELADO'::character varying, 'RESFRIADO'::character varying, 'PAES'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT fruteira_produto_unidade_medida_check CHECK (((unidade_medida)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);

--
-- Name: fruteira_produto fruteira_produto_codigo_barras_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_produto
    ADD CONSTRAINT fruteira_produto_codigo_barras_key UNIQUE (codigo_barras);

--
-- Name: fruteira_produto fruteira_produto_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_produto
    ADD CONSTRAINT fruteira_produto_codigo_key UNIQUE (codigo);

--
-- Name: fruteira_produto fruteira_produto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_produto
    ADD CONSTRAINT fruteira_produto_pkey PRIMARY KEY (id);

-- Insert default categories
INSERT INTO categorias (id, nome, descricao, ativo) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Frutas', 'Frutas frescas e secas', true),
    ('22222222-2222-2222-2222-222222222222', 'Verduras', 'Verduras e legumes', true),
    ('33333333-3333-3333-3333-333333333333', 'Buffet', 'Produtos do buffet', true),
    ('44444444-4444-4444-4444-444444444444', 'Bebidas', 'Bebidas em geral', true),
    ('55555555-5555-5555-5555-555555555555', 'Outros', 'Outros produtos', true)
ON CONFLICT (id) DO NOTHING;

--
-- Name: fruteira_produto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fruteira_produto (
    ativo boolean NOT NULL,
    controla_lote boolean NOT NULL,
    controla_validade boolean NOT NULL,
    cst_cofins character varying(2),
    cst_icms character varying(3),
    cst_pis character varying(2),
    custo_medio numeric(15,4) NOT NULL,
    custo_ultimo numeric(15,4),
    deleted boolean NOT NULL,
    dias_validade_alerta integer,
    estoque_atual numeric(15,4) NOT NULL,
    estoque_maximo numeric(15,4),
    estoque_minimo numeric(15,4),
    margem_desejada numeric(5,2),
    origem_mercadoria integer,
    permite_fracao boolean NOT NULL,
    pesavel_balanca boolean NOT NULL,
    peso_liquido_padrao numeric(10,4),
    preco_atacado numeric(15,4),
    preco_venda numeric(15,4) NOT NULL,
    quantidade_minima_atacado integer,
    tara_padrao numeric(10,4),
    cest character varying(7),
    created_at timestamp(6) without time zone NOT NULL,
    ncm character varying(8),
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade_medida character varying(10) NOT NULL,
    id uuid NOT NULL,
    codigo_balanca character varying(20),
    tipo character varying(30) NOT NULL,
    codigo character varying(50) NOT NULL,
    codigo_barras character varying(100),
    nome character varying(200) NOT NULL,
    imagem_url character varying(500),
    descricao character varying(1000),
    observacoes character varying(2000),
    CONSTRAINT fruteira_produto_pkey PRIMARY KEY (id),
    CONSTRAINT fruteira_produto_codigo_key UNIQUE (codigo),
    CONSTRAINT fruteira_produto_codigo_barras_key UNIQUE (codigo_barras),
    CONSTRAINT fruteira_produto_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying])::text[])),
    CONSTRAINT fruteira_produto_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[])),
    CONSTRAINT fruteira_produto_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying])::text[])),
    CONSTRAINT fruteira_produto_custo_medio_check CHECK ((custo_medio >= (0)::numeric)),
    CONSTRAINT fruteira_produto_estoque_atual_check CHECK ((estoque_atual >= (0)::numeric)),
    CONSTRAINT fruteira_produto_preco_venda_check CHECK ((preco_venda >= (0)::numeric)),
    CONSTRAINT fruteira_produto_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FRUTA'::character varying, 'VERDURA'::character varying, 'LEGUME'::character varying, 'SECO'::character varying, 'BEBIDA'::character varying, 'LIMPEZA'::character varying, 'HIGIENE'::character varying, 'CONGELADO'::character varying, 'RESFRIADO'::character varying, 'PAES'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT fruteira_produto_unidade_medida_check CHECK (((unidade_medida)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);

--
-- Name: fruteira_produto fruteira_produto_codigo_barras_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_produto
    ADD CONSTRAINT fruteira_produto_codigo_barras_key UNIQUE (codigo_barras);

--
-- Name: fruteira_produto fruteira_produto_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_produto
    ADD CONSTRAINT fruteira_produto_codigo_key UNIQUE (codigo);

--
-- Name: fruteira_produto fruteira_produto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_produto
    ADD CONSTRAINT fruteira_produto_pkey PRIMARY KEY (id);