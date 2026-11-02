require('dotenv').config();
const express = require('express');
const http = require('http');
const { WebSocketServer } = require('ws');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const winston = require('winston');
const TefClient = require('./TefClient');
const TefSimulator = require('./TefSimulator');
const ImpressoraTest = require('./ImpressoraTest');

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'tef-agent.log' })
  ]
});

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

app.use(cors());
app.use(express.json());

const pendingRequests = new Map();

const USE_SIMULATOR = process.env.USE_SIMULATOR === 'true' || !process.env.TEF_DLL_PATH;

let tefImplementation;
if (USE_SIMULATOR) {
  logger.info('Using TEF Simulator (no DLL configured)');
  tefImplementation = new TefSimulator(logger);
} else {
  logger.info('Using real TEF Client with DLL:', process.env.TEF_DLL_PATH);
  tefImplementation = new TefClient(process.env.TEF_DLL_PATH, logger);
}

const impressora = new ImpressoraTest(logger);

function broadcastToReactClients(message) {
  const data = JSON.stringify(message);
  const instancias = [wss, wssSeparado].filter(Boolean);
  instancias.forEach((instancia) => {
    instancia.clients.forEach(client => {
      if (client.readyState === 1) {
        client.send(data);
      }
    });
  });
}

function totalClientesWebSocket() {
  return (wss.clients.size || 0) + (wssSeparado ? wssSeparado.clients.size : 0);
}

function comTimeout(promessa, timeoutMs, mensagem = 'Operação excedeu o tempo limite') {
  let timer;
  const timeout = new Promise((_, reject) => {
    timer = setTimeout(() => reject(new Error(mensagem)), timeoutMs);
  });
  return Promise.race([promessa, timeout]).finally(() => clearTimeout(timer));
}

async function verificarSaudeSistema() {
  const healthCheck = {
    timestamp: new Date().toISOString(),
    tef: { status: 'fora', detail: 'unknown' },
    pix: { status: 'desconhecido', detail: 'PIX é verificado pelo backend do Sistema Tenda' }
  };

  try {
    const tefConectado = Boolean(tefImplementation && tefImplementation.initialized);
    healthCheck.tef = USE_SIMULATOR
      ? { status: 'dentro', detail: 'Simulador ativo (nenhuma transação real)' }
      : { status: tefConectado ? 'dentro' : 'fora', detail: 'DLL TEF ' + (tefConectado ? 'inicializada' : 'não inicializada') };
  } catch (error) {
    healthCheck.tef = { status: 'fora', detail: error.message };
  }

  return healthCheck;
}

function handleTefResponse(requestId, response) {
  const pending = pendingRequests.get(requestId);
  if (pending) {
    pending.resolve(response);
    pendingRequests.delete(requestId);
  }
  broadcastToReactClients({
    type: 'TEF_RESPONSE',
    requestId,
    ...response
  });
}

function handleTefEvent(event) {
  broadcastToReactClients({
    type: 'TEF_EVENT',
    ...event
  });
}

tefImplementation.on('response', handleTefResponse);
tefImplementation.on('event', handleTefEvent);

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    mode: USE_SIMULATOR ? 'simulator' : 'real',
    simulator: USE_SIMULATOR,
    tefInitialized: Boolean(tefImplementation && tefImplementation.initialized),
    printerTarget: process.env.IMPRESSORA_FISCAL_DESTINO || null,
    connectedClients: totalClientesWebSocket(),
    pendingRequests: pendingRequests.size,
    timestamp: new Date().toISOString()
  });
});

app.get('/tef/diagnostico', async (req, res) => {
  try {
    if (typeof tefImplementation.diagnose !== 'function') {
      return res.status(501).json({
        success: false,
        status: 'erro',
        mensagem: 'Implementação TEF não suporta diagnóstico',
        detalhe: 'Atualize o tef-agent'
      });
    }
    const resultado = await comTimeout(
      tefImplementation.diagnose(),
      20000,
      'Diagnóstico TEF excedeu o tempo limite'
    );
    res.json({ ...resultado, modo: USE_SIMULATOR ? 'simulador' : 'real' });
  } catch (error) {
    logger.error('Erro no diagnóstico TEF:', error);
    res.status(500).json({
      success: false,
      status: 'erro',
      mensagem: 'Falha ao diagnosticar o TEF',
      detalhe: error.message,
      modo: USE_SIMULATOR ? 'simulador' : 'real'
    });
  }
});

