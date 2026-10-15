-- V3__add_buffet_produto_codigo_sequence.sql
-- Add auto-increment sequence for buffet_produto codigo field

-- Create sequence for produto codigo
CREATE SEQUENCE IF NOT EXISTS buffet_produto_codigo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Add default value to codigo column using the sequence
-- First, update existing records to have a numeric codigo if they don't
UPDATE buffet_produto
SET codigo = nextval('buffet_produto_codigo_seq')::varchar
WHERE codigo IS NULL OR codigo = '';

-- Alter the codigo column to have a default value from sequence
ALTER TABLE buffet_produto
    ALTER COLUMN codigo SET DEFAULT nextval('buffet_produto_codigo_seq')::varchar;

-- Make codigo NOT NULL (it already is in entity)
-- Ensure unique constraint exists
ALTER TABLE buffet_produto
    ALTER COLUMN codigo SET NOT NULL;