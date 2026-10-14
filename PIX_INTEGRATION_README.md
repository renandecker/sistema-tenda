# Integração PIX Multi-Provider - Sistema Tenda

Este projeto implementa uma integração completa de PIX com suporte a múltiplos provedores (PSPs), seguindo as melhores práticas de arquitetura e segurança.

## Provedores Suportados

| Provedor | Tipo | Autenticação | Sandbox | Webhook |
|----------|------|--------------|---------|---------|
| **Efí (Gerencianet)** | Fintech | Client ID/Secret + Certificado P12 | ✅ | ✅ |
| **Mercado Pago** | Fintech | Access Token | ✅ | ✅ (HMAC) |
| **Asaas** | Fintech | API Key | ✅ | ✅ (Token) |
| **Itaú** | Banco (Open Finance) | mTLS + OAuth2 | ✅ | ✅ (HMAC) |
| **Bradesco** | Banco (Open Finance) | mTLS + OAuth2 | ✅ | ✅ (HMAC) |

## Arquitetura

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   React     │────▶│  Java/Quarkus │────▶│    PSP      │
│  Frontend   │     │   Backend     │     │  (Efí/MP/   │
│             │     │               │     │  Asaas/     │
│ - QR Code   │     │ - PixService  │     │  Itaú/      │
│ - Polling   │     │ - Providers   │     │  Bradesco)  │
│ - Copy/Paste│     │ - Webhooks    │     │             │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  PostgreSQL  │
                    │  - Pagamentos│
                    │  - Webhooks  │
                    └──────────────┘
```

## Fluxo de Pagamento

1. **Frontend** solicita pagamento PIX para uma venda
2. **Backend** cria cobrança no PSP via `PixProviderFactory`
3. **PSP** retorna QR Code + "Pix Copia e Cola"
4. **Frontend** exibe QR Code e inicia polling
5. **Cliente** paga via app do banco
6. **PSP** dispara webhook para `/api/pix/webhook/{provider}`
7. **Backend** valida assinatura e atualiza status
8. **Frontend** detecta pagamento via polling/webhook

## Configuração

### 1. application.properties

Configure o provedor desejado em `backend/pdv/src/main/resources/application.properties`:

```properties
# Escolha UM provedor:
pix.provider=EFI                    # EFI, MERCADO_PAGO, ASAAS, ITAU, BRADESCO
pix.sandbox=true                    # true para homologação
pix.chave-pix=sua-chave@exemplo.com # Chave PIX cadastrada no PSP
pix.webhook-base-url=https://seu-dominio.com
```

### 2. Configurações por Provedor

#### Efí (Gerencianet)
```properties
pix.efi.client-id=seu_client_id
pix.efi.client-secret=seu_client_secret
pix.efi.certificado-p12-path=/path/certificado.p12
pix.efi.certificado-senha=senha123
```

#### Mercado Pago
```properties
pix.mercado-pago.access-token=APP_USR-xxxxx
pix.mercado-pago.public-key=APP_USR-xxxxx
pix.mercado-pago.webhook-secret=seu_webhook_secret
```

#### Asaas
```properties
pix.asaas.api-key=SEU_API_KEY
pix.asaas.webhook-token=SEU_WEBHOOK_TOKEN
```

#### Itaú/Bradesco (Open Finance)
```properties
pix.itau.client-id=seu_client_id
pix.itau.client-secret=seu_client_secret
pix.itau.certificado-path=/path/certificado.p12
pix.itau.certificado-senha=senha123
pix.itau.chave-pix=chave@itau.com
pix.itau.ambiente=sandbox
```

## Endpoints da API

### Gerar PIX para Venda
```http
POST /api/pdv/vendas/{vendaId}/pix/gerar?valor=100.00
```

### Consultar Status
```http
GET /api/pdv/vendas/{vendaId}/pix/status?txid=TXID123
```

### Webhooks (configurar no painel do PSP)
```
POST /api/pix/webhook/efi
POST /api/pix/webhook/mercadopago
POST /api/pix/webhook/asaas
POST /api/pix/webhook/openfinance
```

## Frontend - Componente PixQrCode

```tsx
import { PixQrCode, PixPaymentModal } from '@/components/pix';

// Modal completo para pagamento PIX
<PixPaymentModal
  open={pixOpen}
  onClose={() => setPixOpen(false)}
  vendaId="uuid-da-venda"
  valor={150.00}
  onPaymentConfirmed={() => console.log('Pago!')}
/>

// Componente standalone QR Code
<PixQrCode
  txid="TXID123"
  valor={150.00}
  pixCopiaECola="00020126580014br.gov.bcb.pix..."
  qrCodeBase64="iVBORw0KGgoAAAANSUhEUg..."
  status="ATIVA"
  expiracao="2024-12-31T23:59:59"
  onStatusChange={(status) => console.log(status)}
/>
```

## Hook usePix

```tsx
const { 
  pixData, 
  loading, 
  generatePix, 
  consultarStatus, 
  startPolling,
  copyPixCode 
} = usePix();

// Gerar PIX
const pix = await generatePix(vendaId, valor);

