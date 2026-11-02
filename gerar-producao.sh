#!/usr/bin/env bash
# Gera pasta ./producao com Sistema Tenda pronto para rodar em servidor (apenas Java 17+ e PostgreSQL)
# Uso: ./gerar-producao.sh [--zip] [--sem-jars] [--com-testes]

set -euo pipefail
cd "$(dirname "$0")"

DEST="producao"
ZIP=0
JARS=1
TESTES="-DskipTests"
DB_PORT="5432"
# Nome do banco de dados (padrão: sistematenda)
DB_NAME="sistematenda"
# Usuário do banco de dados (padrão: sistematenda)
DB_USER="sistematenda"
# Senha do banco de dados (padrão: sistematenda)
# Se seu PostgreSQL usa outra senha, altere a linha abaixo
DB_PASSWORD="sistematenda"
# Credenciais administrativas usadas apenas para criar o usuario/banco se necessario
DB_ADMIN_USER="postgres"
DB_ADMIN_PASSWORD="sistematenda"
COMPOSE_PROD="docker-compose.prod.yml"

for a in "$@"; do
  case "$a" in
    --zip) ZIP=1 ;;
    --sem-jars) JARS=0 ;;
    --com-testes) TESTES="" ;;
    *) echo "Opção desconhecida: $a"; exit 1 ;;
  esac
done

port_open(){
  if command -v nc >/dev/null 2>&1; then
    nc -z "$1" "$2" >/dev/null 2>&1
  else
    (exec 3<>/dev/tcp/"$1"/$2) >/dev/null 2>&1
  fi
}

ensure_database(){
  if ! command -v psql >/dev/null 2>&1; then
    echo "      [AVISO] psql nao encontrado. Pulando verificacao de banco."
    echo "              O banco '$DB_NAME' sera usado conforme configuracao existente."
    return 0
  fi

  local admin_user="$DB_ADMIN_USER"
  local admin_pass="$DB_ADMIN_PASSWORD"

  export PGPASSWORD="$admin_pass"
  export PGCONNECT_TIMEOUT=5

  if ! psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
    # No Docker Compose o superusuario costuma ser o proprio usuario da aplicacao.
    if [ "$DB_USER" != "$admin_user" ] && \
       PGPASSWORD="$DB_PASSWORD" psql -w -h localhost -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
      admin_user="$DB_USER"
      admin_pass="$DB_PASSWORD"
      export PGPASSWORD="$admin_pass"
      echo "      Usando o usuario da aplicacao '$DB_USER' como administrador."
    else
      echo "      [AVISO] Nao foi possivel conectar como administrador '$admin_user' em localhost:${DB_PORT}."
      echo "              Ajuste DB_ADMIN_USER/DB_ADMIN_PASSWORD ou crie o banco manualmente."
      unset PGPASSWORD PGCONNECT_TIMEOUT
      return 0
    fi
  fi

  # Garante o usuario/role da aplicacao
  local role_exists
  role_exists=$(psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d postgres -tAc \
    "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" 2>/dev/null || true)
  if [ "$(printf '%s' "$role_exists" | tr -d '[:space:]')" != "1" ]; then
    local escaped_pass
    escaped_pass=$(printf '%s' "$DB_PASSWORD" | sed "s/'/''/g")
    if psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d postgres -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL
CREATE ROLE "$DB_USER" WITH LOGIN PASSWORD '$escaped_pass';
SQL
    then
      echo "      Usuario '$DB_USER' criado em localhost:${DB_PORT}."
    else
      echo "      [AVISO] Nao foi possivel criar o usuario '$DB_USER'."
      unset PGPASSWORD PGCONNECT_TIMEOUT
      return 0
    fi
  fi

  # Garante o banco de dados
  local db_exists
  db_exists=$(psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d postgres -tAc \
    "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>/dev/null || true)
  if [ "$(printf '%s' "$db_exists" | tr -d '[:space:]')" = "1" ]; then
    echo "      Banco '$DB_NAME' ja existe em localhost:${DB_PORT}."
  elif psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d postgres -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL
CREATE DATABASE "$DB_NAME" OWNER "$DB_USER";
SQL
  then
    echo "      Banco '$DB_NAME' criado em localhost:${DB_PORT}."
  else
    echo "      [AVISO] Nao foi possivel criar o banco '$DB_NAME' via psql."
    echo "              Verifique manualmente em localhost:${DB_PORT} (usuario: ${DB_USER})."
    unset PGPASSWORD PGCONNECT_TIMEOUT
    return 0
  fi

  # Garante que o usuario da aplicacao possa criar tabelas no schema public
  psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d postgres -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL || true
ALTER DATABASE "$DB_NAME" OWNER TO "$DB_USER";
GRANT ALL PRIVILEGES ON DATABASE "$DB_NAME" TO "$DB_USER";
SQL
  psql -w -h localhost -p "$DB_PORT" -U "$admin_user" -d "$DB_NAME" -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL || true
GRANT ALL ON SCHEMA public TO "$DB_USER";
ALTER SCHEMA public OWNER TO "$DB_USER";
SQL

  unset PGPASSWORD PGCONNECT_TIMEOUT
}

echo "=================================================="
echo "     SISTEMA TENDA - GERADOR DE PRODUCAO"
echo "=================================================="
echo

echo "[1/8] Verificando pré-requisitos..."
need="java node npm"
[ $JARS -eq 1 ] && need="$need mvn"
for c in $need; do
  command -v "$c" >/dev/null 2>&1 || { echo "[ERRO] '$c' não encontrado no PATH"; exit 1; }
done
echo "      Pré-requisitos OK."
echo

echo "[2/8] Garantindo banco PostgreSQL em localhost:${DB_PORT}..."
if port_open localhost "$DB_PORT"; then
  echo "      PostgreSQL já está ativo na porta ${DB_PORT}."
else
  if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRO] PostgreSQL inativo na porta ${DB_PORT} e Docker não encontrado."
    echo "       Suba um PostgreSQL em localhost:${DB_PORT} (db/user/senha = sistematenda) ou instale o Docker."
    exit 1
  fi
  if ! docker info >/dev/null 2>&1; then
    echo "      Docker não está rodando. Tentando iniciar..."
    if command -v open >/dev/null 2>&1; then open -a Docker >/dev/null 2>&1 || true; fi
    for _i in $(seq 1 90); do
      docker info >/dev/null 2>&1 && break
      sleep 2
    done
  fi
  if ! docker info >/dev/null 2>&1; then
    echo "[ERRO] Docker não respondeu. Inicie o Docker e execute novamente."
    exit 1
  fi
  echo "      Criando/subindo container PostgreSQL de produção na porta ${DB_PORT}..."
  if ! docker compose -p sistematenda-prod -f "$COMPOSE_PROD" up -d db; then
    echo "[ERRO] Falha ao subir o container PostgreSQL."
    exit 1
  fi
  echo "      Aguardando o banco ficar pronto..."
  DB_READY=0
  for _i in $(seq 1 60); do
    if port_open localhost "$DB_PORT"; then DB_READY=1; break; fi
    sleep 1
  done
  if [ "$DB_READY" -ne 1 ]; then
    echo "[ERRO] O banco não respondeu na porta ${DB_PORT} após 60s."
    exit 1
  fi
  echo "      PostgreSQL pronto em localhost:${DB_PORT} (banco: sistematenda)."
