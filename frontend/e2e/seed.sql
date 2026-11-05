-- =============================================================================
-- Sistema Tenda - Dados falsos para o ambiente local / testes E2E
-- -----------------------------------------------------------------------------
-- Idempotente: pode ser executado quantas vezes forem necessarias. Todos os
-- registros usam IDs deterministicos e ON CONFLICT (id) DO NOTHING.
--
-- Execucao:
--   docker exec -i sistematenda-db psql -U sistematenda -d sistematenda \
--     -v ON_ERROR_STOP=1 -f - < frontend/e2e/seed.sql
--
-- O Playwright executa este arquivo automaticamente em e2e/global-setup.ts.
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- Fruteira - produtos
-- -----------------------------------------------------------------------------
INSERT INTO fruteira_produto (
  id, codigo, nome, descricao, tipo, unidade_medida, preco_venda, custo_medio,
  estoque_atual, estoque_minimo, estoque_maximo, ativo, permite_fracao,
  pesavel_balanca, controla_lote, controla_validade, margem_desejada,
  codigo_barras, codigo_balanca, created_at, updated_at, deleted, version
) VALUES
  ('a0000000-0000-4000-8000-000000000001', 'F001', 'Maçã Gala', 'Maçã gala nacional', 'FRUTA', 'KG', 8.90, 5.10, 42.500, 10.000, 120.000, true, true, true, false, false, 45.00, '7891234001234', '1001', NOW(), NOW(), false, 0),
  ('a0000000-0000-4000-8000-000000000002', 'F002', 'Banana Prata', 'Banana prata madura', 'FRUTA', 'KG', 6.50, 3.20, 65.000, 15.000, 150.000, true, true, true, false, false, 50.00, '7891234001235', '1002', NOW(), NOW(), false, 0),
  ('a0000000-0000-4000-8000-000000000003', 'F003', 'Laranja Pera', 'Laranja pera suco', 'FRUTA', 'KG', 4.20, 2.00, 80.000, 20.000, 200.000, true, true, true, false, false, 40.00, '7891234001236', '1003', NOW(), NOW(), false, 0),
  ('a0000000-0000-4000-8000-000000000004', 'F004', 'Uva Itália', 'Uva itália fresca', 'FRUTA', 'KG', 12.00, 7.80, 18.000, 8.000, 60.000, true, true, true, false, false, 35.00, '7891234001237', '1004', NOW(), NOW(), false, 0),
  ('a0000000-0000-4000-8000-000000000005', 'F005', 'Alface Crespa', 'Alface crespa hidropônica', 'VERDURA', 'UN', 3.50, 1.80, 24.000, 10.000, 80.000, true, false, false, false, true, 48.00, '7891234001238', '1005', NOW(), NOW(), false, 0),
  ('a0000000-0000-4000-8000-000000000006', 'F006', 'Tomate Italiano', 'Tomate italiano', 'LEGUME', 'KG', 7.80, 4.30, 9.500, 12.000, 90.000, true, true, true, false, false, 45.00, '7891234001239', '1006', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Buffet - produtos
-- -----------------------------------------------------------------------------
INSERT INTO buffet_produto (
  id, codigo, nome, descricao, categoria, unidade_medida, preco_venda,
  custo_medio, estoque_atual, estoque_minimo, ativo, disponivel_balcao,
  permite_fracao, controla_estoque, created_at, updated_at, deleted, version
) VALUES
  ('b0000000-0000-4000-8000-000000000001', 'B100001', 'Arroz branco', 'Arroz branco cozido', 'GUARNICAO', 'KG', 24.90, 9.50, 30.000, 5.000, true, true, true, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000002', 'B100002', 'Feijão preto', 'Feijão preto temperado', 'GUARNICAO', 'KG', 26.90, 10.20, 28.000, 5.000, true, true, true, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000003', 'B100003', 'Frango grelhado', 'Filé de frango grelhado', 'PRATO_QUENTE', 'KG', 54.90, 28.00, 12.500, 4.000, true, true, true, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000004', 'B100004', 'Picanha na chapa', 'Picanha fatiada na chapa', 'PRATO_QUENTE', 'KG', 89.90, 52.00, 6.200, 3.000, true, true, true, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000005', 'B100005', 'Salada de alface', 'Alface, tomate e cenoura', 'SALADA', 'KG', 32.90, 12.00, 8.000, 3.000, true, true, true, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000006', 'B100006', 'Pudim de leite', 'Pudim caseiro', 'SOBREMESA', 'KG', 39.90, 15.00, 4.500, 2.000, true, true, true, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000007', 'B100007', 'Suco de laranja', 'Suco natural 500ml', 'BEBIDA', 'UN', 9.90, 3.20, 40.000, 10.000, true, true, false, true, NOW(), NOW(), false, 0),
  ('b0000000-0000-4000-8000-000000000008', 'B100008', 'Refrigerante lata', 'Refrigerante 350ml', 'BEBIDA', 'UN', 7.50, 3.00, 60.000, 12.000, true, true, false, true, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Estoque - materia-prima / insumos
-- -----------------------------------------------------------------------------
INSERT INTO estoque_produto (
  id, codigo, nome, descricao, categoria, unidade_medida, custo_medio,
  estoque_atual, estoque_minimo, estoque_maximo, ativo, created_at, updated_at,
  deleted, version
) VALUES
  ('c0000000-0000-4000-8000-000000000001', 'MP100001', 'Arroz agulhinha', 'Arroz tipo 1 pacote 5kg', 'MATERIA_PRIMA', 'KG', 5.20, 120.000, 30.000, 300.000, true, NOW(), NOW(), false, 0),
  ('c0000000-0000-4000-8000-000000000002', 'MP100002', 'Feijão preto', 'Feijão preto tipo 1', 'MATERIA_PRIMA', 'KG', 7.80, 85.000, 25.000, 200.000, true, NOW(), NOW(), false, 0),
  ('c0000000-0000-4000-8000-000000000003', 'MP100003', 'Óleo de soja', 'Óleo de soja 900ml', 'MATERIA_PRIMA', 'L', 6.10, 18.000, 20.000, 100.000, true, NOW(), NOW(), false, 0),
  ('c0000000-0000-4000-8000-000000000004', 'MP100004', 'Leite integral', 'Leite integral 1L', 'MATERIA_PRIMA', 'L', 4.90, 40.000, 15.000, 120.000, true, NOW(), NOW(), false, 0),
  ('c0000000-0000-4000-8000-000000000005', 'MP100005', 'Açúcar refinado', 'Açúcar refinado 1kg', 'MATERIA_PRIMA', 'KG', 3.60, 55.000, 10.000, 150.000, true, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Clientes (generico, usado pelo PDV/caderno)
-- -----------------------------------------------------------------------------
INSERT INTO clientes (id, nome, documento, email, telefone, cidade, uf, ativo, created_at, updated_at) VALUES
  ('d0000000-0000-4000-8000-000000000001', 'João Silva', '123.456.789-00', 'joao.silva@email.com', '(51) 99999-1001', 'Lajeado', 'RS', true, NOW(), NOW()),
  ('d0000000-0000-4000-8000-000000000002', 'Maria Oliveira', '987.654.321-00', 'maria.oliveira@email.com', '(51) 99999-1002', 'Lajeado', 'RS', true, NOW(), NOW()),
  ('d0000000-0000-4000-8000-000000000003', 'Pedro Santos', '111.222.333-44', 'pedro.santos@email.com', '(51) 99999-1003', 'Estrela', 'RS', true, NOW(), NOW()),
  ('d0000000-0000-4000-8000-000000000004', 'Ana Costa', '444.555.666-77', 'ana.costa@email.com', '(51) 99999-1004', 'Lajeado', 'RS', true, NOW(), NOW()),
  ('d0000000-0000-4000-8000-000000000005', 'Carlos Lima', '555.666.777-88', 'carlos.lima@email.com', '(51) 99999-1005', 'Encantado', 'RS', false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Cadastro de clientes (modulo de cadastro completo)
-- -----------------------------------------------------------------------------
INSERT INTO cadastro_clientes (
  id, nome, cpf_cnpj, email, telefone, tipo_pessoa, tipo_cliente, status,
  limite_credito, pontos_fidelidade, endereco, numero, bairro, cidade, cep,
  data_nascimento, created_at, updated_at, deleted, version
) VALUES
  ('e0000000-0000-4000-8000-000000000001', 'João Silva', '123.456.789-00', 'joao.silva@email.com', '(51) 99999-1001', 'FISICA', 'CLIENTE', 'ATIVO', 500.00, 120, 'Rua das Flores', '100', 'Centro', 'Lajeado', '95900-000', '1985-03-12', NOW(), NOW(), false, 0),
  ('e0000000-0000-4000-8000-000000000002', 'Maria Oliveira', '987.654.321-00', 'maria.oliveira@email.com', '(51) 99999-1002', 'FISICA', 'CLIENTE', 'ATIVO', 800.00, 340, 'Avenida Central', '250', 'São Cristóvão', 'Lajeado', '95900-100', '1990-07-22', NOW(), NOW(), false, 0),
  ('e0000000-0000-4000-8000-000000000003', 'Pedro Santos', '111.222.333-44', 'pedro.santos@email.com', '(51) 99999-1003', 'FISICA', 'CLIENTE', 'ATIVO', 0.00, 45, 'Rua Sete de Setembro', '45', 'Centro', 'Estrela', '95880-000', '1978-11-05', NOW(), NOW(), false, 0),
  ('e0000000-0000-4000-8000-000000000004', 'Ana Costa', '444.555.666-77', 'ana.costa@email.com', '(51) 99999-1004', 'FISICA', 'CLIENTE', 'ATIVO', 300.00, 210, 'Rua Julio de Castilhos', '890', 'Centro', 'Lajeado', '95900-200', '1992-01-30', NOW(), NOW(), false, 0),
  ('e0000000-0000-4000-8000-000000000005', 'Carlos Lima', '555.666.777-88', 'carlos.lima@email.com', '(51) 99999-1005', 'FISICA', 'CLIENTE', 'INATIVO', 0.00, 0, 'Rua Marechal Deodoro', '12', 'Centro', 'Encantado', '95960-000', '1980-09-18', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Buffet - clientes
-- -----------------------------------------------------------------------------
INSERT INTO buffet_cliente (
  id, cpf, nome, telefone, observacoes, total_visitas, total_consumido,
  primeira_visita, ultima_visita, created_at, updated_at, deleted, version
) VALUES
  ('f0000000-0000-4000-8000-000000000001', '123.456.789-00', 'João Silva', '(51) 99999-1001', 'Cliente frequente', 12, 1450.80, NOW() - INTERVAL '180 days', NOW() - INTERVAL '2 days', NOW(), NOW(), false, 0),
  ('f0000000-0000-4000-8000-000000000002', '987.654.321-00', 'Maria Oliveira', '(51) 99999-1002', 'Prefere pratos quentes', 9, 980.50, NOW() - INTERVAL '150 days', NOW() - INTERVAL '5 days', NOW(), NOW(), false, 0),
  ('f0000000-0000-4000-8000-000000000003', '111.222.333-44', 'Pedro Santos', '(51) 99999-1003', NULL, 4, 320.00, NOW() - INTERVAL '90 days', NOW() - INTERVAL '10 days', NOW(), NOW(), false, 0),
  ('f0000000-0000-4000-8000-000000000004', '444.555.666-77', 'Ana Costa', '(51) 99999-1004', 'Alergia a frutos do mar', 15, 2100.35, NOW() - INTERVAL '365 days', NOW() - INTERVAL '1 days', NOW(), NOW(), false, 0),
  ('f0000000-0000-4000-8000-000000000005', '555.666.777-88', 'Carlos Lima', '(51) 99999-1005', 'Inativo desde o ano passado', 1, 49.90, NOW() - INTERVAL '400 days', NOW() - INTERVAL '400 days', NOW(), NOW(), true, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Buffet - mesas
-- -----------------------------------------------------------------------------
INSERT INTO buffet_mesa (
  id, numero, nome, capacidade, area, status, ativa, posicao_x, posicao_y,
  observacoes, created_at, updated_at, deleted, version
) VALUES
  ('10000000-0000-4000-8000-000000000001', '01', 'Mesa da Janela', 4, 'SALAO', 'LIVRE', true, 1, 1, NULL, NOW(), NOW(), false, 0),
  ('10000000-0000-4000-8000-000000000002', '02', 'Mesa Central', 4, 'SALAO', 'OCUPADA', true, 2, 1, NULL, NOW(), NOW(), false, 0),
  ('10000000-0000-4000-8000-000000000003', '03', 'Mesa da Varanda', 6, 'VARANDA', 'RESERVADA', true, 3, 1, 'Reserva para 19h', NOW(), NOW(), false, 0),
  ('10000000-0000-4000-8000-000000000004', '04', 'Mesa Externa', 4, 'EXTERNA', 'LIVRE', true, 1, 2, NULL, NOW(), NOW(), false, 0),
  ('10000000-0000-4000-8000-000000000005', '05', 'Mesa do Balcão', 2, 'BALCAO', 'LIVRE', true, 2, 2, NULL, NOW(), NOW(), false, 0),
  ('10000000-0000-4000-8000-000000000006', '06', 'Mesa Reservada', 8, 'RESERVADO', 'MANUTENCAO', false, 3, 2, 'Cadeira quebrada', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Buffet - comandas e itens
-- -----------------------------------------------------------------------------
INSERT INTO buffet_comanda (
  id, numero, mesa_id, cliente_nome, cliente_documento, cliente_telefone,
  modalidade, status, subtotal, valor_total, valor_pago, preco_kg, aberta_por,
  data_abertura, data_fechamento, data_pagamento, observacoes, created_at,
  updated_at, deleted, version
) VALUES
  ('20000000-0000-4000-8000-000000000001', '9001', '10000000-0000-4000-8000-000000000002', 'João Silva', '123.456.789-00', '(51) 99999-1001', 'POR_PESO', 'ABERTA', 58.40, 58.40, 0.00, 69.90, 'admin', NOW() - INTERVAL '30 minutes', NULL, NULL, 'Mesa do almoço', NOW(), NOW(), false, 0),
  ('20000000-0000-4000-8000-000000000002', '9002', '10000000-0000-4000-8000-000000000003', 'Maria Oliveira', '987.654.321-00', '(51) 99999-1002', 'POR_PESO', 'FECHADA', 87.25, 87.25, 0.00, 69.90, 'admin', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour', NULL, 'Aguardando pagamento', NOW(), NOW(), false, 0),
  ('20000000-0000-4000-8000-000000000003', '9003', NULL, 'Pedro Santos', '111.222.333-44', '(51) 99999-1003', 'PRATO_FEITO', 'PAGA', 34.90, 34.90, 34.90, NULL, 'admin', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NULL, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO buffet_item_comanda (
  id, comanda_id, produto_id, codigo_produto, nome_produto, tipo, quantidade,
  preco_unitario, valor_total, peso_bruto, peso_liquido, lancado_por,
  data_lancamento, created_at, updated_at, deleted, version, cancelado
) VALUES
  ('21000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', 'b0000000-0000-4000-8000-000000000001', 'B100001', 'Arroz branco', 'PRATO_BUFFET', 0.350, 24.90, 8.72, 0.400, 0.350, 'admin', NOW() - INTERVAL '25 minutes', NOW(), NOW(), false, 0, false),
  ('21000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000001', 'b0000000-0000-4000-8000-000000000003', 'B100003', 'Frango grelhado', 'PRATO_BUFFET', 0.400, 54.90, 21.96, 0.450, 0.400, 'admin', NOW() - INTERVAL '24 minutes', NOW(), NOW(), false, 0, false),
  ('21000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000001', 'b0000000-0000-4000-8000-000000000007', 'B100007', 'Suco de laranja', 'BEBIDA', 3.000, 9.90, 29.70, NULL, NULL, 'admin', NOW() - INTERVAL '20 minutes', NOW(), NOW(), false, 0, false),
  ('21000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000002', 'b0000000-0000-4000-8000-000000000004', 'B100004', 'Picanha na chapa', 'PRATO_BUFFET', 0.500, 89.90, 44.95, 0.550, 0.500, 'admin', NOW() - INTERVAL '100 minutes', NOW(), NOW(), false, 0, false),
  ('21000000-0000-4000-8000-000000000005', '20000000-0000-4000-8000-000000000002', 'b0000000-0000-4000-8000-000000000006', 'B100006', 'Pudim de leite', 'SOBREMESA', 0.300, 39.90, 11.97, 0.350, 0.300, 'admin', NOW() - INTERVAL '90 minutes', NOW(), NOW(), false, 0, false),
  ('21000000-0000-4000-8000-000000000006', '20000000-0000-4000-8000-000000000003', 'b0000000-0000-4000-8000-000000000003', 'B100003', 'Frango grelhado', 'PRATO_BUFFET', 0.500, 54.90, 27.45, 0.550, 0.500, 'admin', NOW() - INTERVAL '1 day', NOW(), NOW(), false, 0, false)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Buffet - receitas / fichas tecnicas
-- -----------------------------------------------------------------------------
INSERT INTO buffet_receita (
  id, produto_id, rendimento, unidade_rendimento, versao, ativa, instrucoes,
  dificuldade, observacoes, created_at, updated_at, deleted, version
) VALUES
  ('22000000-0000-4000-8000-000000000001', 'b0000000-0000-4000-8000-000000000001', 10.000, 'KG', 1, true, 'Cozinhe o arroz e tempere.', 'FACIL', 'Ficha do arroz branco', NOW(), NOW(), false, 0),
  ('22000000-0000-4000-8000-000000000002', 'b0000000-0000-4000-8000-000000000003', 8.000, 'KG', 1, true, 'Grelhe o frango e tempere.', 'MEDIA', 'Ficha do frango grelhado', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO buffet_receita_ingrediente (
  id, receita_id, produto_id, quantidade, unidade, custo_unitario,
  custo_total, obrigatorio, ordem, observacoes, created_at, updated_at,
  deleted, version
) VALUES
  ('23000000-0000-4000-8000-000000000001', '22000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 5.000, 'KG', 5.10, 25.50, true, 1, NULL, NOW(), NOW(), false, 0),
  ('23000000-0000-4000-8000-000000000002', '22000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000003', 0.200, 'KG', 2.00, 0.40, true, 2, NULL, NOW(), NOW(), false, 0),
  ('23000000-0000-4000-8000-000000000003', '22000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000001', 0.300, 'KG', 5.10, 1.53, true, 1, NULL, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Buffet - transferencias de estoque
-- -----------------------------------------------------------------------------
INSERT INTO buffet_transferencia_estoque (
  id, produto_id, quantidade, custo_unitario, valor_total, data, origem,
  destino, status, solicitado_por, aprovado_por, recebido_por, observacoes,
  created_at, updated_at, deleted, version
) VALUES
  ('24000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 10.000, 5.10, 51.00, NOW() - INTERVAL '3 hours', 'FRUTEIRA', 'BUFFET', 'PENDENTE', 'admin', NULL, NULL, 'Transferência de maçã', NOW(), NOW(), false, 0),
  ('24000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', 20.000, 3.20, 64.00, NOW() - INTERVAL '1 day', 'FRUTEIRA', 'BUFFET', 'CONCLUIDA', 'admin', 'admin', 'admin', 'Transferência de banana', NOW(), NOW(), false, 0),
  ('24000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000003', 15.000, 2.00, 30.00, NOW() - INTERVAL '2 days', 'FRUTEIRA', 'BUFFET', 'APROVADA', 'admin', 'admin', NULL, 'Transferência de laranja', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Fruteira - perdas
-- -----------------------------------------------------------------------------
INSERT INTO fruteira_perda (
  id, produto_id, data, hora, tipo, quantidade, custo_unitario, valor_total,
  baixa_estoque_realizada, motivo, responsavel, lote, observacoes, created_at,
  updated_at, deleted, version
) VALUES
  ('30000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', CURRENT_DATE - 1, NOW() - INTERVAL '1 day', 'DETERIORACAO', 2.500, 5.10, 12.75, true, 'Fruta machucada no transporte', 'admin', 'LOTE-MACA-01', NULL, NOW(), NOW(), false, 0),
  ('30000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', CURRENT_DATE - 2, NOW() - INTERVAL '2 days', 'AVARIA', 1.200, 3.20, 3.84, true, 'Cacho danificado', 'admin', 'LOTE-BANANA-02', NULL, NOW(), NOW(), false, 0),
  ('30000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000004', CURRENT_DATE - 3, NOW() - INTERVAL '3 days', 'VENCIMENTO', 0.800, 7.80, 6.24, false, 'Uva passou do ponto', 'admin', 'LOTE-UVA-03', NULL, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Fruteira - etiquetas de balanca
-- -----------------------------------------------------------------------------
INSERT INTO fruteira_etiqueta_balanca (
  id, codigo, produto_id, preco, validade_dias, data_validade, tara,
  imprimir_codigo_barras, imprimir_qr_code, modelo_etiqueta, ativa,
  quantidade_impressa, ultima_impressao, created_at, updated_at, deleted, version
) VALUES
  ('31000000-0000-4000-8000-000000000001', 'ETQ-001', 'a0000000-0000-4000-8000-000000000001', 8.90, 7, CURRENT_DATE + 7, 0.010, true, true, 'PADRAO', true, 120, NOW() - INTERVAL '1 hour', NOW(), NOW(), false, 0),
  ('31000000-0000-4000-8000-000000000002', 'ETQ-002', 'a0000000-0000-4000-8000-000000000002', 6.50, 5, CURRENT_DATE + 5, 0.010, true, false, 'PADRAO', true, 80, NOW() - INTERVAL '2 hours', NOW(), NOW(), false, 0),
  ('31000000-0000-4000-8000-000000000003', 'ETQ-003', 'a0000000-0000-4000-8000-000000000004', 12.00, 4, CURRENT_DATE + 4, 0.020, true, false, 'PADRAO', true, 40, NOW() - INTERVAL '3 days', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Fruteira - tabelas de preco
-- -----------------------------------------------------------------------------
INSERT INTO fruteira_tabela_preco (
  id, produto_id, preco_venda, preco_atacado, quantidade_minima_atacado,
  data_inicio, data_fim, origem, ativa, aplicada_em, aplicada_por,
  observacoes, created_at, updated_at, deleted, version
) VALUES
  ('32000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 8.90, 7.90, 10, CURRENT_DATE - 10, CURRENT_DATE + 20, 'MANUAL', true, NOW() - INTERVAL '10 days', 'admin', 'Tabela vigente maçã', NOW(), NOW(), false, 0),
  ('32000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', 6.50, 5.80, 12, CURRENT_DATE - 5, CURRENT_DATE + 30, 'PROMOCAO', true, NOW() - INTERVAL '5 days', 'admin', 'Promoção banana', NOW(), NOW(), false, 0),
  ('32000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000004', 12.00, NULL, NULL, CURRENT_DATE - 2, CURRENT_DATE + 15, 'MANUAL', true, NOW() - INTERVAL '2 days', 'admin', 'Tabela uva', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Estoque - movimentos e alertas
-- -----------------------------------------------------------------------------
INSERT INTO estoque_movimento (
  id, produto_fruteira_id, produto_buffet_id, produto_estoque_id, tipo, origem,
  destino, quantidade, custo_unitario, valor_total, estoque_anterior,
  estoque_posterior, motivo_defeito, tipo_defeito, responsavel, data,
  observacoes, created_at, updated_at, deleted, version
) VALUES
  ('40000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', NULL, NULL, 'SAIDA_DEFEITO_AVARIA', 'FRUTEIRA', 'DEFEITO', 1.500, 5.10, 7.65, 44.000, 42.500, 'Maçã amassada', 'AVARIA', 'admin', NOW() - INTERVAL '1 day', 'Defeito registrado no recebimento', NOW(), NOW(), false, 0),
  ('40000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', NULL, NULL, 'SAIDA_DEFEITO_DETERIORACAO', 'FRUTEIRA', 'DEFEITO', 0.800, 3.20, 2.56, 65.800, 65.000, 'Banana passada', 'DETERIORACAO', 'admin', NOW() - INTERVAL '2 days', NULL, NOW(), NOW(), false, 0),
  ('40000000-0000-4000-8000-000000000003', NULL, NULL, 'c0000000-0000-4000-8000-000000000003', 'ENTRADA_COMPRA', 'FORNECEDOR', 'ESTOQUE', 24.000, 6.10, 146.40, -6.000, 18.000, NULL, NULL, 'admin', NOW() - INTERVAL '3 days', 'Compra de óleo', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO estoque_alerta (
  id, produto_fruteira_id, produto_buffet_id, tipo, status, prioridade,
  mensagem, valor_referencia, valor_atual, data_vencimento,
  dias_para_vencimento, data_alerta, created_at, updated_at, deleted, version
) VALUES
  ('41000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000006', NULL, 'ESTOQUE_MINIMO', 'ATIVO', 'ALTA', 'Tomate abaixo do estoque mínimo', 12.000, 9.500, NULL, NULL, NOW() - INTERVAL '1 hour', NOW(), NOW(), false, 0),
  ('41000000-0000-4000-8000-000000000002', NULL, NULL, 'VENCIMENTO_PROXIMO', 'ATIVO', 'MEDIA', 'Produto próximo do vencimento', NULL, NULL, CURRENT_DATE + 2, 2, NOW() - INTERVAL '2 hours', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- PDV - vendas, itens e sessoes de caixa
-- -----------------------------------------------------------------------------
INSERT INTO pdv_venda (
  id, numero, pdv_id, status, subtotal, subtotal_buffet, subtotal_fruteira,
  desconto, acrescimo, valor_total, valor_pago, troco, operador, cliente_nome,
  data, data_finalizacao, observacoes, created_at, updated_at, deleted, version
) VALUES
  ('50000000-0000-4000-8000-000000000001', '000101', (SELECT id FROM pdv_terminal LIMIT 1), 'PAGA', 42.70, 29.70, 13.00, 0.00, 0.00, 42.70, 50.00, 7.30, 'admin', 'João Silva', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NULL, NOW(), NOW(), false, 0),
  ('50000000-0000-4000-8000-000000000002', '000102', (SELECT id FROM pdv_terminal LIMIT 1), 'PAGA', 88.80, 44.95, 43.85, 0.00, 0.00, 88.80, 100.00, 11.20, 'admin', 'Maria Oliveira', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NULL, NOW(), NOW(), false, 0),
  ('50000000-0000-4000-8000-000000000003', '000103', (SELECT id FROM pdv_terminal LIMIT 1), 'FINALIZADA', 34.90, 34.90, 0.00, 0.00, 0.00, 34.90, 34.90, 0.00, 'admin', 'Pedro Santos', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NULL, NOW(), NOW(), false, 0),
  ('50000000-0000-4000-8000-000000000004', '000104', (SELECT id FROM pdv_terminal LIMIT 1), 'ABERTA', 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'admin', 'Consumidor', NOW(), NULL, 'Venda em andamento', NOW(), NOW(), false, 0),
  ('50000000-0000-4000-8000-000000000005', '000105', (SELECT id FROM pdv_terminal LIMIT 1), 'CANCELADA', 15.60, 0.00, 15.60, 0.00, 0.00, 15.60, 0.00, 0.00, 'admin', 'Ana Costa', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'Cancelada pelo cliente', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO pdv_item_venda (
  id, venda_id, produto_fruteira_id, produto_buffet_id, origem, tipo,
  codigo_produto, nome_produto, quantidade, preco_unitario, valor_total,
  created_at, updated_at, deleted, version, cancelado
) VALUES
  ('51000000-0000-4000-8000-000000000001', '50000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', NULL, 'FRUTEIRA', 'PRODUTO', 'F001', 'Maçã Gala', 1.000, 8.90, 8.90, NOW(), NOW(), false, 0, false),
  ('51000000-0000-4000-8000-000000000002', '50000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000003', NULL, 'FRUTEIRA', 'PRODUTO', 'F003', 'Laranja Pera', 1.000, 4.10, 4.10, NOW(), NOW(), false, 0, false),
  ('51000000-0000-4000-8000-000000000003', '50000000-0000-4000-8000-000000000001', NULL, 'b0000000-0000-4000-8000-000000000007', 'BUFFET', 'BEBIDA', 'B100007', 'Suco de laranja', 3.000, 9.90, 29.70, NOW(), NOW(), false, 0, false),
  ('51000000-0000-4000-8000-000000000004', '50000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000004', NULL, 'FRUTEIRA', 'PRODUTO', 'F004', 'Uva Itália', 1.500, 12.00, 18.00, NOW(), NOW(), false, 0, false),
  ('51000000-0000-4000-8000-000000000005', '50000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', NULL, 'FRUTEIRA', 'PRODUTO', 'F002', 'Banana Prata', 2.000, 6.50, 13.00, NOW(), NOW(), false, 0, false),
  ('51000000-0000-4000-8000-000000000006', '50000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000005', NULL, 'FRUTEIRA', 'PRODUTO', 'F005', 'Alface Crespa', 1.000, 3.50, 3.50, NOW(), NOW(), false, 0, false),
  ('51000000-0000-4000-8000-000000000007', '50000000-0000-4000-8000-000000000002', NULL, 'b0000000-0000-4000-8000-000000000004', 'BUFFET', 'PRATO_BUFFET', 'B100004', 'Picanha na chapa', 0.500, 89.90, 44.95, NOW(), NOW(), false, 0, false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO pdv_sessao_caixa (
  id, pdv_id, status, operador_abertura, operador_fechamento, valor_inicial,
  total_vendas, vendas_dinheiro, vendas_cartao, vendas_pix, vendas_vale,
  diferenca, valor_final_sistema, valor_final_contado, data_abertura,
  data_fechamento, observacoes_abertura, observacoes_fechamento, created_at,
  updated_at, deleted, version
) VALUES
  ('52000000-0000-4000-8000-000000000001', (SELECT id FROM pdv_terminal LIMIT 1), 'FECHADA', 'admin', 'admin', 200.00, 450.00, 250.00, 120.00, 80.00, 0.00, 0.00, 650.00, 650.00, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', 'Abertura do turno', 'Fechamento sem diferença', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- PDV - caderno de dividas (fiado)
-- -----------------------------------------------------------------------------
INSERT INTO pdv_caderno_divida (
  id, cliente_id, cliente_nome, cliente_documento, cliente_telefone,
  descricao, valor_total, valor_pago, status, data_lancamento, data_vencimento,
  operador, observacoes, created_at, updated_at, deleted, version
) VALUES
  ('54000000-0000-4000-8000-000000000001', 'e0000000-0000-4000-8000-000000000001', 'João Silva', '123.456.789-00', '(51) 99999-1001', 'Compra no fiado - 10/2026', 120.00, 20.00, 'PARCIAL', NOW() - INTERVAL '10 days', CURRENT_DATE + 10, 'admin', NULL, NOW(), NOW(), false, 0),
  ('54000000-0000-4000-8000-000000000002', 'e0000000-0000-4000-8000-000000000002', 'Maria Oliveira', '987.654.321-00', '(51) 99999-1002', 'Compra no fiado - 09/2026', 85.50, 0.00, 'ABERTA', NOW() - INTERVAL '35 days', CURRENT_DATE + 5, 'admin', NULL, NOW(), NOW(), false, 0),
  ('54000000-0000-4000-8000-000000000003', 'e0000000-0000-4000-8000-000000000003', 'Pedro Santos', '111.222.333-44', '(51) 99999-1003', 'Compra quitada', 60.00, 60.00, 'QUITADA', NOW() - INTERVAL '60 days', CURRENT_DATE - 30, 'admin', NULL, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Fiscal - ativos imobilizados
-- -----------------------------------------------------------------------------
INSERT INTO fiscal_ativo_imobilizado (
  id, codigo, descricao, tipo, unidade_negocio, status, data_aquisicao,
  valor_aquisicao, vida_util_meses, valor_residual, taxa_depreciacao_anual,
  depreciacao_acumulada, valor_liquido, data_inicio_depreciacao,
  data_fim_depreciacao, fornecedor, nota_fiscal, localizacao, responsavel,
  observacoes, created_at, updated_at, deleted, version
) VALUES
  ('60000000-0000-4000-8000-000000000001', 'ATV-001', 'Balcão refrigerado 2m', 'CAMARAS_FRIAS', 'FRUTEIRA', 'ATIVO', CURRENT_DATE - 400, 8500.00, 120, 850.00, 10.00, 1416.67, 7083.33, CURRENT_DATE - 400, CURRENT_DATE + 3250, 'Refrigeração Sul', 'NF-12345', 'Loja - Fruteira', 'admin', NULL, NOW(), NOW(), false, 0),
  ('60000000-0000-4000-8000-000000000002', 'ATV-002', 'Caixa registradora PDV', 'EQUIPAMENTOS_INFORMATICA', 'ADMINISTRATIVO', 'ATIVO', CURRENT_DATE - 200, 3200.00, 60, 320.00, 20.00, 640.00, 2560.00, CURRENT_DATE - 200, CURRENT_DATE + 1625, 'Info Store', 'NF-54321', 'Loja - Caixa', 'admin', NULL, NOW(), NOW(), false, 0),
  ('60000000-0000-4000-8000-000000000003', 'ATV-003', 'Balança eletrônica 30kg', 'BALANCAS', 'FRUTEIRA', 'BAIXADO', CURRENT_DATE - 800, 1500.00, 60, 150.00, 20.00, 1100.00, 400.00, CURRENT_DATE - 800, CURRENT_DATE + 1000, 'Toledo', 'NF-99999', 'Depósito', 'admin', 'Baixada por desgaste', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Fiscal - NFC-e / NF-e
-- -----------------------------------------------------------------------------
INSERT INTO fiscal_nfe (
  id, chave_acesso, numero, serie, modelo, status, data_emissao,
  data_saida_entrada, natureza_operacao, cliente_nome, cliente_documento,
  valor_total, valor_icms, valor_pis, valor_cofins, valor_desconto,
  valor_acrescimo, valor_troco, protocolo_autorizacao, data_autorizacao,
  codigo_status, ambiente, created_at, updated_at, deleted, version
) VALUES
  ('61000000-0000-4000-8000-000000000001', '43261119393430000149650010000001011000001018', 101, 1, '65', 'AUTORIZADA', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', 'Venda de mercadorias', 'João Silva', '123.456.789-00', 42.70, 7.69, 0.70, 3.24, 0.00, 0.00, 7.30, '143261000000101', NOW() - INTERVAL '2 hours', '100', 'HOMOLOGACAO', NOW(), NOW(), false, 0),
  ('61000000-0000-4000-8000-000000000002', '43261119393430000149650010000001021000001024', 102, 1, '65', 'AUTORIZADA', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 'Venda de mercadorias', 'Maria Oliveira', '987.654.321-00', 88.80, 15.98, 1.47, 6.75, 0.00, 0.00, 11.20, '143261000000102', NOW() - INTERVAL '1 day', '100', 'HOMOLOGACAO', NOW(), NOW(), false, 0),
  ('61000000-0000-4000-8000-000000000003', '43261119393430000149650010000001031000001030', 103, 1, '65', 'CANCELADA', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'Venda de mercadorias', 'Pedro Santos', '111.222.333-44', 34.90, 6.28, 0.58, 2.65, 0.00, 0.00, 0.00, '143261000000103', NOW() - INTERVAL '3 days', '100', 'HOMOLOGACAO', NOW(), NOW(), false, 0),
  ('61000000-0000-4000-8000-000000000004', '43261119393430000149550010000001041000001046', 104, 1, '55', 'PENDENTE', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour', 'Venda de mercadorias', 'Ana Costa', '444.555.666-77', 15.60, 2.81, 0.26, 1.19, 0.00, 0.00, 0.00, NULL, NULL, NULL, 'HOMOLOGACAO', NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Fiscal - conciliacao de cartao
-- -----------------------------------------------------------------------------
INSERT INTO fiscal_conciliacao_cartao (
  id, data_venda, data_recebimento, valor_bruto, valor_taxa, valor_liquido,
  valor_recebido, diferenca, taxa_mdr, taxa_adesao, bandeira, adquirente,
  nsu, codigo_autorizacao, numero_venda, status, data_conciliacao,
  conciliado_por, observacoes, created_at, updated_at, deleted, version
) VALUES
  ('62000000-0000-4000-8000-000000000001', CURRENT_DATE - 1, CURRENT_DATE, 120.00, 3.60, 116.40, 116.40, 0.00, 3.00, 0.00, 'VISA', 'CIELO', '123456', 'AUTH001', '000101', 'CONCILIADO', NOW(), 'admin', NULL, NOW(), NOW(), false, 0),
  ('62000000-0000-4000-8000-000000000002', CURRENT_DATE - 2, CURRENT_DATE, 250.00, 7.50, 242.50, 240.00, 2.50, 3.00, 0.00, 'MASTERCARD', 'STONE', '654321', 'AUTH002', '000102', 'DIVERGENTE', NULL, NULL, 'Diferença de R$ 2,50', NOW(), NOW(), false, 0),
  ('62000000-0000-4000-8000-000000000003', CURRENT_DATE - 3, NULL, 80.00, 2.40, 77.60, NULL, NULL, 3.00, 0.00, 'ELO', 'CIELO', '987654', 'AUTH003', '000103', 'PENDENTE', NULL, NULL, NULL, NOW(), NOW(), false, 0)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Auditoria - registros de exemplo
-- -----------------------------------------------------------------------------
INSERT INTO common_audit_log (
  id, usuario_id, acao, entidade, registro_id, descricao, ip_address,
  user_agent, sucesso, data_acao
) VALUES
  ('70000000-0000-4000-8000-000000000001', (SELECT id FROM common_usuario WHERE email = 'admin@sistematenda.com'), 'LOGIN', 'common_usuario', (SELECT id FROM common_usuario WHERE email = 'admin@sistematenda.com'), 'Login realizado com sucesso', '127.0.0.1', 'Playwright', true, NOW() - INTERVAL '1 hour'),
  ('70000000-0000-4000-8000-000000000002', (SELECT id FROM common_usuario WHERE email = 'admin@sistematenda.com'), 'CRIAR', 'pdv_venda', '50000000-0000-4000-8000-000000000001', 'Venda 000101 registrada', '127.0.0.1', 'Playwright', true, NOW() - INTERVAL '2 hours'),
  ('70000000-0000-4000-8000-000000000003', (SELECT id FROM common_usuario WHERE email = 'admin@sistematenda.com'), 'EDITAR', 'fruteira_produto', 'a0000000-0000-4000-8000-000000000001', 'Preço da maçã atualizado', '127.0.0.1', 'Playwright', true, NOW() - INTERVAL '3 hours')
ON CONFLICT (id) DO NOTHING;

COMMIT;
