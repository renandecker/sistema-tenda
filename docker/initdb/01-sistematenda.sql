--
-- PostgreSQL database dump
--

-- Dumped from database version 10.23
-- Dumped by pg_dump version 10.23

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: buffet_comanda; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_comanda (
    acrescimo numeric(15,4),
    deleted boolean NOT NULL,
    desconto numeric(15,4),
    preco_kg numeric(15,4),
    subtotal numeric(15,4) NOT NULL,
    tara_padrao numeric(10,4),
    valor_fixo numeric(15,4),
    valor_pago numeric(15,4),
    valor_total numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    data_abertura timestamp(6) without time zone NOT NULL,
    data_fechamento timestamp(6) without time zone,
    data_pagamento timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    mesa_id uuid,
    cliente_documento character varying(20),
    cliente_telefone character varying(20),
    modalidade character varying(20) NOT NULL,
    numero character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    aberta_por character varying(100),
    cancelada_por character varying(100),
    fechada_por character varying(100),
    cliente_nome character varying(200),
    motivo_cancelamento character varying(500),
    observacoes character varying(1000),
    CONSTRAINT buffet_comanda_modalidade_check CHECK (((modalidade)::text = ANY ((ARRAY['POR_PESO'::character varying, 'LIVRE'::character varying, 'PRATO_FEITO'::character varying, 'EXECUTIVO'::character varying, 'A_LA_CARTE'::character varying])::text[]))),
    CONSTRAINT buffet_comanda_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'EM_USO'::character varying, 'FECHADA'::character varying, 'PAGA'::character varying, 'CANCELADA'::character varying, 'EM_ATRASO'::character varying])::text[])))
);


--
-- Name: buffet_item_comanda; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_item_comanda (
    cancelado boolean NOT NULL,
    deleted boolean NOT NULL,
    peso_bruto numeric(10,4),
    peso_liquido numeric(10,4),
    preco_unitario numeric(15,4) NOT NULL,
    quantidade numeric(10,4) NOT NULL,
    tara numeric(10,4),
    valor_total numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    data_cancelamento timestamp(6) without time zone,
    data_lancamento timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    comanda_id uuid NOT NULL,
    id uuid NOT NULL,
    produto_id uuid,
    tipo character varying(20) NOT NULL,
    codigo_produto character varying(50),
    cancelado_por character varying(100),
    lancado_por character varying(100),
    nome_produto character varying(200) NOT NULL,
    motivo_cancelamento character varying(500),
    observacoes character varying(500),
    CONSTRAINT buffet_item_comanda_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PRATO_BUFFET'::character varying, 'BEBIDA'::character varying, 'SOBREMESA'::character varying, 'ADICIONAL'::character varying, 'PRATO_FEITO'::character varying, 'OUTROS'::character varying])::text[])))
);


--
-- Name: buffet_mesa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_mesa (
    ativa boolean NOT NULL,
    capacidade integer,
    deleted boolean NOT NULL,
    posicao_x integer,
    posicao_y integer,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    numero character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    area character varying(50),
    nome character varying(100),
    observacoes character varying(500),
    CONSTRAINT buffet_mesa_status_check CHECK (((status)::text = ANY ((ARRAY['LIVRE'::character varying, 'OCUPADA'::character varying, 'RESERVADA'::character varying, 'MANUTENCAO'::character varying, 'INATIVA'::character varying])::text[])))
);


--
-- Name: buffet_produto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_produto (
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
    CONSTRAINT buffet_produto_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['PRATO_QUENTE'::character varying, 'PRATO_FRIO'::character varying, 'SALADA'::character varying, 'GUARNICAO'::character varying, 'SOBREMESA'::character varying, 'BEBIDA'::character varying, 'BEBIDA_ALCOOLICA'::character varying, 'ADICIONAL'::character varying, 'PRATO_FEITO'::character varying, 'EXECUTIVO'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_103'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT buffet_produto_unidade_medida_check CHECK (((unidade_medida)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);


--
-- Name: buffet_receita; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_receita (
    ativa boolean NOT NULL,
    custo_total numeric(15,4),
    deleted boolean NOT NULL,
    margem_desejada numeric(5,2),
    preco_sugerido numeric(15,4),
    rendimento numeric(10,4) NOT NULL,
    tempo_cozimento_minutos integer,
    tempo_preparo_minutos integer,
    versao integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade_rendimento character varying(10) NOT NULL,
    id uuid NOT NULL,
    produto_id uuid NOT NULL,
    dificuldade character varying(20),
    observacoes character varying(2000),
    instrucoes character varying(5000),
    CONSTRAINT buffet_receita_unidade_rendimento_check CHECK (((unidade_rendimento)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);


--
-- Name: buffet_receita_ingrediente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_receita_ingrediente (
    custo_total numeric(15,4),
    custo_unitario numeric(15,4),
    deleted boolean NOT NULL,
    obrigatorio boolean NOT NULL,
    ordem integer,
    quantidade numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade character varying(10) NOT NULL,
    id uuid NOT NULL,
    produto_id uuid NOT NULL,
    receita_id uuid NOT NULL,
    observacoes character varying(500),
    CONSTRAINT buffet_receita_ingrediente_unidade_check CHECK (((unidade)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);


--
-- Name: buffet_transferencia_estoque; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.buffet_transferencia_estoque (
    custo_unitario numeric(15,4) NOT NULL,
    deleted boolean NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_total numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    data timestamp(6) without time zone NOT NULL,
    data_conclusao timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_id uuid NOT NULL,
    destino character varying(20) NOT NULL,
    origem character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    aprovado_por character varying(100),
    recebido_por character varying(100),
    solicitado_por character varying(100),
    motivo_rejeicao character varying(500),
    observacoes character varying(1000),
    CONSTRAINT buffet_transferencia_estoque_destino_check CHECK (((destino)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying, 'FORNECEDOR'::character varying, 'TRANSFERENCIA'::character varying])::text[]))),
    CONSTRAINT buffet_transferencia_estoque_origem_check CHECK (((origem)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying, 'FORNECEDOR'::character varying, 'TRANSFERENCIA'::character varying])::text[]))),
    CONSTRAINT buffet_transferencia_estoque_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'APROVADA'::character varying, 'REJEITADA'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[])))
);


--
-- Name: cadastro_clientes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cadastro_clientes (
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
    CONSTRAINT cadastro_clientes_tipo_pessoa_check CHECK (((tipo_pessoa)::text = ANY ((ARRAY['FISICA'::character varying, 'JURIDICA'::character varying])::text[])))
);


--
-- Name: cadastro_fornecedores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cadastro_fornecedores (
    aliquota_cofins numeric(5,2),
    aliquota_icms numeric(5,2),
    aliquota_pis numeric(5,2),
    cst_cofins character varying(2),
    cst_icms character varying(3),
    cst_pis character varying(2),
    deleted boolean NOT NULL,
    limite_compra numeric(15,2),
    percentual_desconto numeric(5,2),
    pontos_parceria integer,
    prazo_entrega_dias integer,
    total_compras numeric(15,2),
    ultima_compra date,
    codigo_ncm character varying(8),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    cep character varying(9),
    numero character varying(10),
    id uuid NOT NULL,
    cnpj_cpf character varying(18) NOT NULL,
    inscricao_estadual character varying(20),
    inscricao_municipal character varying(20),
    status character varying(20),
    telefone character varying(20),
    telefone_contato character varying(20),
    status_entrega character varying(30),
    situacao_tributaria character varying(50),
    tipo character varying(50),
    bairro character varying(100),
    cidade character varying(100),
    condicoes_pagamento character varying(100),
    contato character varying(100),
    email character varying(100),
    email_contato character varying(100),
    segmento_mercado character varying(100),
    nome character varying(200) NOT NULL,
    razao_social character varying(200),
    endereco character varying(500),
    observacoes text,
    CONSTRAINT cadastro_fornecedores_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT cadastro_fornecedores_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_103'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[]))),
    CONSTRAINT cadastro_fornecedores_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[])))
);


--
-- Name: common_audit_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.common_audit_log (
    sucesso boolean NOT NULL,
    data_acao timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    registro_id uuid,
    usuario_id uuid,
    ip_address character varying(45),
    entidade character varying(50),
    acao character varying(100) NOT NULL,
    descricao text,
    user_agent text
);


--
-- Name: common_usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.common_usuario (
    ativo boolean NOT NULL,
    primeiro_acesso boolean NOT NULL,
    tentativas_login integer,
    bloqueado_ate timestamp(6) without time zone,
    data_atualizacao timestamp(6) without time zone,
    data_criacao timestamp(6) without time zone NOT NULL,
    senha_expiracao timestamp(6) without time zone,
    ultimo_login timestamp(6) without time zone,
    empresa_id uuid,
    id uuid NOT NULL,
    pdv_id uuid,
    cpf_cnpj character varying(18),
    telefone character varying(20),
    tipo_pessoa character varying(20),
    email character varying(150) NOT NULL,
    nome character varying(150) NOT NULL,
    avatar_url character varying(500),
    senha_hash character varying(255) NOT NULL,
    CONSTRAINT common_usuario_tipo_pessoa_check CHECK (((tipo_pessoa)::text = ANY ((ARRAY['FISICA'::character varying, 'JURIDICA'::character varying])::text[])))
);


--
-- Name: common_usuario_perfil; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.common_usuario_perfil (
    usuario_id uuid NOT NULL,
    perfil character varying(50)
);


--
-- Name: estoque_alerta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estoque_alerta (
    data_vencimento date,
    deleted boolean NOT NULL,
    dias_para_vencimento integer,
    valor_atual numeric(15,4),
    valor_referencia numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_alerta timestamp(6) without time zone NOT NULL,
    data_resolucao timestamp(6) without time zone,
    data_visualizacao timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_buffet_id uuid,
    produto_fruteira_id uuid,
    prioridade character varying(20),
    status character varying(20) NOT NULL,
    tipo character varying(30) NOT NULL,
    resolvido_por character varying(100),
    visualizado_por character varying(100),
    mensagem character varying(500),
    observacoes_resolucao character varying(500),
    CONSTRAINT estoque_alerta_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['BAIXA'::character varying, 'MEDIA'::character varying, 'ALTA'::character varying, 'CRITICA'::character varying])::text[]))),
    CONSTRAINT estoque_alerta_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'VISUALIZADO'::character varying, 'RESOLVIDO'::character varying, 'IGNORADO'::character varying])::text[]))),
    CONSTRAINT estoque_alerta_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ESTOQUE_MINIMO'::character varying, 'ESTOQUE_MAXIMO'::character varying, 'VENCIMENTO_PROXIMO'::character varying, 'VENCIDO'::character varying, 'SEM_MOVIMENTACAO'::character varying, 'TEMPERATURA'::character varying])::text[])))
);


