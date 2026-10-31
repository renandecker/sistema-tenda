@echo off
chcp 65001 >nul
rem Gera estrutura basica de pasta producao\ para Sistema Tenda
rem Uso: gerar-producao-simples.bat
rem Cria a estrutura de pastas necessarias sem build nem compilacao

setlocal enabledelayedexpansion
cd /d "%~dp0"

set DEST=producao

echo [1/1] Criando estrutura de pastas basica...

if exist "%DEST%" rmdir /s /q "%DEST%"
mkdir "%DEST%"
mkdir "%DEST%\app"
mkdir "%DEST%\config"
mkdir "%DEST%\logs"
mkdir "%DEST\logs"

echo       Estrutura de pastas criada: %CD%\%DEST%
echo.
echo       Pastas criadas:
echo       - app/      -> Aplicacao Quarkus (jar sera adicionado depois)
echo       - config/   -> Configuracoes de producao (application.properties)
echo       - logs/     -> Diretorio para logs do sistema
echo.
echo       Para completar o pacote de producao, execute o gerar-producao.bat completo.
echo.
echo Para rodar o sistema:
echo   Windows: %CD%\%DEST%\start.bat
echo   Linux/Mac: ja nao aplicavel (estrutura gerada para Windows)

exit /b 0