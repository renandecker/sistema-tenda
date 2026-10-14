@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Sistema Tenda - Iniciar Ambiente Local

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"
set "LOG_DIR=%ROOT%.logs"
set "QUARKUS_VERSION=3.12.0"
set "DB_CONTAINER=sistematenda-postgres"

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

echo [2/4] Verificando PostgreSQL em localhost:5432...
call :check_port 5432
if "!PORT_OK!"=="1" (
    echo    PostgreSQL ativo.
) else (
    call :start_db
)
echo.

echo [3/4] Compilando backend Maven ^(pode demorar na primeira vez^)...
pushd "%BACKEND%"
call mvn -q clean install -DskipTests
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

echo    Liberando porta 3000 ^(caso haja execucao anterior^)...
powershell -NoProfile -Command "$c=Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue; if($c){$c.OwningProcess | Sort-Object -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }}" >nul 2>&1

echo ============================================================
echo    Iniciando servicos...
echo ============================================================
start "Sistema Tenda - Backend" /D "%BACKEND%\pdv" cmd /k mvn io.quarkus:quarkus-maven-plugin:%QUARKUS_VERSION%:dev
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
powershell -NoProfile -Command "if((New-Object Net.Sockets.TcpClient).ConnectAsync('localhost',%~1).Wait(1000)){exit 0}else{exit 1}" >nul 2>&1
if !errorlevel! equ 0 set "PORT_OK=1"
goto :eof

:start_db
where docker >nul 2>&1
if !errorlevel! neq 0 (
    echo    [AVISO] PostgreSQL inativo e Docker ausente.
    echo            Inicie o banco manualmente ^(localhost:5432, db/user/senha = sistematenda^).
    goto :eof
)
echo    Subindo PostgreSQL via Docker...
docker start %DB_CONTAINER% >nul 2>&1
if !errorlevel! neq 0 (
    docker run -d --name %DB_CONTAINER% -e POSTGRES_USER=sistematenda -e POSTGRES_PASSWORD=sistematenda -e POSTGRES_DB=sistematenda -p 5432:5432 postgres:16 >nul
)
echo    Aguardando o banco ficar pronto...
for /l %%i in (1,1,30) do (
    call :check_port 5432
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