--
-- Name: estoque_inventario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estoque_inventario (
    deleted boolean NOT NULL,
    itens_contados integer,
    itens_divergentes integer,
    total_itens integer,
    valor_divergencia numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_fim timestamp(6) without time zone,
    data_inicio timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    responsavel character varying(100),
    descricao character varying(200),
    observacoes character varying(1000),
    CONSTRAINT estoque_inventario_status_check CHECK (((status)::text = ANY ((ARRAY['PLANEJADO'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDO'::character varying, 'CANCELADO'::character varying])::text[])))
);


--
-- Name: estoque_item_inventario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estoque_item_inventario (
    contado boolean NOT NULL,
    custo_unitario numeric(15,4),
    deleted boolean NOT NULL,
    quantidade_contada numeric(15,4),
    quantidade_divergencia numeric(15,4),
    quantidade_sistema numeric(15,4),
    valor_divergencia numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_contagem timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    inventario_id uuid NOT NULL,
    produto_buffet_id uuid,
    produto_fruteira_id uuid,
    contado_por character varying(100),
    observacoes character varying(500)
);


--
-- Name: estoque_movimento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estoque_movimento (
    custo_unitario numeric(15,4),
    deleted boolean NOT NULL,
    estoque_anterior numeric(15,4),
    estoque_posterior numeric(15,4),
    quantidade numeric(15,4) NOT NULL,
    valor_total numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data timestamp(6) without time zone NOT NULL,
    data_validade timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_buffet_id uuid,
    produto_fruteira_id uuid,
    destino character varying(20),
    origem character varying(20),
    tipo character varying(30) NOT NULL,
    lote character varying(50),
    numero_documento character varying(50),
    documento_referencia character varying(100),
    responsavel character varying(100),
    observacoes character varying(1000),
    CONSTRAINT estoque_movimento_destino_check CHECK (((destino)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying, 'FORNECEDOR'::character varying, 'CLIENTE'::character varying, 'PERDA'::character varying, 'AJUSTE'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT estoque_movimento_origem_check CHECK (((origem)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying, 'FORNECEDOR'::character varying, 'CLIENTE'::character varying, 'PERDA'::character varying, 'AJUSTE'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT estoque_movimento_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA_COMPRA'::character varying, 'ENTRADA_TRANSFERENCIA'::character varying, 'ENTRADA_AJUSTE'::character varying, 'ENTRADA_DEVOLUCAO'::character varying, 'SAIDA_VENDA'::character varying, 'SAIDA_TRANSFERENCIA'::character varying, 'SAIDA_PERDA'::character varying, 'SAIDA_AJUSTE'::character varying, 'SAIDA_CONSUMO_INTERNO'::character varying, 'INVENTARIO'::character varying])::text[])))
);


--
-- Name: fiscal_ativo_imobilizado; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_ativo_imobilizado (
    data_aquisicao date NOT NULL,
    data_baixa date,
    data_fim_depreciacao date,
    data_inicio_depreciacao date,
    deleted boolean NOT NULL,
    depreciacao_acumulada numeric(15,4),
    taxa_depreciacao_anual numeric(5,2),
    ultima_depreciacao date,
    valor_aquisicao numeric(15,4) NOT NULL,
    valor_baixa numeric(15,4),
    valor_liquido numeric(15,4),
    valor_residual numeric(15,4),
    vida_util_meses integer NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    metodo_depreciacao character varying(20),
    status character varying(20) NOT NULL,
    unidade_negocio character varying(20) NOT NULL,
    tipo character varying(30) NOT NULL,
    codigo character varying(50) NOT NULL,
    nota_fiscal character varying(50),
    responsavel character varying(100),
    descricao character varying(200) NOT NULL,
    fornecedor character varying(200),
    localizacao character varying(200),
    motivo_baixa character varying(500),
    observacoes text,
    CONSTRAINT fiscal_ativo_imobilizado_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'BAIXADO'::character varying, 'VENDIDO'::character varying, 'DOADO'::character varying, 'PERDIDO'::character varying, 'EM_MANUTENCAO'::character varying])::text[]))),
    CONSTRAINT fiscal_ativo_imobilizado_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MAQUINA_EQUIPAMENTO'::character varying, 'MOVEIS_UTENSILIOS'::character varying, 'VEICULOS'::character varying, 'EQUIPAMENTOS_INFORMATICA'::character varying, 'CAMARAS_FRIAS'::character varying, 'BALANCAS'::character varying, 'EQUIPAMENTOS_COZINHA'::character varying, 'INSTALACOES'::character varying, 'BENFEITORIAS'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT fiscal_ativo_imobilizado_unidade_negocio_check CHECK (((unidade_negocio)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying, 'ADMINISTRATIVO'::character varying, 'COMUM'::character varying])::text[])))
);


--
-- Name: fiscal_compra; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_compra (
    data_compra date NOT NULL,
    data_recebimento date,
    deleted boolean NOT NULL,
    desconto numeric(15,4),
    frete numeric(15,4),
    outras_despesas numeric(15,4),
    serie_nota integer,
    status smallint,
    valor_cofins numeric(15,4),
    valor_icms numeric(15,4),
    valor_ipi numeric(15,4),
    valor_pis numeric(15,4),
    valor_total numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    fornecedor_id uuid,
    id uuid NOT NULL,
    cnpj_fornecedor character varying(18),
    condicao_pagamento character varying(50),
    numero_nota character varying(50),
    numero_os character varying(50),
    nome_fornecedor character varying(200),
    observacoes text,
    CONSTRAINT fiscal_compra_status_check CHECK (((status >= 0) AND (status <= 3)))
);


--
-- Name: fiscal_conciliacao_cartao; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_conciliacao_cartao (
    data_recebimento date,
    data_venda date NOT NULL,
    deleted boolean NOT NULL,
    diferenca numeric(15,4),
    taxa_adesao numeric(15,4),
    taxa_mdr numeric(5,4),
    valor_bruto numeric(15,4) NOT NULL,
    valor_liquido numeric(15,4),
    valor_recebido numeric(15,4),
    valor_taxa numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_conciliacao timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    venda_id uuid,
    status character varying(20) NOT NULL,
    bandeira character varying(30),
    numero_venda character varying(30),
    codigo_autorizacao character varying(50),
    nsu character varying(50),
    adquirente character varying(100),
    conciliado_por character varying(100),
    observacoes character varying(500),
    CONSTRAINT fiscal_conciliacao_cartao_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'CONCILIADO'::character varying, 'DIVERGENTE'::character varying, 'ESTORNADO'::character varying, 'NAO_IDENTIFICADO'::character varying])::text[])))
);


--
-- Name: fiscal_dre; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_dre (
    cmv numeric(15,4),
    cmv_buffet numeric(15,4),
    cmv_fruteira numeric(15,4),
    csll numeric(15,4),
    data_fim date NOT NULL,
    data_inicio date NOT NULL,
    deleted boolean NOT NULL,
    descontos_concedidos numeric(15,4),
    despesas_administrativas numeric(15,4),
    despesas_financeiras numeric(15,4),
    despesas_nao_operacionais numeric(15,4),
    despesas_operacionais_total numeric(15,4),
    despesas_vendas numeric(15,4),
    devolucoes numeric(15,4),
    impostos_vendas numeric(15,4),
    irpj numeric(15,4),
    lucro_bruto numeric(15,4),
    lucro_liquido numeric(15,4),
    lucro_operacional numeric(15,4),
    margem_bruta_percentual numeric(5,2),
    margem_liquida_percentual numeric(5,2),
    meses_calculados integer,
    receita_bruta numeric(15,4),
    receita_buffet numeric(15,4),
    receita_fruteira numeric(15,4),
    receita_liquida numeric(15,4),
    receita_outros numeric(15,4),
    receitas_nao_operacionais numeric(15,4),
    resultado_antes_ir_csll numeric(15,4),
    resultado_nao_operacional numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    ultima_atualizacao timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    unidade_negocio character varying(20) NOT NULL,
    observacoes text,
    CONSTRAINT fiscal_dre_status_check CHECK (((status)::text = ANY ((ARRAY['GERANDO'::character varying, 'GERADO'::character varying, 'APROVADO'::character varying])::text[]))),
    CONSTRAINT fiscal_dre_unidade_negocio_check CHECK (((unidade_negocio)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying, 'CONSOLIDADO'::character varying, 'EMPRESARIAL'::character varying])::text[])))
);


--
-- Name: fiscal_item_compra; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_item_compra (
    cfop character varying(4),
    cst_icms character varying(3),
    data_cotacao_ceasa date,
    deleted boolean NOT NULL,
    preco_referencia_ceasa numeric(15,4),
    quantidade numeric(15,4),
    valor_total numeric(15,4),
    valor_unitario numeric(15,4),
    codigo_ncm character varying(8),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade character varying(10),
    compra_id uuid NOT NULL,
    id uuid NOT NULL,
    produto_buffet_id uuid,
    produto_id uuid,
    codigo_produto character varying(50),
    nome_produto character varying(200),
    observacoes character varying(500)
);


--
-- Name: fiscal_item_nota_entrada; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_item_nota_entrada (
    cfop character varying(4),
    cst_cofins character varying(2),
    cst_icms character varying(3),
    cst_pis character varying(2),
    deleted boolean NOT NULL,
    origem_mercadoria integer,
    processado boolean NOT NULL,
    quantidade numeric(15,4),
    valor_cofins numeric(15,4),
    valor_icms numeric(15,4),
    valor_ipi numeric(15,4),
    valor_pis numeric(15,4),
    valor_total numeric(15,4),
    valor_unitario numeric(15,4),
    cest character varying(7),
    created_at timestamp(6) without time zone NOT NULL,
    ncm character varying(8),
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    unidade character varying(10),
    id uuid NOT NULL,
    nota_entrada_id uuid NOT NULL,
    produto_id uuid,
    codigo_produto character varying(50),
    nome_produto character varying(200)
);


