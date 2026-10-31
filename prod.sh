#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
FRONTEND="$ROOT/frontend"
PROD_DIR="$ROOT/production"
RESOURCES_DIR="$BACKEND/pdv/src/main/resources/META-INF/resources"
DB_PORT="5432"
DB_NAME="sistematenda"
DB_USER="sistematenda"
DB_PASSWORD="sistematenda"
COMPOSE_PROD="$ROOT/docker-compose.prod.yml"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info(){ printf "${GREEN}%s${NC}\n" "$*"; }
warn(){ printf "${YELLOW}%s${NC}\n" "$*"; }
erro(){ printf "${RED}%s${NC}\n" "$*" >&2; }

command_exists(){ command -v "$1" >/dev/null 2>&1; }

port_open(){
  if command_exists nc; then
    nc -z "$1" "$2" >/dev/null 2>&1
  else
    (exec 3<>"/dev/tcp/$1/$2") >/dev/null 2>&1
  fi
}

ensure_database(){
  if ! command_exists psql; then
    return 0
  fi
  if PGPASSWORD="$DB_PASSWORD" psql -h localhost -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc \
      "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>/dev/null | grep -q 1; then
    info "  Banco '$DB_NAME' ja existe em localhost:${DB_PORT}."
  elif PGPASSWORD="$DB_PASSWORD" psql -h localhost -p "$DB_PORT" -U "$DB_USER" -d postgres -c \
      "CREATE DATABASE \"$DB_NAME\"" >/dev/null 2>&1; then
    info "  Banco '$DB_NAME' criado em localhost:${DB_PORT}."
  else
    warn "  [AVISO] Nao foi possivel verificar/criar o banco '$DB_NAME' via psql."
    warn "          Verifique manualmente em localhost:${DB_PORT} (usuario: ${DB_USER})."
  fi
}

echo "============================================================"
echo "   SISTEMA TENDA - PREPARACAO PARA PRODUCAO"
echo "============================================================"
echo

info "[1/6] Verificando pre-requisitos (Java, Maven, Node, npm)..."
for c in java mvn node npm; do
  if ! command_exists "$c"; then
    erro "  [ERRO] '$c' nao encontrado no PATH."
    exit 1
  fi
done
info "  Pre-requisitos OK."
echo

info "[2/6] Garantindo banco PostgreSQL em localhost:${DB_PORT}..."
if port_open localhost "$DB_PORT"; then
  info "  PostgreSQL ja esta ativo na porta ${DB_PORT}."
else
  if ! command_exists docker; then
    erro "  [ERRO] PostgreSQL inativo na porta ${DB_PORT} e Docker nao encontrado."
    erro "         Suba um PostgreSQL em localhost:${DB_PORT} (db/user/senha = sistematenda) ou instale o Docker."
    exit 1
  fi
  if ! docker info >/dev/null 2>&1; then
    echo "  Docker nao esta rodando. Tentando iniciar..."
    if command_exists open; then open -a Docker >/dev/null 2>&1 || true; fi
    for _i in $(seq 1 90); do
      docker info >/dev/null 2>&1 && break
      sleep 2
    done
  fi
  if ! docker info >/dev/null 2>&1; then
    erro "  [ERRO] Docker nao respondeu. Inicie o Docker e execute novamente."
    exit 1
  fi
  echo "  Criando/subindo container PostgreSQL de producao na porta ${DB_PORT}..."
  if ! ( cd "$ROOT" && docker compose -p sistematenda-prod -f "$COMPOSE_PROD" up -d db ); then
    erro "  [ERRO] Falha ao subir o container PostgreSQL."
    exit 1
  fi
  echo "  Aguardando o banco ficar pronto..."
  DB_READY=0
  for _i in $(seq 1 60); do
    if port_open localhost "$DB_PORT"; then DB_READY=1; break; fi
    sleep 1
  done
  if [ "$DB_READY" -ne 1 ]; then
    erro "  [ERRO] O banco nao respondeu na porta ${DB_PORT} apos 60s."
    exit 1
  fi
  info "  PostgreSQL pronto em localhost:${DB_PORT} (banco: sistematenda)."
fi
ensure_database
echo

info "[3/6] Compilando Frontend (Vite Build)..."
( 
  cd "$FRONTEND"
  if [ ! -d "node_modules" ]; then
    echo "  Instalando dependencias do frontend..."
    npm install
  fi
  npm run build
)
info "  Frontend compilado com sucesso."
echo

info "[4/6] Integrando Frontend ao Backend (META-INF/resources)..."
mkdir -p "$RESOURCES_DIR"
cp -r "$FRONTEND/dist/." "$RESOURCES_DIR/"
info "  Arquivos estaticos copiados para o backend."
echo

info "[5/6] Compilando Backend (Maven - Package Production)..."
( cd "$BACKEND" && mvn clean package -DskipTests )
info "  Backend compilado com sucesso."
echo

info "[6/6] Organizando pacote de producao em '$PROD_DIR'..."
rm -rf "$PROD_DIR"
mkdir -p "$PROD_DIR/backend/quarkus-app/config"

if [ -d "$BACKEND/pdv/target/quarkus-app" ]; then
  cp -r "$BACKEND/pdv/target/quarkus-app" "$PROD_DIR/backend/"
fi

if [ -f "$BACKEND/pdv/src/main/resources/application.prod.properties" ]; then
  cp "$BACKEND/pdv/src/main/resources/application.prod.properties" "$PROD_DIR/backend/quarkus-app/config/application.properties"
fi

if [ -f "$ROOT/.env" ]; then
  cp "$ROOT/.env" "$PROD_DIR/.env"
fi

info "  Arquivos organizados."
echo

info "Criando script de inicializacao em producao..."
cat << 'EOF' > "$PROD_DIR/start-prod.sh"
#!/usr/bin/env bash
set -euo pipefail
echo "Iniciando Sistema Tenda em Modo Producao..."
cd "$(dirname "$0")"
exec java -jar backend/quarkus-app/quarkus-run.jar
EOF
chmod +x "$PROD_DIR/start-prod.sh"

echo "============================================================"
echo "   SUCESSO! SISTEMA PRONTO PARA PRODUCAO."
echo "============================================================"
echo "   Pasta de saida: $PROD_DIR"
echo "   Para iniciar em producao, execute: $PROD_DIR/start-prod.sh"
echo "============================================================"
