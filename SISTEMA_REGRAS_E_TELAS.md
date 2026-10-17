# Documentação de Regras e Telas - Sistema Tenda

Este documento descreve detalhadamente a arquitetura, os módulos, as telas e as regras de negócio que compõem o **Sistema Tenda** (Plataforma integrada de automação comercial para varejo, hortifrúti, buffet e PDV).

---

## 1. Visão Geral da Arquitetura

O Sistema Tenda é construído em uma arquitetura modular moderna:
* **Backend:** Microsserviços em Java (Quarkus) organizados por domínios (`pdv`, `fruteira`, `buffet`, `estoque`, `fiscal`, `common`).
* **Frontend:** Aplicação Single Page Application (SPA) em React com TypeScript, Material-UI (MUI) e React Router DOM.
* **Integrações de Hardware/Serviços:** 
  * **TEF Agent:** Serviço Node.js local no PDV para comunicação com Pinpads (CliSiTef / Simulador) via HTTP/WebSocket.
  * **PIX Multi-Provider:** Integração nativa com múltiplos PSPs (Efí, Mercado Pago, Asaas, Itaú, Bradesco).

---

## 2. Módulos e Telas do Sistema

### 2.1. Dashboard (`/dashboard`)
* **Descrição:** Tela principal de visão gerencial e indicadores em tempo real.
* **Funcionalidades:**
  * Indicadores de vendas do dia (faturamento, ticket médio, quantidade de vendas).
  * Gráficos de fluxo de vendas por período.
  * Alertas rápidos de estoque crítico e vencimentos próximos.

### 2.2. Módulo PDV (Ponto de Venda)

#### A. Frente de Caixa / Vendas (`/pdv`)
* **Descrição:** Tela principal de operação do operador de caixa para lançamento de itens e fechamento de vendas.
* **Funcionalidades:**
  * Identificação de cliente e operador.
  * Leitura de código de barras ou busca de produtos.
  * Lançamento de peso proveniente de balança (integração com etiquetas de balança).
  * Aplicação de descontos por item ou subtotal.
  * Tela de Pagamento (`PdvPaymentPage`) com suporte a Múltiplas Formas de Pagamento:
    * **Dinheiro:** Cálculo automático de troco.
    * **PIX:** Exibição de QR Code dinâmico e "Pix Copia e Cola" com polling automático de confirmação.
    * **TEF (Cartão Crédito/Débito/Alimentação):** Integração com Pinpad via modal de TEF (`TefPaymentModal`), acompanhando estados (`AGUARDANDO_CARTAO`, `PROCESSANDO`, `AGUARDANDO_SENHA`, `AUTORIZADA`, `NEGADA`).

#### B. Histórico de Vendas (`/vendas`)
* **Descrição:** Consulta e auditoria de vendas realizadas.
* **Funcionalidades:**
  * Filtros por período, operador, caixa e forma de pagamento.
  * Visualização de itens da venda e comprovantes.
  * Opção de estorno/cancelamento de venda (conforme permissão de gerente).

#### C. Sessões de Caixa (`/sessoes-caixa`)
* **Descrição:** Gestão do ciclo de vida do caixa.
* **Funcionalidades:**
  * **Abertura de Caixa:** Informação de fundo de troco inicial.
  * **Movimentações:** Registro de Sangrias (retiradas) e Suprimentos (entradas).
  * **Fechamento de Caixa:** Conferência cega ou informada de valores, apuração de quebra de caixa e encerramento da sessão.

---

### 2.3. Módulo Fruteira / Hortifrúti

#### A. Produtos da Fruteira (`/fruteira/produtos`)
* **Descrição:** Cadastro específico para produtos de hortifrúti e açougue (peso/unidade).
* **Funcionalidades:**
  * Gestão de códigos PLU de balança.
  * Definição de validade e perecibilidade.

#### B. Perdas e Quebras (`/fruteira/perdas`)
* **Descrição:** Registro de descarte de produtos perecíveis por deterioração ou dano.
* **Funcionalidades:**
  * Lançamento de quantidade perdida, motivo e impacto financeiro.
  * Baixa automática no estoque correspondente.

#### C. Etiquetas de Balança (`/fruteira/etiquetas`)
* **Descrição:** Integração e geração de carga de preços para balanças comerciais (Toledo, Filizola, Urano).
* **Funcionalidades:**
  * Cadastro de formato de etiquetas e envio de carga de produtos.