--
-- Name: fiscal_nfe; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_nfe (
    area_producao numeric(15,4),
    cfop_produtor character varying(4),
    data_colheita date,
    deleted boolean NOT NULL,
    digito_validador integer,
    eh_produtor_rural boolean,
    modalidade_acre character varying(2),
    modelo character varying(2),
    serie integer NOT NULL,
    tentativas_envio integer,
    valor_cofins numeric(15,4),
    valor_icms numeric(15,4),
    valor_ipi numeric(15,4),
    valor_pis numeric(15,4),
    valor_total numeric(15,4),
    volume_producao numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_emissao timestamp(6) without time zone NOT NULL,
    data_saida_entrada timestamp(6) without time zone,
    numero bigint NOT NULL,
    ultima_tentativa timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    venda_id uuid,
    cnpj_produtor character varying(18),
    ambiente character varying(20),
    cliente_documento character varying(20),
    rural_ie character varying(20),
    status character varying(20) NOT NULL,
    chave_acesso character varying(50),
    protocolo_autorizacao character varying(50),
    tipo_producao character varying(50),
    natureza_operacao character varying(60),
    grade_producao character varying(100),
    cliente_nome character varying(200),
    nome_produtor character varying(200),
    motivo_rejeicao character varying(500),
    obs_produtor_rural text,
    xml_autorizado text,
    xml_cancelamento text,
    xml_original text,
    danfe bytea,
    CONSTRAINT fiscal_nfe_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'EM_PROCESSAMENTO'::character varying, 'AUTORIZADA'::character varying, 'REJEITADA'::character varying, 'CANCELADA'::character varying, 'DENEGADA'::character varying, 'INUTILIZADA'::character varying, 'PRODUTOR_RURAL'::character varying])::text[])))
);


--
-- Name: fiscal_nota_entrada; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_nota_entrada (
    deleted boolean NOT NULL,
    modelo character varying(2),
    processada boolean NOT NULL,
    serie integer,
    valor_cofins numeric(15,4),
    valor_icms numeric(15,4),
    valor_ipi numeric(15,4),
    valor_pis numeric(15,4),
    valor_total numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_emissao timestamp(6) without time zone NOT NULL,
    data_entrada timestamp(6) without time zone,
    data_importacao timestamp(6) without time zone,
    data_processamento timestamp(6) without time zone,
    numero bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    fornecedor_documento character varying(20),
    fornecedor_ie character varying(20),
    status character varying(20) NOT NULL,
    chave_acesso character varying(50),
    protocolo_autorizacao character varying(50),
    fornecedor_nome character varying(200),
    erro_importacao character varying(1000),
    xml_original text,
    CONSTRAINT fiscal_nota_entrada_status_check CHECK (((status)::text = ANY ((ARRAY['IMPORTADA'::character varying, 'PROCESSADA'::character varying, 'ERRO'::character varying, 'CANCELADA'::character varying])::text[])))
);


--
-- Name: fiscal_sped_contribuicoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_sped_contribuicoes (
    data_fim date NOT NULL,
    data_inicio date NOT NULL,
    deleted boolean NOT NULL,
    indicador_movimento character varying(1),
    created_at timestamp(6) without time zone NOT NULL,
    data_geracao timestamp(6) without time zone,
    data_transmissao timestamp(6) without time zone,
    data_validacao timestamp(6) without time zone,
    tamanho_arquivo bigint,
    total_registros bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    versao_leiaute character varying(10),
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    protocolo_envio character varying(50),
    arquivo_gerado text,
    arquivo_validado text,
    erros_validacao text,
    CONSTRAINT fiscal_sped_contribuicoes_status_check CHECK (((status)::text = ANY ((ARRAY['GERANDO'::character varying, 'GERADO'::character varying, 'VALIDADO'::character varying, 'TRANSMITIDO'::character varying, 'ERRO'::character varying])::text[])))
);


--
-- Name: fiscal_sped_fiscal; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiscal_sped_fiscal (
    data_fim date NOT NULL,
    data_inicio date NOT NULL,
    deleted boolean NOT NULL,
    finalidade character varying(1),
    indicador_movimento character varying(1),
    perfil_apresentacao character varying(1),
    created_at timestamp(6) without time zone NOT NULL,
    data_geracao timestamp(6) without time zone,
    data_transmissao timestamp(6) without time zone,
    data_validacao timestamp(6) without time zone,
    tamanho_arquivo bigint,
    total_registros bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    versao_leiaute character varying(10),
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    protocolo_envio character varying(50),
    arquivo_gerado text,
    arquivo_validado text,
    erros_validacao text,
    CONSTRAINT fiscal_sped_fiscal_status_check CHECK (((status)::text = ANY ((ARRAY['GERANDO'::character varying, 'GERADO'::character varying, 'VALIDADO'::character varying, 'TRANSMITIDO'::character varying, 'ERRO'::character varying])::text[])))
);


--
-- Name: fruteira_etiqueta_balanca; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fruteira_etiqueta_balanca (
    ativa boolean NOT NULL,
    data_validade date,
    deleted boolean NOT NULL,
    imprimir_codigo_barras boolean NOT NULL,
    imprimir_qr_code boolean NOT NULL,
    preco numeric(15,4) NOT NULL,
    quantidade_impressa integer,
    tara numeric(10,4),
    validade_dias integer,
    created_at timestamp(6) without time zone NOT NULL,
    ultima_impressao timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_id uuid NOT NULL,
    codigo character varying(20) NOT NULL,
    modelo_etiqueta character varying(50),
    texto_livre_1 character varying(100),
    texto_livre_2 character varying(100)
);


--
-- Name: fruteira_perda; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fruteira_perda (
    baixa_estoque_realizada boolean NOT NULL,
    custo_unitario numeric(15,4) NOT NULL,
    data date NOT NULL,
    data_validade date,
    deleted boolean NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_total numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    data_baixa_estoque timestamp(6) without time zone,
    hora timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_id uuid NOT NULL,
    tipo character varying(20) NOT NULL,
    lote character varying(50),
    autorizado_por character varying(100),
    responsavel character varying(100),
    motivo character varying(500),
    observacoes character varying(1000),
    CONSTRAINT fruteira_perda_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['DETERIORACAO'::character varying, 'AVARIA'::character varying, 'VENCIMENTO'::character varying, 'FURTO'::character varying, 'DOACAO'::character varying, 'OUTROS'::character varying])::text[])))
);


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
    CONSTRAINT fruteira_produto_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT fruteira_produto_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_103'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[]))),
    CONSTRAINT fruteira_produto_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT fruteira_produto_custo_medio_check CHECK ((custo_medio >= (0)::numeric)),
    CONSTRAINT fruteira_produto_estoque_atual_check CHECK ((estoque_atual >= (0)::numeric)),
    CONSTRAINT fruteira_produto_preco_venda_check CHECK ((preco_venda >= (0)::numeric)),
    CONSTRAINT fruteira_produto_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FRUTA'::character varying, 'VERDURA'::character varying, 'LEGUME'::character varying, 'SECO'::character varying, 'BEBIDA'::character varying, 'LIMPEZA'::character varying, 'HIGIENE'::character varying, 'CONGELADO'::character varying, 'RESFRIADO'::character varying, 'PAES'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT fruteira_produto_unidade_medida_check CHECK (((unidade_medida)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);


--
-- Name: fruteira_tabela_preco; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fruteira_tabela_preco (
    ativa boolean NOT NULL,
    data_fim date,
    data_inicio date NOT NULL,
    deleted boolean NOT NULL,
    preco_atacado numeric(15,4),
    preco_venda numeric(15,4) NOT NULL,
    quantidade_minima_atacado integer,
    aplicada_em timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_id uuid NOT NULL,
    origem character varying(50),
    aplicada_por character varying(100),
    observacoes character varying(500)
);


--
-- Name: pdv_item_venda; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdv_item_venda (
    cancelado boolean NOT NULL,
    deleted boolean NOT NULL,
    peso_bruto numeric(10,4),
    peso_liquido numeric(10,4),
    preco_unitario numeric(15,4) NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    tara numeric(10,4),
    valor_total numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    produto_buffet_id uuid,
    produto_fruteira_id uuid,
    venda_id uuid NOT NULL,
    origem character varying(20) NOT NULL,
    tipo character varying(20),
    codigo_produto character varying(50) NOT NULL,
    nome_produto character varying(200) NOT NULL,
    observacoes character varying(500),
    CONSTRAINT pdv_item_venda_origem_check CHECK (((origem)::text = ANY ((ARRAY['FRUTEIRA'::character varying, 'BUFFET'::character varying])::text[]))),
    CONSTRAINT pdv_item_venda_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PRODUTO'::character varying, 'PRATO_BUFFET'::character varying, 'BEBIDA'::character varying, 'SOBREMESA'::character varying, 'ADICIONAL'::character varying, 'PRATO_FEITO'::character varying, 'OUTROS'::character varying, 'SERVICO'::character varying])::text[])))
);


--
-- Name: pdv_movimento_caixa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdv_movimento_caixa (
    deleted boolean NOT NULL,
    valor numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    data timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    sessao_caixa_id uuid NOT NULL,
    venda_id uuid,
    tipo character varying(20) NOT NULL,
    operador character varying(100) NOT NULL,
    descricao character varying(500),
    CONSTRAINT pdv_movimento_caixa_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SANGRIA'::character varying, 'REFORCO'::character varying, 'VENDA'::character varying, 'ESTORNO'::character varying, 'AJUSTE'::character varying])::text[])))
);


--
-- Name: pdv_pagamento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdv_pagamento (
    deleted boolean NOT NULL,
    parcelas integer,
    ultimos_digitos_cartao character varying(4),
    valor numeric(15,4) NOT NULL,
    valor_liquido numeric(15,4),
    valor_taxa numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_autorizacao timestamp(6) without time zone,
    data_pagamento timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    vale_validade timestamp(6) without time zone,
    version bigint,
    id uuid NOT NULL,
    venda_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    bandeira_cartao character varying(30),
    forma_pagamento character varying(30) NOT NULL,
    codigo_autorizacao character varying(50),
    nsu character varying(50),
    vale_numero character varying(50),
    pix_tx_id character varying(100),
    mensagem_retorno character varying(500),
    comprovante text,
    pix_qr_code text,
    CONSTRAINT pdv_pagamento_forma_pagamento_check CHECK (((forma_pagamento)::text = ANY ((ARRAY['DINHEIRO'::character varying, 'CARTAO_CREDITO'::character varying, 'CARTAO_DEBITO'::character varying, 'PIX'::character varying, 'VALE_ALIMENTACAO'::character varying, 'VALE_REFEICAO'::character varying, 'VALE_CULTURA'::character varying, 'CHEQUE'::character varying, 'TRANSFERENCIA'::character varying, 'BOLETO'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT pdv_pagamento_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'AUTORIZADO'::character varying, 'NEGADO'::character varying, 'CANCELADO'::character varying, 'ESTORNADO'::character varying])::text[])))
);


