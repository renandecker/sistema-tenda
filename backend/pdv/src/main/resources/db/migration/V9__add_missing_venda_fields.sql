-- V9__add_missing_venda_fields.sql
-- Adiciona campos ausentes da entidade Venda que sao utilizados
-- pelo fluxo de impressao e nota fiscal.
-- Esta migration e idempotente e segura para execucao em qualquer estado.

ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS nfce_erro character varying(500);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS cupom_impressao text;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS tentativas_impressao integer NOT NULL DEFAULT 0;
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS status_ultima_impressao character varying(30);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS observacoes_impressao character varying(500);
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS data_ultima_impressao timestamp(6) without time zone;