app.get('/impressora/info', async (req, res) => {
  try {
    const info = await impressora.obterInformacoes();
    res.json({ success: true, ...info });
  } catch (error) {
    logger.error('Erro ao obter informações da impressora:', error);
    res.status(500).json({ success: false, mensagem: error.message });
  }
});

app.post('/impressora/teste', async (req, res) => {
  const { destino, imprimirTeste, largura, timeoutMs } = req.body || {};

  try {
    logger.info('Teste de impressora solicitado', {
      destino: destino || process.env.IMPRESSORA_FISCAL_DESTINO || 'padrão',
      imprimirTeste: imprimirTeste === true
    });

    const resultado = await impressora.executarTeste(destino, {
      imprimirTeste: imprimirTeste === true,
      largura: Number(largura) || undefined,
      timeoutMs: Number(timeoutMs) || undefined
    });

    res.json(resultado);
  } catch (error) {
    logger.error('Erro no teste de impressora:', error);
    res.status(500).json({
      success: false,
      status: 'erro',
      mensagem: 'Erro inesperado no teste de impressora',
      detalhe: error.message,
      destino: destino || 'padrão',
      tempoMs: 0
    });
  }
});

const historicoSaude = [];
const MAX_HISTORICO = 50;

function mapearStatusParaFrontend(status, detail, tipo) {
  const statusMap = {
    'dentro': { status: 'funcionando', cor: 'green' },
    'fora': { status: 'problema', cor: 'red' },
    'desconhecido': { status: 'atencao', cor: 'orange' }
  };
  const mapeado = statusMap[status] || { status: 'atencao', cor: 'orange' };
  const icone = tipo === 'pix' ? '💠' : '📟';
  const mensagem = tipo === 'pix'
    ? (status === 'dentro' ? 'PIX operacional' : 'PIX verificado pelo backend')
    : (status === 'dentro' ? 'TEF operacional' : 'TEF indisponível');
  return {
    status: mapeado.status,
    icone,
    cor: mapeado.cor,
    mensagem,
    detalhe: detail || ''
  };
}

async function executarSaudeCheck(manual = false) {
  const healthCheck = await verificarSaudeSistema();
  const agora = new Date().toISOString();

  const pix = mapearStatusParaFrontend(healthCheck.pix.status, healthCheck.pix.detail, 'pix');
  const tef = mapearStatusParaFrontend(healthCheck.tef.status, healthCheck.tef.detail, 'tef');
  pix.horaVerificacao = agora;
  tef.horaVerificacao = agora;

  const resultado = {
    success: true,
    pix,
    tef,
    ultimaVerificacao: agora,
    manual
  };

  historicoSaude.unshift({ ...resultado, timestamp: agora });
  if (historicoSaude.length > MAX_HISTORICO) {
    historicoSaude.pop();
  }

  return resultado;
}