--
-- Name: pdv_sessao_caixa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdv_sessao_caixa (
    deleted boolean NOT NULL,
    diferenca numeric(15,4),
    reforcos_total numeric(15,4),
    sangrias_total numeric(15,4),
    status_sangria_aberta boolean,
    total_vendas numeric(15,4),
    valor_final_contado numeric(15,4),
    valor_final_sistema numeric(15,4),
    valor_inicial numeric(15,4) NOT NULL,
    vendas_cartao numeric(15,4),
    vendas_dinheiro numeric(15,4),
    vendas_pix numeric(15,4),
    vendas_vale numeric(15,4),
    created_at timestamp(6) without time zone NOT NULL,
    data_abertura timestamp(6) without time zone NOT NULL,
    data_fechamento timestamp(6) without time zone,
    numero_ultima_venda bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    pdv_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    operador_abertura character varying(100) NOT NULL,
    operador_fechamento character varying(100),
    observacoes_abertura character varying(1000),
    observacoes_fechamento character varying(1000),
    reducao_z text,
    CONSTRAINT pdv_sessao_caixa_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'FECHADA'::character varying, 'SANGRIA'::character varying, 'REFORCO'::character varying])::text[])))
);


--
-- Name: pdv_terminal; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdv_terminal (
    ativo boolean NOT NULL,
    deleted boolean NOT NULL,
    tef_habilitado boolean NOT NULL,
    tef_porta integer,
    created_at timestamp(6) without time zone NOT NULL,
    numero_ultima_venda bigint,
    ultima_abertura timestamp(6) without time zone,
    ultimo_fechamento timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    codigo character varying(20) NOT NULL,
    porta_balanca_buffet character varying(20),
    porta_balanca_fruteira character varying(20),
    status character varying(20) NOT NULL,
    tef_ip character varying(50),
    impressora_fiscal character varying(100),
    impressora_nao_fiscal character varying(100),
    nome character varying(100) NOT NULL,
    observacoes character varying(500),
    CONSTRAINT pdv_terminal_status_check CHECK (((status)::text = ANY ((ARRAY['LIVRE'::character varying, 'EM_USO'::character varying, 'MANUTENCAO'::character varying, 'INATIVO'::character varying])::text[])))
);


--
-- Name: pdv_venda; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdv_venda (
    acrescimo numeric(15,4),
    deleted boolean NOT NULL,
    desconto numeric(15,4),
    subtotal numeric(15,4) NOT NULL,
    subtotal_buffet numeric(15,4) NOT NULL,
    subtotal_fruteira numeric(15,4) NOT NULL,
    troco numeric(15,4),
    valor_pago numeric(15,4),
    valor_total numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    data timestamp(6) without time zone NOT NULL,
    data_finalizacao timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    comanda_id uuid,
    id uuid NOT NULL,
    nfce_id uuid,
    pdv_id uuid,
    cliente_documento character varying(20),
    numero_nfce character varying(20),
    status character varying(20) NOT NULL,
    numero character varying(30) NOT NULL,
    chave_nfce character varying(50),
    operador character varying(100),
    cliente_nome character varying(200),
    observacoes character varying(1000),
    xml_nfce text,
    danfe_nfce bytea,
    CONSTRAINT pdv_venda_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'EM_ANDAMENTO'::character varying, 'FINALIZADA'::character varying, 'PAGA'::character varying, 'CANCELADA'::character varying, 'DEVOLVIDA'::character varying, 'PENDENTE_PAGAMENTO'::character varying])::text[])))
);


--
-- Data for Name: buffet_comanda; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_comanda (acrescimo, deleted, desconto, preco_kg, subtotal, tara_padrao, valor_fixo, valor_pago, valor_total, created_at, data_abertura, data_fechamento, data_pagamento, updated_at, version, id, mesa_id, cliente_documento, cliente_telefone, modalidade, numero, status, aberta_por, cancelada_por, fechada_por, cliente_nome, motivo_cancelamento, observacoes) FROM stdin;
\.


--
-- Data for Name: buffet_item_comanda; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_item_comanda (cancelado, deleted, peso_bruto, peso_liquido, preco_unitario, quantidade, tara, valor_total, created_at, data_cancelamento, data_lancamento, updated_at, version, comanda_id, id, produto_id, tipo, codigo_produto, cancelado_por, lancado_por, nome_produto, motivo_cancelamento, observacoes) FROM stdin;
\.


--
-- Data for Name: buffet_mesa; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_mesa (ativa, capacidade, deleted, posicao_x, posicao_y, created_at, updated_at, version, id, numero, status, area, nome, observacoes) FROM stdin;
\.


--
-- Data for Name: buffet_produto; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_produto (ativo, calorias, controla_estoque, cst_cofins, cst_icms, cst_pis, custo_medio, deleted, disponivel_balcao, estoque_atual, estoque_minimo, origem_mercadoria, permite_fracao, preco_venda, tempo_preparo_minutos, cest, created_at, ncm, updated_at, version, unidade_medida, id, categoria, codigo, codigo_barras, nome, alergenicos, imagem_url, descricao, informacoes_nutricionais, observacoes) FROM stdin;
\.


--
-- Data for Name: buffet_receita; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_receita (ativa, custo_total, deleted, margem_desejada, preco_sugerido, rendimento, tempo_cozimento_minutos, tempo_preparo_minutos, versao, created_at, updated_at, version, unidade_rendimento, id, produto_id, dificuldade, observacoes, instrucoes) FROM stdin;
\.


--
-- Data for Name: buffet_receita_ingrediente; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_receita_ingrediente (custo_total, custo_unitario, deleted, obrigatorio, ordem, quantidade, created_at, updated_at, version, unidade, id, produto_id, receita_id, observacoes) FROM stdin;
\.


--
-- Data for Name: buffet_transferencia_estoque; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.buffet_transferencia_estoque (custo_unitario, deleted, quantidade, valor_total, created_at, data, data_conclusao, updated_at, version, id, produto_id, destino, origem, status, aprovado_por, recebido_por, solicitado_por, motivo_rejeicao, observacoes) FROM stdin;
\.


--
-- Data for Name: cadastro_clientes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cadastro_clientes (data_nascimento, deleted, limite_credito, pontos_fidelidade, ultima_compra, created_at, updated_at, version, cep, numero, id, cpf_cnpj, inscricao_estadual, rg_ie, status, telefone, tipo_pessoa, orgao_emissor, tipo_cliente, bairro, cidade, contato_comercial, email, email_comercial, nome, nome_fantasia, endereco, observacoes, rg) FROM stdin;
\.


--
-- Data for Name: cadastro_fornecedores; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cadastro_fornecedores (aliquota_cofins, aliquota_icms, aliquota_pis, cst_cofins, cst_icms, cst_pis, deleted, limite_compra, percentual_desconto, pontos_parceria, prazo_entrega_dias, total_compras, ultima_compra, codigo_ncm, created_at, updated_at, version, cep, numero, id, cnpj_cpf, inscricao_estadual, inscricao_municipal, status, telefone, telefone_contato, status_entrega, situacao_tributaria, tipo, bairro, cidade, condicoes_pagamento, contato, email, email_contato, segmento_mercado, nome, razao_social, endereco, observacoes) FROM stdin;
\.


--
-- Data for Name: common_audit_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.common_audit_log (sucesso, data_acao, id, registro_id, usuario_id, ip_address, entidade, acao, descricao, user_agent) FROM stdin;
\.


--
-- Data for Name: common_usuario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.common_usuario (ativo, primeiro_acesso, tentativas_login, bloqueado_ate, data_atualizacao, data_criacao, senha_expiracao, ultimo_login, empresa_id, id, pdv_id, cpf_cnpj, telefone, tipo_pessoa, email, nome, avatar_url, senha_hash) FROM stdin;
t	f	\N	\N	\N	2026-10-14 21:53:42.002722	\N	\N	\N	7afd842a-3ae3-414b-8d51-97963eac145a	\N	00000000000	\N	FISICA	admin@sistematenda.com	Administrador	\N	$2a$12$IF3P6uOfOWu.EufU8gihIOMkAYF3sY5YvtYxptjvGcvMXRR4FDEeK
\.


--
-- Data for Name: common_usuario_perfil; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.common_usuario_perfil (usuario_id, perfil) FROM stdin;
7afd842a-3ae3-414b-8d51-97963eac145a	ADMIN
7afd842a-3ae3-414b-8d51-97963eac145a	GERENTE
7afd842a-3ae3-414b-8d51-97963eac145a	FISCAL
7afd842a-3ae3-414b-8d51-97963eac145a	CONTABIL
7afd842a-3ae3-414b-8d51-97963eac145a	PDV
7afd842a-3ae3-414b-8d51-97963eac145a	ESTOQUE
7afd842a-3ae3-414b-8d51-97963eac145a	FRUTEIRA
7afd842a-3ae3-414b-8d51-97963eac145a	BUFFET
\.


--
-- Data for Name: estoque_alerta; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.estoque_alerta (data_vencimento, deleted, dias_para_vencimento, valor_atual, valor_referencia, created_at, data_alerta, data_resolucao, data_visualizacao, updated_at, version, id, produto_buffet_id, produto_fruteira_id, prioridade, status, tipo, resolvido_por, visualizado_por, mensagem, observacoes_resolucao) FROM stdin;
\.


--
-- Data for Name: estoque_inventario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.estoque_inventario (deleted, itens_contados, itens_divergentes, total_itens, valor_divergencia, created_at, data_fim, data_inicio, updated_at, version, id, status, responsavel, descricao, observacoes) FROM stdin;
\.


--
-- Data for Name: estoque_item_inventario; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.estoque_item_inventario (contado, custo_unitario, deleted, quantidade_contada, quantidade_divergencia, quantidade_sistema, valor_divergencia, created_at, data_contagem, updated_at, version, id, inventario_id, produto_buffet_id, produto_fruteira_id, contado_por, observacoes) FROM stdin;
\.


--
-- Data for Name: estoque_movimento; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.estoque_movimento (custo_unitario, deleted, estoque_anterior, estoque_posterior, quantidade, valor_total, created_at, data, data_validade, updated_at, version, id, produto_buffet_id, produto_fruteira_id, destino, origem, tipo, lote, numero_documento, documento_referencia, responsavel, observacoes) FROM stdin;
\.


--
-- Data for Name: fiscal_ativo_imobilizado; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_ativo_imobilizado (data_aquisicao, data_baixa, data_fim_depreciacao, data_inicio_depreciacao, deleted, depreciacao_acumulada, taxa_depreciacao_anual, ultima_depreciacao, valor_aquisicao, valor_baixa, valor_liquido, valor_residual, vida_util_meses, created_at, updated_at, version, id, metodo_depreciacao, status, unidade_negocio, tipo, codigo, nota_fiscal, responsavel, descricao, fornecedor, localizacao, motivo_baixa, observacoes) FROM stdin;
\.


