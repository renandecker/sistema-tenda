#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

echo "=================================================="
echo "           SISTEMA TENDA"
echo "=================================================="
echo

# Carrega o .env sem executa-lo como script (linhas KEY=VALUE).
if [ -f .env ]; then
  while IFS= read -r line || [ -n "$line" ]; do
    line=${line%$'\r'}
    case "$line" in
      ''|\#*) continue ;;
    esac
    case "$line" in
      *=*) ;;
      *) continue ;;
    esac
    key=${line%%=*}
    value=${line#*=}
    key=$(printf '%s' "$key" | tr -d '[:space:]')
    [ -n "$key" ] || continue
    case "$value" in
      \"*\") value=${value#\"}; value=${value%\"} ;;
      \'*\') value=${value#\'}; value=${value%\'} ;;
    esac
    export "$key=$value"
  done < .env
fi

: "${QUARKUS_DATASOURCE_JDBC_URL:=jdbc:postgresql://localhost:5432/sistematenda}"
: "${QUARKUS_DATASOURCE_USERNAME:=sistematenda}"
: "${QUARKUS_DATASOURCE_PASSWORD:=sistematenda}"
: "${DB_ADMIN_USER:=postgres}"
export QUARKUS_DATASOURCE_JDBC_URL QUARKUS_DATASOURCE_USERNAME
export QUARKUS_DATASOURCE_PASSWORD DB_ADMIN_USER

is_valid_identifier() {
  case "$1" in
    ''|[0-9]*|*[!A-Za-z0-9_]*) return 1 ;;
    *) return 0 ;;
  esac
}

ensure_database() {
  local url="${QUARKUS_DATASOURCE_JDBC_URL}"
  local user="${QUARKUS_DATASOURCE_USERNAME}"
  local pass="${QUARKUS_DATASOURCE_PASSWORD}"
  local admin_user="${DB_ADMIN_USER:-postgres}"
  local admin_pass="${DB_ADMIN_PASSWORD-}"

  local rest="${url%%\?*}"
  rest="${rest#jdbc:postgresql://}"
  if [ "$rest" = "$url" ]; then
    echo "      [AVISO] URL JDBC nao reconhecida: $url"
    echo "              Verifique QUARKUS_DATASOURCE_JDBC_URL no arquivo .env."
    return 0
  fi

  local hostport="${rest%%/*}"
  local dbname="${rest#*/}"
  if [ "$hostport" = "$rest" ] || [ -z "$dbname" ]; then
    echo "      [AVISO] URL JDBC nao reconhecida: $url"
    echo "              Verifique QUARKUS_DATASOURCE_JDBC_URL no arquivo .env."
    return 0
  fi

  local host="${hostport%%:*}"
  local port="5432"
  if [ "$hostport" != "$host" ]; then
    port="${hostport##*:}"
  fi

  if ! is_valid_identifier "$user" || ! is_valid_identifier "$dbname"; then
    echo "      [AVISO] Usuario/banco com caracteres nao suportados: '$user' / '$dbname'."
    echo "              Crie o banco manualmente; o Flyway cuidara das tabelas."
    return 0
  fi

  echo "      Banco configurado: $dbname em ${host}:${port} (usuario: $user)."

  if ! command -v psql >/dev/null 2>&1; then
    echo "      [AVISO] psql nao encontrado no PATH; nao foi possivel verificar/criar o banco."
    echo "              O banco '$dbname' precisa existir. O Flyway criara as tabelas ao iniciar."
    return 0
  fi

  export PGPASSWORD="$admin_pass"
  export PGCONNECT_TIMEOUT=5
  export PGCLIENTENCODING=UTF8

  cleanup_pg() {
    unset PGPASSWORD PGCONNECT_TIMEOUT PGCLIENTENCODING
  }

  if ! psql -w -h "$host" -p "$port" -U "$admin_user" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
    # No Docker Compose o superusuario costuma ser o proprio usuario da aplicacao.
    if [ "$user" != "$admin_user" ] && \
       PGPASSWORD="$pass" psql -w -h "$host" -p "$port" -U "$user" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
      admin_user="$user"
      admin_pass="$pass"
      export PGPASSWORD="$admin_pass"
      echo "      Usando o usuario da aplicacao '$user' como administrador."
    else
      echo "      [AVISO] Nao foi possivel conectar como administrador '$admin_user' em ${host}:${port}."
      echo "              Ajuste DB_ADMIN_USER / DB_ADMIN_PASSWORD no .env ou crie o banco manualmente."
      echo "              O Flyway criara as tabelas automaticamente se o banco '$dbname' ja existir."
      cleanup_pg
      return 0
    fi
  fi

  local role_exists
  role_exists=$(psql -w -h "$host" -p "$port" -U "$admin_user" -d postgres -tAc \
    "SELECT 1 FROM pg_roles WHERE rolname = '$user'" 2>/dev/null || true)
  if [ "$(printf '%s' "$role_exists" | tr -d '[:space:]')" != "1" ]; then
    local escaped_pass
    escaped_pass=$(printf '%s' "$pass" | sed "s/'/''/g")
    if psql -w -h "$host" -p "$port" -U "$admin_user" -d postgres -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL
CREATE ROLE "$user" WITH LOGIN PASSWORD '$escaped_pass';
SQL
    then
      echo "      Usuario '$user' criado."
    else
      echo "      [AVISO] Nao foi possivel criar o usuario '$user'."
      cleanup_pg
      return 0
    fi
  else
    echo "      Usuario '$user' ja existe."
  fi

  local db_exists
  db_exists=$(psql -w -h "$host" -p "$port" -U "$admin_user" -d postgres -tAc \
    "SELECT 1 FROM pg_database WHERE datname = '$dbname'" 2>/dev/null || true)
  local created=0
  if [ "$(printf '%s' "$db_exists" | tr -d '[:space:]')" != "1" ]; then
    if psql -w -h "$host" -p "$port" -U "$admin_user" -d postgres -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL
CREATE DATABASE "$dbname" OWNER "$user";
SQL
    then
      created=1
      echo "      Banco '$dbname' criado."
    else
      echo "      [AVISO] Nao foi possivel criar o banco '$dbname'."
      echo "              Crie o banco manualmente; o Flyway cuidara das tabelas."
      cleanup_pg
      return 0
    fi
  else
    echo "      Banco '$dbname' ja existe."
  fi

  if [ "$created" -eq 0 ]; then
    psql -w -h "$host" -p "$port" -U "$admin_user" -d postgres -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL || \
      echo "      [AVISO] Nao foi possivel ajustar o proprietario do banco '$dbname'."
ALTER DATABASE "$dbname" OWNER TO "$user";
GRANT ALL PRIVILEGES ON DATABASE "$dbname" TO "$user";
SQL
  fi

  psql -w -h "$host" -p "$port" -U "$admin_user" -d "$dbname" -v ON_ERROR_STOP=1 >/dev/null 2>&1 <<SQL || \
    echo "      [AVISO] Nao foi possivel ajustar permissoes do schema public em '$dbname'."
GRANT ALL ON SCHEMA public TO "$user";
ALTER SCHEMA public OWNER TO "$user";
SQL

  cleanup_pg
  echo "      Banco pronto. O Flyway aplicara as migracoes ao iniciar a aplicacao."
}

echo "Verificando o banco de dados..."
ensure_database
echo

echo "Iniciando aplicacao..."
exec java -jar app/quarkus-run.jar
