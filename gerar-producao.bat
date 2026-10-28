@echo off
chcp 65001 >nul
rem Gera pasta producao\ com Sistema Tenda pronto para rodar em servidor (apenas Java 17+ e PostgreSQL)
rem Uso: gerar-producao.bat [--zip] [--sem-jars] [--com-testes]

setlocal enabledelayedexpansion
cd /d "%~dp0"

set DEST=producao
set ZIP=0
set JARS=1
set TESTES=-DskipTests

:args
if "%~1"=="" goto go
if "%~1"=="--zip" set ZIP=1
if "%~1"=="--sem-jars" set JARS=0
if "%~1"=="--com-testes" set TESTES=
shift
goto args

:go
echo ==================================================
echo      SISTEMA TENDA - GERADOR DE PRODUCAO
echo ==================================================
echo.

echo [1/7] Verificando pre-requisitos...
for %%c in (java node npm) do (
  where %%c >nul 2>nul || (
    echo [ERRO] '%%c' nao encontrado no PATH
    exit /b 1
  )
)
if "%JARS%"=="1" (
  where mvn >nul 2>nul || (
    echo [ERRO] 'mvn' nao encontrado no PATH
    exit /b 1
  )
)
echo       Pre-requisitos OK.
echo.

echo [2/7] Limpando pacote anterior...
if exist "%DEST%" rmdir /s /q "%DEST%"
mkdir "%DEST%"
for %%d in (app config logs) do mkdir "%DEST%\%%d"
echo       OK.
echo.

echo [3/7] Build do Frontend React (Vite)...
pushd frontend
if not exist node_modules (
  echo       Instalando dependencias...
  call npm install --silent
)
call npm run build --silent || (
  popd
  echo [ERRO] Falha no build do frontend
  exit /b 1
)
popd
if not exist "frontend\dist\index.html" (
  echo [ERRO] frontend\dist\index.html nao gerado
  exit /b 1
)
echo       Frontend buildado com sucesso.
echo.

echo [4/7] Integrando frontend ao backend (META-INF/resources)...
set RESOURCES=backend\pdv\src\main\resources\META-INF\resources
if exist "%RESOURCES%" rmdir /s /q "%RESOURCES%"
mkdir "%RESOURCES%"
xcopy /e /i /q frontend\dist\* "%RESOURCES%\" >nul
echo       Arquivos estaticos copiados.
echo.

if "%JARS%"=="1" (
  echo [5/7] Compilando Backend (Maven)...
  pushd backend
  call mvn -q clean package %TESTES% || (
    popd
    echo [ERRO] Falha no build do backend
    exit /b 1
  )
  popd
  echo       Backend compilado com sucesso.
  echo.
) else (
  echo [5/7] Pulando compilacao do backend (--sem-jars)
  echo.
)

echo [6/7] Montando pacote em %DEST%\
if exist "backend\pdv\target\quarkus-app" (
  xcopy /e /i /q "backend\pdv\target\quarkus-app\*" "%DEST%\app\" >nul
) else (
  echo [AVISO] backend\pdv\target\quarkus-app nao encontrado
)

if exist "producao-modelo\*" (
  xcopy /e /i /q "producao-modelo\*" "%DEST%\" >nul
)

if exist "backend\pdv\src\main\resources\application.prod.properties" (
  copy /y "backend\pdv\src\main\resources\application.prod.properties" "%DEST%\config\application.properties" >nul
)

if exist ".env" (
  copy /y ".env" "%DEST%\.env" >nul
)
if exist ".env.example" (
  copy /y ".env.example" "%DEST%\.env.example" >nul
)

if exist "certificado.pfx" (
  copy /y "certificado.pfx" "%DEST%\" >nul
)

echo       Pacote montado.
echo.

echo [7/7] Finalizando...
echo ==================================================
echo      SUCESSO! PACOTE PRONTO
echo ==================================================
echo       Pasta: %CD%\%DEST%
echo       Para rodar: precisa apenas Java 17+ e PostgreSQL
echo ==================================================

if "%ZIP%"=="1" (
  if exist sistema-tenda-producao.zip del sistema-tenda-producao.zip
  tar -a -cf sistema-tenda-producao.zip -C "%DEST%" .
  echo       ZIP criado: %CD%\sistema-tenda-producao.zip
  echo ==================================================
)