--
-- Data for Name: fiscal_compra; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_compra (data_compra, data_recebimento, deleted, desconto, frete, outras_despesas, serie_nota, status, valor_cofins, valor_icms, valor_ipi, valor_pis, valor_total, created_at, updated_at, version, fornecedor_id, id, cnpj_fornecedor, condicao_pagamento, numero_nota, numero_os, nome_fornecedor, observacoes) FROM stdin;
\.


--
-- Data for Name: fiscal_conciliacao_cartao; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_conciliacao_cartao (data_recebimento, data_venda, deleted, diferenca, taxa_adesao, taxa_mdr, valor_bruto, valor_liquido, valor_recebido, valor_taxa, created_at, data_conciliacao, updated_at, version, id, venda_id, status, bandeira, numero_venda, codigo_autorizacao, nsu, adquirente, conciliado_por, observacoes) FROM stdin;
\.


--
-- Data for Name: fiscal_dre; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_dre (cmv, cmv_buffet, cmv_fruteira, csll, data_fim, data_inicio, deleted, descontos_concedidos, despesas_administrativas, despesas_financeiras, despesas_nao_operacionais, despesas_operacionais_total, despesas_vendas, devolucoes, impostos_vendas, irpj, lucro_bruto, lucro_liquido, lucro_operacional, margem_bruta_percentual, margem_liquida_percentual, meses_calculados, receita_bruta, receita_buffet, receita_fruteira, receita_liquida, receita_outros, receitas_nao_operacionais, resultado_antes_ir_csll, resultado_nao_operacional, created_at, ultima_atualizacao, updated_at, version, id, status, unidade_negocio, observacoes) FROM stdin;
\.


--
-- Data for Name: fiscal_item_compra; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_item_compra (cfop, cst_icms, data_cotacao_ceasa, deleted, preco_referencia_ceasa, quantidade, valor_total, valor_unitario, codigo_ncm, created_at, updated_at, version, unidade, compra_id, id, produto_buffet_id, produto_id, codigo_produto, nome_produto, observacoes) FROM stdin;
\.


--
-- Data for Name: fiscal_item_nota_entrada; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_item_nota_entrada (cfop, cst_cofins, cst_icms, cst_pis, deleted, origem_mercadoria, processado, quantidade, valor_cofins, valor_icms, valor_ipi, valor_pis, valor_total, valor_unitario, cest, created_at, ncm, updated_at, version, unidade, id, nota_entrada_id, produto_id, codigo_produto, nome_produto) FROM stdin;
\.


--
-- Data for Name: fiscal_nfe; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_nfe (area_producao, cfop_produtor, data_colheita, deleted, digito_validador, eh_produtor_rural, modalidade_acre, modelo, serie, tentativas_envio, valor_cofins, valor_icms, valor_ipi, valor_pis, valor_total, volume_producao, created_at, data_emissao, data_saida_entrada, numero, ultima_tentativa, updated_at, version, id, venda_id, cnpj_produtor, ambiente, cliente_documento, rural_ie, status, chave_acesso, protocolo_autorizacao, tipo_producao, natureza_operacao, grade_producao, cliente_nome, nome_produtor, motivo_rejeicao, obs_produtor_rural, xml_autorizado, xml_cancelamento, xml_original, danfe) FROM stdin;
\.


--
-- Data for Name: fiscal_nota_entrada; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_nota_entrada (deleted, modelo, processada, serie, valor_cofins, valor_icms, valor_ipi, valor_pis, valor_total, created_at, data_emissao, data_entrada, data_importacao, data_processamento, numero, updated_at, version, id, fornecedor_documento, fornecedor_ie, status, chave_acesso, protocolo_autorizacao, fornecedor_nome, erro_importacao, xml_original) FROM stdin;
\.


--
-- Data for Name: fiscal_sped_contribuicoes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_sped_contribuicoes (data_fim, data_inicio, deleted, indicador_movimento, created_at, data_geracao, data_transmissao, data_validacao, tamanho_arquivo, total_registros, updated_at, version, versao_leiaute, id, status, protocolo_envio, arquivo_gerado, arquivo_validado, erros_validacao) FROM stdin;
\.


--
-- Data for Name: fiscal_sped_fiscal; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fiscal_sped_fiscal (data_fim, data_inicio, deleted, finalidade, indicador_movimento, perfil_apresentacao, created_at, data_geracao, data_transmissao, data_validacao, tamanho_arquivo, total_registros, updated_at, version, versao_leiaute, id, status, protocolo_envio, arquivo_gerado, arquivo_validado, erros_validacao) FROM stdin;
\.


--
-- Data for Name: fruteira_etiqueta_balanca; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fruteira_etiqueta_balanca (ativa, data_validade, deleted, imprimir_codigo_barras, imprimir_qr_code, preco, quantidade_impressa, tara, validade_dias, created_at, ultima_impressao, updated_at, version, id, produto_id, codigo, modelo_etiqueta, texto_livre_1, texto_livre_2) FROM stdin;
\.


--
-- Data for Name: fruteira_perda; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fruteira_perda (baixa_estoque_realizada, custo_unitario, data, data_validade, deleted, quantidade, valor_total, created_at, data_baixa_estoque, hora, updated_at, version, id, produto_id, tipo, lote, autorizado_por, responsavel, motivo, observacoes) FROM stdin;
\.


--
-- Data for Name: fruteira_produto; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fruteira_produto (ativo, controla_lote, controla_validade, cst_cofins, cst_icms, cst_pis, custo_medio, custo_ultimo, deleted, dias_validade_alerta, estoque_atual, estoque_maximo, estoque_minimo, margem_desejada, origem_mercadoria, permite_fracao, pesavel_balanca, peso_liquido_padrao, preco_atacado, preco_venda, quantidade_minima_atacado, tara_padrao, cest, created_at, ncm, updated_at, version, unidade_medida, id, codigo_balanca, tipo, codigo, codigo_barras, nome, imagem_url, descricao, observacoes) FROM stdin;
\.


--
-- Data for Name: fruteira_tabela_preco; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.fruteira_tabela_preco (ativa, data_fim, data_inicio, deleted, preco_atacado, preco_venda, quantidade_minima_atacado, aplicada_em, created_at, updated_at, version, id, produto_id, origem, aplicada_por, observacoes) FROM stdin;
\.


--
-- Data for Name: pdv_item_venda; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pdv_item_venda (cancelado, deleted, peso_bruto, peso_liquido, preco_unitario, quantidade, tara, valor_total, created_at, updated_at, version, id, produto_buffet_id, produto_fruteira_id, venda_id, origem, tipo, codigo_produto, nome_produto, observacoes) FROM stdin;
\.


--
-- Data for Name: pdv_movimento_caixa; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pdv_movimento_caixa (deleted, valor, created_at, data, updated_at, version, id, sessao_caixa_id, venda_id, tipo, operador, descricao) FROM stdin;
\.


--
-- Data for Name: pdv_pagamento; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pdv_pagamento (deleted, parcelas, ultimos_digitos_cartao, valor, valor_liquido, valor_taxa, created_at, data_autorizacao, data_pagamento, updated_at, vale_validade, version, id, venda_id, status, bandeira_cartao, forma_pagamento, codigo_autorizacao, nsu, vale_numero, pix_tx_id, mensagem_retorno, comprovante, pix_qr_code) FROM stdin;
\.


--
-- Data for Name: pdv_sessao_caixa; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pdv_sessao_caixa (deleted, diferenca, reforcos_total, sangrias_total, status_sangria_aberta, total_vendas, valor_final_contado, valor_final_sistema, valor_inicial, vendas_cartao, vendas_dinheiro, vendas_pix, vendas_vale, created_at, data_abertura, data_fechamento, numero_ultima_venda, updated_at, version, id, pdv_id, status, operador_abertura, operador_fechamento, observacoes_abertura, observacoes_fechamento, reducao_z) FROM stdin;
\.


--
-- Data for Name: pdv_terminal; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pdv_terminal (ativo, deleted, tef_habilitado, tef_porta, created_at, numero_ultima_venda, ultima_abertura, ultimo_fechamento, updated_at, version, id, codigo, porta_balanca_buffet, porta_balanca_fruteira, status, tef_ip, impressora_fiscal, impressora_nao_fiscal, nome, observacoes) FROM stdin;
\.


--
-- Data for Name: pdv_venda; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pdv_venda (acrescimo, deleted, desconto, subtotal, subtotal_buffet, subtotal_fruteira, troco, valor_pago, valor_total, created_at, data, data_finalizacao, updated_at, version, comanda_id, id, nfce_id, pdv_id, cliente_documento, numero_nfce, status, numero, chave_nfce, operador, cliente_nome, observacoes, xml_nfce, danfe_nfce) FROM stdin;
\.


--
-- Name: buffet_comanda buffet_comanda_numero_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_comanda
    ADD CONSTRAINT buffet_comanda_numero_key UNIQUE (numero);


--
-- Name: buffet_comanda buffet_comanda_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_comanda
    ADD CONSTRAINT buffet_comanda_pkey PRIMARY KEY (id);


--
-- Name: buffet_item_comanda buffet_item_comanda_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_item_comanda
    ADD CONSTRAINT buffet_item_comanda_pkey PRIMARY KEY (id);


--
-- Name: buffet_mesa buffet_mesa_numero_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_mesa
    ADD CONSTRAINT buffet_mesa_numero_key UNIQUE (numero);


--
-- Name: buffet_mesa buffet_mesa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_mesa
    ADD CONSTRAINT buffet_mesa_pkey PRIMARY KEY (id);


--
-- Name: buffet_produto buffet_produto_codigo_barras_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_produto
    ADD CONSTRAINT buffet_produto_codigo_barras_key UNIQUE (codigo_barras);


--
-- Name: buffet_produto buffet_produto_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_produto
    ADD CONSTRAINT buffet_produto_codigo_key UNIQUE (codigo);


--
-- Name: buffet_produto buffet_produto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_produto
    ADD CONSTRAINT buffet_produto_pkey PRIMARY KEY (id);


--
-- Name: buffet_receita_ingrediente buffet_receita_ingrediente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_receita_ingrediente
    ADD CONSTRAINT buffet_receita_ingrediente_pkey PRIMARY KEY (id);


--
-- Name: buffet_receita buffet_receita_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_receita
    ADD CONSTRAINT buffet_receita_pkey PRIMARY KEY (id);


