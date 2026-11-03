-- V8__ensure_complete_schema.sql
-- Garante o schema completo do Sistema Tenda de forma IDEMPOTENTE.
--
-- Pode ser executada em:
--   * banco vazio (instalacao nova);
--   * banco criado pelo dump antigo docker/initdb/01-sistematenda.sql;
--   * banco parcialmente criado por versoes anteriores das migrations.
--
-- Todas as tabelas, colunas, indices e constraints usam IF NOT EXISTS /
-- verificacao em pg_constraint, portanto a execucao e segura em qualquer estado.
--
-- Esta migration NAO cria extensoes: "uuid-ossp" exige superusuario e nao e
-- usada em nenhum ponto deste script (o PostgreSQL 13+ ja possui
-- gen_random_uuid() nativo). Assim a V8 funciona com um usuario comum, owner
-- do banco, sem privilegios administrativos.

-- Tabela public.buffet_comanda
CREATE TABLE IF NOT EXISTS public.buffet_comanda (
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

-- Tabela public.buffet_item_comanda
CREATE TABLE IF NOT EXISTS public.buffet_item_comanda (
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

-- Tabela public.buffet_mesa
CREATE TABLE IF NOT EXISTS public.buffet_mesa (
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

-- Tabela public.buffet_produto
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
    CONSTRAINT buffet_produto_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['PRATO_QUENTE'::character varying, 'PRATO_FRIO'::character varying, 'SALADA'::character varying, 'GUARNICAO'::character varying, 'SOBREMESA'::character varying, 'BEBIDA'::character varying, 'BEBIDA_ALCOOLICA'::character varying, 'ADICIONAL'::character varying, 'PRATO_FEITO'::character varying, 'EXECUTIVO'::character varying, 'OUTROS'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_cofins_check CHECK (((cst_cofins)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_icms_check CHECK (((cst_icms)::text = ANY ((ARRAY['CST_00'::character varying, 'CST_10'::character varying, 'CST_20'::character varying, 'CST_30'::character varying, 'CST_40'::character varying, 'CST_41'::character varying, 'CST_50'::character varying, 'CST_51'::character varying, 'CST_60'::character varying, 'CST_70'::character varying, 'CST_90'::character varying, 'CSOSN_101'::character varying, 'CSOSN_102'::character varying, 'CSOSN_103'::character varying, 'CSOSN_201'::character varying, 'CSOSN_202'::character varying, 'CSOSN_203'::character varying, 'CSOSN_300'::character varying, 'CSOSN_400'::character varying, 'CSOSN_500'::character varying, 'CSOSN_900'::character varying])::text[]))),
    CONSTRAINT buffet_produto_cst_pis_check CHECK (((cst_pis)::text = ANY ((ARRAY['PIS_01'::character varying, 'PIS_02'::character varying, 'PIS_03'::character varying, 'PIS_04'::character varying, 'PIS_05'::character varying, 'PIS_06'::character varying, 'PIS_07'::character varying, 'PIS_08'::character varying, 'PIS_09'::character varying, 'PIS_49'::character varying, 'PIS_50'::character varying, 'PIS_51'::character varying, 'PIS_52'::character varying, 'PIS_53'::character varying, 'PIS_54'::character varying, 'PIS_55'::character varying, 'PIS_56'::character varying, 'PIS_60'::character varying, 'PIS_61'::character varying, 'PIS_62'::character varying, 'PIS_63'::character varying, 'PIS_64'::character varying, 'PIS_65'::character varying, 'PIS_66'::character varying, 'PIS_67'::character varying, 'PIS_70'::character varying, 'PIS_71'::character varying, 'PIS_72'::character varying, 'PIS_73'::character varying, 'PIS_74'::character varying, 'PIS_75'::character varying, 'PIS_98'::character varying, 'PIS_99'::character varying, 'COFINS_01'::character varying, 'COFINS_02'::character varying, 'COFINS_03'::character varying, 'COFINS_04'::character varying, 'COFINS_05'::character varying, 'COFINS_06'::character varying, 'COFINS_07'::character varying, 'COFINS_08'::character varying, 'COFINS_09'::character varying, 'COFINS_49'::character varying, 'COFINS_50'::character varying, 'COFINS_51'::character varying, 'COFINS_52'::character varying, 'COFINS_53'::character varying, 'COFINS_54'::character varying, 'COFINS_55'::character varying, 'COFINS_56'::character varying, 'COFINS_60'::character varying, 'COFINS_61'::character varying, 'COFINS_62'::character varying, 'COFINS_63'::character varying, 'COFINS_64'::character varying, 'COFINS_65'::character varying, 'COFINS_66'::character varying, 'COFINS_67'::character varying, 'COFINS_70'::character varying, 'COFINS_71'::character varying, 'COFINS_72'::character varying, 'COFINS_73'::character varying, 'COFINS_74'::character varying, 'COFINS_75'::character varying, 'COFINS_98'::character varying, 'COFINS_99'::character varying])::text[]))),
    CONSTRAINT buffet_produto_unidade_medida_check CHECK (((unidade_medida)::text = ANY ((ARRAY['KG'::character varying, 'UN'::character varying, 'L'::character varying, 'ML'::character varying, 'G'::character varying, 'PC'::character varying, 'CX'::character varying, 'FD'::character varying, 'KT'::character varying, 'MT'::character varying, 'M2'::character varying, 'M3'::character varying])::text[])))
);

-- Tabela public.buffet_receita
CREATE TABLE IF NOT EXISTS public.buffet_receita (
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

-- Tabela public.buffet_receita_ingrediente
CREATE TABLE IF NOT EXISTS public.buffet_receita_ingrediente (
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

-- Tabela public.buffet_transferencia_estoque
CREATE TABLE IF NOT EXISTS public.buffet_transferencia_estoque (
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

-- Tabela public.cadastro_clientes
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
    CONSTRAINT cadastro_clientes_tipo_pessoa_check CHECK (((tipo_pessoa)::text = ANY ((ARRAY['FISICA'::character varying, 'JURIDICA'::character varying])::text[])))
);

-- Tabela public.cadastro_fornecedores
CREATE TABLE IF NOT EXISTS public.cadastro_fornecedores (
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

-- Tabela public.common_audit_log
CREATE TABLE IF NOT EXISTS public.common_audit_log (
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

-- Tabela public.common_usuario
CREATE TABLE IF NOT EXISTS public.common_usuario (
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

-- Tabela public.common_usuario_perfil
CREATE TABLE IF NOT EXISTS public.common_usuario_perfil (
    usuario_id uuid NOT NULL,
    perfil character varying(50)
);

-- Tabela public.estoque_alerta
CREATE TABLE IF NOT EXISTS public.estoque_alerta (
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

-- Tabela public.estoque_inventario
CREATE TABLE IF NOT EXISTS public.estoque_inventario (
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

-- Tabela public.estoque_item_inventario
CREATE TABLE IF NOT EXISTS public.estoque_item_inventario (
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

-- Tabela public.estoque_movimento
CREATE TABLE IF NOT EXISTS public.estoque_movimento (
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

-- Tabela public.fiscal_ativo_imobilizado
CREATE TABLE IF NOT EXISTS public.fiscal_ativo_imobilizado (
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

-- Tabela public.fiscal_compra
CREATE TABLE IF NOT EXISTS public.fiscal_compra (
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

-- Tabela public.fiscal_conciliacao_cartao
CREATE TABLE IF NOT EXISTS public.fiscal_conciliacao_cartao (
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

-- Tabela public.fiscal_dre
CREATE TABLE IF NOT EXISTS public.fiscal_dre (
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

-- Tabela public.fiscal_item_compra
CREATE TABLE IF NOT EXISTS public.fiscal_item_compra (
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

-- Tabela public.fiscal_item_nota_entrada
CREATE TABLE IF NOT EXISTS public.fiscal_item_nota_entrada (
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

-- Tabela public.fiscal_nfe
CREATE TABLE IF NOT EXISTS public.fiscal_nfe (
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

-- Tabela public.fiscal_nota_entrada
CREATE TABLE IF NOT EXISTS public.fiscal_nota_entrada (
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

-- Tabela public.fiscal_sped_contribuicoes
CREATE TABLE IF NOT EXISTS public.fiscal_sped_contribuicoes (
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

-- Tabela public.fiscal_sped_fiscal
CREATE TABLE IF NOT EXISTS public.fiscal_sped_fiscal (
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

-- Tabela public.fruteira_etiqueta_balanca
CREATE TABLE IF NOT EXISTS public.fruteira_etiqueta_balanca (
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

-- Tabela public.fruteira_perda
CREATE TABLE IF NOT EXISTS public.fruteira_perda (
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

-- Tabela public.fruteira_produto
CREATE TABLE IF NOT EXISTS public.fruteira_produto (
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

-- Tabela public.fruteira_tabela_preco
CREATE TABLE IF NOT EXISTS public.fruteira_tabela_preco (
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

-- Tabela public.pdv_item_venda
CREATE TABLE IF NOT EXISTS public.pdv_item_venda (
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

-- Tabela public.pdv_movimento_caixa
CREATE TABLE IF NOT EXISTS public.pdv_movimento_caixa (
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

-- Tabela public.pdv_pagamento
CREATE TABLE IF NOT EXISTS public.pdv_pagamento (
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

-- Tabela public.pdv_sessao_caixa
CREATE TABLE IF NOT EXISTS public.pdv_sessao_caixa (
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

-- Tabela public.pdv_terminal
CREATE TABLE IF NOT EXISTS public.pdv_terminal (
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

-- Tabela public.pdv_venda
CREATE TABLE IF NOT EXISTS public.pdv_venda (
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
    nfce_erro character varying(500),
    cupom_impressao text,
    tentativas_impressao integer NOT NULL DEFAULT 0,
    status_ultima_impressao character varying(30),
    observacoes_impressao character varying(500),
    data_ultima_impressao timestamp(6) without time zone,
    CONSTRAINT pdv_venda_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'EM_ANDAMENTO'::character varying, 'FINALIZADA'::character varying, 'PAGA'::character varying, 'CANCELADA'::character varying, 'DEVOLVIDA'::character varying, 'PENDENTE_PAGAMENTO'::character varying])::text[])))
);

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_comanda_numero_key'
          AND conrelid = 'public.buffet_comanda'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_comanda
            ADD CONSTRAINT buffet_comanda_numero_key UNIQUE (numero);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_comanda_pkey'
          AND conrelid = 'public.buffet_comanda'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_comanda
            ADD CONSTRAINT buffet_comanda_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_item_comanda_pkey'
          AND conrelid = 'public.buffet_item_comanda'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_item_comanda
            ADD CONSTRAINT buffet_item_comanda_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_mesa_numero_key'
          AND conrelid = 'public.buffet_mesa'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_mesa
            ADD CONSTRAINT buffet_mesa_numero_key UNIQUE (numero);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_mesa_pkey'
          AND conrelid = 'public.buffet_mesa'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_mesa
            ADD CONSTRAINT buffet_mesa_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_produto_codigo_barras_key'
          AND conrelid = 'public.buffet_produto'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_produto
            ADD CONSTRAINT buffet_produto_codigo_barras_key UNIQUE (codigo_barras);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_produto_codigo_key'
          AND conrelid = 'public.buffet_produto'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_produto
            ADD CONSTRAINT buffet_produto_codigo_key UNIQUE (codigo);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_produto_pkey'
          AND conrelid = 'public.buffet_produto'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_produto
            ADD CONSTRAINT buffet_produto_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_receita_ingrediente_pkey'
          AND conrelid = 'public.buffet_receita_ingrediente'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_receita_ingrediente
            ADD CONSTRAINT buffet_receita_ingrediente_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_receita_pkey'
          AND conrelid = 'public.buffet_receita'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_receita
            ADD CONSTRAINT buffet_receita_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_receita_produto_id_key'
          AND conrelid = 'public.buffet_receita'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_receita
            ADD CONSTRAINT buffet_receita_produto_id_key UNIQUE (produto_id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'buffet_transferencia_estoque_pkey'
          AND conrelid = 'public.buffet_transferencia_estoque'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_transferencia_estoque
            ADD CONSTRAINT buffet_transferencia_estoque_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'cadastro_clientes_email_key'
          AND conrelid = 'public.cadastro_clientes'::regclass
    ) THEN
        ALTER TABLE ONLY public.cadastro_clientes
            ADD CONSTRAINT cadastro_clientes_email_key UNIQUE (email);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'cadastro_clientes_pkey'
          AND conrelid = 'public.cadastro_clientes'::regclass
    ) THEN
        ALTER TABLE ONLY public.cadastro_clientes
            ADD CONSTRAINT cadastro_clientes_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'cadastro_fornecedores_email_key'
          AND conrelid = 'public.cadastro_fornecedores'::regclass
    ) THEN
        ALTER TABLE ONLY public.cadastro_fornecedores
            ADD CONSTRAINT cadastro_fornecedores_email_key UNIQUE (email);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'cadastro_fornecedores_pkey'
          AND conrelid = 'public.cadastro_fornecedores'::regclass
    ) THEN
        ALTER TABLE ONLY public.cadastro_fornecedores
            ADD CONSTRAINT cadastro_fornecedores_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'common_audit_log_pkey'
          AND conrelid = 'public.common_audit_log'::regclass
    ) THEN
        ALTER TABLE ONLY public.common_audit_log
            ADD CONSTRAINT common_audit_log_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'common_usuario_email_key'
          AND conrelid = 'public.common_usuario'::regclass
    ) THEN
        ALTER TABLE ONLY public.common_usuario
            ADD CONSTRAINT common_usuario_email_key UNIQUE (email);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'common_usuario_pkey'
          AND conrelid = 'public.common_usuario'::regclass
    ) THEN
        ALTER TABLE ONLY public.common_usuario
            ADD CONSTRAINT common_usuario_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'estoque_alerta_pkey'
          AND conrelid = 'public.estoque_alerta'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_alerta
            ADD CONSTRAINT estoque_alerta_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'estoque_inventario_pkey'
          AND conrelid = 'public.estoque_inventario'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_inventario
            ADD CONSTRAINT estoque_inventario_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'estoque_item_inventario_pkey'
          AND conrelid = 'public.estoque_item_inventario'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_item_inventario
            ADD CONSTRAINT estoque_item_inventario_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'estoque_movimento_pkey'
          AND conrelid = 'public.estoque_movimento'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_movimento
            ADD CONSTRAINT estoque_movimento_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_ativo_imobilizado_codigo_key'
          AND conrelid = 'public.fiscal_ativo_imobilizado'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_ativo_imobilizado
            ADD CONSTRAINT fiscal_ativo_imobilizado_codigo_key UNIQUE (codigo);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_ativo_imobilizado_pkey'
          AND conrelid = 'public.fiscal_ativo_imobilizado'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_ativo_imobilizado
            ADD CONSTRAINT fiscal_ativo_imobilizado_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_compra_pkey'
          AND conrelid = 'public.fiscal_compra'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_compra
            ADD CONSTRAINT fiscal_compra_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_conciliacao_cartao_pkey'
          AND conrelid = 'public.fiscal_conciliacao_cartao'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_conciliacao_cartao
            ADD CONSTRAINT fiscal_conciliacao_cartao_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_dre_pkey'
          AND conrelid = 'public.fiscal_dre'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_dre
            ADD CONSTRAINT fiscal_dre_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_item_compra_pkey'
          AND conrelid = 'public.fiscal_item_compra'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_compra
            ADD CONSTRAINT fiscal_item_compra_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_item_nota_entrada_pkey'
          AND conrelid = 'public.fiscal_item_nota_entrada'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_nota_entrada
            ADD CONSTRAINT fiscal_item_nota_entrada_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_nfe_chave_acesso_key'
          AND conrelid = 'public.fiscal_nfe'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_nfe
            ADD CONSTRAINT fiscal_nfe_chave_acesso_key UNIQUE (chave_acesso);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_nfe_pkey'
          AND conrelid = 'public.fiscal_nfe'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_nfe
            ADD CONSTRAINT fiscal_nfe_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_nota_entrada_chave_acesso_key'
          AND conrelid = 'public.fiscal_nota_entrada'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_nota_entrada
            ADD CONSTRAINT fiscal_nota_entrada_chave_acesso_key UNIQUE (chave_acesso);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_nota_entrada_pkey'
          AND conrelid = 'public.fiscal_nota_entrada'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_nota_entrada
            ADD CONSTRAINT fiscal_nota_entrada_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_sped_contribuicoes_pkey'
          AND conrelid = 'public.fiscal_sped_contribuicoes'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_sped_contribuicoes
            ADD CONSTRAINT fiscal_sped_contribuicoes_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fiscal_sped_fiscal_pkey'
          AND conrelid = 'public.fiscal_sped_fiscal'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_sped_fiscal
            ADD CONSTRAINT fiscal_sped_fiscal_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_etiqueta_balanca_codigo_key'
          AND conrelid = 'public.fruteira_etiqueta_balanca'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_etiqueta_balanca
            ADD CONSTRAINT fruteira_etiqueta_balanca_codigo_key UNIQUE (codigo);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_etiqueta_balanca_pkey'
          AND conrelid = 'public.fruteira_etiqueta_balanca'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_etiqueta_balanca
            ADD CONSTRAINT fruteira_etiqueta_balanca_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_perda_pkey'
          AND conrelid = 'public.fruteira_perda'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_perda
            ADD CONSTRAINT fruteira_perda_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_produto_codigo_barras_key'
          AND conrelid = 'public.fruteira_produto'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_produto
            ADD CONSTRAINT fruteira_produto_codigo_barras_key UNIQUE (codigo_barras);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_produto_codigo_key'
          AND conrelid = 'public.fruteira_produto'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_produto
            ADD CONSTRAINT fruteira_produto_codigo_key UNIQUE (codigo);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_produto_pkey'
          AND conrelid = 'public.fruteira_produto'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_produto
            ADD CONSTRAINT fruteira_produto_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fruteira_tabela_preco_pkey'
          AND conrelid = 'public.fruteira_tabela_preco'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_tabela_preco
            ADD CONSTRAINT fruteira_tabela_preco_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_item_venda_pkey'
          AND conrelid = 'public.pdv_item_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_item_venda
            ADD CONSTRAINT pdv_item_venda_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_movimento_caixa_pkey'
          AND conrelid = 'public.pdv_movimento_caixa'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_movimento_caixa
            ADD CONSTRAINT pdv_movimento_caixa_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_pagamento_pkey'
          AND conrelid = 'public.pdv_pagamento'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_pagamento
            ADD CONSTRAINT pdv_pagamento_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_sessao_caixa_pkey'
          AND conrelid = 'public.pdv_sessao_caixa'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_sessao_caixa
            ADD CONSTRAINT pdv_sessao_caixa_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_terminal_codigo_key'
          AND conrelid = 'public.pdv_terminal'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_terminal
            ADD CONSTRAINT pdv_terminal_codigo_key UNIQUE (codigo);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_terminal_pkey'
          AND conrelid = 'public.pdv_terminal'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_terminal
            ADD CONSTRAINT pdv_terminal_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_venda_numero_key'
          AND conrelid = 'public.pdv_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_venda
            ADD CONSTRAINT pdv_venda_numero_key UNIQUE (numero);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_venda_pkey'
          AND conrelid = 'public.pdv_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_venda
            ADD CONSTRAINT pdv_venda_pkey PRIMARY KEY (id);
    END IF;
END
$do$;

CREATE INDEX IF NOT EXISTS idx_audit_data ON public.common_audit_log USING btree (data_acao);
CREATE INDEX IF NOT EXISTS idx_audit_usuario ON public.common_audit_log USING btree (usuario_id);
CREATE INDEX IF NOT EXISTS idx_buffet_comanda_cliente ON public.buffet_comanda USING btree (cliente_nome);
CREATE INDEX IF NOT EXISTS idx_buffet_comanda_data ON public.buffet_comanda USING btree (data_abertura);
CREATE INDEX IF NOT EXISTS idx_buffet_comanda_mesa ON public.buffet_comanda USING btree (mesa_id);
CREATE INDEX IF NOT EXISTS idx_buffet_comanda_numero ON public.buffet_comanda USING btree (numero);
CREATE INDEX IF NOT EXISTS idx_buffet_comanda_status ON public.buffet_comanda USING btree (status);
CREATE INDEX IF NOT EXISTS idx_buffet_item_comanda_comanda ON public.buffet_item_comanda USING btree (comanda_id);
CREATE INDEX IF NOT EXISTS idx_buffet_item_comanda_produto ON public.buffet_item_comanda USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_buffet_item_comanda_tipo ON public.buffet_item_comanda USING btree (tipo);
CREATE INDEX IF NOT EXISTS idx_buffet_mesa_area ON public.buffet_mesa USING btree (area);
CREATE INDEX IF NOT EXISTS idx_buffet_mesa_status ON public.buffet_mesa USING btree (status);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_ativo ON public.buffet_produto USING btree (ativo);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_categoria ON public.buffet_produto USING btree (categoria);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_codigo_barras ON public.buffet_produto USING btree (codigo_barras);
CREATE INDEX IF NOT EXISTS idx_buffet_produto_nome ON public.buffet_produto USING btree (nome);
CREATE INDEX IF NOT EXISTS idx_buffet_receita_ativa ON public.buffet_receita USING btree (ativa);
CREATE INDEX IF NOT EXISTS idx_buffet_receita_ingrediente_produto ON public.buffet_receita_ingrediente USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_buffet_receita_ingrediente_receita ON public.buffet_receita_ingrediente USING btree (receita_id);
CREATE INDEX IF NOT EXISTS idx_buffet_receita_produto ON public.buffet_receita USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_buffet_transferencia_data ON public.buffet_transferencia_estoque USING btree (data);
CREATE INDEX IF NOT EXISTS idx_buffet_transferencia_produto ON public.buffet_transferencia_estoque USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_buffet_transferencia_status ON public.buffet_transferencia_estoque USING btree (status);
CREATE INDEX IF NOT EXISTS idx_cliente_ativo ON public.cadastro_clientes USING btree (status);
CREATE INDEX IF NOT EXISTS idx_cliente_tipo ON public.cadastro_clientes USING btree (tipo_pessoa);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_data ON public.estoque_alerta USING btree (data_alerta);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_produto_buffet ON public.estoque_alerta USING btree (produto_buffet_id);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_produto_fruteira ON public.estoque_alerta USING btree (produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_status ON public.estoque_alerta USING btree (status);
CREATE INDEX IF NOT EXISTS idx_estoque_alerta_tipo ON public.estoque_alerta USING btree (tipo);
CREATE INDEX IF NOT EXISTS idx_estoque_inventario_data ON public.estoque_inventario USING btree (data_inicio);
CREATE INDEX IF NOT EXISTS idx_estoque_inventario_status ON public.estoque_inventario USING btree (status);
CREATE INDEX IF NOT EXISTS idx_estoque_item_inventario_inventario ON public.estoque_item_inventario USING btree (inventario_id);
CREATE INDEX IF NOT EXISTS idx_estoque_item_inventario_produto_buffet ON public.estoque_item_inventario USING btree (produto_buffet_id);
CREATE INDEX IF NOT EXISTS idx_estoque_item_inventario_produto_fruteira ON public.estoque_item_inventario USING btree (produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_data ON public.estoque_movimento USING btree (data);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_origem_destino ON public.estoque_movimento USING btree (origem, destino);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_produto_buffet ON public.estoque_movimento USING btree (produto_buffet_id);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_produto_fruteira ON public.estoque_movimento USING btree (produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_estoque_movimento_tipo ON public.estoque_movimento USING btree (tipo);
CREATE INDEX IF NOT EXISTS idx_fiscal_ativo_status ON public.fiscal_ativo_imobilizado USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_ativo_unidade ON public.fiscal_ativo_imobilizado USING btree (unidade_negocio);
CREATE INDEX IF NOT EXISTS idx_fiscal_compra_data ON public.fiscal_compra USING btree (data_compra);
CREATE INDEX IF NOT EXISTS idx_fiscal_compra_fornecedor ON public.fiscal_compra USING btree (fornecedor_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_compra_status ON public.fiscal_compra USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_conciliacao_adquirente ON public.fiscal_conciliacao_cartao USING btree (adquirente);
CREATE INDEX IF NOT EXISTS idx_fiscal_conciliacao_data ON public.fiscal_conciliacao_cartao USING btree (data_venda);
CREATE INDEX IF NOT EXISTS idx_fiscal_conciliacao_nsu ON public.fiscal_conciliacao_cartao USING btree (nsu);
CREATE INDEX IF NOT EXISTS idx_fiscal_conciliacao_status ON public.fiscal_conciliacao_cartao USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_dre_periodo ON public.fiscal_dre USING btree (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_fiscal_dre_status ON public.fiscal_dre USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_dre_unidade ON public.fiscal_dre USING btree (unidade_negocio);
CREATE INDEX IF NOT EXISTS idx_fiscal_item_compra_compra ON public.fiscal_item_compra USING btree (compra_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_item_compra_produto ON public.fiscal_item_compra USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_item_nota_entrada_nota ON public.fiscal_item_nota_entrada USING btree (nota_entrada_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_item_nota_entrada_produto ON public.fiscal_item_nota_entrada USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_data_emissao ON public.fiscal_nfe USING btree (data_emissao);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_numero ON public.fiscal_nfe USING btree (numero);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_produtor_rural ON public.fiscal_nfe USING btree (eh_produtor_rural);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_status ON public.fiscal_nfe USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_venda ON public.fiscal_nfe USING btree (venda_id);
CREATE INDEX IF NOT EXISTS idx_fiscal_nota_entrada_data ON public.fiscal_nota_entrada USING btree (data_emissao);
CREATE INDEX IF NOT EXISTS idx_fiscal_nota_entrada_fornecedor ON public.fiscal_nota_entrada USING btree (fornecedor_documento);
CREATE INDEX IF NOT EXISTS idx_fiscal_nota_entrada_status ON public.fiscal_nota_entrada USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_sped_contrib_periodo ON public.fiscal_sped_contribuicoes USING btree (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_fiscal_sped_contrib_status ON public.fiscal_sped_contribuicoes USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_sped_periodo ON public.fiscal_sped_fiscal USING btree (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_fiscal_sped_status ON public.fiscal_sped_fiscal USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fornecedor_ativo ON public.cadastro_fornecedores USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fornecedor_cnpj ON public.cadastro_fornecedores USING btree (cnpj_cpf);
CREATE INDEX IF NOT EXISTS idx_fruteira_etiqueta_ativa ON public.fruteira_etiqueta_balanca USING btree (ativa);
CREATE INDEX IF NOT EXISTS idx_fruteira_etiqueta_codigo ON public.fruteira_etiqueta_balanca USING btree (codigo);
CREATE INDEX IF NOT EXISTS idx_fruteira_etiqueta_produto ON public.fruteira_etiqueta_balanca USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_fruteira_perda_data ON public.fruteira_perda USING btree (data);
CREATE INDEX IF NOT EXISTS idx_fruteira_perda_produto ON public.fruteira_perda USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_fruteira_perda_tipo ON public.fruteira_perda USING btree (tipo);
CREATE INDEX IF NOT EXISTS idx_fruteira_produto_ativo ON public.fruteira_produto USING btree (ativo);
CREATE INDEX IF NOT EXISTS idx_fruteira_produto_codigo ON public.fruteira_produto USING btree (codigo);
CREATE INDEX IF NOT EXISTS idx_fruteira_produto_codigo_barras ON public.fruteira_produto USING btree (codigo_barras);
CREATE INDEX IF NOT EXISTS idx_fruteira_produto_nome ON public.fruteira_produto USING btree (nome);
CREATE INDEX IF NOT EXISTS idx_fruteira_produto_tipo ON public.fruteira_produto USING btree (tipo);
CREATE INDEX IF NOT EXISTS idx_fruteira_tabela_preco_ativa ON public.fruteira_tabela_preco USING btree (ativa);
CREATE INDEX IF NOT EXISTS idx_fruteira_tabela_preco_produto ON public.fruteira_tabela_preco USING btree (produto_id);
CREATE INDEX IF NOT EXISTS idx_fruteira_tabela_preco_vigencia ON public.fruteira_tabela_preco USING btree (data_inicio, data_fim);
CREATE INDEX IF NOT EXISTS idx_pdv_item_venda_origem ON public.pdv_item_venda USING btree (origem);
CREATE INDEX IF NOT EXISTS idx_pdv_item_venda_produto_buffet ON public.pdv_item_venda USING btree (produto_buffet_id);
CREATE INDEX IF NOT EXISTS idx_pdv_item_venda_produto_fruteira ON public.pdv_item_venda USING btree (produto_fruteira_id);
CREATE INDEX IF NOT EXISTS idx_pdv_item_venda_venda ON public.pdv_item_venda USING btree (venda_id);
CREATE INDEX IF NOT EXISTS idx_pdv_movimento_data ON public.pdv_movimento_caixa USING btree (data);
CREATE INDEX IF NOT EXISTS idx_pdv_movimento_sessao ON public.pdv_movimento_caixa USING btree (sessao_caixa_id);
CREATE INDEX IF NOT EXISTS idx_pdv_movimento_tipo ON public.pdv_movimento_caixa USING btree (tipo);
CREATE INDEX IF NOT EXISTS idx_pdv_pagamento_forma ON public.pdv_pagamento USING btree (forma_pagamento);
CREATE INDEX IF NOT EXISTS idx_pdv_pagamento_nsu ON public.pdv_pagamento USING btree (nsu);
CREATE INDEX IF NOT EXISTS idx_pdv_pagamento_venda ON public.pdv_pagamento USING btree (venda_id);
CREATE INDEX IF NOT EXISTS idx_pdv_sessao_data ON public.pdv_sessao_caixa USING btree (data_abertura);
CREATE INDEX IF NOT EXISTS idx_pdv_sessao_pdv ON public.pdv_sessao_caixa USING btree (pdv_id);
CREATE INDEX IF NOT EXISTS idx_pdv_sessao_status ON public.pdv_sessao_caixa USING btree (status);
CREATE INDEX IF NOT EXISTS idx_pdv_terminal_ativo ON public.pdv_terminal USING btree (ativo);
CREATE INDEX IF NOT EXISTS idx_pdv_venda_cliente ON public.pdv_venda USING btree (cliente_documento);
CREATE INDEX IF NOT EXISTS idx_pdv_venda_comanda ON public.pdv_venda USING btree (comanda_id);
CREATE INDEX IF NOT EXISTS idx_pdv_venda_data ON public.pdv_venda USING btree (data);
CREATE INDEX IF NOT EXISTS idx_pdv_venda_pdv ON public.pdv_venda USING btree (pdv_id);
CREATE INDEX IF NOT EXISTS idx_pdv_venda_status ON public.pdv_venda USING btree (status);
CREATE INDEX IF NOT EXISTS idx_usuario_ativo ON public.common_usuario USING btree (ativo);
DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk2hmh1s2vfws4ke4avisn1lx4i'
          AND conrelid = 'public.estoque_item_inventario'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_item_inventario
            ADD CONSTRAINT fk2hmh1s2vfws4ke4avisn1lx4i FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk383xt96cfqnatg7a30uq4gvvd'
          AND conrelid = 'public.pdv_pagamento'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_pagamento
            ADD CONSTRAINT fk383xt96cfqnatg7a30uq4gvvd FOREIGN KEY (venda_id) REFERENCES public.pdv_venda(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk3nq4r0hrxtx9b78dgxhdfn0wa'
          AND conrelid = 'public.buffet_receita_ingrediente'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_receita_ingrediente
            ADD CONSTRAINT fk3nq4r0hrxtx9b78dgxhdfn0wa FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk45c275bapcfxflcsfyopca7rb'
          AND conrelid = 'public.buffet_receita_ingrediente'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_receita_ingrediente
            ADD CONSTRAINT fk45c275bapcfxflcsfyopca7rb FOREIGN KEY (receita_id) REFERENCES public.buffet_receita(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk4e5vymc38nepgk76s8orijbu5'
          AND conrelid = 'public.estoque_movimento'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_movimento
            ADD CONSTRAINT fk4e5vymc38nepgk76s8orijbu5 FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk4pfgjytufx4h1icu15hnv8tk'
          AND conrelid = 'public.pdv_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_venda
            ADD CONSTRAINT fk4pfgjytufx4h1icu15hnv8tk FOREIGN KEY (comanda_id) REFERENCES public.buffet_comanda(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk4u70pfm409tisvav88aajae51'
          AND conrelid = 'public.pdv_sessao_caixa'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_sessao_caixa
            ADD CONSTRAINT fk4u70pfm409tisvav88aajae51 FOREIGN KEY (pdv_id) REFERENCES public.pdv_terminal(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk6g1m3e7strhm5g2bj954ix2yy'
          AND conrelid = 'public.estoque_item_inventario'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_item_inventario
            ADD CONSTRAINT fk6g1m3e7strhm5g2bj954ix2yy FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk6l82eh6rsn15i8s7yo17wvwck'
          AND conrelid = 'public.pdv_item_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_item_venda
            ADD CONSTRAINT fk6l82eh6rsn15i8s7yo17wvwck FOREIGN KEY (venda_id) REFERENCES public.pdv_venda(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk6x0147j8p81tdno8p2gq17mmn'
          AND conrelid = 'public.estoque_item_inventario'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_item_inventario
            ADD CONSTRAINT fk6x0147j8p81tdno8p2gq17mmn FOREIGN KEY (inventario_id) REFERENCES public.estoque_inventario(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk75io9wyqukk2hfga4r7cw55ga'
          AND conrelid = 'public.fiscal_compra'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_compra
            ADD CONSTRAINT fk75io9wyqukk2hfga4r7cw55ga FOREIGN KEY (fornecedor_id) REFERENCES public.cadastro_fornecedores(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk7ais7qmr6su6q5pmbk6wus6ve'
          AND conrelid = 'public.buffet_receita'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_receita
            ADD CONSTRAINT fk7ais7qmr6su6q5pmbk6wus6ve FOREIGN KEY (produto_id) REFERENCES public.buffet_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk7bnm6m8hf971e4vlg44s2jfp'
          AND conrelid = 'public.fruteira_etiqueta_balanca'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_etiqueta_balanca
            ADD CONSTRAINT fk7bnm6m8hf971e4vlg44s2jfp FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk7etwbw26ik6cbrngfqh8y752g'
          AND conrelid = 'public.pdv_movimento_caixa'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_movimento_caixa
            ADD CONSTRAINT fk7etwbw26ik6cbrngfqh8y752g FOREIGN KEY (sessao_caixa_id) REFERENCES public.pdv_sessao_caixa(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk8ssavhqbl11jh94l1dc2pakbp'
          AND conrelid = 'public.fruteira_perda'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_perda
            ADD CONSTRAINT fk8ssavhqbl11jh94l1dc2pakbp FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk9hc24ore64oxdu4vx6wk4xsgb'
          AND conrelid = 'public.buffet_item_comanda'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_item_comanda
            ADD CONSTRAINT fk9hc24ore64oxdu4vx6wk4xsgb FOREIGN KEY (comanda_id) REFERENCES public.buffet_comanda(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fka4igflcsyejicobwsfotr895t'
          AND conrelid = 'public.estoque_movimento'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_movimento
            ADD CONSTRAINT fka4igflcsyejicobwsfotr895t FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fka6b7sco3hslscyb2sk2la01ne'
          AND conrelid = 'public.pdv_item_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_item_venda
            ADD CONSTRAINT fka6b7sco3hslscyb2sk2la01ne FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkbf7qnaffd147gwg2y8cxayqwt'
          AND conrelid = 'public.fiscal_item_compra'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_compra
            ADD CONSTRAINT fkbf7qnaffd147gwg2y8cxayqwt FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkc0veedeid5r04452cxakus2ih'
          AND conrelid = 'public.fiscal_item_nota_entrada'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_nota_entrada
            ADD CONSTRAINT fkc0veedeid5r04452cxakus2ih FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkchr6f8hb6872mid92rx6lo7bw'
          AND conrelid = 'public.fiscal_item_compra'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_compra
            ADD CONSTRAINT fkchr6f8hb6872mid92rx6lo7bw FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkgs364n2ttg1fs43pvjoruhcjm'
          AND conrelid = 'public.common_usuario_perfil'::regclass
    ) THEN
        ALTER TABLE ONLY public.common_usuario_perfil
            ADD CONSTRAINT fkgs364n2ttg1fs43pvjoruhcjm FOREIGN KEY (usuario_id) REFERENCES public.common_usuario(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkjc9apey1o4ocmxgvwnt0kuv3p'
          AND conrelid = 'public.fruteira_tabela_preco'::regclass
    ) THEN
        ALTER TABLE ONLY public.fruteira_tabela_preco
            ADD CONSTRAINT fkjc9apey1o4ocmxgvwnt0kuv3p FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fknws6y908o3pxl0oq18r3fn00o'
          AND conrelid = 'public.fiscal_item_compra'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_compra
            ADD CONSTRAINT fknws6y908o3pxl0oq18r3fn00o FOREIGN KEY (compra_id) REFERENCES public.fiscal_compra(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkogo964dgi4la67twf6j8pvmua'
          AND conrelid = 'public.estoque_alerta'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_alerta
            ADD CONSTRAINT fkogo964dgi4la67twf6j8pvmua FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkp0speotfcwdnc4vty28w36stf'
          AND conrelid = 'public.buffet_transferencia_estoque'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_transferencia_estoque
            ADD CONSTRAINT fkp0speotfcwdnc4vty28w36stf FOREIGN KEY (produto_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkpfey1x1rq9y46mblcwkld05cc'
          AND conrelid = 'public.fiscal_item_nota_entrada'::regclass
    ) THEN
        ALTER TABLE ONLY public.fiscal_item_nota_entrada
            ADD CONSTRAINT fkpfey1x1rq9y46mblcwkld05cc FOREIGN KEY (nota_entrada_id) REFERENCES public.fiscal_nota_entrada(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkpl1pwlotob7114pdnulbocpi5'
          AND conrelid = 'public.estoque_alerta'::regclass
    ) THEN
        ALTER TABLE ONLY public.estoque_alerta
            ADD CONSTRAINT fkpl1pwlotob7114pdnulbocpi5 FOREIGN KEY (produto_buffet_id) REFERENCES public.buffet_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkqyyykciw4p08efebv729h5u4q'
          AND conrelid = 'public.pdv_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_venda
            ADD CONSTRAINT fkqyyykciw4p08efebv729h5u4q FOREIGN KEY (nfce_id) REFERENCES public.fiscal_nfe(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fktk3h3m7xj9npnxe3ssmqm215y'
          AND conrelid = 'public.pdv_item_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_item_venda
            ADD CONSTRAINT fktk3h3m7xj9npnxe3ssmqm215y FOREIGN KEY (produto_fruteira_id) REFERENCES public.fruteira_produto(id);
    END IF;
END
$do$;

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fkwjrjh79rau142sy9qb270ern'
          AND conrelid = 'public.buffet_comanda'::regclass
    ) THEN
        ALTER TABLE ONLY public.buffet_comanda
            ADD CONSTRAINT fkwjrjh79rau142sy9qb270ern FOREIGN KEY (mesa_id) REFERENCES public.buffet_mesa(id);
    END IF;
END
$do$;

-- Tabela public.pdv_caderno_divida
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
DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'pdv_caderno_divida_cliente_fk'
          AND conrelid = 'public.pdv_caderno_divida'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_caderno_divida
            ADD CONSTRAINT pdv_caderno_divida_cliente_fk FOREIGN KEY (cliente_id) REFERENCES public.cadastro_clientes(id);
    END IF;
END
$do$;

-- Tabela public.buffet_cliente
CREATE TABLE IF NOT EXISTS public.buffet_cliente (
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

CREATE INDEX IF NOT EXISTS idx_buffet_cliente_cpf ON public.buffet_cliente (cpf);
CREATE INDEX IF NOT EXISTS idx_buffet_cliente_nome ON public.buffet_cliente (nome);
-- Tabela public.buffet_cliente_visita
CREATE TABLE IF NOT EXISTS public.buffet_cliente_visita (
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

CREATE INDEX IF NOT EXISTS idx_buffet_visita_cliente ON public.buffet_cliente_visita (cliente_id);
CREATE INDEX IF NOT EXISTS idx_buffet_visita_data ON public.buffet_cliente_visita (data_visita);
--
-- Colunas que possam faltar em tabelas criadas por versoes antigas
-- (adicionadas de forma anulavel para nao falhar em bancos com dados).
--
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS acrescimo numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS desconto numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS preco_kg numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS subtotal numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS tara_padrao numeric(10,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS valor_fixo numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS valor_pago numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS data_abertura timestamp(6) without time zone;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS data_fechamento timestamp(6) without time zone;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS data_pagamento timestamp(6) without time zone;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS mesa_id uuid;
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS cliente_documento character varying(20);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS cliente_telefone character varying(20);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS modalidade character varying(20);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS numero character varying(20);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS aberta_por character varying(100);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS cancelada_por character varying(100);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS fechada_por character varying(100);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS cliente_nome character varying(200);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS motivo_cancelamento character varying(500);
ALTER TABLE public.buffet_comanda ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS cancelado boolean;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS peso_bruto numeric(10,4);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS peso_liquido numeric(10,4);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS preco_unitario numeric(15,4);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS quantidade numeric(10,4);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS tara numeric(10,4);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS data_cancelamento timestamp(6) without time zone;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS data_lancamento timestamp(6) without time zone;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS comanda_id uuid;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS tipo character varying(20);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS codigo_produto character varying(50);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS cancelado_por character varying(100);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS lancado_por character varying(100);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS nome_produto character varying(200);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS motivo_cancelamento character varying(500);
ALTER TABLE public.buffet_item_comanda ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS ativa boolean;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS capacidade integer;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS posicao_x integer;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS posicao_y integer;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS numero character varying(20);
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS area character varying(50);
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS nome character varying(100);
ALTER TABLE public.buffet_mesa ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS ativo boolean;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS calorias integer;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS controla_estoque boolean;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS cst_cofins character varying(2);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS cst_pis character varying(2);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS custo_medio numeric(15,4);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS disponivel_balcao boolean;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS estoque_atual numeric(15,4);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS estoque_minimo numeric(15,4);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS origem_mercadoria integer;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS permite_fracao boolean;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS preco_venda numeric(15,4);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS tempo_preparo_minutos integer;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS cest character varying(7);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS ncm character varying(8);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS unidade_medida character varying(10);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS categoria character varying(30);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS codigo character varying(50);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS codigo_barras character varying(100);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS nome character varying(200);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS alergenicos character varying(500);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS imagem_url character varying(500);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS descricao character varying(1000);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS informacoes_nutricionais character varying(1000);
ALTER TABLE public.buffet_produto ADD COLUMN IF NOT EXISTS observacoes character varying(2000);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS ativa boolean;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS custo_total numeric(15,4);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS margem_desejada numeric(5,2);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS preco_sugerido numeric(15,4);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS rendimento numeric(10,4);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS tempo_cozimento_minutos integer;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS tempo_preparo_minutos integer;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS versao integer;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS unidade_rendimento character varying(10);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS dificuldade character varying(20);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS observacoes character varying(2000);
ALTER TABLE public.buffet_receita ADD COLUMN IF NOT EXISTS instrucoes character varying(5000);
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS custo_total numeric(15,4);
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS custo_unitario numeric(15,4);
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS obrigatorio boolean;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS ordem integer;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS unidade character varying(10);
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS receita_id uuid;
ALTER TABLE public.buffet_receita_ingrediente ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS custo_unitario numeric(15,4);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS data timestamp(6) without time zone;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS data_conclusao timestamp(6) without time zone;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS destino character varying(20);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS origem character varying(20);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS aprovado_por character varying(100);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS recebido_por character varying(100);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS solicitado_por character varying(100);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS motivo_rejeicao character varying(500);
ALTER TABLE public.buffet_transferencia_estoque ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS data_nascimento date;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS limite_credito numeric(15,2);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS pontos_fidelidade integer;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS ultima_compra date;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS cep character varying(9);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS numero character varying(10);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS cpf_cnpj character varying(18);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS inscricao_estadual character varying(20);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS rg_ie character varying(20);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS telefone character varying(20);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS tipo_pessoa character varying(20);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS orgao_emissor character varying(50);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS tipo_cliente character varying(50);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS bairro character varying(100);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS cidade character varying(100);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS contato_comercial character varying(100);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS email character varying(100);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS email_comercial character varying(100);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS nome character varying(200);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS nome_fantasia character varying(200);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS endereco character varying(500);
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS observacoes text;
ALTER TABLE public.cadastro_clientes ADD COLUMN IF NOT EXISTS rg character varying(255);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS aliquota_cofins numeric(5,2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS aliquota_icms numeric(5,2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS aliquota_pis numeric(5,2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS cst_cofins character varying(2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS cst_pis character varying(2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS limite_compra numeric(15,2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS percentual_desconto numeric(5,2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS pontos_parceria integer;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS prazo_entrega_dias integer;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS total_compras numeric(15,2);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS ultima_compra date;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS codigo_ncm character varying(8);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS cep character varying(9);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS numero character varying(10);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS cnpj_cpf character varying(18);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS inscricao_estadual character varying(20);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS inscricao_municipal character varying(20);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS telefone character varying(20);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS telefone_contato character varying(20);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS status_entrega character varying(30);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS situacao_tributaria character varying(50);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS tipo character varying(50);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS bairro character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS cidade character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS condicoes_pagamento character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS contato character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS email character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS email_contato character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS segmento_mercado character varying(100);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS nome character varying(200);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS razao_social character varying(200);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS endereco character varying(500);
ALTER TABLE public.cadastro_fornecedores ADD COLUMN IF NOT EXISTS observacoes text;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS sucesso boolean;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS data_acao timestamp(6) without time zone;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS registro_id uuid;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS usuario_id uuid;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS ip_address character varying(45);
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS entidade character varying(50);
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS acao character varying(100);
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS descricao text;
ALTER TABLE public.common_audit_log ADD COLUMN IF NOT EXISTS user_agent text;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS ativo boolean;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS primeiro_acesso boolean;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS tentativas_login integer;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS bloqueado_ate timestamp(6) without time zone;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS data_atualizacao timestamp(6) without time zone;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS data_criacao timestamp(6) without time zone;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS senha_expiracao timestamp(6) without time zone;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS ultimo_login timestamp(6) without time zone;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS empresa_id uuid;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS pdv_id uuid;
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS cpf_cnpj character varying(18);
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS telefone character varying(20);
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS tipo_pessoa character varying(20);
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS email character varying(150);
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS nome character varying(150);
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS avatar_url character varying(500);
ALTER TABLE public.common_usuario ADD COLUMN IF NOT EXISTS senha_hash character varying(255);
ALTER TABLE public.common_usuario_perfil ADD COLUMN IF NOT EXISTS usuario_id uuid;
ALTER TABLE public.common_usuario_perfil ADD COLUMN IF NOT EXISTS perfil character varying(50);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS data_vencimento date;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS dias_para_vencimento integer;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS valor_atual numeric(15,4);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS valor_referencia numeric(15,4);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS data_alerta timestamp(6) without time zone;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS data_resolucao timestamp(6) without time zone;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS data_visualizacao timestamp(6) without time zone;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS produto_buffet_id uuid;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS produto_fruteira_id uuid;
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS prioridade character varying(20);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS tipo character varying(30);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS resolvido_por character varying(100);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS visualizado_por character varying(100);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS mensagem character varying(500);
ALTER TABLE public.estoque_alerta ADD COLUMN IF NOT EXISTS observacoes_resolucao character varying(500);
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS itens_contados integer;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS itens_divergentes integer;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS total_itens integer;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS valor_divergencia numeric(15,4);
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS data_fim timestamp(6) without time zone;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS data_inicio timestamp(6) without time zone;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS responsavel character varying(100);
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS descricao character varying(200);
ALTER TABLE public.estoque_inventario ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS contado boolean;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS custo_unitario numeric(15,4);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS quantidade_contada numeric(15,4);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS quantidade_divergencia numeric(15,4);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS quantidade_sistema numeric(15,4);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS valor_divergencia numeric(15,4);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS data_contagem timestamp(6) without time zone;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS inventario_id uuid;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS produto_buffet_id uuid;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS produto_fruteira_id uuid;
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS contado_por character varying(100);
ALTER TABLE public.estoque_item_inventario ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS custo_unitario numeric(15,4);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS estoque_anterior numeric(15,4);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS estoque_posterior numeric(15,4);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS data timestamp(6) without time zone;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS data_validade timestamp(6) without time zone;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS produto_buffet_id uuid;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS produto_fruteira_id uuid;
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS destino character varying(20);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS origem character varying(20);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS tipo character varying(30);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS lote character varying(50);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS numero_documento character varying(50);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS documento_referencia character varying(100);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS responsavel character varying(100);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS motivo_defeito character varying(500);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS tipo_defeito character varying(50);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS lote_defeituoso character varying(50);
ALTER TABLE public.estoque_movimento ADD COLUMN IF NOT EXISTS quantidade_recuperavel numeric(15,4);

-- A V7 cria as tabelas de estoque com TIMESTAMP WITH TIME ZONE, mas as
-- entidades usam LocalDateTime (timestamp sem fuso). Padroniza as colunas para
-- que instalacoes novas e bancos legados fiquem com o mesmo tipo.
DO $do$
DECLARE
    r record;
BEGIN
    FOR r IN
        SELECT table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND data_type = 'timestamp with time zone'
          AND table_name IN ('estoque_movimento', 'estoque_alerta',
                             'estoque_inventario', 'estoque_item_inventario')
    LOOP
        EXECUTE format(
            'ALTER TABLE public.%I ALTER COLUMN %I TYPE timestamp(6) without time zone USING %I AT TIME ZONE current_setting(''TimeZone'')',
            r.table_name, r.column_name, r.column_name);
    END LOOP;
END
$do$;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS data_aquisicao date;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS data_baixa date;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS data_fim_depreciacao date;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS data_inicio_depreciacao date;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS depreciacao_acumulada numeric(15,4);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS taxa_depreciacao_anual numeric(5,2);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS ultima_depreciacao date;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_aquisicao numeric(15,4);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_baixa numeric(15,4);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_liquido numeric(15,4);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS valor_residual numeric(15,4);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS vida_util_meses integer;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS metodo_depreciacao character varying(20);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS unidade_negocio character varying(20);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS tipo character varying(30);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS codigo character varying(50);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS nota_fiscal character varying(50);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS responsavel character varying(100);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS descricao character varying(200);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS fornecedor character varying(200);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS localizacao character varying(200);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS motivo_baixa character varying(500);
ALTER TABLE public.fiscal_ativo_imobilizado ADD COLUMN IF NOT EXISTS observacoes text;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS data_compra date;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS data_recebimento date;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS desconto numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS frete numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS outras_despesas numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS serie_nota integer;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS status smallint;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS valor_cofins numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS valor_icms numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS valor_ipi numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS valor_pis numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS fornecedor_id uuid;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS cnpj_fornecedor character varying(18);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS condicao_pagamento character varying(50);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS numero_nota character varying(50);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS numero_os character varying(50);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS nome_fornecedor character varying(200);
ALTER TABLE public.fiscal_compra ADD COLUMN IF NOT EXISTS observacoes text;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS data_recebimento date;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS data_venda date;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS diferenca numeric(15,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS taxa_adesao numeric(15,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS taxa_mdr numeric(5,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS valor_bruto numeric(15,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS valor_liquido numeric(15,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS valor_recebido numeric(15,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS valor_taxa numeric(15,4);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS data_conciliacao timestamp(6) without time zone;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS venda_id uuid;
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS bandeira character varying(30);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS numero_venda character varying(30);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS codigo_autorizacao character varying(50);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS nsu character varying(50);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS adquirente character varying(100);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS conciliado_por character varying(100);
ALTER TABLE public.fiscal_conciliacao_cartao ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS cmv numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS cmv_buffet numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS cmv_fruteira numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS csll numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS data_fim date;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS data_inicio date;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS descontos_concedidos numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS despesas_administrativas numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS despesas_financeiras numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS despesas_nao_operacionais numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS despesas_operacionais_total numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS despesas_vendas numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS devolucoes numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS impostos_vendas numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS irpj numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS lucro_bruto numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS lucro_liquido numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS lucro_operacional numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS margem_bruta_percentual numeric(5,2);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS margem_liquida_percentual numeric(5,2);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS meses_calculados integer;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS receita_bruta numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS receita_buffet numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS receita_fruteira numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS receita_liquida numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS receita_outros numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS receitas_nao_operacionais numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS resultado_antes_ir_csll numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS resultado_nao_operacional numeric(15,4);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS ultima_atualizacao timestamp(6) without time zone;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS unidade_negocio character varying(20);
ALTER TABLE public.fiscal_dre ADD COLUMN IF NOT EXISTS observacoes text;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS cfop character varying(4);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS data_cotacao_ceasa date;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS preco_referencia_ceasa numeric(15,4);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS valor_unitario numeric(15,4);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS codigo_ncm character varying(8);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS unidade character varying(10);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS compra_id uuid;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS produto_buffet_id uuid;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS codigo_produto character varying(50);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS nome_produto character varying(200);
ALTER TABLE public.fiscal_item_compra ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS cfop character varying(4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS cst_cofins character varying(2);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS cst_pis character varying(2);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS origem_mercadoria integer;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS processado boolean;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS valor_cofins numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS valor_icms numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS valor_ipi numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS valor_pis numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS valor_unitario numeric(15,4);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS cest character varying(7);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS ncm character varying(8);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS unidade character varying(10);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS nota_entrada_id uuid;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS codigo_produto character varying(50);
ALTER TABLE public.fiscal_item_nota_entrada ADD COLUMN IF NOT EXISTS nome_produto character varying(200);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS area_producao numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cfop_produtor character varying(4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS data_colheita date;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS digito_validador integer;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS eh_produtor_rural boolean;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS modalidade_acre character varying(2);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS modelo character varying(2);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS serie integer;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS tentativas_envio integer;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_cofins numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_icms numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_ipi numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_pis numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS volume_producao numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS data_emissao timestamp(6) without time zone;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS data_saida_entrada timestamp(6) without time zone;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS numero bigint;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS ultima_tentativa timestamp(6) without time zone;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS venda_id uuid;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cnpj_produtor character varying(18);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS ambiente character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cliente_documento character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS rural_ie character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS chave_acesso character varying(50);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS protocolo_autorizacao character varying(50);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS tipo_producao character varying(50);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS natureza_operacao character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS grade_producao character varying(100);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cliente_nome character varying(200);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS nome_produtor character varying(200);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS motivo_rejeicao character varying(500);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS obs_produtor_rural text;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS xml_autorizado text;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS xml_cancelamento text;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS xml_original text;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS danfe bytea;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS modelo character varying(2);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS processada boolean;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS serie integer;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS valor_cofins numeric(15,4);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS valor_icms numeric(15,4);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS valor_ipi numeric(15,4);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS valor_pis numeric(15,4);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS data_emissao timestamp(6) without time zone;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS data_entrada timestamp(6) without time zone;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS data_importacao timestamp(6) without time zone;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS data_processamento timestamp(6) without time zone;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS numero bigint;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS fornecedor_documento character varying(20);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS fornecedor_ie character varying(20);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS chave_acesso character varying(50);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS protocolo_autorizacao character varying(50);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS fornecedor_nome character varying(200);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS erro_importacao character varying(1000);
ALTER TABLE public.fiscal_nota_entrada ADD COLUMN IF NOT EXISTS xml_original text;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS data_fim date;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS data_inicio date;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS indicador_movimento character varying(1);
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS data_geracao timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS data_transmissao timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS data_validacao timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS tamanho_arquivo bigint;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS total_registros bigint;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS versao_leiaute character varying(10);
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS protocolo_envio character varying(50);
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS arquivo_gerado text;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS arquivo_validado text;
ALTER TABLE public.fiscal_sped_contribuicoes ADD COLUMN IF NOT EXISTS erros_validacao text;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS data_fim date;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS data_inicio date;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS finalidade character varying(1);
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS indicador_movimento character varying(1);
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS perfil_apresentacao character varying(1);
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS data_geracao timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS data_transmissao timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS data_validacao timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS tamanho_arquivo bigint;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS total_registros bigint;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS versao_leiaute character varying(10);
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS protocolo_envio character varying(50);
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS arquivo_gerado text;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS arquivo_validado text;
ALTER TABLE public.fiscal_sped_fiscal ADD COLUMN IF NOT EXISTS erros_validacao text;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS ativa boolean;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS data_validade date;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS imprimir_codigo_barras boolean;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS imprimir_qr_code boolean;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS preco numeric(15,4);
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS quantidade_impressa integer;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS tara numeric(10,4);
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS validade_dias integer;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS ultima_impressao timestamp(6) without time zone;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS codigo character varying(20);
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS modelo_etiqueta character varying(50);
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS texto_livre_1 character varying(100);
ALTER TABLE public.fruteira_etiqueta_balanca ADD COLUMN IF NOT EXISTS texto_livre_2 character varying(100);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS baixa_estoque_realizada boolean;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS custo_unitario numeric(15,4);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS data date;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS data_validade date;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS data_baixa_estoque timestamp(6) without time zone;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS hora timestamp(6) without time zone;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS tipo character varying(20);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS lote character varying(50);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS autorizado_por character varying(100);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS responsavel character varying(100);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS motivo character varying(500);
ALTER TABLE public.fruteira_perda ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS ativo boolean;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS controla_lote boolean;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS controla_validade boolean;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS cst_cofins character varying(2);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS cst_pis character varying(2);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS custo_medio numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS custo_ultimo numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS dias_validade_alerta integer;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS estoque_atual numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS estoque_maximo numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS estoque_minimo numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS margem_desejada numeric(5,2);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS origem_mercadoria integer;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS permite_fracao boolean;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS pesavel_balanca boolean;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS peso_liquido_padrao numeric(10,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS preco_atacado numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS preco_venda numeric(15,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS quantidade_minima_atacado integer;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS tara_padrao numeric(10,4);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS cest character varying(7);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS ncm character varying(8);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS unidade_medida character varying(10);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS codigo_balanca character varying(20);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS tipo character varying(30);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS codigo character varying(50);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS codigo_barras character varying(100);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS nome character varying(200);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS imagem_url character varying(500);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS descricao character varying(1000);
ALTER TABLE public.fruteira_produto ADD COLUMN IF NOT EXISTS observacoes character varying(2000);
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS ativa boolean;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS data_fim date;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS data_inicio date;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS preco_atacado numeric(15,4);
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS preco_venda numeric(15,4);
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS quantidade_minima_atacado integer;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS aplicada_em timestamp(6) without time zone;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS produto_id uuid;
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS origem character varying(50);
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS aplicada_por character varying(100);
ALTER TABLE public.fruteira_tabela_preco ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS cancelado boolean;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS peso_bruto numeric(10,4);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS peso_liquido numeric(10,4);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS preco_unitario numeric(15,4);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS quantidade numeric(15,4);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS tara numeric(10,4);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS produto_buffet_id uuid;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS produto_fruteira_id uuid;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS venda_id uuid;
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS origem character varying(20);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS tipo character varying(20);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS codigo_produto character varying(50);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS nome_produto character varying(200);
ALTER TABLE public.pdv_item_venda ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS valor numeric(15,4);
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS data timestamp(6) without time zone;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS sessao_caixa_id uuid;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS venda_id uuid;
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS tipo character varying(20);
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS operador character varying(100);
ALTER TABLE public.pdv_movimento_caixa ADD COLUMN IF NOT EXISTS descricao character varying(500);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS parcelas integer;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS ultimos_digitos_cartao character varying(4);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS valor numeric(15,4);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS valor_liquido numeric(15,4);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS valor_taxa numeric(15,4);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS data_autorizacao timestamp(6) without time zone;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS data_pagamento timestamp(6) without time zone;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS vale_validade timestamp(6) without time zone;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS venda_id uuid;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS bandeira_cartao character varying(30);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS forma_pagamento character varying(30);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS codigo_autorizacao character varying(50);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS nsu character varying(50);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS vale_numero character varying(50);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS pix_tx_id character varying(100);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS mensagem_retorno character varying(500);
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS comprovante text;
ALTER TABLE public.pdv_pagamento ADD COLUMN IF NOT EXISTS pix_qr_code text;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS diferenca numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS reforcos_total numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS sangrias_total numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS status_sangria_aberta boolean;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS total_vendas numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS valor_final_contado numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS valor_final_sistema numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS valor_inicial numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS vendas_cartao numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS vendas_dinheiro numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS vendas_pix numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS vendas_vale numeric(15,4);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS data_abertura timestamp(6) without time zone;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS data_fechamento timestamp(6) without time zone;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS numero_ultima_venda bigint;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS pdv_id uuid;
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS operador_abertura character varying(100);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS operador_fechamento character varying(100);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS observacoes_abertura character varying(1000);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS observacoes_fechamento character varying(1000);
ALTER TABLE public.pdv_sessao_caixa ADD COLUMN IF NOT EXISTS reducao_z text;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS ativo boolean;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS tef_habilitado boolean;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS tef_porta integer;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS numero_ultima_venda bigint;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS ultima_abertura timestamp(6) without time zone;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS ultimo_fechamento timestamp(6) without time zone;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS codigo character varying(20);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS porta_balanca_buffet character varying(20);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS porta_balanca_fruteira character varying(20);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS tef_ip character varying(50);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS impressora_fiscal character varying(100);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS impressora_nao_fiscal character varying(100);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS nome character varying(100);
ALTER TABLE public.pdv_terminal ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS acrescimo numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS deleted boolean;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS desconto numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS subtotal numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS subtotal_buffet numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS subtotal_fruteira numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS troco numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS valor_pago numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS valor_total numeric(15,4);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS data timestamp(6) without time zone;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS data_finalizacao timestamp(6) without time zone;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS comanda_id uuid;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS nfce_id uuid;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS pdv_id uuid;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS cliente_documento character varying(20);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS numero_nfce character varying(20);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS status character varying(20);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS numero character varying(30);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS chave_nfce character varying(50);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS operador character varying(100);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS cliente_nome character varying(200);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS xml_nfce text;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS danfe_nfce bytea;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS nfce_erro character varying(500);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS cupom_impressao text;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS tentativas_impressao integer NOT NULL DEFAULT 0;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS status_ultima_impressao character varying(30);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS observacoes_impressao character varying(500);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS data_ultima_impressao timestamp(6) without time zone;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS cliente_id uuid;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS cliente_nome character varying(200);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS cliente_documento character varying(20);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS cliente_telefone character varying(20);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS venda_id uuid;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS numero_venda character varying(30);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS descricao character varying(500);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS valor_total numeric(15,2) DEFAULT 0;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS valor_pago numeric(15,2) DEFAULT 0;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS status character varying(20) DEFAULT 'ABERTA';
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS data_lancamento timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS data_vencimento date;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS data_quitacao timestamp(6) without time zone;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS operador character varying(100);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS pdv_id uuid;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS observacoes character varying(1000);
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.pdv_caderno_divida ADD COLUMN IF NOT EXISTS deleted boolean DEFAULT false;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS deleted boolean DEFAULT false;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS total_visitas integer DEFAULT 0;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS total_consumido numeric(15,4) DEFAULT 0;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS primeira_visita timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS ultima_visita timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS cpf character varying(14);
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS nome character varying(200);
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS telefone character varying(20);
ALTER TABLE public.buffet_cliente ADD COLUMN IF NOT EXISTS observacoes character varying(500);
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS deleted boolean DEFAULT false;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS created_at timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS updated_at timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS version bigint;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS id uuid;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS cliente_id uuid;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS comanda_id uuid;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS data_visita timestamp(6) without time zone;
ALTER TABLE public.buffet_cliente_visita ADD COLUMN IF NOT EXISTS valor_consumido numeric(15,4) DEFAULT 0;

-- =============================================================================
-- Tabela adicionada depois do dump inicial (formas de pagamento por venda)
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.pdv_forma_pagamento_venda (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted boolean NOT NULL DEFAULT false,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version bigint,
    formaPagamento character varying(255),
    valor numeric(38,2),
    venda_id uuid,
    CONSTRAINT pdv_forma_pagamento_venda_pkey PRIMARY KEY (id)
);

DO $do$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_pdv_forma_pagamento_venda_venda'
          AND conrelid = 'public.pdv_forma_pagamento_venda'::regclass
    ) THEN
        ALTER TABLE ONLY public.pdv_forma_pagamento_venda
            ADD CONSTRAINT fk_pdv_forma_pagamento_venda_venda
            FOREIGN KEY (venda_id) REFERENCES public.pdv_venda(id);
    END IF;
END
$do$;

CREATE INDEX IF NOT EXISTS idx_pdv_forma_pagamento_venda_venda
    ON public.pdv_forma_pagamento_venda (venda_id);

-- =============================================================================
-- Sequencias de codigo dos produtos (idempotente)
-- =============================================================================
CREATE SEQUENCE IF NOT EXISTS fruteira_produto_codigo_seq
    START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE IF EXISTS public.fruteira_produto
    ALTER COLUMN codigo SET DEFAULT nextval('fruteira_produto_codigo_seq')::varchar;

CREATE SEQUENCE IF NOT EXISTS buffet_produto_codigo_seq
    START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER TABLE IF EXISTS public.buffet_produto
    ALTER COLUMN codigo SET DEFAULT nextval('buffet_produto_codigo_seq')::varchar;

-- =============================================================================
-- Compatibilidade com as migrations V5 e V6 (fiscal_nfe em producao)
--
-- Bancos antigos sao marcados com baseline na versao 7, portanto as migrations
-- V5 e V6 NAO sao executadas neles. Este bloco replica, de forma idempotente,
-- tudo o que elas criam para que o schema fique igual em instalacoes novas e
-- em bancos legados.
-- =============================================================================
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS codigo_cfop character varying(4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS codigo_ncm character varying(8);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cst_pis character varying(2);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS cst_cofins character varying(2);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS meio_pagamento character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_desconto numeric(15,4) DEFAULT 0;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_acrescimo numeric(15,4) DEFAULT 0;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS valor_troco numeric(15,4) DEFAULT 0;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS informacoes_complementares text;

ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS informacoes_fisco text;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_ie character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_logradouro character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_numero character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_complemento character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_bairro character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_codigo_municipio integer;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_municipio character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_uf character varying(2);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_cep character varying(8);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_email character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS modalidade_frete integer DEFAULT 9;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_nome character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_documento character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_ie character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_endereco character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_municipio character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_uf character varying(2);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS volume_quantidade numeric(15,4);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS volume_especie character varying(60);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS volume_peso_liquido numeric(15,3);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS volume_peso_bruto numeric(15,3);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS finalidade_nfe integer DEFAULT 1;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS tipo_operacao integer DEFAULT 1;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS consumidor_final integer DEFAULT 1;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS presenca_comprador integer DEFAULT 1;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS tipo_emissao integer DEFAULT 1;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS recibo_lote character varying(20);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS data_autorizacao timestamp(6) without time zone;
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS codigo_status character varying(10);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS versao_xml character varying(10);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS protocolo_cancelamento character varying(50);
ALTER TABLE public.fiscal_nfe ADD COLUMN IF NOT EXISTS data_cancelamento timestamp(6) without time zone;

CREATE UNIQUE INDEX IF NOT EXISTS ux_fiscal_nfe_modelo_serie_numero
    ON public.fiscal_nfe (modelo, serie, numero);

CREATE TABLE IF NOT EXISTS public.fiscal_nfe_sequencia (
    modelo character varying(2) NOT NULL,
    serie integer NOT NULL,
    ultimo_numero bigint NOT NULL DEFAULT 0,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version bigint,
    CONSTRAINT fiscal_nfe_sequencia_pkey PRIMARY KEY (modelo, serie)
);

CREATE TABLE IF NOT EXISTS public.fiscal_nfe_item (
    id uuid NOT NULL,
    nfe_id uuid NOT NULL,
    numero_item integer NOT NULL,
    codigo_produto character varying(60),
    descricao character varying(200) NOT NULL,
    ncm character varying(8),
    cfop character varying(4) NOT NULL,
    cst_icms character varying(3),
    cst_pis character varying(2),
    cst_cofins character varying(2),
    unidade_comercial character varying(6) NOT NULL,
    quantidade numeric(15,4) NOT NULL,
    valor_unitario numeric(15,4) NOT NULL,
    valor_total numeric(15,4) NOT NULL,
    valor_desconto numeric(15,4) NOT NULL DEFAULT 0,
    origem_mercadoria integer NOT NULL DEFAULT 0,
    valor_icms numeric(15,4) NOT NULL DEFAULT 0,
    aliquota_icms numeric(7,4) NOT NULL DEFAULT 0,
    valor_pis numeric(15,4) NOT NULL DEFAULT 0,
    valor_cofins numeric(15,4) NOT NULL DEFAULT 0,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version bigint,
    deleted boolean NOT NULL DEFAULT false,
    CONSTRAINT fiscal_nfe_item_pkey PRIMARY KEY (id),
    CONSTRAINT fiscal_nfe_item_nfe_fk FOREIGN KEY (nfe_id) REFERENCES public.fiscal_nfe (id) ON DELETE CASCADE,
    CONSTRAINT ux_fiscal_nfe_item_numero UNIQUE (nfe_id, numero_item)
);

CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_item_nfe ON public.fiscal_nfe_item (nfe_id);

CREATE TABLE IF NOT EXISTS public.fiscal_nfe_pagamento (
    id uuid NOT NULL,
    nfe_id uuid NOT NULL,
    forma_pagamento character varying(2) NOT NULL,
    descricao character varying(30),
    valor numeric(15,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version bigint,
    deleted boolean NOT NULL DEFAULT false,
    CONSTRAINT fiscal_nfe_pagamento_pkey PRIMARY KEY (id),
    CONSTRAINT fiscal_nfe_pagamento_nfe_fk FOREIGN KEY (nfe_id) REFERENCES public.fiscal_nfe (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_pagamento_nfe ON public.fiscal_nfe_pagamento (nfe_id);

-- =============================================================================
-- Dados minimos necessarios (administrador e perfis)
-- Senha do administrador: 123456 (troque apos o primeiro acesso)
-- =============================================================================
INSERT INTO public.common_usuario (
    ativo, primeiro_acesso, tentativas_login, bloqueado_ate, data_atualizacao,
    data_criacao, senha_expiracao, ultimo_login, empresa_id, id, pdv_id,
    cpf_cnpj, telefone, tipo_pessoa, email, nome, avatar_url, senha_hash
)
SELECT
    true, false, 0, NULL, NULL,
    timestamp '2026-10-14 21:53:42.002722', NULL, NULL, NULL,
    '7afd842a-3ae3-414b-8d51-97963eac145a', NULL,
    '00000000000', NULL, 'FISICA', 'admin@sistematenda.com', 'Administrador', NULL,
    '$2a$12$IF3P6uOfOWu.EufU8gihIOMkAYF3sY5YvtYxptjvGcvMXRR4FDEeK'
WHERE NOT EXISTS (
    SELECT 1 FROM public.common_usuario WHERE email = 'admin@sistematenda.com'
);

INSERT INTO public.common_usuario_perfil (usuario_id, perfil)
SELECT u.id, p.perfil
FROM public.common_usuario u
CROSS JOIN (VALUES ('ADMIN'), ('GERENTE'), ('FISCAL'), ('CONTABIL'),
                   ('PDV'), ('ESTOQUE'), ('FRUTEIRA'), ('BUFFET')) AS p(perfil)
WHERE u.email = 'admin@sistematenda.com'
  AND NOT EXISTS (
      SELECT 1 FROM public.common_usuario_perfil x
      WHERE x.usuario_id = u.id AND x.perfil = p.perfil
  );

-- Usuario admin e categorias legados (tabelas criadas pela V1).
-- Bancos legados (baseline na V7) nao possuem essas tabelas, por isso os
-- inserts so rodam quando elas existem.
DO $do$
BEGIN
    IF to_regclass('public.usuarios') IS NOT NULL THEN
        INSERT INTO public.usuarios (id, nome, email, senha_hash, role, ativo)
        VALUES (
            '00000000-0000-0000-0000-000000000001',
            'Administrador',
            'admin@sistematenda.com',
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa',
            'ADMIN',
            true
        ) ON CONFLICT (id) DO NOTHING;
    END IF;

    IF to_regclass('public.categorias') IS NOT NULL THEN
        INSERT INTO public.categorias (id, nome, descricao, ativo) VALUES
            ('11111111-1111-1111-1111-111111111111', 'Frutas', 'Frutas frescas e secas', true),
            ('22222222-2222-2222-2222-222222222222', 'Verduras', 'Verduras e legumes', true),
            ('33333333-3333-3333-3333-333333333333', 'Buffet', 'Produtos do buffet', true),
            ('44444444-4444-4444-4444-444444444444', 'Bebidas', 'Bebidas em geral', true),
            ('55555555-5555-5555-5555-555555555555', 'Outros', 'Outros produtos', true)
        ON CONFLICT (id) DO NOTHING;
    END IF;
END
$do$;

-- =============================================================================
-- Fim
-- =============================================================================
