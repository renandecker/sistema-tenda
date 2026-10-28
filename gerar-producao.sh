#!/usr/bin/env bash
# Gera pasta ./producao com Sistema Tenda pronto para rodar em servidor (apenas Java 17+ e PostgreSQL)
# Uso: ./gerar-producao.sh [--zip] [--sem-jars] [--com-testes]

set -euo pipefail
cd "$(dirname "$0")"

DEST="producao"
ZIP=0
JARS=1
TESTES="-DskipTests"

for a in "$@"; do
  case "$a" in
    --zip) ZIP=1 ;;
    --sem-jars) JARS=0 ;;
    --com-testes) TESTES="" ;;
    *) echo "Opção desconhecida: $a"; exit 1 ;;
  esac
done

echo "=================================================="
echo "     SISTEMA TENDA - GERADOR DE PRODUCAO"
echo "=================================================="
echo

echo "[1/7] Verificando pré-requisitos..."
need="java node npm"
[ $JARS -eq 1 ] && need="$need mvn"
for c in $need; do
  command -v "$c" >/dev/null 2>&1 || { echo "[ERRO] '$c' não encontrado no PATH"; exit 1; }
done
echo "      Pré-requisitos OK."
echo

echo "[2/7] Limpando pacote anterior..."
rm -rf "$DEST"
mkdir -p "$DEST"/{app,config,logs}
echo "      OK."
echo

echo "[3/7] Build do Frontend React (Vite)..."
(
  cd frontend
  [ -d node_modules ] || npm install --silent
  npm run build --silent
)
[ -f "frontend/dist/index.html" ] || { echo "[ERRO] frontend/dist/index.html não gerado"; exit 1; }
echo "      Frontend buildado com sucesso."
echo

echo "[4/7] Integrando frontend ao backend (META-INF/resources)..."
RESOURCES="backend/pdv/src/main/resources/META-INF/resources"
rm -rf "$RESOURCES"
mkdir -p "$RESOURCES"
cp -r frontend/dist/. "$RESOURCES/"
echo "      Arquivos estáticos copiados."
echo

if [ $JARS -eq 1 ]; then
  echo "[5/7] Compilando Backend (Maven)..."
  (
    cd backend
    mvn -q clean package $TESTES
  )
  echo "      Backend compilado com sucesso."
  echo
else
  echo "[5/7] Pulando compilação do backend (--sem-jars)"
  echo
fi

echo "[6/7] Montando pacote em $DEST/..."
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
[ -f "certificado.pfx" ] && cp "certificado.pfx" "$DEST/"

chmod +x "$DEST/start.sh" 2>/dev/null || true

echo "      Pacote montado."
echo

echo "[7/7] Finalizando..."
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