--
-- Name: buffet_receita buffet_receita_produto_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_receita
    ADD CONSTRAINT buffet_receita_produto_id_key UNIQUE (produto_id);


--
-- Name: buffet_transferencia_estoque buffet_transferencia_estoque_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_transferencia_estoque
    ADD CONSTRAINT buffet_transferencia_estoque_pkey PRIMARY KEY (id);


--
-- Name: cadastro_clientes cadastro_clientes_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cadastro_clientes
    ADD CONSTRAINT cadastro_clientes_email_key UNIQUE (email);


--
-- Name: cadastro_clientes cadastro_clientes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cadastro_clientes
    ADD CONSTRAINT cadastro_clientes_pkey PRIMARY KEY (id);


--
-- Name: cadastro_fornecedores cadastro_fornecedores_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cadastro_fornecedores
    ADD CONSTRAINT cadastro_fornecedores_email_key UNIQUE (email);


--
-- Name: cadastro_fornecedores cadastro_fornecedores_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cadastro_fornecedores
    ADD CONSTRAINT cadastro_fornecedores_pkey PRIMARY KEY (id);


--
-- Name: common_audit_log common_audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.common_audit_log
    ADD CONSTRAINT common_audit_log_pkey PRIMARY KEY (id);


--
-- Name: common_usuario common_usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.common_usuario
    ADD CONSTRAINT common_usuario_email_key UNIQUE (email);


--
-- Name: common_usuario common_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.common_usuario
    ADD CONSTRAINT common_usuario_pkey PRIMARY KEY (id);


--
-- Name: estoque_alerta estoque_alerta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_alerta
    ADD CONSTRAINT estoque_alerta_pkey PRIMARY KEY (id);


--
-- Name: estoque_inventario estoque_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_inventario
    ADD CONSTRAINT estoque_inventario_pkey PRIMARY KEY (id);


--
-- Name: estoque_item_inventario estoque_item_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_item_inventario
    ADD CONSTRAINT estoque_item_inventario_pkey PRIMARY KEY (id);


--
-- Name: estoque_movimento estoque_movimento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_movimento
    ADD CONSTRAINT estoque_movimento_pkey PRIMARY KEY (id);


--
-- Name: fiscal_ativo_imobilizado fiscal_ativo_imobilizado_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_ativo_imobilizado
    ADD CONSTRAINT fiscal_ativo_imobilizado_codigo_key UNIQUE (codigo);


--
-- Name: fiscal_ativo_imobilizado fiscal_ativo_imobilizado_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_ativo_imobilizado
    ADD CONSTRAINT fiscal_ativo_imobilizado_pkey PRIMARY KEY (id);


--
-- Name: fiscal_compra fiscal_compra_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_compra
    ADD CONSTRAINT fiscal_compra_pkey PRIMARY KEY (id);


--
-- Name: fiscal_conciliacao_cartao fiscal_conciliacao_cartao_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_conciliacao_cartao
    ADD CONSTRAINT fiscal_conciliacao_cartao_pkey PRIMARY KEY (id);


--
-- Name: fiscal_dre fiscal_dre_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_dre
    ADD CONSTRAINT fiscal_dre_pkey PRIMARY KEY (id);


--
-- Name: fiscal_item_compra fiscal_item_compra_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_compra
    ADD CONSTRAINT fiscal_item_compra_pkey PRIMARY KEY (id);


--
-- Name: fiscal_item_nota_entrada fiscal_item_nota_entrada_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_nota_entrada
    ADD CONSTRAINT fiscal_item_nota_entrada_pkey PRIMARY KEY (id);


--
-- Name: fiscal_nfe fiscal_nfe_chave_acesso_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_nfe
    ADD CONSTRAINT fiscal_nfe_chave_acesso_key UNIQUE (chave_acesso);


--
-- Name: fiscal_nfe fiscal_nfe_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_nfe
    ADD CONSTRAINT fiscal_nfe_pkey PRIMARY KEY (id);


--
-- Name: fiscal_nota_entrada fiscal_nota_entrada_chave_acesso_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_nota_entrada
    ADD CONSTRAINT fiscal_nota_entrada_chave_acesso_key UNIQUE (chave_acesso);


--
-- Name: fiscal_nota_entrada fiscal_nota_entrada_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_nota_entrada
    ADD CONSTRAINT fiscal_nota_entrada_pkey PRIMARY KEY (id);


--
-- Name: fiscal_sped_contribuicoes fiscal_sped_contribuicoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_sped_contribuicoes
    ADD CONSTRAINT fiscal_sped_contribuicoes_pkey PRIMARY KEY (id);


--
-- Name: fiscal_sped_fiscal fiscal_sped_fiscal_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_sped_fiscal
    ADD CONSTRAINT fiscal_sped_fiscal_pkey PRIMARY KEY (id);


--
-- Name: fruteira_etiqueta_balanca fruteira_etiqueta_balanca_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_etiqueta_balanca
    ADD CONSTRAINT fruteira_etiqueta_balanca_codigo_key UNIQUE (codigo);


--
-- Name: fruteira_etiqueta_balanca fruteira_etiqueta_balanca_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_etiqueta_balanca
    ADD CONSTRAINT fruteira_etiqueta_balanca_pkey PRIMARY KEY (id);


--
-- Name: fruteira_perda fruteira_perda_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_perda
    ADD CONSTRAINT fruteira_perda_pkey PRIMARY KEY (id);


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


--
-- Name: fruteira_tabela_preco fruteira_tabela_preco_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_tabela_preco
    ADD CONSTRAINT fruteira_tabela_preco_pkey PRIMARY KEY (id);


--
-- Name: pdv_item_venda pdv_item_venda_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_item_venda
    ADD CONSTRAINT pdv_item_venda_pkey PRIMARY KEY (id);


--
-- Name: pdv_movimento_caixa pdv_movimento_caixa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_movimento_caixa
    ADD CONSTRAINT pdv_movimento_caixa_pkey PRIMARY KEY (id);


--
-- Name: pdv_pagamento pdv_pagamento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_pagamento
    ADD CONSTRAINT pdv_pagamento_pkey PRIMARY KEY (id);


--
-- Name: pdv_sessao_caixa pdv_sessao_caixa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_sessao_caixa
    ADD CONSTRAINT pdv_sessao_caixa_pkey PRIMARY KEY (id);


--
-- Name: pdv_terminal pdv_terminal_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_terminal
    ADD CONSTRAINT pdv_terminal_codigo_key UNIQUE (codigo);


--
-- Name: pdv_terminal pdv_terminal_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_terminal
    ADD CONSTRAINT pdv_terminal_pkey PRIMARY KEY (id);


--
-- Name: pdv_venda pdv_venda_numero_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_venda
    ADD CONSTRAINT pdv_venda_numero_key UNIQUE (numero);


--
-- Name: pdv_venda pdv_venda_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_venda
    ADD CONSTRAINT pdv_venda_pkey PRIMARY KEY (id);


--
-- Name: idx_audit_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_data ON public.common_audit_log USING btree (data_acao);


--
-- Name: idx_audit_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_usuario ON public.common_audit_log USING btree (usuario_id);


--
-- Name: idx_buffet_comanda_cliente; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_comanda_cliente ON public.buffet_comanda USING btree (cliente_nome);


--
-- Name: idx_buffet_comanda_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_comanda_data ON public.buffet_comanda USING btree (data_abertura);


--
-- Name: idx_buffet_comanda_mesa; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_comanda_mesa ON public.buffet_comanda USING btree (mesa_id);


--
-- Name: idx_buffet_comanda_numero; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_comanda_numero ON public.buffet_comanda USING btree (numero);


--
-- Name: idx_buffet_comanda_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_comanda_status ON public.buffet_comanda USING btree (status);


--
-- Name: idx_buffet_item_comanda_comanda; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_item_comanda_comanda ON public.buffet_item_comanda USING btree (comanda_id);


--
-- Name: idx_buffet_item_comanda_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_item_comanda_produto ON public.buffet_item_comanda USING btree (produto_id);


--
-- Name: idx_buffet_item_comanda_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_item_comanda_tipo ON public.buffet_item_comanda USING btree (tipo);


--
-- Name: idx_buffet_mesa_area; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_mesa_area ON public.buffet_mesa USING btree (area);


--
-- Name: idx_buffet_mesa_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_mesa_status ON public.buffet_mesa USING btree (status);


--
-- Name: idx_buffet_produto_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_produto_ativo ON public.buffet_produto USING btree (ativo);


--
-- Name: idx_buffet_produto_categoria; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_produto_categoria ON public.buffet_produto USING btree (categoria);


--
-- Name: idx_buffet_produto_codigo_barras; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_produto_codigo_barras ON public.buffet_produto USING btree (codigo_barras);


--
-- Name: idx_buffet_produto_nome; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_produto_nome ON public.buffet_produto USING btree (nome);


--
-- Name: idx_buffet_receita_ativa; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_receita_ativa ON public.buffet_receita USING btree (ativa);


--
-- Name: idx_buffet_receita_ingrediente_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_receita_ingrediente_produto ON public.buffet_receita_ingrediente USING btree (produto_id);


--
-- Name: idx_buffet_receita_ingrediente_receita; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_receita_ingrediente_receita ON public.buffet_receita_ingrediente USING btree (receita_id);


--
-- Name: idx_buffet_receita_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_receita_produto ON public.buffet_receita USING btree (produto_id);


--
-- Name: idx_buffet_transferencia_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_transferencia_data ON public.buffet_transferencia_estoque USING btree (data);


--
-- Name: idx_buffet_transferencia_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_transferencia_produto ON public.buffet_transferencia_estoque USING btree (produto_id);


--
-- Name: idx_buffet_transferencia_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_buffet_transferencia_status ON public.buffet_transferencia_estoque USING btree (status);


--
-- Name: idx_cliente_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cliente_ativo ON public.cadastro_clientes USING btree (status);


--
-- Name: idx_cliente_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cliente_tipo ON public.cadastro_clientes USING btree (tipo_pessoa);


--
-- Name: idx_estoque_alerta_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_alerta_data ON public.estoque_alerta USING btree (data_alerta);


--
-- Name: idx_estoque_alerta_produto_buffet; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_alerta_produto_buffet ON public.estoque_alerta USING btree (produto_buffet_id);


--
-- Name: idx_estoque_alerta_produto_fruteira; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_alerta_produto_fruteira ON public.estoque_alerta USING btree (produto_fruteira_id);


--
-- Name: idx_estoque_alerta_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_alerta_status ON public.estoque_alerta USING btree (status);


--
-- Name: idx_estoque_alerta_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_alerta_tipo ON public.estoque_alerta USING btree (tipo);


