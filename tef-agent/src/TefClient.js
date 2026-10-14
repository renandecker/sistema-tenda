const EventEmitter = require('events');
const { spawn } = require('child_process');
const path = require('path');

class TefClient extends EventEmitter {
  constructor(dllPath, logger) {
    super();
    this.dllPath = dllPath;
    this.logger = logger;
    this.process = null;
    this.requestId = 0;
    this.pendingCommands = new Map();
    this.initialized = false;
  }

  async initialize() {
    if (this.initialized) return;

    return new Promise((resolve, reject) => {
      const exePath = path.join(path.dirname(this.dllPath), 'CliSiTef.exe');
      
      this.logger.info('Starting TEF DLL process:', exePath);
      
      this.process = spawn(exePath, [], {
        stdio: ['pipe', 'pipe', 'pipe'],
        windowsHide: true
      });

      this.process.stdout.on('data', (data) => {
        this.handleOutput(data.toString());
      });

      this.process.stderr.on('data', (data) => {
        this.logger.error('TEF DLL stderr:', data.toString());
      });

      this.process.on('error', (err) => {
        this.logger.error('TEF DLL process error:', err);
        if (!this.initialized) reject(err);
      });

      this.process.on('exit', (code) => {
        this.logger.warn('TEF DLL process exited:', code);
        this.initialized = false;
      });

      setTimeout(() => {
        this.initialized = true;
        this.logger.info('TEF DLL initialized');
        resolve();
      }, 2000);
    });
  }

  handleOutput(output) {
    this.logger.debug('TEF DLL output:', output);
    
    try {
      const lines = output.trim().split('\n');
      for (const line of lines) {
        if (!line.trim()) continue;
        
        const response = JSON.parse(line);
        
        if (response.requestId && this.pendingCommands.has(response.requestId)) {
          const { resolve } = this.pendingCommands.get(response.requestId);
          this.pendingCommands.delete(response.requestId);
          resolve(response);
          this.emit('response', response.requestId, response);
        } else if (response.event) {
          this.emit('event', response);
        }
      }
    } catch (e) {
      this.logger.warn('Failed to parse TEF output:', output);
    }
  }

  async sendCommand(command, params, requestId) {
    if (!this.initialized) {
      await this.initialize();
    }

    const cmd = {
      requestId,
      command,
      params,
      timestamp: Date.now()
    };

    this.logger.debug('Sending to TEF DLL:', cmd);

    return new Promise((resolve, reject) => {
      this.pendingCommands.set(requestId, { resolve, reject });
      
      this.process.stdin.write(JSON.stringify(cmd) + '\n');

      setTimeout(() => {
        if (this.pendingCommands.has(requestId)) {
          this.pendingCommands.delete(requestId);
          reject(new Error('TEF command timeout'));
        }
      }, 120000);
    });
  }

  async cancel(requestId) {
    if (this.pendingCommands.has(requestId)) {
      const { reject } = this.pendingCommands.get(requestId);
      this.pendingCommands.delete(requestId);
      reject(new Error('Cancelled by user'));
    }
    
    await this.sendCommand('CANCEL', { originalRequestId: requestId }, uuidv4());
  }

  shutdown() {
    if (this.process) {
      this.process.kill();
      this.process = null;
    }
    this.initialized = false;
  }
}

function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

module.exports = TefClient;