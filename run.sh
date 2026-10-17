#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
FRONTEND="$ROOT/frontend"
LOG_DIR="$ROOT/.logs"
QUARKUS_VERSION="3.12.0"
DB_PORT="5433"

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

info "[2/4] Verificando PostgreSQL em localhost:${DB_PORT}..."
if port_open localhost "$DB_PORT"; then
  info "  PostgreSQL ativo."
else
  if command_exists docker; then
    if ! docker info >/dev/null 2>&1; then
      echo "  Docker Desktop nao esta rodando. Tentando iniciar..."
      if command_exists open; then open -a Docker >/dev/null 2>&1 || true; fi
      for _i in $(seq 1 90); do
        docker info >/dev/null 2>&1 && break
        sleep 2
      done
    fi
    echo "  Subindo PostgreSQL via Docker Compose..."
    ( cd "$ROOT" && docker compose up -d db ) >/dev/null 2>&1
    if wait_for_port localhost "$DB_PORT" 30; then
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

info "Parando containers de app do compose (mantendo o banco)..."
( cd "$ROOT" && docker compose stop backend frontend ) >/dev/null 2>&1 || true

info "Liberando portas 3000, 8080 e 8081 (caso haja execucao anterior)..."
for port in 3000 8080 8081; do
  if command_exists lsof; then
    lsof -t -i:"$port" 2>/dev/null | while read -r pid; do
      comm="$(ps -p "$pid" -o comm= 2>/dev/null || true)"
      case "$comm" in
        *java*|*node*) kill -9 "$pid" >/dev/null 2>&1 || true ;;
      esac
    done
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
