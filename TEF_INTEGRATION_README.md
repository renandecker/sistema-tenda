# Integração TEF - Sistema Tenda

Este documento descreve a arquitetura e configuração para integração com TEF (Transferência Eletrônica de Fundos) no Sistema Tenda.

## Arquitetura

```
┌─────────────────┐     HTTP/WS      ┌──────────────────┐     DLL/Serial      ┌─────────────┐
│  React Frontend │ ◄──────────────► │   TEF Agent      │ ◄─────────────────► │  Pinpad     │
│  (Navegador)    │   localhost:8080 │   (Node.js)      │     USB/Serial      │  (Maquininha)│
└─────────────────┘     localhost:8081 └──────────────────┘                     └─────────────┘
         │
         │ REST API
         ▼
┌─────────────────┐
│  Java Backend   │
│  (Spring/Quarkus)│
└─────────────────┘
```

### Componentes

1. **TEF Agent** (`tef-agent/`) - Serviço Node.js rodando no PDV (Windows/Linux)
   - Expõe API HTTP na porta 8080 para comandos
   - Expõe WebSocket na porta 8081 para eventos em tempo real
   - Comunica com DLL do TEF (CliSiTef) ou usa simulador

2. **Frontend TEF Client** (`frontend/src/services/tef/`) - Biblioteca React
   - `TefClient` - Cliente HTTP/WebSocket para comunicação com TEF Agent
   - `useTef` - Hook React para gerenciar estado da transação
   - `TefPaymentModal` - Componente UI para fluxo de pagamento TEF

3. **Backend TEF Service** (`backend/pdv/src/main/java/.../tef/`) - Java
   - `TefService` - Cliente HTTP para TEF Agent
   - `TefResource` - Endpoints REST para frontend
   - Integração com `PagamentoService` para persistência

## Configuração

### 1. TEF Agent (PDV Local)

```bash
cd tef-agent
cp .env.example .env
# Edite .env conforme necessário
npm install
npm start
```

**Variáveis de ambiente importantes:**
- `USE_SIMULATOR=true` - Usa simulador (desenvolvimento)
- `USE_SIMULATOR=false` + `TEF_DLL_PATH` - Usa DLL real (produção)
- `TEF_DLL_PATH=C:\Program Files\Software Express\SiTef\CliSiTef64.dll`

### 2. Frontend

```bash
# Copie as variáveis para .env.local
cp .env.tef.example .env.local
```

### 3. Backend

Adicione ao `application.properties`:

```properties
tef.agent.url=http://localhost:8080
tef.timeout.seconds=120
```

## Fluxo de Pagamento TEF

### 1. Iniciar Transação (Frontend → Backend → TEF Agent)

```typescript
// Frontend chama useTef().iniciarTransacao()
const transacao = await tef.iniciarTransacao({
  valor: 10000, // centavos
  tipoTransacao: 'DEBITO',
  bandeira: 'ALELO_ALIMENTACAO',
  documento: '123456'
});
```

### 2. Eventos em Tempo Real (TEF Agent → Frontend via WebSocket)

```
AGUARDANDO_CARTAO  → "Insira ou aproxime o cartão"
PROCESSANDO        → "Processando transação..."
AGUARDANDO_SENHA   → "Digite a senha no pinpad"
AUTORIZANDO        → "Autorizando com a administradora..."
AUTORIZADA         → "Transação aprovada!" (retorna NSU, código auth, comprovantes)
NEGADA             → "Transação negada: Saldo insuficiente"
```

### 3. Confirmar Transação (Frontend → Backend → TEF Agent)

```typescript
// Após AUTORIZADA, operador clica "Confirmar Transação"
const comprovante = await tef.confirmarTransacao();
```

### 4. Desfazer/Cancelar (se necessário)

```typescript
// Antes de confirmar, operador pode cancelar
await tef.desfazerTransacao();
```

### 5. Persistência no Backend

O `PagamentoService` recebe o `tefRequestId` e:
1. Chama `tefService.confirmarTransacao(tefRequestId)` 
2. Salva `nsu`, `codigoAutorizacao`, `bandeiraCartao`, `comprovante`
3. Atualiza `valorPago` da venda

## Bandeiras Suportadas

Todas as 9 bandeiras configuradas em `frontend/src/constants/bandeiras.ts`:

