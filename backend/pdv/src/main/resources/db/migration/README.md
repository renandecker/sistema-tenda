# Flyway Database Migrations

Este projeto usa **Flyway 10** (via Quarkus) para gerenciar o schema do
PostgreSQL de forma versionada.

> **Banco suportado:** PostgreSQL 16. O Flyway 10 **não suporta PostgreSQL 10**
> (`Unsupported Database: PostgreSQL 10.x`). Use PostgreSQL 16 em
> desenvolvimento e produção.

## Estrutura

```
backend/pdv/src/main/resources/db/migration/
├── V1__create_initial_schema.sql
├── V2__...
├── V2_1__ensure_produto_tables.sql
├── V3__...
├── V4__...
├── V4_1__ensure_fiscal_nfe_table.sql
├── V5__...
├── V6__...
├── V7__create_estoque_schema.sql      (módulo estoque)
├── V8__ensure_complete_schema.sql
├── V9__add_missing_venda_fields.sql
└── V10__create_estoque_produto.sql    (produtos de estoque/matéria-prima)
```

As migrações dos outros módulos (`estoque`, etc.) também ficam em
`db/migration` e são descobertas pelo Flyway no classpath. A ordem de execução é
definida pela versão, não pela localização do arquivo.

## Como o schema é criado

| Situação do banco | O que o Flyway faz |
|---|---|
| Banco vazio | Executa `V1` até `V10` |
| Banco existente **sem** histórico do Flyway | Cria o histórico na versão 7 (`baseline-version=7`) e executa da `V8` até a `V10` |
| Banco já migrado | Aplica apenas migrações pendentes (validação de checksum desativada) |

A `V8__ensure_complete_schema.sql` é **idempotente**: usa `CREATE TABLE IF NOT
EXISTS`, `ADD COLUMN IF NOT EXISTS`, índices/constraints com guarda e seeds com
`ON CONFLICT`/`NOT EXISTS`. Ela garante todas as tabelas, índices, colunas que
faltavam e os dados iniciais:

- usuário administrador (`admin@sistematenda.com` / `123456`)
- perfis do administrador
- categorias iniciais (somente se a tabela legada `categorias` da V1 existir)

Como bancos existentes recebem baseline na versão 7, a `V8` também replica de
forma idempotente o que as migrations `V2`, `V3`, `V4`, `V5`, `V6` e `V7`
adicionariam, incluindo as colunas de emissão de NF-e/NFC-e e as tabelas de
estoque. Assim, um banco antigo termina com o mesmo schema de uma instalação
nova.

As migrações `V2_1` e `V4_1` existem apenas para garantir tabelas que eram
referenciadas por migrações antigas (`buffet_produto`, `cadastro_clientes` e
`fiscal_nfe`) antes das versões seguintes.

A `V10__create_estoque_produto.sql` cria a tabela `estoque_produto` (cadastro de
matéria-prima/insumos), adiciona `produto_estoque_id` em `estoque_movimento`,
atualiza as constraints de origem/destino/tipo e desliga o controle de estoque
dos produtos do buffet (`controla_estoque = false`).

## Convenção de Nomes

- **Prefixo**: `V` (versioned) ou `R` (repeatable)
- **Versão**: Número sequencial (`1`, `2`, `2_1`, `3`...)
- **Separador**: `__` (dois underscores)
- **Descrição**: Snake_case descritiva
- **Extensão**: `.sql`

Exemplos:
- `V9__add_cliente_telefone_column.sql`
- `R__refresh_materialized_views.sql`

## Como Executar

### Automático (Recomendado)
As migrações rodam automaticamente na inicialização da aplicação:
```properties
quarkus.flyway.migrate-at-start=true
```

### Via Maven
```bash
# Rodar migrações pendentes
mvn flyway:migrate -pl backend/pdv

# Ver status
mvn flyway:info -pl backend/pdv

# Validar migrações
mvn flyway:validate -pl backend/pdv

# Reparar (após erro de checksum)
mvn flyway:repair -pl backend/pdv
```

### Via CLI Flyway (se instalado)
```bash
flyway migrate \
  -url=jdbc:postgresql://localhost:5432/sistematenda \
  -user=sistematenda -password=sistematenda \
  -locations=filesystem:backend/pdv/src/main/resources/db/migration
```

## Boas Práticas

1. **Nunca altere scripts já aplicados** - Crie um novo script para correções.
2. **Uma mudança por script** - Facilita rollback e debug.
3. **Teste localmente** - Rode contra um PostgreSQL 16 limpo e contra um banco
   já migrado antes do commit.
4. **Scripts idempotentes** - Use `IF NOT EXISTS`, `ON CONFLICT DO NOTHING`.
5. **Transações** - O Flyway roda cada script em transação (exceto alguns DDLs
   do PostgreSQL).
6. **Rollback manual** - Crie `V{n+1}__rollback_{descricao}.sql` se necessário.

## Configuração Atual (application.properties)

```properties
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.baseline-on-migrate=true
quarkus.flyway.baseline-version=7
quarkus.flyway.validate-on-migrate=false
quarkus.flyway.clean-disabled=true
quarkus.hibernate-orm.database.generation=none
```

- `baseline-version=7`: ao encontrar um banco não vazio sem histórico, o Flyway
  considera a versão 7 como já aplicada e executa a `V8`.
- `validate-on-migrate=false`: evita que bancos migrados com versões antigas da
  `V8` (o script foi ajustado depois de aplicado em algumas instalações) deixem
  de iniciar por checksum. As migrations são idempotentes; ainda assim, prefira
  criar uma nova versão em vez de editar um script já aplicado.
- `clean-disabled=true`: impede `flyway:clean` de apagar o banco por acidente.
  Use apenas em desenvolvimento, sobrescrevendo explicitamente a configuração.
- `database.generation=none`: o Hibernate **não** altera o schema. Quem manda no
  schema é o Flyway.

## Troubleshooting

### Erro: "Migration checksum mismatch"
```bash
mvn flyway:repair -pl backend/pdv
# Ou corrige o script e recria com nova versão (nunca edite um script já aplicado)
```

### Erro: "Table already exists" / "column already exists"
- Esperado em bancos antigos: a `V8` é idempotente e registra apenas avisos.
- Verifique se `baseline-on-migrate=true` e `baseline-version=7` estão ativos.

### Erro: "Unsupported Database: PostgreSQL 10.x"
- Atualize o PostgreSQL para 16. Um data directory do PG10 não abre no PG16;
  faça `pg_dump` na versão antiga e restaure na nova.

### Erro: "permission denied to create extension" (uuid-ossp)
A `V8` **não cria extensões**: `uuid-ossp` exige superusuário e não é usada por
ela (`gen_random_uuid()` é nativo no PostgreSQL 13+). Se o erro vier de uma
instalação antiga (V1/V7), crie a extensão uma única vez como superusuário:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

Em bancos criados pelo dump antigo isso não é necessário: o `pgcrypto` já vem
instalado e a `V8` não depende de `uuid-ossp`.

### Erro: "Detected failed migration"
```bash
# Veja a versão com falha no log e remova/repare a entrada do histórico
mvn flyway:repair -pl backend/pdv
```

### Banco de desenvolvimento sujo
```bash
# Em desenvolvimento, recrie o banco/volume do Docker e deixe o Flyway rodar
docker compose down -v
docker compose up -d db
```

## Variáveis de Ambiente

Para diferentes ambientes, use profiles:
```properties
# Dev
%dev.quarkus.flyway.migrate-at-start=true

# Test
%test.quarkus.flyway.migrate-at-start=true

# Prod
%prod.quarkus.flyway.migrate-at-start=true
```
