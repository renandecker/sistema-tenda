-- Initialize admin user
-- Password: 123456 (BCrypt hash with cost 12)
INSERT INTO common_usuario (id, nome, email, senha_hash, cpf_cnpj, tipo_pessoa, ativo, perfis, data_criacao, primeiro_acesso)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@sistematenda.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PZvO.S',
    '00000000000',
    'FISICA',
    true,
    '{"ADMIN","GERENTE","FISCAL","CONTABIL","PDV","ESTOQUE","FRUTEIRA","BUFFET"}',
    NOW(),
    false
) ON CONFLICT (email) DO NOTHING;

-- Initialize default roles/perfis reference (for documentation)
-- ADMIN - Acesso total ao sistema
-- GERENTE - Gestão geral, relatórios, configurações
-- FISCAL - Módulo fiscal, notas, SPED
-- CONTABIL - Contabilidade, DRE, conciliação
-- PDV - Ponto de venda, vendas, caixa
-- ESTOQUE - Controle de estoque, inventário
-- FRUTEIRA - Módulo fruteira, balança, etiquetas
-- BUFFET - Módulo buffet, comandas, receitas