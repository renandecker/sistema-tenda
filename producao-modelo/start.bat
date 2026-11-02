@echo off
setlocal EnableExtensions DisableDelayedExpansion
title Sistema Tenda - Producao
cd /d "%~dp0"

echo ==================================================
echo           SISTEMA TENDA
echo ==================================================
echo.

rem Carrega as variaveis do arquivo .env, se existir (linhas KEY=VALUE).
if exist ".env" (
  for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do (
    set "%%A=%%B"
  )
)

rem Valores padrao caso o .env nao defina o banco.
if not defined QUARKUS_DATASOURCE_JDBC_URL set "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/sistematenda"
if not defined QUARKUS_DATASOURCE_USERNAME set "QUARKUS_DATASOURCE_USERNAME=sistematenda"
if not defined QUARKUS_DATASOURCE_PASSWORD set "QUARKUS_DATASOURCE_PASSWORD=sistematenda"
if not defined DB_ADMIN_USER set "DB_ADMIN_USER=postgres"

echo Verificando o banco de dados...
where powershell >nul 2>nul
if errorlevel 1 (
  echo       [AVISO] PowerShell nao encontrado; nao foi possivel verificar o banco.
) else (
  if exist "%~dp0ensure-db.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0ensure-db.ps1"
  )
)
echo.

echo Iniciando aplicacao...
java -jar app\quarkus-run.jar
pause
