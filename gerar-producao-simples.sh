#!/usr/bin/env bash
# Gera estrutura basica de pasta ./producao para Sistema Tenda
# Apenas cria pastas, sem build nem compilacao
# Uso: ./gerar-producao-simples.sh

set -euo pipefail
cd "$(dirname "$0")"

DEST="producao"

echo "========================================="
echo "  SISTEMA TENDA - ESTRUTURA BASE"
echo "========================================="
echo

echo "[1/1] Criando estrutura de pastas basica..."

# Remove pasta existente se houver
if [ -d "$DEST" ]; then
  rm -rf "$DEST"
fi

# Cria estrutura de pastas basica
mkdir -p "$DEST"/{app,config,logs}

echo "      Estrutura de pastas criada: $(pwd)/$DEST"
echo
echo "      Pastas criadas:"
echo "      - app/      -> Aplicacao Quarkus (jar sera adicionado depois)"
echo "      - config/   -> Configuracoes de producao"
echo "      - logs/     -> Diretorio para logs do sistema"
echo
echo "      Para completar o pacote de producao, execute ./gerar-producao.sh completo."
echo
echo "Para rodar o sistema no Windows:"
echo "  Inicie o Java com o jar da aplicacao Quarkus no folder app/"

exit 0