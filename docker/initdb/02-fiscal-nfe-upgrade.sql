-- 02-fiscal-nfe-upgrade.sql
-- Complemento idempotente do schema fiscal para instalações novas do Docker.
-- O mesmo conteúdo existe em backend/pdv/src/main/resources/db/migration/V5__fiscal_nfe_production.sql
-- e pode ser aplicado manualmente em bancos já existentes.

ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS codigo_cfop character varying(4);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS codigo_ncm character varying(8);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS cst_icms character varying(3);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS cst_pis character varying(2);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS cst_cofins character varying(2);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS meio_pagamento character varying(20);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS valor_desconto numeric(15,4) DEFAULT 0;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS valor_acrescimo numeric(15,4) DEFAULT 0;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS valor_troco numeric(15,4) DEFAULT 0;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS informacoes_complementares text;

CREATE UNIQUE INDEX IF NOT EXISTS ux_fiscal_nfe_modelo_serie_numero
    ON fiscal_nfe (modelo, serie, numero);

CREATE TABLE IF NOT EXISTS fiscal_nfe_sequencia (
    modelo character varying(2) NOT NULL,
    serie integer NOT NULL,
    ultimo_numero bigint NOT NULL DEFAULT 0,
    created_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version bigint,
    CONSTRAINT fiscal_nfe_sequencia_pkey PRIMARY KEY (modelo, serie)
);

CREATE TABLE IF NOT EXISTS fiscal_nfe_item (
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
    CONSTRAINT fiscal_nfe_item_nfe_fk FOREIGN KEY (nfe_id) REFERENCES fiscal_nfe (id) ON DELETE CASCADE,
    CONSTRAINT ux_fiscal_nfe_item_numero UNIQUE (nfe_id, numero_item)
);

CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_item_nfe ON fiscal_nfe_item (nfe_id);

CREATE TABLE IF NOT EXISTS fiscal_nfe_pagamento (
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
    CONSTRAINT fiscal_nfe_pagamento_nfe_fk FOREIGN KEY (nfe_id) REFERENCES fiscal_nfe (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_pagamento_nfe ON fiscal_nfe_pagamento (nfe_id);