#### D. Tabelas de Preço (`/fruteira/tabelas-preco`)
* **Descrição:** Gestão de precificação diferenciada por atacado/varejo ou sazonalidade.

---

### 2.4. Módulo Buffet / Restaurante

#### A. Comandas (`/buffet/comandas`)
* **Descrição:** Controle de comandas eletrônicas ou cartões de consumo por peso/livre.
* **Funcionalidades:**
  * Abertura, adição de pesagens/consumo e fechamento de comanda.

#### B. Mesas (`/buffet/mesas`)
* **Descrição:** Gestão de ocupação do salão e atendimento de mesas.

#### C. Produtos do Buffet (`/buffet/produtos`)
* **Descrição:** Cadastro de insumos e pratos servidos no buffet.

#### D. Receitas / Ficha Técnica (`/buffet/receitas`)
* **Descrição:** Composição de pratos a partir de insumos do estoque.
* **Funcionalidades:**
  * Definição de proporções e baixa automática de estoque de insumos no momento da venda/preparo.

#### E. Transferências de Estoque (`/buffet/transferencias`)
* **Descrição:** Movimentação interna entre o almoxarifado central e a cozinha/buffet.

---

### 2.5. Módulo Estoque

#### A. Gestão de Estoque (`/estoque`)
* **Descrição:** Consulta de saldos atuais por depósito/loja.

#### B. Inventário (`/estoque/inventario`)
* **Descrição:** Contagem física de mercadorias (geral ou rotativo).
* **Funcionalidades:**
  * Lançamento de contagens, conciliação e ajuste de divergências de estoque.

#### C. Alertas de Estoque (`/estoque/alertas`)
* **Descrição:** Monitoramento automático de rupturas e proximidade de vencimento.

#### D. Defeitos (`/estoque/defeitos`)
* **Descrição:** Registro de mercadorias avariadas para devolução a fornecedores.

---

### 2.6. Módulo Fiscal & Gerencial

#### A. Emissão de Notas (NFC-e / NF-e) (`/fiscal/nfe`)
* **Descrição:** Transmissão de notas fiscais eletrônicas ao Sefaz.
* **Funcionalidades:**
  * Inutilização, cancelamento e consulta de notas autorizadas.

#### B. SPED Fiscal / EFD (`/fiscal/sped-fiscal`)
* **Descrição:** Geração de arquivos digitais para prestação de contas ao fisco (ICMS/IPI).

#### C. DRE - Demonstração do Resultado (`/fiscal/dre`)
* **Descrição:** Relatório contábil gerencial de receitas, custos e despesas por período.

#### D. Ativos Imobilizados (`/fiscal/ativos`)
* **Descrição:** Controle patrimonial de bens da empresa.

#### E. Conciliação de Cartões (`/fiscal/conciliacao-cartao`)
* **Descrição:** Conferência entre as vendas registradas via TEF/Cartão e os extratos das adquirentes.

---

### 2.7. Segurança & Auditoria

#### A. Autenticação e Perfil (`/login`, `/perfil`)
* **Descrição:** Controle de acesso baseado em credenciais de operadores e gerentes.

#### B. Trilha de Auditoria (`/audit`)
* **Descrição:** Log imutável de operações sensíveis (estornos, descontos elevados, sangrias, alterações de preço).

---

## 3. Regras de Negócio Principais

1. **Sessão de Caixa Obrigatória:** Nenhuma venda pode ser realizada no PDV sem que haja uma sessão de caixa aberta pelo operador logado.
2. **Controle de Estoque em Tempo Real:** Toda venda concluída, baixa por receita de buffet ou registro de perda reduz imediatamente o saldo do estoque do item correspondente.
3. **Idempotência em Pagamentos:** Transações PIX e TEF possuem validação estrita de idempotência no backend para evitar dupla cobrança ou crédito duplicado em caso de falhas de rede.
4. **Alçadas de Autorização:** Operações críticas (como cancelamento de itens já lançados, estornos de vendas e aplicação de descontos acima do limite parametrizado) exigem login de supervisor/gerente.
5. **Validação Fiscal:** A finalização de venda no PDV dispara a geração e emissão do documento fiscal eletrônico (NFC-e) quando configurado em ambiente de produção.
