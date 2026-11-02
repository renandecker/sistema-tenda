-- V2_1__ensure_produto_tables.sql
-- Garante as tabelas de produtos usadas pelas migrations seguintes (V3, V4 e V7)
-- e pelas entidades do sistema, mesmo em um banco criado do zero.
--
-- As migrations antigas (V1) criam apenas parte do schema. Sem esta migration,
-- um banco vazio falha na V3 (buffet_produto) e na V4 (cadastro_clientes).

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- buffet_produto (entidade ProdutoBuffet)
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.buffet_produto (
    ativo boolean NOT NULL,
    calorias integer,
    controla_estoque boolean NOT NULL,
    cst_cofins character varying(2),
    cst_icms character varying(3),
    cst_pis character varying(2),
    custo_medio numeric(15,4),
    deleted boolean NOT NULL,
    disponivel_balcao boolean NOT NULL,
    estoque_atual numeric(15,4),
    estoque_minimo numeric(15,4),
    origem_mercadoria integer,
    permite_fracao boolean NOT NULL,
    preco_venda numeric(15,4) NOT NULL,
    tempo_preparo_minutos integer,
    cest character varying(7),
    created_at timestamp(6) without time zone NOT NULL,
    ncm character varying(8),
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade_medida character varying(10) NOT NULL,
    id uuid NOT NULL,
    categoria character varying(30) NOT NULL,
    codigo character varying(50) NOT NULL,
    codigo_barras character varying(100),
    nome character varying(200) NOT NULL,
    alergenicos character varying(500),
    imagem_url character varying(500),
    descricao character varying(1000),
    informacoes_nutricionais character varying(1000),
    observacoes character varying(2000),
    CONSTRAINT buffet_produto_pkey PRIMARY KEY (id),
    CONSTRAINT buffet_produto_codigo_key UNIQUE (codigo),
    CONSTRAINT buffet_produto_codigo_barras_key UNIQUE (codigo_barras),
    CONSTRAINT buffet_produto_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['PRATO_QUENTE'::character varying, 'PRATO_FRIO'::character varying, 'SALADA'::character varying, 'GUARNICAO'::character varying, 'SOBREMESA'::character varying, 'BEBIDA'::character varying, 'BEBIDA_ALCOOLICA'::character varying, 'ADICIONAL'::character varying, 'PRATO_FEITO'::character varying, 'EXECUTIVO'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_103'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT buffet_produto_unidade_medida_check CHECK (((unidade_medida)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_buffet_produto_codigo ON public.buffet_produto USING btree (codigo);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_ativo ON public.buffet_produto USING btree (ativo);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_categoria ON public.buffet_produto USING btree (categoria);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_codigo_barras ON public.buffet_produto USING btree (codigo_barras);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_nome ON public.buffet_produto USING btree (nome);

-- =============================================================================
-- cadastro_clientes (entidade Cliente)
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.cadastro_clientes (
    data_nascimento date,
    deleted boolean NOT NULL,
    limite_credito numeric(15,2),
    pontos_fidelidade integer,
    ultima_compra date,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    cep character varying(9),
    numero character varying(10),
    id uuid NOT NULL,
    cpf_cnpj character varying(18) NOT NULL,
    inscricao_estadual character varying(20),
    rg_ie character varying(20),
    status character varying(20),
    telefone character varying(20),
    tipo_pessoa character varying(20),
    orgao_emissor character varying(50),
    tipo_cliente character varying(50),
    bairro character varying(100),
    cidade character varying(100),
    contato_comercial character varying(100),
    email character varying(100) NOT NULL,
    email_comercial character varying(100),
    nome character varying(200) NOT NULL,
    nome_fantasia character varying(200),
    endereco character varying(500),
    observacoes text,
    rg character varying(255),
    CONSTRAINT cadastro_clientes_pkey PRIMARY KEY (id),
    CONSTRAINT cadastro_clientes_email_key UNIQUE (email),
    CONSTRAINT cadastro_clientes_tipo_pessoa_check CHECK (((tipo_pessoa)::text = ANY ((ARRAY['FISICA'::character varying, 'JURIDICA'::character varying])::text[])))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_cliente_email ON public.cadastro_clientes USING btree (email);
CREATE INDEX IF NOT EXISTS idx_cliente_tipo ON public.cadastro_clientes USING btree (tipo_pessoa);
CREATE INDEX IF NOT EXISTS idx_cliente_ativo ON public.cadastro_clientes USING btree (status);
