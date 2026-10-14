# TEF Agent - Serviço Local para Integração com Pinpad

Serviço Node.js que roda na máquina do PDV (Windows/Linux) e faz a ponte entre:
- **Frontend React** (via HTTP/WebSocket em `localhost`)
- **Pinpad/Maquininha** (via DLL do provedor TEF: CliSiTef, Cappta, etc.)

## Instalação

```bash
cd tef-agent
npm install
cp .env.example .env
# Edite .env conforme necessário
```

## Configuração (.env)

```env
# Portas
PORT=8080          # HTTP API
WS_PORT=8081       # WebSocket

# Modo de operação
USE_SIMULATOR=true  # true = simulador (dev), false = DLL real (prod)

# DLL Real (quando USE_SIMULATOR=false)
TEF_DLL_PATH=C:\Program Files\Software Express\SiTef\CliSiTef64.dll

# Configurações do provedor (produção)
TEF_PROVIDER=SITEF           # SITEF|CAPPTA|PAYGO|STONE|LINX
TEF_CODIGO_CREDENCIADORA=123456
TEF_TERMINAL_ID=001

# Pinpad
PINPAD_TIPO=GERTEC           # GERTEC|VERIFONE|INGENICO
PINPAD_PORTA=USB             # USB|SERIAL|TCP
# PINPAD_IP=192.168.1.100    # Se TCP
# PINPAD_PORTA_TCP=10001     # Se TCP
```

## Execução

### Desenvolvimento (com simulador)
```bash
npm run dev
# ou
USE_SIMULATOR=true npm start
```

### Produção (com DLL real)
```bash
USE_SIMULATOR=false TEF_DLL_PATH="C:\Program Files\Software Express\SiTef\CliSiTef64.dll" npm start
```

### Como serviço Windows (produção)
```bash
# Usar PM2 ou NSSM
npm install -g pm2
pm2 start src/index.js --name tef-agent
pm2 startup
pm2 save
```

## API HTTP (Porta 8080)

### Health Check
```
GET /health
```
Response:
```json
{
  "status": "ok",
  "mode": "simulator|real",
  "connectedClients": 1,
  "pendingRequests": 0
}
```

### Enviar Comando TEF
```
POST /tef/command
Content-Type: application/json

{
  "command": "INICIAR_TRANSACAO",
  "params": {
    "valor": 10000,
    "tipoTransacao": "DEBITO",
    "parcelas": 1,
    "bandeira": "ALELO_ALIMENTACAO",
    "documento": "123456"
  }
}
```

Response imediato:
```json
{
  "requestId": "abc-123",
  "success": true,
  "message": "Transação iniciada. Aguarde eventos via WebSocket.",
  "documento": "123456"
}
```

### Cancelar Transação
```
POST /tef/cancel
Content-Type: application/json

{ "requestId": "abc-123" }
```

## WebSocket (Porta 8081)

### Conexão
```javascript
const ws = new WebSocket('ws://localhost:8081');
ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  console.log(msg.type, msg);
};
```

### Eventos Recebidos

