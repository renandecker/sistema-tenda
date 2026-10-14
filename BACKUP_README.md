# Sistema Tenda - Rotina de Backup

Scripts para criar backup completo do projeto (código fonte + banco de dados) para armazenamento em pendrive ou Google Drive.

## Scripts Disponíveis

| Script | Sistema Operacional |
|--------|---------------------|
| `backup.bat` | Windows |
| `backup.sh` | Linux / macOS |

## O que é incluído no backup

✅ **Código fonte completo:**
- Backend (Java/Quarkus/Maven - todos os módulos)
- Frontend (React/Vite/TypeScript)
- Arquivos de configuração (.properties, pom.xml, package.json, etc.)
- Documentação (README, docs)

✅ **Banco de dados PostgreSQL:**
- Dump completo (`database_dump.sql`) com estrutura e dados
- Inclui `--clean --if-exists` para restauração limpa

❌ **Excluído automaticamente:**
- `node_modules`, `target`, `dist`, `build`
- `.git`, `.idea`, arquivos de IDE
- Arquivos de log, temporários, compilados (.class, .jar)
- Lock files de dependências
- A própria pasta `backup/`

## Como usar

### Windows
```cmd
# Executar direto (duplo clique) ou via terminal:
backup.bat
```

### Linux/macOS
```bash
# Dar permissão de execução (apenas primeira vez)
chmod +x backup.sh

# Executar
./backup.sh
```

## Pré-requisitos para backup do banco

### Windows
- PostgreSQL Client Tools instalado (`pg_dump` no PATH)
- Ou adicione `C:\Program Files\PostgreSQL\16\bin` ao PATH do sistema

### Linux (Ubuntu/Debian)
```bash
sudo apt update && sudo apt install postgresql-client
```

### macOS
```bash
brew install postgresql
```

## Configuração do banco (se necessário)

Edite as variáveis no script se seu banco tiver configurações diferentes:

```bash
# No backup.sh
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="sistematenda"
DB_USER="sistematenda"
```

```bat
:: No backup.bat
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=sistematenda
set DB_USER=sistematenda
```

**Senha do banco:** Configure via variável de ambiente `PGPASSWORD` ou arquivo `.pgpass`:
```bash
# Linux/macOS
export PGPASSWORD="sua_senha"
./backup.sh

# Windows (cmd)
set PGPASSWORD=sua_senha
backup.bat
```

## Resultado

Após execução, será criado em `backup/`:
```
backup/
└── sistema-tenda-backup_20261013_143052.zip
```

Este ZIP contém:
```
sistema-tenda-backup_20261013_143052/
├── sistema-tenda/           # Código fonte completo
├── database_dump.sql        # Dump do PostgreSQL
└── BACKUP_INFO.txt          # Instruções de restauração
```

## Restaurar backup

```bash
# 1. Extrair
unzip sistema-tenda-backup_*.zip

# 2. Restaurar banco (se houver dump)
psql -U sistematenda -d sistematenda -f database_dump.sql

# 3. Configurar credenciais em backend/*/src/main/resources/application.properties

# 4. Reconstruir
# Backend:
cd backend && mvn clean install

# Frontend:
cd frontend && npm install && npm run build
```

## Automatização (opcional)

### Windows - Agendador de Tarefas
1. Abra "Agendador de Tarefas"
2. Criar Tarefa Básica → Diária/Semanal
3. Ação: Iniciar programa → `C:\caminho\sistema-tenda\backup.bat`

### Linux/macOS - Cron
```bash
# Editar crontab
crontab -e

# Backup diário às 02:00
0 2 * * * /caminho/sistema-tenda/backup.sh >> /caminho/sistema-tenda/backup.log 2>&1
```

## Dicas

- **Pendrive:** Copie apenas o `.zip` final para o pendrive
- **Google Drive:** Faça upload do `.zip` ou sincronize a pasta `backup/`
- **Retenção:** Os scripts não apagam backups antigos automaticamente - gerencie manualmente ou adicione lógica de retenção
- **Tamanho:** Backups típicos ficam entre 50-200MB (dependendo do tamanho do banco)