--
-- Name: idx_estoque_inventario_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_inventario_data ON public.estoque_inventario USING btree (data_inicio);


--
-- Name: idx_estoque_inventario_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_inventario_status ON public.estoque_inventario USING btree (status);


--
-- Name: idx_estoque_item_inventario_inventario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_item_inventario_inventario ON public.estoque_item_inventario USING btree (inventario_id);


--
-- Name: idx_estoque_item_inventario_produto_buffet; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_item_inventario_produto_buffet ON public.estoque_item_inventario USING btree (produto_buffet_id);


--
-- Name: idx_estoque_item_inventario_produto_fruteira; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_item_inventario_produto_fruteira ON public.estoque_item_inventario USING btree (produto_fruteira_id);


--
-- Name: idx_estoque_movimento_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_movimento_data ON public.estoque_movimento USING btree (data);


--
-- Name: idx_estoque_movimento_origem_destino; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_movimento_origem_destino ON public.estoque_movimento USING btree (origem, destino);


--
-- Name: idx_estoque_movimento_produto_buffet; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_movimento_produto_buffet ON public.estoque_movimento USING btree (produto_buffet_id);


--
-- Name: idx_estoque_movimento_produto_fruteira; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_movimento_produto_fruteira ON public.estoque_movimento USING btree (produto_fruteira_id);


--
-- Name: idx_estoque_movimento_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_estoque_movimento_tipo ON public.estoque_movimento USING btree (tipo);


--
-- Name: idx_fiscal_ativo_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_ativo_status ON public.fiscal_ativo_imobilizado USING btree (status);


--
-- Name: idx_fiscal_ativo_unidade; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_ativo_unidade ON public.fiscal_ativo_imobilizado USING btree (unidade_negocio);


--
-- Name: idx_fiscal_compra_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_compra_data ON public.fiscal_compra USING btree (data_compra);


--
-- Name: idx_fiscal_compra_fornecedor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_compra_fornecedor ON public.fiscal_compra USING btree (fornecedor_id);


--
-- Name: idx_fiscal_compra_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_compra_status ON public.fiscal_compra USING btree (status);


--
-- Name: idx_fiscal_conciliacao_adquirente; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_conciliacao_adquirente ON public.fiscal_conciliacao_cartao USING btree (adquirente);


--
-- Name: idx_fiscal_conciliacao_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_conciliacao_data ON public.fiscal_conciliacao_cartao USING btree (data_venda);


--
-- Name: idx_fiscal_conciliacao_nsu; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_conciliacao_nsu ON public.fiscal_conciliacao_cartao USING btree (nsu);


--
-- Name: idx_fiscal_conciliacao_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_conciliacao_status ON public.fiscal_conciliacao_cartao USING btree (status);


--
-- Name: idx_fiscal_dre_periodo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_dre_periodo ON public.fiscal_dre USING btree (data_inicio, data_fim);


--
-- Name: idx_fiscal_dre_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_dre_status ON public.fiscal_dre USING btree (status);


--
-- Name: idx_fiscal_dre_unidade; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_dre_unidade ON public.fiscal_dre USING btree (unidade_negocio);


--
-- Name: idx_fiscal_item_compra_compra; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_item_compra_compra ON public.fiscal_item_compra USING btree (compra_id);


--
-- Name: idx_fiscal_item_compra_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_item_compra_produto ON public.fiscal_item_compra USING btree (produto_id);


--
-- Name: idx_fiscal_item_nota_entrada_nota; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_item_nota_entrada_nota ON public.fiscal_item_nota_entrada USING btree (nota_entrada_id);


--
-- Name: idx_fiscal_item_nota_entrada_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_item_nota_entrada_produto ON public.fiscal_item_nota_entrada USING btree (produto_id);


--
-- Name: idx_fiscal_nfe_data_emissao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nfe_data_emissao ON public.fiscal_nfe USING btree (data_emissao);


--
-- Name: idx_fiscal_nfe_numero; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nfe_numero ON public.fiscal_nfe USING btree (numero);


--
-- Name: idx_fiscal_nfe_produtor_rural; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nfe_produtor_rural ON public.fiscal_nfe USING btree (eh_produtor_rural);


--
-- Name: idx_fiscal_nfe_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nfe_status ON public.fiscal_nfe USING btree (status);


--
-- Name: idx_fiscal_nfe_venda; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nfe_venda ON public.fiscal_nfe USING btree (venda_id);


--
-- Name: idx_fiscal_nota_entrada_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nota_entrada_data ON public.fiscal_nota_entrada USING btree (data_emissao);


--
-- Name: idx_fiscal_nota_entrada_fornecedor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nota_entrada_fornecedor ON public.fiscal_nota_entrada USING btree (fornecedor_documento);


--
-- Name: idx_fiscal_nota_entrada_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_nota_entrada_status ON public.fiscal_nota_entrada USING btree (status);


--
-- Name: idx_fiscal_sped_contrib_periodo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_sped_contrib_periodo ON public.fiscal_sped_contribuicoes USING btree (data_inicio, data_fim);


--
-- Name: idx_fiscal_sped_contrib_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_sped_contrib_status ON public.fiscal_sped_contribuicoes USING btree (status);


--
-- Name: idx_fiscal_sped_periodo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_sped_periodo ON public.fiscal_sped_fiscal USING btree (data_inicio, data_fim);


--
-- Name: idx_fiscal_sped_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fiscal_sped_status ON public.fiscal_sped_fiscal USING btree (status);


--
-- Name: idx_fornecedor_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fornecedor_ativo ON public.cadastro_fornecedores USING btree (status);


--
-- Name: idx_fornecedor_cnpj; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fornecedor_cnpj ON public.cadastro_fornecedores USING btree (cnpj_cpf);


--
-- Name: idx_fruteira_etiqueta_ativa; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_etiqueta_ativa ON public.fruteira_etiqueta_balanca USING btree (ativa);


--
-- Name: idx_fruteira_etiqueta_codigo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_etiqueta_codigo ON public.fruteira_etiqueta_balanca USING btree (codigo);


--
-- Name: idx_fruteira_etiqueta_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_etiqueta_produto ON public.fruteira_etiqueta_balanca USING btree (produto_id);


--
-- Name: idx_fruteira_perda_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_perda_data ON public.fruteira_perda USING btree (data);


--
-- Name: idx_fruteira_perda_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_perda_produto ON public.fruteira_perda USING btree (produto_id);


--
-- Name: idx_fruteira_perda_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_perda_tipo ON public.fruteira_perda USING btree (tipo);


--
-- Name: idx_fruteira_produto_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_produto_ativo ON public.fruteira_produto USING btree (ativo);


--
-- Name: idx_fruteira_produto_codigo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_produto_codigo ON public.fruteira_produto USING btree (codigo);


--
-- Name: idx_fruteira_produto_codigo_barras; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_produto_codigo_barras ON public.fruteira_produto USING btree (codigo_barras);


--
-- Name: idx_fruteira_produto_nome; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_produto_nome ON public.fruteira_produto USING btree (nome);


--
-- Name: idx_fruteira_produto_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_produto_tipo ON public.fruteira_produto USING btree (tipo);


--
-- Name: idx_fruteira_tabela_preco_ativa; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_tabela_preco_ativa ON public.fruteira_tabela_preco USING btree (ativa);


--
-- Name: idx_fruteira_tabela_preco_produto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_tabela_preco_produto ON public.fruteira_tabela_preco USING btree (produto_id);


--
-- Name: idx_fruteira_tabela_preco_vigencia; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fruteira_tabela_preco_vigencia ON public.fruteira_tabela_preco USING btree (data_inicio, data_fim);


--
-- Name: idx_pdv_item_venda_origem; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_item_venda_origem ON public.pdv_item_venda USING btree (origem);


--
-- Name: idx_pdv_item_venda_produto_buffet; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_item_venda_produto_buffet ON public.pdv_item_venda USING btree (produto_buffet_id);


--
-- Name: idx_pdv_item_venda_produto_fruteira; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_item_venda_produto_fruteira ON public.pdv_item_venda USING btree (produto_fruteira_id);


--
-- Name: idx_pdv_item_venda_venda; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_item_venda_venda ON public.pdv_item_venda USING btree (venda_id);


--
-- Name: idx_pdv_movimento_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_movimento_data ON public.pdv_movimento_caixa USING btree (data);


--
-- Name: idx_pdv_movimento_sessao; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_movimento_sessao ON public.pdv_movimento_caixa USING btree (sessao_caixa_id);


--
-- Name: idx_pdv_movimento_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_movimento_tipo ON public.pdv_movimento_caixa USING btree (tipo);


--
-- Name: idx_pdv_pagamento_forma; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_pagamento_forma ON public.pdv_pagamento USING btree (forma_pagamento);


--
-- Name: idx_pdv_pagamento_nsu; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_pagamento_nsu ON public.pdv_pagamento USING btree (nsu);


--
-- Name: idx_pdv_pagamento_venda; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_pagamento_venda ON public.pdv_pagamento USING btree (venda_id);


--
-- Name: idx_pdv_sessao_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_sessao_data ON public.pdv_sessao_caixa USING btree (data_abertura);


--
-- Name: idx_pdv_sessao_pdv; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_sessao_pdv ON public.pdv_sessao_caixa USING btree (pdv_id);


--
-- Name: idx_pdv_sessao_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_sessao_status ON public.pdv_sessao_caixa USING btree (status);


--
-- Name: idx_pdv_terminal_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_terminal_ativo ON public.pdv_terminal USING btree (ativo);


--
-- Name: idx_pdv_venda_cliente; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_venda_cliente ON public.pdv_venda USING btree (cliente_documento);


--
-- Name: idx_pdv_venda_comanda; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_venda_comanda ON public.pdv_venda USING btree (comanda_id);


--
-- Name: idx_pdv_venda_data; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_venda_data ON public.pdv_venda USING btree (data);


--
-- Name: idx_pdv_venda_pdv; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_venda_pdv ON public.pdv_venda USING btree (pdv_id);


--
-- Name: idx_pdv_venda_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pdv_venda_status ON public.pdv_venda USING btree (status);


--
-- Name: idx_usuario_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_usuario_ativo ON public.common_usuario USING btree (ativo);


--
-- Name: estoque_item_inventario fk2hmh1s2vfws4ke4avisn1lx4i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_item_inventario
    ADD CONSTRAINT fk2hmh1s2vfws4ke4avisn1lx4i FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);


--
-- Name: pdv_pagamento fk383xt96cfqnatg7a30uq4gvvd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_pagamento
    ADD CONSTRAINT fk383xt96cfqnatg7a30uq4gvvd FOREIGN KEY (venda_id) REFERENCES public.pdv_venda(id);


