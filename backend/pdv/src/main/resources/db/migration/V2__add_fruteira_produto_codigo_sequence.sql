-- V2__add_fruteira_produto_codigo_sequence.sql
-- Add auto-increment sequence for fruteira_produto codigo field

-- Create sequence for produto codigo
CREATE SEQUENCE IF NOT EXISTS fruteira_produto_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Add default value to codigo column using the sequence
-- First, update existing records to have a numeric codigo if they don't
UPDATE fruteira_produto
SET codigo = nextval('fruteira_produto_codigo_seq')::varchar
WHERE codigo IS NULL OR codigo = '';

-- Alter the codigo column to have a default value from sequence
ALTER TABLE fruteira_produto
    ALTER COLUMN codigo SET DEFAULT nextval('fruteira_produto_codigo_seq')::varchar;

-- Make codigo NOT NULL (it already is in entity)
-- Ensure unique constraint exists
ALTER TABLE fruteira_produto
    ALTER COLUMN codigo SET NOT NULL;