@echo off
chcp 65001 >nul
rem Gera pasta producao\ com Sistema Tenda pronto para rodar em servidor
rem Uso: gerar-producao.bat [--zip] [--sem-jars] [--com-testes]
rem 
rem Credenciais do Banco de Dados (padrão: sistematenda/sistematenda/sistematenda)
rem Caso seu PostgreSQL use senha diferente, altere antes do setlocal:
rem   set DB_PASSWORD=sua_senha_aqui
rem 
setlocal enabledelayedexpansion
cd /d "%~dp0"

set DEST=producao
set ZIP=0
set JARS=1
set TESTES=-DskipTests
set DB_PORT=5432
set DB_NAME=sistematenda
set DB_USER=sistematenda
set DB_PASSWORD=sistematenda
rem Credenciais administrativas usadas apenas para criar o usuario/banco se necessario
set DB_ADMIN_USER=postgres
set DB_ADMIN_PASSWORD=sistematenda
set COMPOSE_PROD=docker-compose.prod.yml

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

echo [1/8] Verificando pre-requisitos...
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

echo [2/8] Garantindo banco PostgreSQL em localhost:%DB_PORT%...
call :check_port %DB_PORT%
if "!PORT_OK!"=="1" (
  echo       PostgreSQL ja esta ativo na porta %DB_PORT%.
  goto :db_ok
)
where docker >nul 2>nul || (
  echo [ERRO] PostgreSQL inativo na porta %DB_PORT% e Docker nao encontrado.
  echo        Suba um PostgreSQL em localhost:%DB_PORT% ^(db/user/senha = sistematenda^) ou instale o Docker.
  exit /b 1
)
docker info >nul 2>&1
if errorlevel 1 (
  echo       Docker Desktop nao esta rodando. Iniciando...
  if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
    start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
    for /l %%i in (1,1,90) do (
      docker info >nul 2>&1
      if !errorlevel! equ 0 goto :docker_prod_up
      timeout /t 2 /nobreak >nul
    )
    echo [ERRO] Docker Desktop nao respondeu a tempo.
    exit /b 1
  ) else (
    echo [ERRO] Docker Desktop nao encontrado.
    exit /b 1
  )
)
:docker_prod_up
echo       Criando/subindo container PostgreSQL de producao na porta %DB_PORT%...
call docker compose -p sistematenda-prod -f %COMPOSE_PROD% up -d db
if errorlevel 1 (
  echo [ERRO] Falha ao subir o container PostgreSQL.
  exit /b 1
)
echo       Aguardando o banco ficar pronto...
for /l %%i in (1,1,60) do (
  call :check_port %DB_PORT%
  if "!PORT_OK!"=="1" goto :db_ok
  timeout /t 1 /nobreak >nul
)
echo [ERRO] O banco nao respondeu na porta %DB_PORT% apos 60s.
exit /b 1
:db_ok
echo       PostgreSQL pronto em localhost:%DB_PORT% ^(banco: sistematenda^).
call :ensure_database
echo.

echo [3/8] Limpando pacote anterior...
if exist "%DEST%" rmdir /s /q "%DEST%"
mkdir "%DEST%"
for %%d in (app config logs) do mkdir "%DEST%\%%d"
echo       OK.
echo.

echo [4/8] Build do Frontend React (Vite)...
pushd frontend
if not exist node_modules (
  echo       Instalando dependencias...
  call npm install --silent
)
call npm run build --silent || (popd & echo [ERRO] Falha no build do front & exit /b 1)
popd
if not exist "frontend\dist\index.html" (
  echo [ERRO] frontend\dist\index.html nao gerado
  exit /b 1
)
echo       Frontend buildado com sucesso.
echo.

echo [5/8] Integrando frontend ao backend (META-INF/resources)...
set RESOURCES=backend\pdv\src\main\resources\META-INF\resources
if exist "%RESOURCES%" rmdir /s /q "%RESOURCES%"
mkdir "%RESOURCES%"
xcopy /e /i /q frontend\dist\* "%RESOURCES%\" >nul
echo       Arquivos estaticos copiados.
echo.

if "%JARS%"=="1" (
  echo [6/8] Compilando Backend ^(Maven^)...
  pushd backend
  call mvn -q clean package %TESTES%
  popd
  echo       Backend compilado com sucesso.
  echo.
) else (
  echo [6/8] Pulando compilacao do backend ^(--sem-jars^)
  echo.
)

echo [7/8] Montando pacote em %DEST%\
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

rem Adicionar configuracao do TEF Agent no .env se nao existir
if exist "%DEST%\.env" (
  findstr /i "TEF_PROVIDER=" "%DEST%\.env" >nul
  if errorlevel 1 (
    echo TEF_PROVIDER=STONE >> "%DEST%\.env"
    echo TEF_CODIGO_CREDENCIADORA=123456 >> "%DEST%\.env"
    echo TEF_TERMINAL_ID=001 >> "%DEST%\.env"
  )
)

