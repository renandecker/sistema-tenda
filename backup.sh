#!/bin/bash

# =============================================================================
# Sistema Tenda - Script de Backup (Linux/macOS)
# Cria backup do código fonte e banco de dados para pendrive/Google Drive
# Uso: ./backup.sh [--skip-db] [--no-zip] [--auto-clean]
# =============================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_BASE="$PROJECT_DIR/backup"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="$BACKUP_BASE/backup_$TIMESTAMP"
ZIP_NAME="sistema-tenda-backup_$TIMESTAMP.zip"

# Parse arguments
SKIP_DB=0
NO_ZIP=0
AUTO_CLEAN=0
for arg in "$@"; do
    case $arg in
        --skip-db) SKIP_DB=1 ;;
        --no-zip) NO_ZIP=1 ;;
        --auto-clean) AUTO_CLEAN=1 ;;
    esac
done

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[AVISO]${NC} $1"; }
log_error() { echo -e "${RED}[ERRO]${NC} $1"; }

echo "============================================================================="
echo "  SISTEMA TENDA - BACKUP"
echo "============================================================================="
log_info "Data/Hora: $(date)"
log_info "Diretório do projeto: $PROJECT_DIR"
log_info "Diretório de backup: $BACKUP_DIR"
echo "============================================================================="

# Criar diretório de backup
mkdir -p "$BACKUP_BASE"
mkdir -p "$BACKUP_DIR"

# -----------------------------------------------------------------------------
# 1. Backup do código fonte (exclui build artifacts, node_modules, etc.)
# -----------------------------------------------------------------------------
log_info "[1/4] Copiando código fonte..."

rsync -av --progress "$PROJECT_DIR/" "$BACKUP_DIR/sistema-tenda/" \
    --exclude='node_modules' \
    --exclude='target' \
    --exclude='.git' \
    --exclude='.idea' \
    --exclude='dist' \
    --exclude='build' \
    --exclude='out' \
    --exclude='bin' \
    --exclude='obj' \
    --exclude='backup' \
    --exclude='*.log' \
    --exclude='*.tmp' \
    --exclude='*.bak' \
    --exclude='*.class' \
    --exclude='*.jar' \
    --exclude='*.war' \
    --exclude='*.ear' \
    --exclude='*.zip' \
    --exclude='*.tar' \
    --exclude='*.gz' \
    --exclude='*.tgz' \
    --exclude='Thumbs.db' \
    --exclude='Desktop.ini' \
    --exclude='package-lock.json' \
    --exclude='yarn.lock' \
    --exclude='pnpm-lock.yaml' \
    --exclude='*.iml' \
    --exclude='*.ipr' \
    --exclude='*.iws' \
    --exclude='.gitignore' \
    --exclude='.gitattributes'

log_success "Código fonte copiado"

# -----------------------------------------------------------------------------
# 2. Backup do banco de dados PostgreSQL
# -----------------------------------------------------------------------------
if [[ $SKIP_DB -eq 1 ]]; then
    log_info "[2/4] Pulando backup do banco de dados (--skip-db)"
else
    log_info "[2/4] Fazendo dump do banco de dados PostgreSQL..."

    DB_HOST="localhost"
    DB_PORT="5432"
    DB_NAME="sistematenda"
    DB_USER="sistematenda"
    DB_DUMP_FILE="$BACKUP_DIR/database_dump.sql"

    if command -v pg_dump &> /dev/null; then
        log_info "Host: $DB_HOST:$DB_PORT"
        log_info "Database: $DB_NAME"
        log_info "Usuário: $DB_USER"
        
        # PGPASSWORD pode ser definido via variável de ambiente ou .pgpass
        if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
            -f "$DB_DUMP_FILE" --no-owner --no-privileges --clean --if-exists 2>&1; then
            log_success "Dump do banco salvo em: $DB_DUMP_FILE"
        else
            log_warn "Falha ao fazer dump do banco. Verifique credenciais/conexão."
            log_warn "O arquivo de dump pode estar incompleto ou vazio."
        fi
    else
        log_warn "pg_dump não encontrado. Pulando backup do banco de dados."
        log_warn "Instale postgresql-client-tools: sudo apt install postgresql-client (Ubuntu/Debian)"
        log_warn "Ou: brew install postgresql (macOS)"
    fi
fi

# -----------------------------------------------------------------------------
# 3. Criar arquivo de informações do backup
# -----------------------------------------------------------------------------
log_info "[3/4] Criando arquivo de informações do backup..."

INFO_FILE="$BACKUP_DIR/BACKUP_INFO.txt"
cat > "$INFO_FILE" << EOF
Sistema Tenda - Informações do Backup
========================================

Data/Hora do backup: $(date)
Diretório original: $PROJECT_DIR
Versão do script: 1.0

Conteúdo do backup:
  - sistema-tenda/     (código fonte completo)
  - database_dump.sql  (dump do PostgreSQL, se disponível)
  - BACKUP_INFO.txt    (este arquivo)

Para restaurar:
  1. Extraia o ZIP no local desejado
  2. Restaure o banco: psql -U sistematenda -d sistematenda -f database_dump.sql
  3. Configure application.properties com suas credenciais
  4. Execute: mvn clean install (backend) e npm install (frontend)
EOF

log_success "Info criada"

# -----------------------------------------------------------------------------
# 4. Compactar tudo em ZIP (opcional)
# -----------------------------------------------------------------------------
if [[ $NO_ZIP -eq 1 ]]; then
    log_info "[4/4] Pulando compactação ZIP (--no-zip)"
else
    log_info "[4/4] Compactando backup em ZIP..."

    cd "$BACKUP_BASE"
    if zip -r "$ZIP_NAME" "backup_$TIMESTAMP" > /dev/null; then
        log_success "ZIP criado: $BACKUP_BASE/$ZIP_NAME"
    else
        log_error "Falha ao criar ZIP!"
        exit 1
    fi
fi

# -----------------------------------------------------------------------------
# Limpeza automática ou interativa
# -----------------------------------------------------------------------------
if [[ $AUTO_CLEAN -eq 1 ]]; then
    if [[ $NO_ZIP -eq 0 ]]; then
        rm -rf "$BACKUP_DIR"
        log_info "Pasta descompactada removida (--auto-clean)."
    fi
else
    echo
    read -p "Deseja remover a pasta descompactada e manter apenas o ZIP? (s/N): " CONFIRM
    if [[ "$CONFIRM" =~ ^[Ss]$ ]]; then
        rm -rf "$BACKUP_DIR"
        log_info "Pasta removida. Apenas o ZIP permanece."
    fi
fi

# -----------------------------------------------------------------------------
# Resumo final
# -----------------------------------------------------------------------------
echo
echo "============================================================================="
echo "  BACKUP CONCLUÍDO COM SUCESSO!"
echo "============================================================================="
if [[ $NO_ZIP -eq 0 ]]; then
    log_info "Arquivo: $BACKUP_BASE/$ZIP_NAME"
    log_info "Tamanho: $(du -h "$BACKUP_BASE/$ZIP_NAME" | cut -f1)"
else
    log_info "Pasta: $BACKUP_DIR"
fi
echo
log_info "Copie este arquivo/pasta para seu pendrive ou Google Drive."
echo "============================================================================="