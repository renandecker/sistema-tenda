# Sistema Tenda - Guia para Gerar Pacote de Produção

Este guia explica como gerar a pasta `producao/` pronta para instalar em um servidor.

**Requisitos por ambiente:**
- **Máquina de desenvolvimento (para GERAR o pacote):** Java 17+, Maven, Node.js 18+ e npm
- **Servidor de produção (para RODAR o sistema):** **APENAS Java 17+ e PostgreSQL**. Não precisa de Node.js, nem de Docker.

## 1) Pré-requisitos

### Na máquina de desenvolvimento

| Ferramenta | Versão | Observação |
|---|---|---|
| [Java JDK](https://adoptium.net/) | 17 ou superior | Para compilar e rodar o backend Quarkus |
| [Apache Maven](https://maven.apache.org/) | 3.9+ | Para empacotar o backend |
| [Node.js](https://nodejs.org/) | 18 LTS ou superior | Para buildar o frontend React + Vite |
| [npm](https://www.npmjs.com/) | 9+ | Geralmente vem junto com Node.js |

### No servidor de produção

| Ferramenta | Versão | Observação |
|---|---|---|
| [Java JRE/JDK](https://adoptium.net/) | 17 ou superior | Único requisito para rodar a aplicação |
| [PostgreSQL](https://www.postgresql.org/) | 16+ | Banco de dados. O Flyway 10 **não suporta PostgreSQL 10**. |

> O banco e as tabelas **não precisam ser criados manualmente**: ao iniciar, os scripts carregam o `.env`, criam o usuário/banco quando possível e o Flyway aplica todas as migrações. Veja a seção 2.1.1.

> O frontend React é compilado com Vite e embutido no backend via `META-INF/resources`. Após gerado, **não existe servidor separado para o frontend**. Tudo roda pelo único arquivo `app/quarkus-run.jar`.

## 2) Configurações que devem ser ajustadas

Antes de gerar o pacote, ajuste os arquivos na raiz da máquina de desenvolvimento:

### 2.1 Arquivo `.env`
Copie `.env.example` para `.env` na raiz e preencha com os valores do seu ambiente:

```properties
# Aplicação
QUARKUS_HTTP_PORT=8080

# Banco de dados usado pela aplicação
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/sistematenda
QUARKUS_DATASOURCE_USERNAME=sistematenda
QUARKUS_DATASOURCE_PASSWORD=sua_senha

# Credenciais administrativas: usadas apenas para criar usuário/banco se faltarem
DB_ADMIN_USER=postgres
DB_ADMIN_PASSWORD=sua_senha_do_postgres
```

O script copia `.env` para a pasta `producao/`. No servidor, edite `producao/.env`
com os dados reais. Se `DB_ADMIN_PASSWORD` ficar vazio, os scripts não tentam
criar o banco (ele precisa existir e ser acessível com as credenciais acima).

### 2.1.1 Criação automática do banco e do schema
Ao iniciar, `start.bat` e `start.sh` carregam o `.env` e, se o `psql` estiver
disponível, criam o usuário e o banco quando ainda não existirem. Em seguida o
**Flyway** aplica todas as migrações e cria/atualiza tabelas, índices e os dados
iniciais (usuário admin, perfis e categorias).

O processo funciona tanto em **banco vazio** quanto em **banco já existente**:

- Banco vazio: o Flyway executa as migrações `V1` até `V8`.
- Banco já existente sem histórico do Flyway: o Flyway cria o histórico na
  versão 7 e executa a `V8`, que garante o schema completo de forma idempotente.
- Banco já migrado: o Flyway apenas valida e aplica migrações pendentes.

Se o `psql` não estiver no `PATH`, o script apenas avisa. Nesse caso, crie o
usuário e o banco manualmente (veja a seção 8) e inicie o sistema normalmente:
o Flyway ainda cria todas as tabelas.

### 2.2 Configuração Quarkus (produção)
Revise `backend/pdv/src/main/resources/application.prod.properties`.

Pontos importantes:
- Configurações de banco de dados corretas
- Porta HTTP
- `quarkus.hibernate-orm.database.generation=none` (o schema é controlado pelo
  Flyway; **não** use `update`)
- Caminhos para certificados (`.pfx`) caso utilize TEF/HTTPS
- Configurações de CORS
- Parâmetros de integrações (TEF, PIX) - veja `TEF_INTEGRATION_README.md` e `PIX_INTEGRATION_README.md`

O script copia esse arquivo para `producao/config/application.properties`.

### 2.3 Certificado (opcional)
Se houver `certificado.pfx` na raiz, ele será copiado para a pasta `producao/` automaticamente.

## 3) Gerando o pacote de produção

### Windows (cmd/PowerShell)
Na raiz do projeto, execute:

```bat
gerar-producao.bat
```

### Linux/Mac (bash)
Na raiz do projeto:

```bash
chmod +x gerar-producao.sh
./gerar-producao.sh
```

### Opções

| Opção | Descrição |
|---|---|
| (padrão) | Gera apenas a pasta `producao/` |
| `--zip` | Gera a pasta `producao/` e um arquivo `sistema-tenda-producao.zip` |
| `--sem-jars` | Não compila o backend com Maven. Útil se já tiver os jars gerados ou para ajustar apenas frontend/arquivos |
| `--com-testes` | Roda os testes do Maven ao compilar o backend (por padrão, usa `-DskipTests`) |

**Exemplos:**
```bash
./gerar-producao.sh --zip
```
```bat
gerar-producao.bat --zip --com-testes
```

O script irá:
1. Verificar pré-requisitos (`java`, `node`, `npm` e `mvn`)
2. Garantir o PostgreSQL de produção em `localhost:5432` (cria/sube o container `sistematenda-db-prod` via `docker-compose.prod.yml`; banco/usuário/senha = `sistematenda`), criando o usuário/banco e ajustando o schema `public` quando possível
3. Limpar o pacote anterior
4. Instalar dependências do frontend se `frontend/node_modules` não existir e rodar `npm run build`
5. Validar `frontend/dist/index.html` e limpar/copiar para `backend/pdv/src/main/resources/META-INF/resources/`
6. Compilar backend com `mvn clean package -DskipTests` (ou com testes)
7. Montar `producao/` com `quarkus-run.jar`, arquivos modelo, `application.properties`, `.env` e `certificado.pfx`
8. Finalizar (e gerar ZIP se `--zip`)

> As migrações do Flyway **não** rodam durante a geração do pacote. Elas são
> aplicadas na primeira inicialização da aplicação no servidor (ou em qualquer
> inicialização seguinte, para migrações pendentes).

> O banco de desenvolvimento continua usando o `docker-compose.yml` na porta `5433`. O `docker-compose.prod.yml` usa um projeto Docker separado (`sistematenda-prod`) e a porta padrão `5432`.

## 4) Como levantar o sistema em produção

Leve **toda** a pasta `producao/` para o servidor de destino.

No servidor, verifique se tem Java e PostgreSQL instalados e configurados.

### Configurar antes de iniciar
1. Edite `producao/.env` com as credenciais corretas do PostgreSQL
   (`QUARKUS_DATASOURCE_*`). Se quiser que o script crie o usuário/banco,
   informe também `DB_ADMIN_USER` e `DB_ADMIN_PASSWORD`.
2. Edite `producao/config/application.properties` conforme necessário (porta, certificados, integrações)
3. O banco **não precisa existir**: os scripts criam o usuário/banco quando o
   `psql` está disponível. Se preferir, crie manualmente antes (seção 8).
4. Se usar certificado TEF/HTTPS, coloque o `.pfx` no local indicado pela configuração

### O que acontece na primeira inicialização
1. O script carrega o `.env`.
2. Com `psql` disponível, cria o usuário/banco se necessário e ajusta o schema `public`.
3. O Flyway aplica todas as migrações (tabelas, índices, dados iniciais).
4. A aplicação começa a atender em `http://IP_DO_SERVIDOR:PORTA`.

Primeiro acesso: **admin@sistematenda.com** / **123456** (troque a senha após o login).

### Windows
```bat
cd producao
start.bat
```

### Linux/Mac
```bash
cd producao
chmod +x start.sh
./start.sh
```

Por baixo dos panos, os scripts executam:
```bash
java -jar app/quarkus-run.jar
```

Acesse no navegador: `http://IP_DO_SERVIDOR:PORTA` (conforme `quarkus.http.port` configurado).

## 5) Dicas e solução de problemas

| Problema | Possível causa | Solução |
|---|---|---|
| `'java' não encontrado` (dev) | Java não no PATH | Instale JDK 17+ e configure PATH |
| `'mvn' não encontrado` (dev) | Maven não no PATH | Instale Maven e configure PATH |
| `'node'/'npm' não encontrado` (dev) | Node não no PATH | Instale Node.js LTS |
| Falha no build do frontend | Erros no React/Vite | Rode `npm install` e `npm run build` manualmente em `frontend/` para ver erros |
| Falha no build do backend | Erros Maven/Java | Veja logs do Maven. Use `--com-testes` para identificar falhas de teste |
| `frontend/dist/index.html` não encontrado | Build não gerou | Verifique se `npm run build` finalizou sem erros |
| Aplicação não conecta ao PostgreSQL | `.env`/`application.properties` incorretos | Verifique URL, usuário, senha e se o banco está acessível |
| `psql não encontrado` / banco não criado | Cliente do PostgreSQL fora do PATH | Crie o banco manualmente (seção 8) ou adicione o `psql` ao PATH. O Flyway ainda cria as tabelas |
| Senha do admin do PostgreSQL inválida | `DB_ADMIN_PASSWORD` incorreta | Ajuste no `.env` ou crie usuário/banco manualmente (seção 8) |
| `Unsupported Database: PostgreSQL 10` | PostgreSQL antigo | Atualize para PostgreSQL 16 (o Flyway 10 não suporta PG10) |
| `Flyway ... failed migration` | Migração anterior falhou | Veja o erro no log, corrija a causa e limpe a entrada com `flyway repair`/recrie o histórico |
| Aplicação não sobe no servidor | Java ausente ou porta em uso | Verifique `java -version` e se a porta está livre |
| Arquivos estáticos não carregam | Frontend não embutido corretamente | Refaça o build garantindo cópia para `META-INF/resources/` |

## 6) Estrutura da pasta `producao/`

```text
producao/
├── app/                        # Backend Quarkus com frontend React embutido
│   ├── quarkus-run.jar
│   └── lib/
├── config/
│   └── application.properties  # Configuração de produção
├── logs/                       # Logs (criada na primeira execução)
├── start.bat
├── start.sh
├── ensure-db.ps1               # Auxiliar do start.bat (cria o banco se necessário)
├── LEIA-ME.txt
├── .env                        # Variáveis de ambiente (se copiado)
├── .env.example
└── certificado.pfx             # Opcional
```

## 7) Arquivos relevantes no projeto

- `gerar-producao.bat` / `gerar-producao.sh` - **Recomendado** para gerar o pacote
- `docker-compose.prod.yml` - PostgreSQL de produção na porta `5432` (container `sistematenda-db-prod`)
- `producao-modelo/` - Modelo de arquivos copiados para o pacote
- `prod.bat` / `prod.sh` - Scripts antigos (mantidos para compatibilidade)
- `run.bat` / `run.sh` - Execução em modo desenvolvimento
- `backend/pdv/src/main/resources/application.prod.properties` - Template de config produção
- `TEF_INTEGRATION_README.md`, `PIX_INTEGRATION_README.md` - Documentação de integrações

---

**Dica:** Sempre teste o pacote gerado em um ambiente similar ao servidor antes de colocar em produção.

## 8) Criação manual do banco (opcional)

Se o `psql` não estiver disponível no servidor (ou você preferir não usar as
credenciais administrativas no `.env`), crie o usuário e o banco manualmente.
Use o `psql` ou o pgAdmin conectado como administrador (`postgres`):

```sql
-- 1. Criar o usuário (role) com a senha
CREATE ROLE sistematenda WITH LOGIN PASSWORD 'sistematenda';

-- 2. Criar o banco de dados pertencente a esse usuário
CREATE DATABASE sistematenda OWNER sistematenda;

-- 3. Garantir que o usuário possa criar tabelas no schema public
\c sistematenda
GRANT ALL ON SCHEMA public TO sistematenda;
ALTER SCHEMA public OWNER TO sistematenda;
```

Depois inicie o sistema normalmente. O Flyway cria todas as tabelas, índices e
os dados iniciais na primeira execução. Não é necessário importar nenhum dump.


