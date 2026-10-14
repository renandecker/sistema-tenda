#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
FRONTEND="$ROOT/frontend"
PROD_DIR="$ROOT/production"

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

info "[2/5] Compilando Backend (Maven - Package Production)..."
( cd "$BACKEND" && mvn clean package -DskipTests )
info "  Backend compilado com sucesso."
echo

info "[3/5] Compilando Frontend (Vite Build)..."
( 
  cd "$FRONTEND"
  if [ ! -d "node_modules" ]; then
    echo "  Instalando dependencias do frontend..."
    npm install
  fi
  npm run build
)
info "  Frontend compilado com sucesso (pasta dist gerada)."
echo

info "[4/5] Organizando pacote de producao em '$PROD_DIR'..."
rm -rf "$PROD_DIR"
mkdir -p "$PROD_DIR/backend" "$PROD_DIR/frontend"

if [ -d "$BACKEND/pdv/target/quarkus-app" ]; then
  cp -r "$BACKEND/pdv/target/quarkus-app" "$PROD_DIR/backend/"
elif [ -f "$BACKEND/pdv/target/sistema-tenda-pdv-1.0.0-SNAPSHOT-runner.jar" ]; then
  cp "$BACKEND/pdv/target/sistema-tenda-pdv-1.0.0-SNAPSHOT-runner.jar" "$PROD_DIR/backend/"
fi

if [ -d "$FRONTEND/dist" ]; then
  cp -r "$FRONTEND/dist" "$PROD_DIR/frontend/"
fi

info "  Arquivos organizados."
echo

info "[5/5] Criando script de inicializacao em producao..."
cat << 'EOF' > "$PROD_DIR/start-prod.sh"
#!/usr/bin/env bash
set -euo pipefail
echo "Iniciando Sistema Tenda em Modo Producao..."
cd "$(dirname "$0")/backend/quarkus-app"
exec java -jar quarkus-run.jar
EOF
chmod +x "$PROD_DIR/start-prod.sh"

echo "============================================================"
echo "   SUCESSO! SISTEMA PRONTO PARA PRODUCAO."
echo "============================================================"
echo "   Pasta de saida: $PROD_DIR"
echo "   Para iniciar em producao, execute: $PROD_DIR/start-prod.sh"
echo "============================================================"
