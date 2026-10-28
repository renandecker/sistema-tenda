#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
FRONTEND="$ROOT/frontend"
PROD_DIR="$ROOT/production"
RESOURCES_DIR="$BACKEND/pdv/src/main/resources/META-INF/resources"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info(){ printf "${GREEN}%s${NC}\n" "$*"; }
erro(){ printf "${RED}%s${NC}\n" "$*" >&2; }

command_exists(){ command -v "$1" >/dev/null 2>&1; }

echo "============================================================"
echo "   SISTEMA TENDA - PREPARACAO PARA PRODUCAO"
echo "============================================================"
echo

info "[1/5] Verificando pre-requisitos (Java, Maven, Node, npm)..."
for c in java mvn node npm; do
  if ! command_exists "$c"; then
    erro "  [ERRO] '$c' nao encontrado no PATH."
    exit 1
  fi
done
info "  Pre-requisitos OK."
echo

info "[2/5] Compilando Frontend (Vite Build)..."
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

info "[3/5] Integrando Frontend ao Backend (META-INF/resources)..."
mkdir -p "$RESOURCES_DIR"
cp -r "$FRONTEND/dist/." "$RESOURCES_DIR/"
info "  Arquivos estaticos copiados para o backend."
echo

info "[4/5] Compilando Backend (Maven - Package Production)..."
( cd "$BACKEND" && mvn clean package -DskipTests )
info "  Backend compilado com sucesso."
echo

info "[5/5] Organizando pacote de producao em '$PROD_DIR'..."
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