fi
ensure_database
echo

echo "[3/8] Limpando pacote anterior..."
rm -rf "$DEST"
mkdir -p "$DEST"/{app,config,logs}
echo "      OK."
echo

echo "[4/8] Build do Frontend React (Vite)..."
(
  cd frontend
  [ -d node_modules ] || npm install --silent
  npm run build --silent
)
[ -f "frontend/dist/index.html" ] || { echo "[ERRO] frontend/dist/index.html não gerado"; exit 1; }
echo "      Frontend buildado com sucesso."
echo

echo "[5/8] Integrando frontend ao backend (META-INF/resources)..."
RESOURCES="backend/pdv/src/main/resources/META-INF/resources"
rm -rf "$RESOURCES"
mkdir -p "$RESOURCES"
cp -r frontend/dist/. "$RESOURCES/"
echo "      Arquivos estáticos copiados."
echo

if [ $JARS -eq 1 ]; then
  echo "[6/8] Compilando Backend (Maven)..."
  (
    cd backend
    mvn -q clean package $TESTES
  )
  echo "      Backend compilado com sucesso."
  echo
else
  echo "[6/8] Pulando compilação do backend (--sem-jars)"
  echo
fi

echo "[7/8] Montando pacote em $DEST/..."
if [ -d "backend/pdv/target/quarkus-app" ]; then
  cp -r backend/pdv/target/quarkus-app/. "$DEST/app/"
else
  echo "[AVISO] backend/pdv/target/quarkus-app não encontrado"
fi

if [ -d "producao-modelo" ]; then
  cp -r producao-modelo/. "$DEST/"
fi

if [ -f "backend/pdv/src/main/resources/application.prod.properties" ]; then
  cp "backend/pdv/src/main/resources/application.prod.properties" "$DEST/config/application.properties"
fi

[ -f ".env" ] && cp ".env" "$DEST/.env"
[ -f ".env.example" ] && cp ".env.example" "$DEST/.env.example"
# Add TEF agent configuration to .env if not already present
if [ -f "$DEST/.env" ]; then
  # Check if TEF_PROVIDER is already in .env, if not add default
  if ! grep -q '^TEF_PROVIDER=' "$DEST/.env" 2>/dev/null; then
    cat >> "$DEST/.env" << 'EOF'

# TEF Agent Configuration
# Provider: SITEF|CAPPTA|PAYGO|STONE|LINX|CIELO
# Use STONE for Stone machines, CIELO for Cielo machines
TEF_PROVIDER=STONE  # Provider do TEF (SITEF|CAPPTA|PAYGO|STONE|LINX|CIELO)
TEF_CODIGO_CREDENCIADORA=123456  # Código da credenciadora do provedor escolhido
TEF_TERMINAL_ID=001  # ID do terminal junto ao provedor
EOF
  fi
fi
[ -f "certificado.pfx" ] && cp "certificado.pfx" "$DEST/"

chmod +x "$DEST"/start.sh 2>/dev/null || true

echo "      Pacote montado."
echo

echo "[8/8] Finalizando..."
echo "=================================================="
echo "      SUCESSO! PACOTE PRONTO"
echo "=================================================="
echo "      Pasta: $(pwd)/$DEST"
echo "      Requisitos no servidor: Java 17+ e PostgreSQL"
echo "      (Frontend já embutido - não precisa Node.js)"
echo "=================================================="

if [ $ZIP -eq 1 ]; then
  ZIPFILE="sistema-tenda-producao.zip"
  rm -f "$ZIPFILE"
  (cd "$DEST" && zip -qr "../$ZIPFILE" .)
  echo "      ZIP criado: $(pwd)/$ZIPFILE"
  echo "=================================================="
fi