#### Conectado
```json
{
  "type": "CONNECTED",
  "mode": "simulator",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### Evento de Transação (tempo real)
```json
{
  "type": "TEF_EVENT",
  "requestId": "abc-123",
  "event": "AGUARDANDO_CARTAO",
  "message": "Insira ou aproxime o cartão",
  "data": {},
  "timestamp": "2024-01-15T10:30:01.000Z"
}
```

Eventos possíveis:
- `AGUARDANDO_CARTAO` - Aguardando inserção/aproximação do cartão
- `PROCESSANDO` - Processando
- `AGUARDANDO_SENHA` - Aguardando digitação da senha
- `AUTORIZANDO` - Comunicando com administradora
- `AUTORIZADA` - **Sucesso!** Contém `nsu`, `codigoAutorizacao`, `comprovanteCliente`, `comprovanteEstabelecimento`
- `NEGADA` - Negada, contém `motivo`
- `CANCELADA` - Cancelada pelo operador
- `ERRO` - Erro de comunicação

#### Resposta de Comando
```json
{
  "type": "TEF_RESPONSE",
  "requestId": "abc-123",
  "success": true,
  "data": { ... }
}
```

## Comandos Suportados

| Comando | Parâmetros | Descrição |
|---------|------------|-----------|
| `INICIAR_TRANSACAO` | valor, tipoTransacao, parcelas, bandeira, documento, cpfCnpjConsumidor | Inicia nova transação |
| `CONFIRMAR_TRANSACAO` | originalRequestId | Confirma transação autorizada |
| `DESFAZER_TRANSACAO` | originalRequestId | Desfaz/cancela transação |
| `REIMPRIMIR_COMPROVANTE` | originalRequestId, via (CLIENTE\|ESTABELECIMENTO) | Reimprime comprovante |
| `CONSULTAR_STATUS` | originalRequestId | Consulta status da transação |

## Integração com DLL Real (Produção)

O arquivo `src/TefClient.js` é um **exemplo base** para integração com DLL via JNI/JNA.

### Opções de Integração:

1. **JNA (Java Native Access) + Java Wrapper** - Recomendado
   - Criar pequeno JAR Java que carrega DLL via JNA
   - TEF Agent chama via `spawn` ou HTTP interno

2. **Node-FFI / koffi** - Direto do Node.js
   - `npm install koffi`
   - Carregar DLL e chamar funções exportadas

3. **Executável Intermediário (C#/Go/Rust)**
   - Compilar exe que usa DLL nativamente
   - TEF Agent comunica via stdin/stdout JSON

### Exemplo com koffi (Node.js direto):

```javascript
// src/TefClientReal.js
const koffi = require('koffi');
const path = require('path');

const dllPath = process.env.TEF_DLL_PATH;
const lib = koffi.load(dllPath);

// Definir funções exportadas da DLL (exemplo SiTef)
const IniciarTransacao = lib.func('int IniciarTransacao(char* params, char* response, int maxLen)');
const ConfirmarTransacao = lib.func('int ConfirmarTransacao(char* nsu, char* response, int maxLen)');
// ... outras funções

async function sendCommand(command, params) {
  const paramsJson = JSON.stringify(params);
  const buffer = Buffer.alloc(8192);
  const result = IniciarTransacao(paramsJson, buffer, buffer.length);
  return JSON.parse(buffer.toString().trim());
}
```

## Logs

Logs são salvos em:
- Console (stdout)
- Arquivo `tef-agent.log` (rotação automática via Winston)

Níveis: `error`, `warn`, `info`, `debug`
Configure via `LOG_LEVEL=debug`

## Testes

```bash
# Testar health
curl http://localhost:8080/health

# Testar comando (simulador)
curl -X POST http://localhost:8080/tef/command \
  -H "Content-Type: application/json" \
  -d '{"command":"INICIAR_TRANSACAO","params":{"valor":1000,"tipoTransacao":"DEBITO"}}'
```

## Estrutura

```
tef-agent/
├── src/
│   ├── index.js           # Servidor principal (Express + WS)
│   ├── TefClient.js       # Cliente DLL real (template JNA/koffi)
│   └── TefSimulator.js    # Simulador completo para dev
├── package.json
├── .env.example
└── tef-agent.log          # Logs (criado em runtime)
```

## Troubleshooting

### "Cannot find module" / DLL não carrega
- Verifique `TEF_DLL_PATH` aponta para arquivo `.dll` existente
- Arquitetura: DLL 64-bit precisa Node.js 64-bit
- Dependências: Visual C++ Redistributable instalado

### Porta em uso
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F
```

### Firewall/Antivírus
- Libere portas 8080 e 8081 para localhost
- Adicione exceção para `node.exe` e pasta do projeto

### Mixed Content (Frontend HTTPS)
Se frontend roda em `https://`, navegador bloqueia `ws://localhost`:
1. Use certificado auto-assinado no TEF Agent (HTTPS/WSS)
2. Ou configure frontend para `http://localhost` em desenvolvimento
3. Em produção, use proxy reverso (nginx) com SSL terminando no proxy