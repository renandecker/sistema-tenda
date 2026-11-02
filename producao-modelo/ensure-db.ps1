# =============================================================================
# Sistema Tenda - preparacao do banco PostgreSQL
# =============================================================================
# Le as variaveis QUARKUS_DATASOURCE_* e DB_ADMIN_* (ja carregadas do .env pelo
# start.bat) e cria o usuario/banco caso ainda nao existam.
# Nunca impede a inicializacao: se nao for possivel criar, apenas avisa e o
# Flyway cuida do schema quando o banco ja existir.

$ErrorActionPreference = 'Continue'

function Write-Info([string]$Message) {
    Write-Host "      $Message"
}

function Write-Warn([string]$Message) {
    Write-Host "      [AVISO] $Message"
}

$dbUrl = $env:QUARKUS_DATASOURCE_JDBC_URL
if ([string]::IsNullOrWhiteSpace($dbUrl)) {
    $dbUrl = 'jdbc:postgresql://localhost:5432/sistematenda'
}
$dbUser = $env:QUARKUS_DATASOURCE_USERNAME
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'sistematenda' }
$dbPass = $env:QUARKUS_DATASOURCE_PASSWORD
if ($null -eq $dbPass) { $dbPass = 'sistematenda' }
$adminUser = $env:DB_ADMIN_USER
if ([string]::IsNullOrWhiteSpace($adminUser)) { $adminUser = 'postgres' }
$adminPass = $env:DB_ADMIN_PASSWORD

# Extrai host, porta e nome do banco de jdbc:postgresql://host[:porta]/banco
$hostName = $null
$port = '5432'
$dbName = $null
if ($dbUrl -match '^jdbc:postgresql://([^/:?;]+)(?::([0-9]+))?/([^?;]+)') {
    $hostName = $Matches[1]
    if ($Matches[2]) { $port = $Matches[2] }
    $dbName = $Matches[3]
}

if ([string]::IsNullOrWhiteSpace($hostName) -or [string]::IsNullOrWhiteSpace($dbName)) {
    Write-Warn "URL JDBC nao reconhecida: $dbUrl"
    Write-Warn 'Verifique QUARKUS_DATASOURCE_JDBC_URL no arquivo .env.'
    exit 0
}

# Evita injecao de SQL nos identificadores usados nos comandos CREATE.
function Test-Identifier([string]$Name) {
    return $Name -match '^[A-Za-z_][A-Za-z0-9_]*$'
}

if (-not (Test-Identifier $dbUser) -or -not (Test-Identifier $dbName)) {
    Write-Warn "Usuario/banco com caracteres nao suportados: '$dbUser' / '$dbName'."
    Write-Warn 'Crie o banco manualmente; o Flyway cuidara das tabelas.'
    exit 0
}

Write-Info "Banco configurado: $dbName em ${hostName}:${port} (usuario: $dbUser)."

$psql = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psql) {
    Write-Warn 'psql nao encontrado no PATH; nao foi possivel verificar/criar o banco.'
    Write-Warn "O banco '$dbName' precisa existir. O Flyway criara as tabelas ao iniciar."
    exit 0
}

$psqlPath = $psql.Source

$env:PGPASSWORD = if ($null -eq $adminPass) { '' } else { $adminPass }
$env:PGCONNECT_TIMEOUT = '5'
$env:PGCLIENTENCODING = 'UTF8'

function Invoke-AdminQuery([string]$Sql) {
    $output = & $psqlPath -w -h $hostName -p $port -U $adminUser -d postgres -tA -c $Sql 2>&1
    return @{
        Code = $LASTEXITCODE
        Text = (($output | Out-String).Trim())
    }
}

function Invoke-AdminSql([string]$Sql) {
    $output = $Sql | & $psqlPath -w -h $hostName -p $port -U $adminUser -d postgres -v ON_ERROR_STOP=1 2>&1
    return @{
        Code = $LASTEXITCODE
        Text = (($output | Out-String).Trim())
    }
}

