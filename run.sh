#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
FRONTEND="$ROOT/frontend"
LOG_DIR="$ROOT/.logs"
QUARKUS_VERSION="3.12.0"
DB_CONTAINER="sistematenda-postgres"

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

wait_for_port(){
  local host="$1" port="$2" tries="$3" _
  for (( _=0; _<tries; _++ )); do
    if port_open "$host" "$port"; then return 0; fi
    sleep 1
  done
  return 1
}

echo "============================================================"
echo "   SISTEMA TENDA - AMBIENTE LOCAL"
echo "============================================================"
echo

info "[1/4] Verificando pre-requisitos..."
for c in java mvn node npm; do
  if ! command_exists "$c"; then
    erro "  [ERRO] '$c' nao encontrado no PATH."
    exit 1
  fi
done
info "  Java, Maven, Node e npm: OK"
echo

info "[2/4] Verificando PostgreSQL em localhost:5432..."
if port_open localhost 5432; then
  info "  PostgreSQL ativo."
else
  if command_exists docker; then
    echo "  Subindo PostgreSQL via Docker..."
    docker start "$DB_CONTAINER" >/dev/null 2>&1 || \
      docker run -d --name "$DB_CONTAINER" \
        -e POSTGRES_USER=sistematenda -e POSTGRES_PASSWORD=sistematenda \
        -e POSTGRES_DB=sistematenda -p 5432:5432 postgres:16 >/dev/null
    if wait_for_port localhost 5432 30; then
      info "  PostgreSQL pronto."
    else
      warn "  [AVISO] Nao foi possivel confirmar o banco apos 30s."
    fi
  else
    warn "  [AVISO] PostgreSQL inativo e Docker ausente."
    warn "          Inicie o banco manualmente (db/user/senha = sistematenda)."
  fi
fi
echo

info "[3/4] Compilando backend Maven (pode demorar na primeira vez)..."
( cd "$BACKEND" && mvn -q clean install -DskipTests )
info "  Backend compilado."
echo

info "[4/4] Preparando frontend..."
if [ ! -d "$FRONTEND/node_modules" ]; then
  echo "  Instalando dependencias npm..."
  ( cd "$FRONTEND" && npm install )
else
  echo "  node_modules ja existe."
fi
echo

mkdir -p "$LOG_DIR"

info "Liberando portas 3000, 8080 e 8081 (caso haja execucao anterior)..."
for port in 3000 8080 8081; do
  if command_exists fuser; then
    fuser -k "${port}/tcp" >/dev/null 2>&1 || true
  elif command_exists lsof; then
    lsof -t -i:"$port" | xargs -r kill -9 >/dev/null 2>&1 || true
  fi
done

info "Iniciando backend Quarkus (http://localhost:8080)..."
( cd "$BACKEND/pdv" && mvn "io.quarkus:quarkus-maven-plugin:${QUARKUS_VERSION}:dev" ) > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!

cleanup(){
  echo
  echo "Encerrando backend (PID $BACKEND_PID)..."
  kill "$BACKEND_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

echo "  Logs do backend: $LOG_DIR/backend.log"
echo
echo "============================================================"
echo "   Backend : http://localhost:8080  (aguardando...)"
echo "   Swagger : http://localhost:8080/swagger"
echo "   Frontend: http://localhost:3000"
echo "   Ctrl+C para encerrar tudo."
echo "============================================================"
echo

cd "$FRONTEND"
npm run dev
