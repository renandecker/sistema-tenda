-- V4__create_caderno_divida.sql
-- Caderno de dividas (fiado) dos clientes do PDV

CREATE TABLE IF NOT EXISTS pdv_caderno_divida (
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
    CONSTRAINT pdv_caderno_divida_pkey PRIMARY KEY (id),
    CONSTRAINT pdv_caderno_divida_cliente_fk FOREIGN KEY (cliente_id) REFERENCES cadastro_clientes(id)
);

CREATE INDEX IF NOT EXISTS idx_caderno_cliente ON pdv_caderno_divida (cliente_id);
CREATE INDEX IF NOT EXISTS idx_caderno_status ON pdv_caderno_divida (status);
CREATE INDEX IF NOT EXISTS idx_caderno_documento ON pdv_caderno_divida (cliente_documento);
CREATE INDEX IF NOT EXISTS idx_caderno_data ON pdv_caderno_divida (data_lancamento);
