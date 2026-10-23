ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS informacoes_fisco TEXT;

ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_ie VARCHAR(20);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_logradouro VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_numero VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_complemento VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_bairro VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_codigo_municipio INTEGER;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_municipio VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_uf VARCHAR(2);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_cep VARCHAR(8);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS destinatario_email VARCHAR(60);

ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS modalidade_frete INTEGER DEFAULT 9;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_nome VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_documento VARCHAR(20);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_ie VARCHAR(20);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_endereco VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_municipio VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS transportadora_uf VARCHAR(2);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS volume_quantidade NUMERIC(15,4);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS volume_especie VARCHAR(60);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS volume_peso_liquido NUMERIC(15,3);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS volume_peso_bruto NUMERIC(15,3);

ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS finalidade_nfe INTEGER DEFAULT 1;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS tipo_operacao INTEGER DEFAULT 1;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS consumidor_final INTEGER DEFAULT 1;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS presenca_comprador INTEGER DEFAULT 1;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS tipo_emissao INTEGER DEFAULT 1;

ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS recibo_lote VARCHAR(20);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS data_autorizacao TIMESTAMP;
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS codigo_status VARCHAR(10);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS versao_xml VARCHAR(10);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS protocolo_cancelamento VARCHAR(50);
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS data_cancelamento TIMESTAMP;
