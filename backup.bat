@echo off
chcp 65001 >nul

:: =============================================================================
:: Sistema Tenda - Script de Backup
:: Cria backup do codigo fonte e banco de dados para pendrive/Google Drive
:: Uso: backup.bat [--skip-db] [--no-zip] [--auto-clean]
:: =============================================================================

set PROJECT_DIR=%~dp0
set BACKUP_BASE=%PROJECT_DIR%backup
set TIMESTAMP=%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_DIR=%BACKUP_BASE%\backup_%TIMESTAMP%
set ZIP_NAME=sistema-tenda-backup_%TIMESTAMP%.zip

:: Parse arguments
set SKIP_DB=0
set NO_ZIP=0
set AUTO_CLEAN=0
for %%a in (%*) do (
    if "%%a"=="--skip-db" set SKIP_DB=1
    if "%%a"=="--no-zip" set NO_ZIP=1
    if "%%a"=="--auto-clean" set AUTO_CLEAN=1
)

echo =============================================================================
echo  SISTEMA TENDA - BACKUP
echo =============================================================================
echo Data/Hora: %date% %time%
echo Diretorio do projeto: %PROJECT_DIR%
echo Diretorio de backup: %BACKUP_DIR%
echo =============================================================================

:: Criar diretorio de backup
if not exist "%BACKUP_BASE%" mkdir "%BACKUP_BASE%"
mkdir "%BACKUP_DIR%"

:: -----------------------------------------------------------------------------
:: 1. Backup do codigo fonte (exclui build artifacts, node_modules, etc.)
:: -----------------------------------------------------------------------------
echo.
echo [1/4] Copiando codigo fonte...

powershell -Command "if (-not (Test-Path '%BACKUP_DIR%\sistema-tenda')) { New-Item -ItemType Directory -Path '%BACKUP_DIR%\sistema-tenda' | Out-Null }; Copy-Item -Path '%PROJECT_DIR%*' -Destination '%BACKUP_DIR%\sistema-tenda' -Recurse -Force -Exclude @('node_modules','target','.git','.idea','dist','build','out','bin','obj','backup','*.log','*.tmp','*.bak','*.class','*.jar','*.war','*.ear','*.zip','*.tar','*.gz','*.tgz','Thumbs.db','Desktop.ini','package-lock.json','yarn.lock','pnpm-lock.yaml','*.iml','*.ipr','*.iws','.gitignore','.gitattributes')"

if %ERRORLEVEL% GTR 3 (
    echo ERRO ao copiar arquivos!
    exit /b 1
)
echo OK - Codigo fonte copiado

:: -----------------------------------------------------------------------------
:: 2. Backup do banco de dados PostgreSQL (opcional)
:: -----------------------------------------------------------------------------
echo.
echo [2/4] Verificando backup do banco de dados...

if %SKIP_DB%==1 (
    echo Pulando backup do banco de dados --skip-db
    goto DO_DB_SKIP
)

:: Verificar se pg_dump esta disponivel
where pg_dump >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo AVISO: pg_dump nao encontrado no PATH.
    echo O backup do banco de dados sera ignorado.
    echo Instale o PostgreSQL Client Tools ou adicione pg_dump ao PATH.
    goto DO_DB_SKIP
)

:: Configuracoes do banco (ajuste conforme necessario)
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=sistematenda
set DB_USER=sistematenda
set DB_DUMP_FILE=%BACKUP_DIR%\database_dump.sql

echo Host: %DB_HOST%:%DB_PORT%
echo Database: %DB_NAME%
echo Usuario: %DB_USER%

:: Tentar fazer o dump (usa PGPASSWORD do ambiente se definido)
pg_dump -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%DB_DUMP_FILE%" --no-owner --no-privileges --clean --if-exists 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo AVISO: Falha ao fazer dump do banco. Verifique credenciais/conexao.
    echo O arquivo de dump pode estar incompleto.
) else (
    echo OK - Dump do banco salvo em: %DB_DUMP_FILE%
)

