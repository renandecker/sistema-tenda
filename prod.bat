@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Sistema Tenda - Build de Producao

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"
set "PROD_DIR=%ROOT%production"
set "RESOURCES_DIR=%BACKEND%\pdv\src\main\resources\META-INF\resources"
set "DB_PORT=5432"
set "DB_NAME=sistematenda"
set "DB_USER=sistematenda"
set "DB_PASSWORD=sistematenda"
set "COMPOSE_PROD=%ROOT%docker-compose.prod.yml"

echo ============================================================
echo    SISTEMA TENDA - PREPARACAO PARA PRODUCAO
echo ============================================================
echo.

echo [1/6] Verificando pre-requisitos (Java, Maven, Node, npm)...
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

echo [2/6] Garantindo banco PostgreSQL em localhost:%DB_PORT%...
call :check_port %DB_PORT%
if "!PORT_OK!"=="1" (
    echo    PostgreSQL ja esta ativo na porta %DB_PORT%.
    goto :db_ok
)
where docker >nul 2>&1
if errorlevel 1 (
    echo [ERRO] PostgreSQL inativo na porta %DB_PORT% e Docker nao encontrado.
    echo        Suba um PostgreSQL em localhost:%DB_PORT% ^(db/user/senha = sistematenda^) ou instale o Docker.
    goto :fail
)
docker info >nul 2>&1
if errorlevel 1 (
    echo    Docker Desktop nao esta rodando. Iniciando...
    if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
        start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
        for /l %%i in (1,1,90) do (
            docker info >nul 2>&1
            if !errorlevel! equ 0 goto :docker_prod_up
            timeout /t 2 /nobreak >nul
        )
        echo [ERRO] Docker Desktop nao respondeu a tempo.
        goto :fail
    ) else (
        echo [ERRO] Docker Desktop nao encontrado.
        goto :fail
    )
)
:docker_prod_up
echo    Criando/subindo container PostgreSQL de producao na porta %DB_PORT%...
pushd "%ROOT%"
call docker compose -p sistematenda-prod -f "%COMPOSE_PROD%" up -d db
set "DOCKER_ERR=!errorlevel!"
popd
if not "!DOCKER_ERR!"=="0" (
    echo [ERRO] Falha ao subir o container PostgreSQL.
    goto :fail
)
echo    Aguardando o banco ficar pronto...
for /l %%i in (1,1,60) do (
    call :check_port %DB_PORT%
    if "!PORT_OK!"=="1" goto :db_ok
    timeout /t 1 /nobreak >nul
)
echo [ERRO] O banco nao respondeu na porta %DB_PORT% apos 60s.
goto :fail
:db_ok
echo    PostgreSQL pronto em localhost:%DB_PORT% ^(banco: sistematenda^).
call :ensure_database
echo.

echo [3/6] Compilando Frontend (Vite Build)...
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
echo    Frontend compilado com sucesso.
echo.

echo [4/6] Integrando Frontend ao Backend (META-INF/resources)...
if not exist "%RESOURCES_DIR%" mkdir "%RESOURCES_DIR%"
xcopy /e /i /q /y "%FRONTEND%\dist\*" "%RESOURCES_DIR%\" >nul
echo    Arquivos estaticos copiados para o backend.
echo.

echo [5/6] Compilando Backend (Maven - Package Production)...
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

echo [6/6] Organizando pacote de producao em "%PROD_DIR%"...
if exist "%PROD_DIR%" rmdir /s /q "%PROD_DIR%"
mkdir "%PROD_DIR%"
mkdir "%PROD_DIR%\backend"
mkdir "%PROD_DIR%\backend\quarkus-app\config"

:: Copiar artefatos do backend (quarkus-app)
if exist "%BACKEND%\pdv\target\quarkus-app" (
    xcopy /e /i /q "%BACKEND%\pdv\target\quarkus-app" "%PROD_DIR%\backend\quarkus-app" >nul
)

:: Copiar application.prod.properties como application.properties na pasta config
if exist "%BACKEND%\pdv\src\main\resources\application.prod.properties" (
    copy "%BACKEND%\pdv\src\main\resources\application.prod.properties" "%PROD_DIR%\backend\quarkus-app\config\application.properties" >nul
)

:: Copiar variaveis de ambiente
if exist "%ROOT%.env" (
    copy "%ROOT%.env" "%PROD_DIR%\.env" >nul
)

:: Criar script de inicializacao em producao
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

:check_port
set "PORT_OK=0"
powershell -NoProfile -Command "$ok=$false; foreach($h in @('127.0.0.1','::1')){ try{ if((New-Object Net.Sockets.TcpClient).ConnectAsync($h,%~1).Wait(700)){$ok=$true;break} }catch{} }; if($ok){exit 0}else{exit 1}" >nul 2>&1
if !errorlevel! equ 0 set "PORT_OK=1"
goto :eof

:ensure_database
where psql >nul 2>&1
if errorlevel 1 goto :eof
set "PGPASSWORD=%DB_PASSWORD%"
set "DB_EXISTS="
for /f "delims=" %%r in ('psql -h localhost -p %DB_PORT% -U %DB_USER% -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='%DB_NAME%'" 2^>nul') do set "DB_EXISTS=%%r"
if not "!DB_EXISTS!"=="" (
    echo    Banco %DB_NAME% ja existe em localhost:%DB_PORT%.
    set "PGPASSWORD="
    goto :eof
)
psql -h localhost -p %DB_PORT% -U %DB_USER% -d postgres -c "CREATE DATABASE %DB_NAME%" >nul 2>&1
if errorlevel 1 (
    echo    [AVISO] Nao foi possivel verificar/criar o banco %DB_NAME% via psql.
    echo            Verifique manualmente em localhost:%DB_PORT% ^(usuario: %DB_USER%^).
) else (
    echo    Banco %DB_NAME% criado em localhost:%DB_PORT%.
)
set "PGPASSWORD="
goto :eof

:fail
echo.
echo [ERRO] Ocorreu uma falha durante a preparacao para producao.
pause
exit /b 1
