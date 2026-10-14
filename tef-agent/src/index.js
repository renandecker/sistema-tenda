require('dotenv').config();
const express = require('express');
const http = require('http');
const { WebSocketServer } = require('ws');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const winston = require('winston');
const TefClient = require('./TefClient');
const TefSimulator = require('./TefSimulator');

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

const tefClients = new Map();
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

function broadcastToReactClients(message) {
  const data = JSON.stringify(message);
  wss.clients.forEach(client => {
    if (client.readyState === 1) {
      client.send(data);
    }
  });
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
    connectedClients: wss.clients.size,
    pendingRequests: pendingRequests.size
  });
});

app.post('/tef/command', async (req, res) => {
  const { command, params } = req.body;
  const requestId = uuidv4();

  logger.info('Received TEF command:', { command, requestId, params });

  try {
    const promise = tefImplementation.sendCommand(command, params, requestId);
    
    const timeout = setTimeout(() => {
      if (pendingRequests.has(requestId)) {
        pendingRequests.delete(requestId);
        res.status(408).json({ error: 'TEF command timeout', requestId });
      }
    }, 120000);

    pendingRequests.set(requestId, { resolve: null, reject: null, timeout });
    
    const response = await new Promise((resolve, reject) => {
      pendingRequests.get(requestId).resolve = resolve;
      pendingRequests.get(requestId).reject = reject;
    });

    clearTimeout(timeout);
    res.json({ requestId, ...response });
  } catch (error) {
    logger.error('TEF command error:', error);
    res.status(500).json({ error: error.message, requestId });
  }
});

app.post('/tef/cancel', async (req, res) => {
  const { requestId } = req.body;
  logger.info('Cancel TEF request:', requestId);
  
  try {
    await tefImplementation.cancel(requestId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

wss.on('connection', (ws, req) => {
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

const PORT = process.env.PORT || 8080;
const WS_PORT = process.env.WS_PORT || 8081;

server.listen(PORT, () => {
  logger.info(`TEF Agent HTTP server running on port ${PORT}`);
  logger.info(`TEF Agent WebSocket server running on port ${WS_PORT}`);
  logger.info(`Mode: ${USE_SIMULATOR ? 'SIMULATOR' : 'REAL (DLL)'}`);
});

process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down...');
  tefImplementation.shutdown();
  server.close(() => process.exit(0));
});

module.exports = { app, server, wss, tefImplementation };