:DO_DB_SKIP

:: -----------------------------------------------------------------------------
:: 3. Criar arquivo de informacoes do backup
:: -----------------------------------------------------------------------------
echo.
echo [3/4] Criando arquivo de informacoes do backup...

set INFO_FILE=%BACKUP_DIR%\BACKUP_INFO.txt
echo Sistema Tenda - Informacoes do Backup > "%INFO_FILE%"
echo ======================================== >> "%INFO_FILE%"
echo. >> "%INFO_FILE%"
echo Data/Hora do backup: %date% %time% >> "%INFO_FILE%"
echo Diretorio original: %PROJECT_DIR% >> "%INFO_FILE%"
echo Versao do script: 1.0 >> "%INFO_FILE%"
echo. >> "%INFO_FILE%"
echo Conteudo do backup: >> "%INFO_FILE%"
echo   - sistema-tenda/     (codigo fonte completo) >> "%INFO_FILE%"
echo   - database_dump.sql  (dump do PostgreSQL, se disponivel) >> "%INFO_FILE%"
echo   - BACKUP_INFO.txt    (este arquivo) >> "%INFO_FILE%"
echo. >> "%INFO_FILE%"
echo Para restaurar: >> "%INFO_FILE%"
echo   1. Extraia o ZIP no local desejado >> "%INFO_FILE%"
echo   2. Restaure o banco: psql -U sistematenda -d sistematenda -f database_dump.sql >> "%INFO_FILE%"
echo   3. Configure application.properties com suas credenciais >> "%INFO_FILE%"
echo   4. Execute: mvn clean install (backend) e npm install (frontend) >> "%INFO_FILE%"

echo OK - Info criada

:: -----------------------------------------------------------------------------
:: 4. Compactar tudo em ZIP (opcional)
:: -----------------------------------------------------------------------------
if %NO_ZIP%==1 (
    echo.
    echo [4/4] Pulando compactacao ZIP --no-zip
    goto DO_ZIP_SKIP
)

echo.
echo [4/4] Compactando backup em ZIP...

cd /d "%BACKUP_BASE%"
powershell -Command "Compress-Archive -Path 'backup_%TIMESTAMP%' -DestinationPath '%ZIP_NAME%' -Force"

if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao criar ZIP!
    exit /b 1
)

echo OK - ZIP criado: %BACKUP_BASE%\%ZIP_NAME%

:DO_ZIP_SKIP

:: -----------------------------------------------------------------------------
:: Limpeza automatica ou interativa
:: -----------------------------------------------------------------------------
if %AUTO_CLEAN%==1 (
    if %NO_ZIP%==0 (
        rmdir /s /q "%BACKUP_DIR%"
        echo Pasta descompactada removida --auto-clean.
    )
) else (
    echo.
    echo Deseja remover a pasta descompactada e manter apenas o ZIP? (S/N)
    set /p CONFIRM=
    if /i "%CONFIRM%"=="S" (
        rmdir /s /q "%BACKUP_DIR%"
        echo Pasta removida. Apenas o ZIP permanece.
    )
)

:: -----------------------------------------------------------------------------
:: Resumo final
:: -----------------------------------------------------------------------------
echo.
echo =============================================================================
echo  BACKUP CONCLUIDO COM SUCESSO!
echo =============================================================================
if %NO_ZIP%==0 (
    echo Arquivo: %BACKUP_BASE%\%ZIP_NAME%
    echo Tamanho: 
    for %%A in ("%BACKUP_BASE%\%ZIP_NAME%") do echo %%~zA bytes
) else (
    echo Pasta: %BACKUP_DIR%
)
echo.
echo Copie este arquivo/pasta para seu pendrive ou Google Drive.
echo =============================================================================

:: Pause apenas se nao for modo automatico
if %AUTO_CLEAN%==0 pause