@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Sistema Tenda - Iniciar Ambiente Local

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"
set "LOG_DIR=%ROOT%.logs"
set "QUARKUS_VERSION=3.12.0"
set "DB_PORT=5433"

echo ============================================================
echo    SISTEMA TENDA - AMBIENTE LOCAL
echo ============================================================
echo.

echo [1/4] Verificando pre-requisitos...
where java >nul 2>&1
if errorlevel 1 (echo    [ERRO] Java nao encontrado no PATH. & goto :fail)
where mvn >nul 2>&1
if errorlevel 1 (echo    [ERRO] Maven nao encontrado no PATH. & goto :fail)
where node >nul 2>&1
if errorlevel 1 (echo    [ERRO] Node.js nao encontrado no PATH. & goto :fail)
where npm >nul 2>&1
if errorlevel 1 (echo    [ERRO] npm nao encontrado no PATH. & goto :fail)
echo    Java, Maven, Node e npm: OK
echo.

echo [2/4] Verificando PostgreSQL em localhost:%DB_PORT%...
call :check_port %DB_PORT%
if "!PORT_OK!"=="1" (
    echo    PostgreSQL ativo.
) else (
    call :start_db
)
echo.

echo [3/4] Compilando backend Maven ^(pode demorar na primeira vez^)...
pushd "%BACKEND%"
call mvn -o -q clean install -DskipTests
set "MVN_ERR=!errorlevel!"
popd
if not "!MVN_ERR!"=="0" (
    echo    [ERRO] Falha ao compilar o backend.
    goto :fail
)
echo    Backend compilado.
echo.

echo [4/4] Preparando frontend...
if not exist "%FRONTEND%\node_modules" (
    echo    Instalando dependencias npm...
    pushd "%FRONTEND%"
    call npm install
    set "NPM_ERR=!errorlevel!"
    popd
    if not "!NPM_ERR!"=="0" (
        echo    [ERRO] Falha no npm install.
        goto :fail
    )
) else (
    echo    node_modules ja existe.
)
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
echo.

echo    Liberando portas 3000, 8080 e 8081 ^(caso haja execucao anterior^)...
echo    Parando containers de app do compose (mantendo o banco)...
pushd "%ROOT%"
docker compose stop backend frontend >nul 2>&1
popd
for %%p in (3000 8080 8081) do (
    powershell -NoProfile -Command "$c=Get-NetTCPConnection -LocalPort %%p -State Listen -ErrorAction SilentlyContinue; if($c){$c.OwningProcess | Sort-Object -Unique | ForEach-Object { $pr=Get-Process -Id $_ -ErrorAction SilentlyContinue; if($pr -and $pr.ProcessName -match '^(java|javaw|node)$'){ Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue } }}" >nul 2>&1
)

echo ============================================================
echo    Iniciando servicos...
echo ============================================================
start "Sistema Tenda - Backend" /D "%BACKEND%\pdv" cmd /k mvn -o io.quarkus:quarkus-maven-plugin:%QUARKUS_VERSION%:dev
start "Sistema Tenda - Frontend" /D "%FRONTEND%" cmd /k npm run dev
echo.
echo    Backend : http://localhost:8080
echo    Swagger : http://localhost:8080/swagger
echo    Frontend: http://localhost:3000
echo.
echo    Aguardando o frontend responder para abrir o navegador...
set "OPENED=0"
for /l %%i in (1,1,60) do (
    call :check_port 3000
    if "!PORT_OK!"=="1" (
        start "" http://localhost:3000
        set "OPENED=1"
        goto :opened
    )
    timeout /t 1 /nobreak >nul
)
:opened
if "!OPENED!"=="0" echo    [AVISO] Abra manualmente no navegador: http://localhost:3000
echo.
echo Pressione qualquer tecla para fechar esta janela (os servicos continuam rodando)...
pause >nul
exit /b 0

:check_port
set "PORT_OK=0"
powershell -NoProfile -Command "$ok=$false; foreach($h in @('127.0.0.1','::1')){ try{ if((New-Object Net.Sockets.TcpClient).ConnectAsync($h,%~1).Wait(700)){$ok=$true;break} }catch{} }; if($ok){exit 0}else{exit 1}" >nul 2>&1
if !errorlevel! equ 0 set "PORT_OK=1"
goto :eof

:start_db
where docker >nul 2>&1
if !errorlevel! neq 0 (
    echo    [AVISO] PostgreSQL inativo e Docker ausente.
    echo            Inicie o banco manualmente ^(localhost:%DB_PORT%, db/user/senha = sistematenda^).
    goto :eof
)
docker info >nul 2>&1
if !errorlevel! neq 0 (
    echo    Docker Desktop nao esta rodando. Iniciando...
    if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
        start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
        echo    Aguardando o Docker Desktop iniciar...
        for /l %%i in (1,1,90) do (
            docker info >nul 2>&1
            if !errorlevel! equ 0 goto :docker_up
            timeout /t 2 /nobreak >nul
        )
        echo    [AVISO] Docker Desktop nao respondeu a tempo.
    ) else (
        echo    [AVISO] Docker Desktop nao encontrado.
    )
)
:docker_up
echo    Subindo PostgreSQL via Docker Compose...
pushd "%ROOT%"
docker compose up -d db >nul 2>&1
popd
echo    Aguardando o banco ficar pronto...
for /l %%i in (1,1,30) do (
    call :check_port %DB_PORT%
    if "!PORT_OK!"=="1" (
        echo    PostgreSQL pronto.
        goto :eof
    )
    timeout /t 1 /nobreak >nul
)
echo    [AVISO] Nao foi possivel confirmar o banco apos 30s.
goto :eof

:fail
echo.
echo Falha ao iniciar o ambiente. Verifique as mensagens acima.
pause
exit /b 1
