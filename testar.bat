@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Sistema Tenda - Testes E2E (Playwright)

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"
set "LOG_DIR=%ROOT%.logs"
set "DB_PORT=5433"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=3000"
set "QUARKUS_VERSION=3.12.0"

echo ============================================================
echo    SISTEMA TENDA - TESTES E2E (Playwright)
echo ============================================================
echo.
echo   Modo de execucao:
echo     1) Completo   - sobe banco/backend e roda toda a suite
echo     2) Rapido     - assume backend/banco ativos e roda a suite
echo     3) Interface  - roda em modo headed (navegador visivel)
echo     4) Relatorio  - abre o ultimo relatorio HTML
echo     5) Sair
echo.
set "MODO=1"
set /p "MODO=Escolha uma opcao [1-5] (Enter = 1): "
if "%MODO%"=="" set "MODO=1"

if "%MODO%"=="5" exit /b 0
if "%MODO%"=="4" goto :relatorio

echo.
echo [1/5] Verificando pre-requisitos...
where node >nul 2>&1
if errorlevel 1 (echo    [ERRO] Node.js nao encontrado no PATH. & goto :fail)
where npm >nul 2>&1
if errorlevel 1 (echo    [ERRO] npm nao encontrado no PATH. & goto :fail)
echo    Node e npm: OK
echo.

if "%MODO%"=="2" goto :skip_stack

where docker >nul 2>&1
if errorlevel 1 (echo    [AVISO] Docker nao encontrado. Banco/backend devem ja estar ativos.) & goto :skip_stack

echo [2/5] Garantindo PostgreSQL em localhost:%DB_PORT%...
call :check_port %DB_PORT%
if "!PORT_OK!"=="1" (
    echo    PostgreSQL ativo.
) else (
    echo    Subindo PostgreSQL via Docker Compose...
    pushd "%ROOT%"
    docker compose up -d db >nul 2>&1
    popd
    for /l %%i in (1,1,30) do (
        call :check_port %DB_PORT%
        if "!PORT_OK!"=="1" echo    PostgreSQL pronto. & goto :db_ok
        timeout /t 1 /nobreak >nul
    )
    echo    [ERRO] Banco nao respondeu na porta %DB_PORT%.
    goto :fail
)
:db_ok
echo.

echo [3/5] Garantindo backend em localhost:%BACKEND_PORT%...
call :check_port %BACKEND_PORT%
if "!PORT_OK!"=="1" (
    echo    Backend ja esta ativo.
    goto :backend_ok
)
echo    Compilando backend Maven ^(pode demorar na primeira vez^)...
pushd "%BACKEND%"
call mvn -o -q clean install -DskipTests
set "MVN_ERR=!errorlevel!"
popd
if not "!MVN_ERR!"=="0" (
    echo    [ERRO] Falha ao compilar o backend.
    goto :fail
)
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
echo    Iniciando backend Quarkus em nova janela...
start "Sistema Tenda - Backend (E2E)" /D "%BACKEND%\pdv" cmd /k "java -jar target\quarkus-app\quarkus-run.jar"
echo    Aguardando o backend responder na porta %BACKEND_PORT%...
for /l %%i in (1,1,90) do (
    call :check_port %BACKEND_PORT%
    if "!PORT_OK!"=="1" echo    Backend pronto. & goto :backend_ok
    timeout /t 1 /nobreak >nul
)
echo    [ERRO] Backend nao respondeu a tempo.
goto :fail

:backend_ok
echo.

:skip_stack
echo [4/5] Verificando dependencias do frontend...
if not exist "%FRONTEND%\node_modules" (
    echo    Instalando dependencias npm...
    pushd "%FRONTEND%"
    call npm install
    set "NPM_ERR=!errorlevel!"
    popd
    if not "!NPM_ERR!"=="0" (echo    [ERRO] Falha no npm install. & goto :fail)
)
if not exist "%FRONTEND%\.env.e2e" (
    echo    [ERRO] Arquivo .env.e2e nao encontrado em frontend\.
    echo            Copie .env.e2e.example e preencha as credenciais.
    goto :fail
)
if not exist "%FRONTEND%\e2e\.auth" mkdir "%FRONTEND%\e2e\.auth"
echo    Dependencias e configuracao: OK
echo.

echo [5/5] Executando a suite Playwright...
echo ============================================================
if "%MODO%"=="3" (
    pushd "%FRONTEND%"
    call npx playwright test --headed
    set "TEST_ERR=!errorlevel!"
    popd
) else (
    pushd "%FRONTEND%"
    call npx playwright test
    set "TEST_ERR=!errorlevel!"
    popd
)
echo ============================================================
if not "!TEST_ERR!"=="0" (
    echo.
    echo    [FALHA] Um ou mais testes falharam ^(codigo !TEST_ERR!^).
    echo    Abrindo o relatorio HTML para investigar...
    pushd "%FRONTEND%"
    call npx playwright show-report
    popd
    goto :fail
)
echo.
echo    [OK] Todos os testes passaram.
echo    Relatorio HTML: frontend\playwright-report\index.html
echo.
pause
exit /b 0

:relatorio
if not exist "%FRONTEND%\playwright-report" (
    echo    [ERRO] Nenhum relatorio encontrado. Rode os testes primeiro.
    pause
    exit /b 1
)
pushd "%FRONTEND%"
call npx playwright show-report
popd
exit /b 0

:check_port
set "PORT_OK=0"
powershell -NoProfile -Command "$ok=$false; foreach($h in @('127.0.0.1','::1')){ try{ if((New-Object Net.Sockets.TcpClient).ConnectAsync($h,%~1).Wait(700)){$ok=$true;break} }catch{} }; if($ok){exit 0}else{exit 1}" >nul 2>&1
if !errorlevel! equ 0 set "PORT_OK=1"
goto :eof

:fail
echo.
echo Falha na execucao dos testes. Verifique as mensagens acima.
pause
exit /b 1