--
-- Name: buffet_receita_ingrediente fk3nq4r0hrxtx9b78dgxhdfn0wa; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_receita_ingrediente
    ADD CONSTRAINT fk3nq4r0hrxtx9b78dgxhdfn0wa FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: buffet_receita_ingrediente fk45c275bapcfxflcsfyopca7rb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_receita_ingrediente
    ADD CONSTRAINT fk45c275bapcfxflcsfyopca7rb FOREIGN KEY (receita_id) REFERENCES public.buffet_receita(id);


--
-- Name: estoque_movimento fk4e5vymc38nepgk76s8orijbu5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_movimento
    ADD CONSTRAINT fk4e5vymc38nepgk76s8orijbu5 FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);


--
-- Name: pdv_venda fk4pfgjytufx4h1icu15hnv8tk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_venda
    ADD CONSTRAINT fk4pfgjytufx4h1icu15hnv8tk FOREIGN KEY (comanda_id) REFERENCES public.buffet_comanda(id);


--
-- Name: pdv_sessao_caixa fk4u70pfm409tisvav88aajae51; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_sessao_caixa
    ADD CONSTRAINT fk4u70pfm409tisvav88aajae51 FOREIGN KEY (pdv_id) REFERENCES public.pdv_terminal(id);


--
-- Name: estoque_item_inventario fk6g1m3e7strhm5g2bj954ix2yy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_item_inventario
    ADD CONSTRAINT fk6g1m3e7strhm5g2bj954ix2yy FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);


--
-- Name: pdv_item_venda fk6l82eh6rsn15i8s7yo17wvwck; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_item_venda
    ADD CONSTRAINT fk6l82eh6rsn15i8s7yo17wvwck FOREIGN KEY (venda_id) REFERENCES public.pdv_venda(id);


--
-- Name: estoque_item_inventario fk6x0147j8p81tdno8p2gq17mmn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_item_inventario
    ADD CONSTRAINT fk6x0147j8p81tdno8p2gq17mmn FOREIGN KEY (inventario_id) REFERENCES public.estoque_inventario(id);


--
-- Name: fiscal_compra fk75io9wyqukk2hfga4r7cw55ga; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_compra
    ADD CONSTRAINT fk75io9wyqukk2hfga4r7cw55ga FOREIGN KEY (fornecedor_id) REFERENCES public.cadastro_fornecedores(id);


--
-- Name: buffet_receita fk7ais7qmr6su6q5pmbk6wus6ve; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_receita
    ADD CONSTRAINT fk7ais7qmr6su6q5pmbk6wus6ve FOREIGN KEY (produto_id) REFERENCES public.buffet_produto(id);


--
-- Name: fruteira_etiqueta_balanca fk7bnm6m8hf971e4vlg44s2jfp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_etiqueta_balanca
    ADD CONSTRAINT fk7bnm6m8hf971e4vlg44s2jfp FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: pdv_movimento_caixa fk7etwbw26ik6cbrngfqh8y752g; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_movimento_caixa
    ADD CONSTRAINT fk7etwbw26ik6cbrngfqh8y752g FOREIGN KEY (sessao_caixa_id) REFERENCES public.pdv_sessao_caixa(id);


--
-- Name: fruteira_perda fk8ssavhqbl11jh94l1dc2pakbp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_perda
    ADD CONSTRAINT fk8ssavhqbl11jh94l1dc2pakbp FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: buffet_item_comanda fk9hc24ore64oxdu4vx6wk4xsgb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_item_comanda
    ADD CONSTRAINT fk9hc24ore64oxdu4vx6wk4xsgb FOREIGN KEY (comanda_id) REFERENCES public.buffet_comanda(id);


--
-- Name: estoque_movimento fka4igflcsyejicobwsfotr895t; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_movimento
    ADD CONSTRAINT fka4igflcsyejicobwsfotr895t FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);


--
-- Name: pdv_item_venda fka6b7sco3hslscyb2sk2la01ne; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_item_venda
    ADD CONSTRAINT fka6b7sco3hslscyb2sk2la01ne FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);


--
-- Name: fiscal_item_compra fkbf7qnaffd147gwg2y8cxayqwt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_compra
    ADD CONSTRAINT fkbf7qnaffd147gwg2y8cxayqwt FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);


--
-- Name: fiscal_item_nota_entrada fkc0veedeid5r04452cxakus2ih; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_nota_entrada
    ADD CONSTRAINT fkc0veedeid5r04452cxakus2ih FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: fiscal_item_compra fkchr6f8hb6872mid92rx6lo7bw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_compra
    ADD CONSTRAINT fkchr6f8hb6872mid92rx6lo7bw FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: common_usuario_perfil fkgs364n2ttg1fs43pvjoruhcjm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.common_usuario_perfil
    ADD CONSTRAINT fkgs364n2ttg1fs43pvjoruhcjm FOREIGN KEY (usuario_id) REFERENCES public.common_usuario(id);


--
-- Name: fruteira_tabela_preco fkjc9apey1o4ocmxgvwnt0kuv3p; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fruteira_tabela_preco
    ADD CONSTRAINT fkjc9apey1o4ocmxgvwnt0kuv3p FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: fiscal_item_compra fknws6y908o3pxl0oq18r3fn00o; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_compra
    ADD CONSTRAINT fknws6y908o3pxl0oq18r3fn00o FOREIGN KEY (compra_id) REFERENCES public.fiscal_compra(id);


--
-- Name: estoque_alerta fkogo964dgi4la67twf6j8pvmua; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_alerta
    ADD CONSTRAINT fkogo964dgi4la67twf6j8pvmua FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);


--
-- Name: buffet_transferencia_estoque fkp0speotfcwdnc4vty28w36stf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_transferencia_estoque
    ADD CONSTRAINT fkp0speotfcwdnc4vty28w36stf FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);


--
-- Name: fiscal_item_nota_entrada fkpfey1x1rq9y46mblcwkld05cc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiscal_item_nota_entrada
    ADD CONSTRAINT fkpfey1x1rq9y46mblcwkld05cc FOREIGN KEY (nota_entrada_id) REFERENCES public.fiscal_nota_entrada(id);


--
-- Name: estoque_alerta fkpl1pwlotob7114pdnulbocpi5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estoque_alerta
    ADD CONSTRAINT fkpl1pwlotob7114pdnulbocpi5 FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);


--
-- Name: pdv_venda fkqyyykciw4p08efebv729h5u4q; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_venda
    ADD CONSTRAINT fkqyyykciw4p08efebv729h5u4q FOREIGN KEY (nfce_id) REFERENCES public.fiscal_nfe(id);


--
-- Name: pdv_item_venda fktk3h3m7xj9npnxe3ssmqm215y; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdv_item_venda
    ADD CONSTRAINT fktk3h3m7xj9npnxe3ssmqm215y FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);


--
-- Name: buffet_comanda fkwjrjh79rau142sy9qb270ern; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.buffet_comanda
    ADD CONSTRAINT fkwjrjh79rau142sy9qb270ern FOREIGN KEY (mesa_id) REFERENCES public.buffet_mesa(id);


--
-- Name: pdv_caderno_divida; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE IF NOT EXISTS public.pdv_caderno_divida (
    id uuid NOT NULL,
    cliente_id uuid,
    cliente_nome character varying(200) NOT NULL,
    cliente_documento character varying(20),
    cliente_telefone character varying(20),
    venda_id uuid,
    numero_venda character varying(30),
    descricao character varying(500),
    valor_total numeric(15,2) NOT NULL DEFAULT 0,
    valor_pago numeric(15,2) NOT NULL DEFAULT 0,
    status character varying(20) NOT NULL DEFAULT 'ABERTA',
    data_lancamento timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_vencimento date,
    data_quitacao timestamp(6) without time zone,
    operador character varying(100),
    pdv_id uuid,
    observacoes character varying(1000),
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version bigint,
    deleted boolean NOT NULL DEFAULT false,
    CONSTRAINT pdv_caderno_divida_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_caderno_cliente ON public.pdv_caderno_divida (cliente_id);
CREATE INDEX IF NOT EXISTS idx_caderno_status ON public.pdv_caderno_divida (status);
CREATE INDEX IF NOT EXISTS idx_caderno_documento ON public.pdv_caderno_divida (cliente_documento);
CREATE INDEX IF NOT EXISTS idx_caderno_data ON public.pdv_caderno_divida (data_lancamento);

ALTER TABLE ONLY public.pdv_caderno_divida
    ADD CONSTRAINT pdv_caderno_divida_cliente_fk FOREIGN KEY (cliente_id) REFERENCES public.cadastro_clientes(id);


--
-- PostgreSQL database dump complete
--


--
-- Name: buffet_cliente; Type: TABLE
--
CREATE TABLE public.buffet_cliente (
    deleted boolean NOT NULL DEFAULT false,
    total_visitas integer NOT NULL DEFAULT 0,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    total_consumido numeric(15,4) NOT NULL DEFAULT 0,
    primeira_visita timestamp(6) without time zone,
    ultima_visita timestamp(6) without time zone,
    cpf character varying(14) NOT NULL,
    nome character varying(200) NOT NULL,
    telefone character varying(20),
    observacoes character varying(500),
    CONSTRAINT buffet_cliente_pkey PRIMARY KEY (id),
    CONSTRAINT buffet_cliente_cpf_key UNIQUE (cpf)
);

CREATE INDEX idx_buffet_cliente_cpf ON public.buffet_cliente (cpf);
CREATE INDEX idx_buffet_cliente_nome ON public.buffet_cliente (nome);

--
-- Name: buffet_cliente_visita; Type: TABLE
--
CREATE TABLE public.buffet_cliente_visita (
    deleted boolean NOT NULL DEFAULT false,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    version bigint,
    id uuid NOT NULL,
    cliente_id uuid NOT NULL,
    comanda_id uuid,
    data_visita timestamp(6) without time zone NOT NULL,
    valor_consumido numeric(15,4) NOT NULL DEFAULT 0,
    CONSTRAINT buffet_cliente_visita_pkey PRIMARY KEY (id),
    CONSTRAINT buffet_cliente_visita_cliente_fk FOREIGN KEY (cliente_id) REFERENCES public.buffet_cliente(id),
    CONSTRAINT buffet_cliente_visita_comanda_fk FOREIGN KEY (comanda_id) REFERENCES public.buffet_comanda(id)
);

CREATE INDEX idx_buffet_visita_cliente ON public.buffet_cliente_visita (cliente_id);
CREATE INDEX idx_buffet_visita_data ON public.buffet_cliente_visita (data_visita);