// Iniciar polling automático
const stopPolling = startPolling(vendaId, pix.txid, 5000);

// Copiar código
await copyPixCode(pix.pixCopiaECola);
```

## Segurança

### Validação de Webhook
Cada provedor implementa validação de assinatura:
- **Efí**: Verificação via SDK oficial
- **Mercado Pago**: HMAC-SHA256 com webhook secret
- **Asaas**: Token de validação no header
- **Open Finance**: HMAC-SHA256 com client_secret

### Idempotência
O sistema verifica se o pagamento já foi processado antes de atualizar:
```java
if (pagamento.status == StatusPagamento.AUTORIZADO) {
    return; // Já processado
}
```

### mTLS (Bancos Tradicionais)
Para Itaú/Bradesco, configure o certificado PKCS#12:
```java
// Configurado automaticamente via PixConfig
SSLContext sslContext = createSSLContext(certPath, certPassword);
```

## Desenvolvimento Local

### 1. Expor webhook local com ngrok
```bash
ngrok http 8080
# Configure webhook URL no PSP: https://abc123.ngrok.io/api/pix/webhook/efi
```

### 2. Testar em Sandbox
- Todos provedores oferecem ambiente de testes
- Use chaves PIX de teste fornecidas pelo PSP
- Simule pagamentos via painel do PSP

### 3. Certificados mTLS
```bash
# Converter .crt/.key para .p12 (Itaú/Bradesco)
openssl pkcs12 -export -in certificado.crt -inkey chave.key -out certificado.p12 -name "pix"
```

## Estrutura de Arquivos

```
backend/
├── common/
│   └── src/main/java/br/com/sistematenda/common/
│       ├── config/PixConfig.java           # Configuração centralizada
│       ├── enums/PixProvider.java          # Enum dos provedores
│       ├── enums/PixStatus.java            # Status normalizados
│       └── pix/
│           ├── PixProviderService.java     # Interface comum
│           ├── PixChargeRequest.java       # Request criação
│           ├── PixChargeResponse.java      # Response criação
│           ├── PixWebhookNotification.java # Notificação webhook
│           └── PixException.java           # Exceção padronizada
│
└── pdv/
    └── src/main/java/br/com/sistematenda/pdv/
        ├── pix/
        │   ├── EfiPixProvider.java         # Implementação Efí
        │   ├── MercadoPagoPixProvider.java # Implementação Mercado Pago
        │   ├── AsaasPixProvider.java       # Implementação Asaas
        │   ├── OpenFinancePixProvider.java # Implementação Itaú/Bradesco
        │   ├── PixProviderFactory.java     # Factory pattern
        │   └── PixService.java             # Orquestração
        ├── resource/
        │   ├── PixResource.java            # Endpoints REST
        │   └── VendaResource.java          # Endpoint /pix/gerar
        └── service/
            └── PagamentoService.java       # Integração com fluxo existente

frontend/
└── src/
    ├── components/pix/
    │   ├── PixQrCode.tsx                   # Componente QR Code + Polling
    │   └── index.ts
    ├── hooks/
    │   └── usePix.ts                       # Hook para lógica PIX
    ├── pages/pdv/
    │   └── PdvPaymentPage.tsx              # Página de pagamentos PDV
    └── services/
        └── api.ts                          # Endpoints API PIX
```

## Troubleshooting

### Webhook não recebe notificações
1. Verifique se `pix.webhook-base-url` está acessível publicamente
2. Confirme URL no painel do PSP (ex: `https://dominio.com/api/pix/webhook/efi`)
3. Teste com `curl -X POST https://seu-dominio.com/api/pix/webhook/efi -d '{}'`

### Erro de certificado mTLS
```bash
# Verificar certificado
openssl pkcs12 -info -in certificado.p12

# Testar conexão
curl --cert certificado.p12:senha https://api.sandbox.itau.com.br/pix/v2/cob
```

### Polling não atualiza
- Verifique se `txid` está correto
- Confirme se o PSP suporta consulta por txid
- Verifique logs do backend: `quarkus.log.category."br.com.sistematenda.pdv.pix".level=DEBUG`

## Migração entre Provedores

A arquitetura permite trocar provedor apenas alterando configuração:

```properties
# Antes: Efí
pix.provider=EFI

# Depois: Mercado Pago
pix.provider=MERCADO_PAGO
# (configure as credenciais do MP)
```

O código da aplicação não muda - apenas a implementação injetada via `PixProviderFactory`.

## Testes

```bash
# Backend
cd backend
./mvnw test -pl pdv

# Frontend
cd frontend
npm test
```

## Referências

- [Banco Central - API Pix](https://www.bcb.gov.br/estabilidadefinanceira/pix)
- [Efí API Docs](https://dev.gerencianet.com.br/docs/pix)
- [Mercado Pago Pix](https://www.mercadopago.com.br/developers/pt/docs/checkout-api/pix)
- [Asaas Pix](https://docs.asaas.com/reference/pix-1)
- [Open Finance Brasil](https://openfinancebrasil.org.br/)