$probe = Invoke-AdminQuery 'SELECT 1'
if ($probe.Code -ne 0 -and $adminUser -ne $dbUser) {
    # No Docker Compose o superusuario costuma ser o proprio usuario da aplicacao.
    $adminUser = $dbUser
    $env:PGPASSWORD = if ($null -eq $dbPass) { '' } else { $dbPass }
    $probe = Invoke-AdminQuery 'SELECT 1'
    if ($probe.Code -eq 0) {
        Write-Info "Usando o usuario da aplicacao '$dbUser' como administrador."
    }
}
if ($probe.Code -ne 0) {
    Write-Warn "Nao foi possivel conectar como administrador '$adminUser' em ${hostName}:${port}."
    Write-Warn 'Ajuste DB_ADMIN_USER / DB_ADMIN_PASSWORD no .env ou crie o banco manualmente.'
    Write-Warn "O Flyway criara as tabelas automaticamente se o banco '$dbName' ja existir."
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    exit 0
}

# Usuario (role)
$roleCheck = Invoke-AdminQuery "SELECT 1 FROM pg_roles WHERE rolname = '$dbUser'"
if ($roleCheck.Code -ne 0) {
    Write-Warn "Falha ao consultar o usuario '$dbUser': $($roleCheck.Text)"
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    exit 0
}

if ($roleCheck.Text -ne '1') {
    $escapedPass = $dbPass.Replace("'", "''")
    $result = Invoke-AdminSql "CREATE ROLE ""$dbUser"" WITH LOGIN PASSWORD '$escapedPass';"
    if ($result.Code -ne 0) {
        Write-Warn "Nao foi possivel criar o usuario '$dbUser': $($result.Text)"
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        exit 0
    }
    Write-Info "Usuario '$dbUser' criado."
} else {
    Write-Info "Usuario '$dbUser' ja existe."
}

# Banco de dados
$dbCheck = Invoke-AdminQuery "SELECT 1 FROM pg_database WHERE datname = '$dbName'"
if ($dbCheck.Code -ne 0) {
    Write-Warn "Falha ao consultar o banco '$dbName': $($dbCheck.Text)"
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    exit 0
}

$created = $false
if ($dbCheck.Text -ne '1') {
    $result = Invoke-AdminSql "CREATE DATABASE ""$dbName"" OWNER ""$dbUser"";"
    if ($result.Code -ne 0) {
        Write-Warn "Nao foi possivel criar o banco '$dbName': $($result.Text)"
        Write-Warn 'Crie o banco manualmente; o Flyway cuidara das tabelas.'
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        exit 0
    }
    $created = $true
    Write-Info "Banco '$dbName' criado."
} else {
    Write-Info "Banco '$dbName' ja existe."
}

# Garante que o usuario da aplicacao possa criar tabelas.
if (-not $created) {
    $ownerSql = "ALTER DATABASE ""$dbName"" OWNER TO ""$dbUser""; GRANT ALL PRIVILEGES ON DATABASE ""$dbName"" TO ""$dbUser"";"
    $result = Invoke-AdminSql $ownerSql
    if ($result.Code -ne 0) {
        Write-Warn "Nao foi possivel ajustar o proprietario do banco '$dbName'."
    }
}

$schemaSql = "GRANT ALL ON SCHEMA public TO ""$dbUser""; ALTER SCHEMA public OWNER TO ""$dbUser"";"
$output = $schemaSql | & $psqlPath -w -h $hostName -p $port -U $adminUser -d $dbName -v ON_ERROR_STOP=1 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Warn "Nao foi possivel ajustar permissoes do schema public em '$dbName'."
}

# Extensoes usadas pelas migrations antigas (V1/V7). "uuid-ossp" exige
# superusuario, por isso e criada aqui com o usuario administrador para que o
# Flyway possa rodar depois apenas com o usuario da aplicacao.
$extensionSql = 'CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; CREATE EXTENSION IF NOT EXISTS pgcrypto;'
$output = $extensionSql | & $psqlPath -w -h $hostName -p $port -U $adminUser -d $dbName -v ON_ERROR_STOP=1 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Warn "Nao foi possivel criar as extensoes uuid-ossp/pgcrypto em '$dbName'."
    Write-Warn 'Se a V1/V7 ainda nao foi aplicada, crie as extensoes manualmente como superusuario.'
}

Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

Write-Info 'Banco pronto. O Flyway aplicara as migracoes ao iniciar a aplicacao.'
exit 0
