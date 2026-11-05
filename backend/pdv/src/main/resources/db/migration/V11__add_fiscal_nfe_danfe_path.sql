-- Caminho do PDF do DANFE armazenado no disco local.
-- O arquivo tambem continua salvo em fiscal_nfe.danfe (BYTEA) como fallback.
ALTER TABLE fiscal_nfe ADD COLUMN IF NOT EXISTS danfe_path VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_fiscal_nfe_cliente_documento
    ON fiscal_nfe (cliente_documento);
