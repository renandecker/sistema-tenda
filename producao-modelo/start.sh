#!/usr/bin/env bash
set -euo pipefail

echo "=================================================="
echo "           SISTEMA TENDA"
echo "=================================================="
echo
echo "Iniciando aplicacao..."
echo
cd "$(dirname "$0")"
exec java -jar app/quarkus-run.jar