if exist "certificado.pfx" (
  copy /y "certificado.pfx" "%DEST%\" >nul
)

echo       Pacote montado.
echo.

echo [8/8] Finalizando...
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

exit /b 0

:ensure_database
where psql >nul 2>&1
if errorlevel 1 (
    echo       [AVISO] O utilitario psql nao encontrado. Pulando verificacao de banco.
    echo               O banco %DB_NAME% sera usado conforme configuracao existente.
    set "PGPASSWORD="
    set "PGCONNECT_TIMEOUT="
    goto :eof
)
set "DB_ADMIN_LOGIN="
set "PGPASSWORD=%DB_ADMIN_PASSWORD%"
psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d postgres -tAc "SELECT 1" >nul 2>&1
if not errorlevel 1 set "DB_ADMIN_LOGIN=1"

rem No Docker Compose o superusuario costuma ser o proprio usuario da aplicacao.
if not defined DB_ADMIN_LOGIN (
    if /i not "%DB_USER%"=="%DB_ADMIN_USER%" (
        set "PGPASSWORD=%DB_PASSWORD%"
        psql -w -h localhost -p %DB_PORT% -U %DB_USER% -d postgres -tAc "SELECT 1" >nul 2>&1
        if not errorlevel 1 (
            set "DB_ADMIN_LOGIN=1"
            set "DB_ADMIN_USER=%DB_USER%"
            set "DB_ADMIN_PASSWORD=%DB_PASSWORD%"
            echo       Usando o usuario da aplicacao %DB_USER% como administrador.
        )
    )
)

if not defined DB_ADMIN_LOGIN (
    echo       [AVISO] Nao foi possivel conectar como administrador em localhost:%DB_PORT%.
    echo               Ajuste DB_ADMIN_USER/DB_ADMIN_PASSWORD ou crie o banco manualmente.
    set "PGPASSWORD="
    set "PGCONNECT_TIMEOUT="
    goto :eof
)

set "PGPASSWORD=%DB_ADMIN_PASSWORD%"
set "PGCONNECT_TIMEOUT=5"
set "DB_EXISTS="

rem Garantir que o usuario/role tambem exista antes de tentar criar o banco
psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d postgres -c "DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname='%DB_USER%') THEN CREATE ROLE %DB_USER% WITH LOGIN PASSWORD '%DB_PASSWORD%'; END IF; END $$;" >nul 2>&1

for /f "delims=" %%r in ('psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='%DB_NAME%'" 2^>nul') do set "DB_EXISTS=%%r"
if not "!DB_EXISTS!"=="" (
    echo       Banco %DB_NAME% ja existe em localhost:%DB_PORT%.
    psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d postgres -c "ALTER DATABASE %DB_NAME% OWNER TO %DB_USER%;" >nul 2>&1
    psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d %DB_NAME% -c "GRANT ALL ON SCHEMA public TO %DB_USER%;" >nul 2>&1
    psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d %DB_NAME% -c "ALTER SCHEMA public OWNER TO %DB_USER%;" >nul 2>&1
    set "PGPASSWORD="
    set "PGCONNECT_TIMEOUT="
    goto :eof
)
echo       Tentando criar banco %DB_NAME% em localhost:%DB_PORT%...
psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d postgres -c "CREATE DATABASE %DB_NAME% OWNER %DB_USER%" >nul 2>&1
if errorlevel 1 (
    echo       [AVISO] Nao foi possivel criar o banco %DB_NAME% via psql.
    echo               Verifique manualmente em localhost:%DB_PORT% ^(usuario: %DB_USER%^).
    echo               O banco sera usado conforme configuracao existente.
) else (
    echo       Banco %DB_NAME% criado com sucesso em localhost:%DB_PORT%.
    psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d %DB_NAME% -c "GRANT ALL ON SCHEMA public TO %DB_USER%;" >nul 2>&1
    psql -w -h localhost -p %DB_PORT% -U %DB_ADMIN_USER% -d %DB_NAME% -c "ALTER SCHEMA public OWNER TO %DB_USER%;" >nul 2>&1
)
set "PGPASSWORD="
set "PGCONNECT_TIMEOUT="
goto :eof

:check_port
set "PORT_OK=0"
powershell -NoProfile -Command "$ok=$false; foreach($h in @('127.0.0.1','::1')){ try{ if((New-Object Net.Sockets.TcpClient).ConnectAsync($h,%~1).Wait(700)){$ok=$true;break} }catch{} }; if($ok){exit 0}else{exit 1}" >nul 2>&1
if !errorlevel! equ 0 set "PORT_OK=1"
goto :eof