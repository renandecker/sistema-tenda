-- V9__add_pdv_venda_reimpressao_cupom
ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS nfce_erro character varying(500);

ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS cupom_impressao text;

ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS tentativas_impressao integer NOT NULL DEFAULT 0;

ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS status_ultima_impressao character varying(30);

ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS observacoes_impressao character varying(500);

ALTER TABLE public.pdv_venda ADD COLUMN IF NOT EXISTS data_ultima_impressao timestamp(6) without time zone;

DO $do$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = 'public'
           AND table_name = 'pdv_venda'
           AND column_name = 'nfce_erro'
    ) THEN
        ALTER TABLE public.pdv_venda
            ALTER COLUMN nfce_erro TYPE character varying(500);
    END IF;
END
$do$;

UPDATE public.pdv_venda
   SET tentativas_impressao = 0
 WHERE tentativas_impressao IS NULL;

ALTER TABLE public.pdv_venda ALTER COLUMN tentativas_impressao SET DEFAULT 0;

ALTER TABLE public.pdv_venda ALTER COLUMN tentativas_impressao SET NOT NULL;