app.get('/saude-check', async (req, res) => {
  try {
    const resultado = await executarSaudeCheck(false);
    res.json(resultado);
  } catch (error) {
    logger.error('Erro no saude-check GET:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

app.post('/saude-check', async (req, res) => {
  try {
    const resultado = await executarSaudeCheck(true);
    res.json(resultado);
  } catch (error) {
    logger.error('Erro no saude-check POST:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

app.get('/saude-check/historico', (req, res) => {
  res.json({ success: true, historico: historicoSaude });
});

app.post('/tef/command', async (req, res) => {
  const { command, params } = req.body || {};
  const requestId = uuidv4();

  if (!command) {
    return res.status(400).json({ error: 'Campo "command" é obrigatório', requestId });
  }

  logger.info('Received TEF command:', { command, requestId, params });

  const timeoutMs = Number(process.env.TEF_COMMAND_TIMEOUT_MS) || 120000;

  try {
    const response = await comTimeout(
      tefImplementation.sendCommand(command, params, requestId),
      timeoutMs,
      'TEF command timeout'
    );
    res.json({ requestId, ...(response || { success: true }) });
  } catch (error) {
    logger.error('TEF command error:', error);
    const status = /timeout/i.test(error.message || '') ? 408 : 500;
    res.status(status).json({ error: error.message, requestId });
  }
});

app.post('/tef/cancel', async (req, res) => {
  const { requestId } = req.body || {};
  logger.info('Cancel TEF request:', requestId);

  try {
    await tefImplementation.cancel(requestId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

function configurarWebSocket(instancia) {
  instancia.on('connection', (ws, req) => {
    logger.info('React client connected via WebSocket');

    ws.on('message', (data) => {
      try {
        const message = JSON.parse(data);
        if (message.type === 'PING') {
          ws.send(JSON.stringify({ type: 'PONG' }));
        }
      } catch (e) {
        logger.warn('Invalid WS message:', e.message);
      }
    });

    ws.on('close', () => {
      logger.info('React client disconnected');
    });

    ws.send(JSON.stringify({
      type: 'CONNECTED',
      mode: USE_SIMULATOR ? 'simulator' : 'real',
      timestamp: new Date().toISOString()
    }));
  });
}

configurarWebSocket(wss);

const PORT = Number(process.env.PORT) || 8080;
const WS_PORT = Number(process.env.WS_PORT) || 8081;

// O frontend usa por padrão ws://localhost:8081. Mantemos também o WebSocket
// na mesma porta HTTP para facilitar instalações atrás de proxy.
let wssSeparado = null;
if (WS_PORT !== PORT) {
  wssSeparado = new WebSocketServer({ port: WS_PORT });
  configurarWebSocket(wssSeparado);
}

server.listen(PORT, () => {
  logger.info(`TEF Agent HTTP server running on port ${PORT}`);
  logger.info(`TEF Agent WebSocket server running on port ${WS_PORT}`);
  logger.info(`Mode: ${USE_SIMULATOR ? 'SIMULATOR' : 'REAL (DLL)'}`);
  logger.info(`Printer target: ${process.env.IMPRESSORA_FISCAL_DESTINO || 'padrão do Windows'}`);

  // Rotina periódica de saúde
  const saudacaoIntervalo = process.env.SAUDE_INTERVALO;
  const saudacaoPrimeiraEm = process.env.SAUDE_PRIMEIRA_EM || '1m';

  let saudacaoTimer = null;
  let primeiraConferencia = true;

  // Parse interval (30m, 15m, 2h, off desliga)
  const parseIntervalo = (intervalo) => {
    if (!intervalo || intervalo === 'off') {
      return null;
    }
    const match = intervalo.match(/^(\d+)([mh])$/);
    if (!match) {
      logger.warn(`Intervalo inválido: ${intervalo}, usando padrão 1h`);
      return 60 * 60 * 1000; // 1 hour in ms
    }
    const valor = parseInt(match[1], 10);
    const unidade = match[2];
    if (unidade === 'm') {
      return valor * 60 * 1000;
    } else if (unidade === 'h') {
      return valor * 60 * 60 * 1000;
    }
    return 60 * 60 * 1000;
  };

  const intervaloMs = parseIntervalo(saudacaoIntervalo);

  if (intervaloMs) {
    const executarVerificacaoSaude = async () => {
      try {
        const healthCheck = await verificarSaudeSistema();
        broadcastToReactClients({
          type: 'SAUDE_ATUALIZADA',
          ...healthCheck
        });
        logger.info('Verificação de saúde concluída:', healthCheck);
      } catch (error) {
        logger.error('Erro na verificação de saúde:', error);
      }
    };

    const iniciarSaude = () => {
      if (primeiraConferencia) {
        primeiraConferencia = false;
        setTimeout(executarVerificacaoSaude, parseInt(saudacaoPrimeiraEm, 10) || 60000);
      }

      executarVerificacaoSaude();

      saudacaoTimer = setInterval(executarVerificacaoSaude, intervaloMs);
      logger.info(`Rotina de saúde iniciada: a cada ${saudacaoIntervalo}`);
    };

    iniciarSaude();
  } else {
    logger.info('Verificação de saúde desabilitada (SAUDE_INTERVALO off)');
  }
});

process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down...');
  tefImplementation.shutdown();
  server.close(() => process.exit(0));
});
