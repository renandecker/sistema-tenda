# Flyway Database Migrations

Este projeto usa **Flyway** para gerenciar migrações de banco de dados de forma versionada.

## Estrutura

```
backend/pdv/src/main/resources/db/migration/
├── V1__create_initial_schema.sql
├── V2__add_new_table.sql
└── V3__alter_existing_table.sql
```

## Convenção de Nomes

- **Prefixo**: `V` (versioned) ou `R` (repeatable)
- **Versão**: Número sequencial (1, 2, 3...)
- **Separador**: `__` (dois underscores)
- **Descrição**: Snake_case descritiva
- **Extensão**: `.sql`

Exemplos:
- `V1__create_initial_schema.sql`
- `V2__add_cliente_telefone_column.sql`
- `V3__create_index_on_vendas_data.sql`
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

# Limpar banco (CUIDADO: apaga tudo!)
mvn flyway:clean -pl backend/pdv
```

### Via CLI Flyway (se instalado)
```bash
flyway migrate -url=jdbc:postgresql://localhost:5432/sistematenda -user=sistematenda -password=sistematenda -locations=filesystem:backend/pdv/src/main/resources/db/migration
```

## Boas Práticas

1. **Nunca altere scripts já aplicados** - Crie novo script para correções
2. **Uma mudança por script** - Facilita rollback e debug
3. **Teste localmente** - Rode `mvn flyway:migrate` antes de commit
4. **Scripts idempotentes** - Use `IF NOT EXISTS`, `ON CONFLICT DO NOTHING`
5. **Transações** - Flyway roda cada script em transação (exceto PostgreSQL DDL)
6. **Rollback manual** - Crie script `V{n+1}__rollback_{description}.sql` se necessário

## Configuração Atual (application.properties)

```properties
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.baseline-on-migrate=true
quarkus.flyway.baseline-version=0
quarkus.flyway.validate-on-migrate=true
quarkus.hibernate-orm.database.generation=none
```

## Fluxo de Trabalho

1. **Desenvolvedor cria script** na pasta `db/migration`
2. **Testa localmente** com `mvn flyway:migrate`
3. **Commita o script** (não o checksum do flyway_schema_history)
4. **CI/CD roda migrações** no deploy
5. **Produção aplica** automaticamente na inicialização

## Troubleshooting

### Erro: "Migration checksum mismatch"
```bash
mvn flyway:repair -pl backend/pdv
# Ou corrija o script e recrie com nova versão
```

### Erro: "Table already exists"
- Verifique se `baseline-on-migrate=true` está configurado
- Para banco existente, rode `mvn flyway:baseline` primeiro

### Banco de desenvolvimento sujo
```bash
mvn flyway:clean flyway:migrate -pl backend/pdv
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