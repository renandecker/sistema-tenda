@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Sistema Tenda - Build de Producao

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"
set "PROD_DIR=%ROOT%production"

echo ============================================================
echo    SISTEMA TENDA - PREPARACAO PARA PRODUCAO
echo ============================================================
echo.

echo [1/5] Verificando pre-requisitos (Java, Maven, Node, npm)...
where java >nul 2>&1
if errorlevel 1 (echo [ERRO] Java nao encontrado no PATH. & goto :fail)
where mvn >nul 2>&1
if errorlevel 1 (echo [ERRO] Maven nao encontrado no PATH. & goto :fail)
where node >nul 2>&1
if errorlevel 1 (echo [ERRO] Node.js nao encontrado no PATH. & goto :fail)
where npm >nul 2>&1
if errorlevel 1 (echo [ERRO] npm nao encontrado no PATH. & goto :fail)
echo    Pre-requisitos OK.
echo.

echo [2/5] Compilando Backend (Maven - Package Production)...
pushd "%BACKEND%"
call mvn clean package -DskipTests
set "MVN_ERR=!errorlevel!"
popd
if not "!MVN_ERR!"=="0" (
    echo [ERRO] Falha na compilacao do Maven.
    goto :fail
)
echo    Backend compilado com sucesso.
echo.

echo [3/5] Compilando Frontend (Vite Build)...
pushd "%FRONTEND%"
if not exist "node_modules" (
    echo    Instalando dependencias do frontend...
    call npm install
)
call npm run build
set "NPM_ERR=!errorlevel!"
popd
if not "!NPM_ERR!"=="0" (
    echo [ERRO] Falha no build do frontend.
    goto :fail
)
echo    Frontend compilado com sucesso (pasta dist gerada).
echo.

echo [4/5] Organizando pacote de producao em "%PROD_DIR%"...
if exist "%PROD_DIR%" rmdir /s /q "%PROD_DIR%"
mkdir "%PROD_DIR%"
mkdir "%PROD_DIR%\backend"
mkdir "%PROD_DIR%\frontend"

:: Copiar artefatos do backend (quarkus-app ou jar runner)
if exist "%BACKEND%\pdv\target\quarkus-app" (
    xcopy /e /i /q "%BACKEND%\pdv\target\quarkus-app" "%PROD_DIR%\backend\quarkus-app" >nul
) else if exist "%BACKEND%\pdv\target\sistema-tenda-pdv-1.0.0-SNAPSHOT-runner.jar" (
    copy "%BACKEND%\pdv\target\sistema-tenda-pdv-1.0.0-SNAPSHOT-runner.jar" "%PROD_DIR%\backend\" >nul
)

:: Copiar build do frontend
if exist "%FRONTEND%\dist" (
    xcopy /e /i /q "%FRONTEND%\dist" "%PROD_DIR%\frontend\dist" >nul
)

:: Copiar variaveis de ambiente
if exist "%ROOT%.env" (
    copy "%ROOT%.env" "%PROD_DIR%\.env" >nul
)

echo    Arquivos organizados.
echo.

echo [5/5] Criando script de inicializacao em producao...
(
echo @echo off
echo setlocal
echo title Sistema Tenda - Producao
echo echo Iniciando Sistema Tenda em Modo Producao...
echo cd /d %%~dp0
echo java -jar backend\quarkus-app\quarkus-run.jar
echo pause
) > "%PROD_DIR%\start-prod.bat"

echo ============================================================
echo    SUCESSO! SISTEMA PRONTO PARA PRODUCAO.
echo ============================================================
echo    Pasta de saida: %PROD_DIR%
echo    Para iniciar em producao, execute: %PROD_DIR%\start-prod.bat
echo ============================================================
pause
exit /b 0

:fail
echo.
echo [ERRO] Ocorreu uma falha durante a preparacao para producao.
pause
exit /b 1
