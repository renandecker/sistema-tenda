-- V4_1__ensure_fiscal_nfe_table.sql
-- Garante a tabela fiscal_nfe antes das migrations V5/V6.
--
-- A V5 faz ALTER TABLE fiscal_nfe e a V6 adiciona as colunas usadas na emissao
-- real da NF-e/NFC-e. Em um banco novo a tabela ainda nao existia, causando
-- "relation fiscal_nfe does not exist". As FKs para pdv_venda sao criadas na V8,
-- depois que todas as tabelas existem.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
    CONSTRAINT fiscal_nfe_pkey PRIMARY KEY (id),
    CONSTRAINT fiscal_nfe_chave_acesso_key UNIQUE (chave_acesso),
    CONSTRAINT fiscal_nfe_status_check CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'EM_PROCESSAMENTO'::character varying, 'AUTORIZADA'::character varying, 'REJEITADA'::character varying, 'CANCELADA'::character varying, 'DENEGADA'::character varying, 'INUTILIZADA'::character varying, 'PRODUTOR_RURAL'::character varying])::text[])))
);

CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_data_emissao ON public.fiscal_nfe USING btree (data_emissao);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_numero ON public.fiscal_nfe USING btree (numero);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_produtor_rural ON public.fiscal_nfe USING btree (eh_produtor_rural);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_status ON public.fiscal_nfe USING btree (status);
CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_venda ON public.fiscal_nfe USING btree (venda_id);
