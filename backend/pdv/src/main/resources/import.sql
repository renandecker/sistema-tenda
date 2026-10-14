-- Initialize admin user
-- Password: 123456 (BCrypt hash with cost 12)
INSERT INTO common_usuario (id, nome, email, senha_hash, cpf_cnpj, tipo_pessoa, ativo, data_criacao, primeiro_acesso)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@sistematenda.com',
    '$2a$12$IF3P6uOfOWu.EufU8gihIOMkAYF3sY5YvtYxptjvGcvMXRR4FDEeK',
    '00000000000',
    'FISICA',
    true,
    NOW(),
    false
) ON CONFLICT (email) DO NOTHING;

-- Perfis do admin (ElementCollection -> common_usuario_perfil)
INSERT INTO common_usuario_perfil (usuario_id, perfil)
SELECT u.id, t.perfil
FROM common_usuario u
CROSS JOIN (VALUES
    ('ADMIN'), ('GERENTE'), ('FISCAL'), ('CONTABIL'),
    ('PDV'), ('ESTOQUE'), ('FRUTEIRA'), ('BUFFET')
) AS t(perfil)
WHERE u.email = 'admin@sistematenda.com';

-- Initialize default roles/perfis reference (for documentation)
-- ADMIN - Acesso total ao sistema
-- GERENTE - Gestao geral, relatorios, configuracoes
-- FISCAL - Modulo fiscal, notas, SPED
-- CONTABIL - Contabilidade, DRE, conciliacao
-- PDV - Ponto de venda, vendas, caixa
-- ESTOQUE - Controle de estoque, inventario
-- FRUTEIRA - Modulo fruteira, balanca, etiquetas
-- BUFFET - Modulo buffet, comandas, receitas