| Código | Nome |
|--------|------|
| `ALELO_ALIMENTACAO` | Alelo Alimentação |
| `ALELO_REFEICAO` | Alelo Refeição |
| `ALELO_TUDO` | Alelo Tudo (Flexível) |
| `BANRICOMPRAS_BANRICARD` | Banricompras & Banricard |
| `TICKET_EDENRED` | Ticket (Edenred) |
| `PLUXEE` | Pluxee (antiga Sodexo) |
| `VR_BENEFICIOS` | VR Benefícios |
| `GREEN_CARD` | Green Card |
| `VALECARD_VOLUS` | ValeCard / Vólus |

## Formas de Pagamento TEF

| Frontend | Backend (TipoTransacaoTef) |
|----------|----------------------------|
| `CARTAO_DEBITO` | `DEBITO` |
| `CARTAO_CREDITO` (1x) | `CREDITO_A_VISTA` |
| `CARTAO_CREDITO` (2x+) | `CREDITO_PARCELADO` |
| `VALE_ALIMENTACAO` | `VALE_ALIMENTACAO` |
| `VALE_REFEICAO` | `VALE_REFEICAO` |
| `VALE_CULTURA` | `VALE_CULTURA` |

## Produção - Checklist

- [ ] Contrato com provedor TEF (SiTef/Software Express, Cappta, PayGo, Stone/Linx)
- [ ] Pinpad certificado (Gertec, Verifone, Ingenico) com criptografia ABECS
- [ ] Instalação da DLL do provedor no PDV (`CliSiTef32.dll` ou `CliSiTef64.dll`)
- [ ] Configuração `TEF_DLL_PATH` no TEF Agent
- [ ] `USE_SIMULATOR=false` no `.env`
- [ ] Homologação com provedor (testes: queda energia, sem saldo, cancelamento, sem papel)
- [ ] Certificado SSL para WebSocket (wss://) se frontend em HTTPS
- [ ] Configuração de firewall: portas 8080/8081 acessíveis no localhost

## Desenvolvimento - Simulador

Com `USE_SIMULATOR=true` (padrão):
- Simula todo fluxo TEF sem hardware
- Aprova ~90% das transações aleatoriamente
- Gera NSU, código autorização e comprovantes fake
- Útil para desenvolvimento e testes de UI

## Troubleshooting

### "Mixed Content" (HTTPS frontend → HTTP TEF Agent)
- Use `wss://` e `https://` no TEF Agent com certificado auto-assinado
- Ou configure `VITE_TEF_WS_URL=ws://localhost:8081` e `VITE_TEF_HTTP_URL=http://localhost:8080` no frontend

### TEF Agent não conecta no Pinpad
- Verifique `TEF_DLL_PATH` correto
- Verifique drivers do pinpad (USB/Serial)
- Teste com ferramenta do provedor (ex: SiTef Teste)

### WebSocket não conecta
- Verifique porta 8081 livre
- Firewall/antivírus pode bloquear
- Logs no console do navegador e no TEF Agent

## Endpoints Backend

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/pdv/tef/iniciar` | Inicia transação TEF |
| POST | `/pdv/tef/confirmar?requestId=` | Confirma transação |
| POST | `/pdv/tef/desfazer?requestId=` | Desfaz transação |
| GET | `/pdv/tef/comprovante?requestId=&via=CLIENTE` | Reimprime comprovante |
| POST | `/pdv/tef/webhook/evento` | Webhook para eventos TEF Agent |

## Estrutura de Arquivos

```
sistema-tenda/
├── tef-agent/                    # Serviço Node.js local (PDV)
│   ├── src/
│   │   ├── index.js             # Servidor Express + WebSocket
│   │   ├── TefClient.js         # Cliente DLL real (JNI/JNA)
│   │   └── TefSimulator.js      # Simulador para desenvolvimento
│   ├── package.json
│   └── .env.example
│
├── frontend/
│   └── src/services/tef/
│       ├── types.ts             # Types TypeScript
│       ├── TefClient.ts         # Cliente HTTP/WS
│       ├── useTef.ts            # Hook React
│       └── index.ts
│   └── src/components/tef/
│       └── TefPaymentModal.tsx  # Modal UI pagamento TEF
│
└── backend/pdv/src/main/java/br/com/sistematenda/pdv/tef/
    ├── TipoTransacaoTef.java
    ├── StatusTransacaoTef.java
    ├── TefIniciarTransacaoRequest.java
    ├── TefTransacaoResponse.java
    ├── TefEventoResponse.java
    ├── TefService.java          # Cliente HTTP para TEF Agent
    └── TefResource.java         # REST endpoints
```