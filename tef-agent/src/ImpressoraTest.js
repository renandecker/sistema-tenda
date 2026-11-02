const net = require('net');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execFile } = require('child_process');
const { promisify } = require('util');

const execFileAsync = promisify(execFile);

const TIMEOUT_PADRAO_MS = 5000;
const LARGURA_PADRAO = 48;

/**
 * Testa a comunicação real com a impressora fiscal/não fiscal configurada.
 *
 * A impressora está fisicamente ligada ao computador do PDV, por isso o teste
 * precisa ser executado pelo agente local (e não pelo backend Quarkus, que pode
 * rodar em outro servidor).
 *
 * Destinos aceitos:
 *  - tcp://192.168.0.50:9100      impressora de rede (raw/ESC-POS)
 *  - COM3 / COM3:9600             porta serial
 *  - /dev/usb/lp0                 dispositivo Linux
 *  - \\servidor\compartilhamento  compartilhamento Windows (raw)
 *  - C:\caminho\arquivo.txt       arquivo/spool
 *  - "EPSON TM-T20"               nome de impressora instalada no Windows
 *  - vazio                        impressora padrão do Windows
 */
class ImpressoraTest {
  constructor(logger) {
    this.logger = logger || console;
  }

  parseDestino(destino) {
    const bruto = destino != null ? String(destino).trim() : '';

    if (!bruto) {
      return { tipo: 'padrao', bruto, descricao: 'Impressora padrão do Windows' };
    }

    if (/^tcp:\/\//i.test(bruto)) {
      const semPrefixo = bruto.substring('tcp://'.length);
      const ultimoDoisPontos = semPrefixo.lastIndexOf(':');
      const host = ultimoDoisPontos > 0 ? semPrefixo.substring(0, ultimoDoisPontos) : semPrefixo;
      const portaTexto = ultimoDoisPontos > 0 ? semPrefixo.substring(ultimoDoisPontos + 1) : '9100';
      const porta = Number.parseInt(portaTexto, 10);
      return {
        tipo: 'tcp',
        bruto,
        host: host.replace(/^\[|\]$/g, ''),
        porta: Number.isFinite(porta) ? porta : 9100,
        descricao: `Rede ${host}:${Number.isFinite(porta) ? porta : 9100}`,
      };
    }

    if (/^serial:\/\//i.test(bruto)) {
      const semPrefixo = bruto.substring('serial://'.length);
      const [porta, baud] = semPrefixo.split(':');
      return { tipo: 'serial', bruto, porta, baud: baud ? Number.parseInt(baud, 10) : null, descricao: `Serial ${porta}` };
    }

    if (/^COM\d+(:\d+)?$/i.test(bruto)) {
      const [porta, baud] = bruto.split(':');
      return { tipo: 'serial', bruto, porta, baud: baud ? Number.parseInt(baud, 10) : null, descricao: `Serial ${porta}` };
    }

    if (bruto.startsWith('/dev/')) {
      return { tipo: 'dispositivo', bruto, caminho: bruto, descricao: `Dispositivo ${bruto}` };
    }

    if (bruto.startsWith('\\\\')) {
      return { tipo: 'compartilhamento', bruto, caminho: bruto, descricao: `Compartilhamento ${bruto}` };
    }

    if (/^[A-Za-z]:[\\/]/.test(bruto)) {
      return { tipo: 'arquivo', bruto, caminho: bruto, descricao: `Arquivo ${bruto}` };
    }

    return { tipo: 'windows', bruto, nome: bruto, descricao: `Impressora Windows "${bruto}"` };
  }

  // ---------------------------------------------------------------------------
  // Testes de conexão
  // ---------------------------------------------------------------------------

  async testarConexao(destino, opcoes = {}) {
    const inicio = Date.now();
    const timeoutMs = opcoes.timeoutMs || TIMEOUT_PADRAO_MS;
    const cfg = this.parseDestino(destino);

    try {
      if (cfg.tipo === 'tcp') {
        const resultado = await this.testarTcp(cfg.host, cfg.porta, timeoutMs);
        return this.montarResultado({
          cfg,
          inicio,
          sucesso: resultado.ok,
          status: resultado.ok ? 'ok' : 'erro',
          mensagem: resultado.ok
            ? `Conexão TCP estabelecida com ${cfg.host}:${cfg.porta}`
            : `Não foi possível conectar em ${cfg.host}:${cfg.porta}`,
          detalhe: resultado.ok ? 'Porta da impressora acessível na rede' : resultado.erro,
        });
      }

      if (cfg.tipo === 'serial') {
        return await this.testarSerial(cfg, inicio, timeoutMs);
      }

      if (cfg.tipo === 'dispositivo' || cfg.tipo === 'arquivo' || cfg.tipo === 'compartilhamento') {
        return await this.testarCaminho(cfg, inicio);
      }

      if (cfg.tipo === 'windows' || cfg.tipo === 'padrao') {
        return await this.testarImpressoraWindows(cfg, inicio, timeoutMs);
      }

      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: 'Destino de impressora não reconhecido',
        detalhe: `Valor informado: ${cfg.bruto || '(vazio)'}`,
      });
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: 'Erro inesperado ao testar a impressora',
        detalhe: e.message,
      });
    }
  }

  testarTcp(host, porta, timeoutMs) {
    return new Promise((resolve) => {
      const socket = new net.Socket();
      let finalizado = false;

      const encerrar = (ok, erro) => {
        if (finalizado) return;
        finalizado = true;
        clearTimeout(timer);
        try { socket.destroy(); } catch { /* ignora */ }
        resolve({ ok, erro });
      };

      const timer = setTimeout(() => encerrar(false, `Timeout de ${timeoutMs} ms ao conectar`), timeoutMs);

      socket.setTimeout(timeoutMs);
      socket.once('connect', () => encerrar(true));
      socket.once('timeout', () => encerrar(false, `Timeout de ${timeoutMs} ms ao conectar`));
      socket.once('error', (err) => encerrar(false, err.message));

      socket.connect(porta, host);
    });
  }

  async testarSerial(cfg, inicio, timeoutMs) {
    const caminho = process.platform === 'win32'
      ? `\\\\.\\${cfg.porta}`
      : cfg.porta;

    try {
      const handle = await this.comTimeout(fs.promises.open(caminho, 'r+'), timeoutMs);
      await handle.close();
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: true,
        status: 'ok',
        mensagem: `Porta serial ${cfg.porta} aberta com sucesso`,
        detalhe: cfg.baud ? `Baud configurado: ${cfg.baud}` : 'Porta acessível pelo sistema operacional',
      });
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: `Não foi possível abrir a porta serial ${cfg.porta}`,
        detalhe: this.mensagemErro(e),
      });
    }
  }

  async testarCaminho(cfg, inicio) {
    try {
      const stat = await fs.promises.stat(cfg.caminho);
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: true,
        status: cfg.tipo === 'dispositivo' ? 'ok' : 'atencao',
        mensagem: `Caminho acessível: ${cfg.caminho}`,
        detalhe: cfg.tipo === 'dispositivo'
          ? 'Dispositivo encontrado'
          : `Caminho encontrado (${stat.isDirectory() ? 'pasta' : 'arquivo'})`,
      });
    } catch (e) {
      if (cfg.tipo === 'compartilhamento') {
        return this.montarResultado({
          cfg,
          inicio,
          sucesso: true,
          status: 'atencao',
          mensagem: `Compartilhamento não validado automaticamente: ${cfg.caminho}`,
          detalhe: 'O teste definitivo é o cupom de teste. Verifique se o PDV tem acesso ao compartilhamento.',
        });
      }
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: `Caminho não encontrado: ${cfg.caminho}`,
        detalhe: this.mensagemErro(e),
      });
    }
  }

  async testarImpressoraWindows(cfg, inicio, timeoutMs) {
    let impressoras;
    try {
      impressoras = await this.listarImpressorasWindows(timeoutMs);
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: 'Não foi possível consultar as impressoras instaladas no Windows',
        detalhe: this.mensagemErro(e),
      });
    }

    const nomeProcurado = cfg.nome ? cfg.nome.toLowerCase() : null;
    const encontrada = nomeProcurado
      ? impressoras.find((imp) => (imp.nome || '').toLowerCase() === nomeProcurado)
      : impressoras.find((imp) => imp.padrao);

    if (!encontrada) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: cfg.nome
          ? `Impressora "${cfg.nome}" não está instalada neste computador`
          : 'Nenhuma impressora padrão definida no Windows',
        detalhe: impressoras.length
          ? `Instaladas: ${impressoras.map((imp) => imp.nome).join(', ')}`
          : 'Nenhuma impressora encontrada no spooler do Windows',
      });
    }

    if (encontrada.offline) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: `Impressora "${encontrada.nome}" está offline`,
        detalhe: 'Verifique cabo, energia e conexão da impressora',
        destino: encontrada.nome,
      });
    }

    return this.montarResultado({
      cfg,
      inicio,
      sucesso: true,
      status: 'ok',
      mensagem: `Impressora encontrada: ${encontrada.nome}`,
      detalhe: encontrada.padrao ? 'Impressora padrão do Windows' : 'Instalada e disponível no spooler',
      destino: encontrada.nome,
    });
  }

  async listarImpressorasWindows(timeoutMs) {
    const script = [
      '$ErrorActionPreference = "Stop"',
      'Get-CimInstance Win32_Printer |',
      '  Select-Object Name, Default, WorkOffline, PrinterStatus |',
      '  ConvertTo-Json -Compress',
    ].join('\n');

    const { stdout } = await execFileAsync(
      'powershell.exe',
      ['-NoProfile', '-NonInteractive', '-ExecutionPolicy', 'Bypass', '-Command', script],
      { timeout: timeoutMs, windowsHide: true, maxBuffer: 1024 * 1024 },
    );

    const texto = (stdout || '').trim();
    if (!texto) return [];

    const dados = JSON.parse(texto);
    const lista = Array.isArray(dados) ? dados : [dados];

    return lista.map((item) => ({
      nome: item.Name,
      padrao: item.Default === true,
      offline: item.WorkOffline === true || item.PrinterStatus === 7,
    }));
  }

  // ---------------------------------------------------------------------------
  // Impressão de teste
  // ---------------------------------------------------------------------------

  async imprimirTeste(destino, opcoes = {}) {
    const inicio = Date.now();
    const timeoutMs = opcoes.timeoutMs || TIMEOUT_PADRAO_MS;
    const cfg = this.parseDestino(destino);
    const conteudo = opcoes.conteudo || this.gerarCupomTeste(opcoes.largura || LARGURA_PADRAO);

    try {
      if (cfg.tipo === 'tcp') {
        const resultado = await this.enviarTcp(cfg.host, cfg.porta, conteudo, timeoutMs);
        return this.montarResultado({
          cfg,
          inicio,
          sucesso: resultado.ok,
          status: resultado.ok ? 'ok' : 'erro',
          mensagem: resultado.ok
            ? `Cupom de teste enviado para ${cfg.host}:${cfg.porta}`
            : `Falha ao enviar cupom para ${cfg.host}:${cfg.porta}`,
          detalhe: resultado.ok ? 'Bytes ESC/POS enviados para a impressora de rede' : resultado.erro,
          impressaoTeste: true,
        });
      }

      if (cfg.tipo === 'dispositivo' || cfg.tipo === 'compartilhamento' || cfg.tipo === 'arquivo') {
        await fs.promises.writeFile(cfg.caminho, conteudo);
        return this.montarResultado({
          cfg,
          inicio,
          sucesso: true,
          status: cfg.tipo === 'arquivo' ? 'atencao' : 'ok',
          mensagem: cfg.tipo === 'arquivo'
            ? `Cupom gravado no arquivo ${cfg.caminho}`
            : `Cupom de teste enviado para ${cfg.caminho}`,
          detalhe: cfg.tipo === 'arquivo'
            ? 'Destino é um arquivo; nenhum papel foi impresso'
            : 'Bytes ESC/POS gravados no destino',
          impressaoTeste: true,
        });
      }

      if (cfg.tipo === 'serial') {
        return await this.imprimirSerial(cfg, conteudo, inicio, timeoutMs);
      }

      if (cfg.tipo === 'windows' || cfg.tipo === 'padrao') {
        return await this.imprimirWindows(cfg, conteudo, inicio, timeoutMs);
      }

      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: 'Destino de impressora não reconhecido',
        detalhe: `Valor informado: ${cfg.bruto || '(vazio)'}`,
      });
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: 'Erro ao enviar o cupom de teste',
        detalhe: this.mensagemErro(e),
        impressaoTeste: true,
      });
    }
  }

  async imprimirSerial(cfg, conteudo, inicio, timeoutMs) {
    const caminho = process.platform === 'win32'
      ? `\\\\.\\${cfg.porta}`
      : cfg.porta;

    let handle;
    try {
      handle = await this.comTimeout(fs.promises.open(caminho, 'w'), timeoutMs);
      await this.comTimeout(handle.writeFile(conteudo), timeoutMs);
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: true,
        status: 'ok',
        mensagem: `Cupom de teste enviado para a porta serial ${cfg.porta}`,
        detalhe: 'Bytes ESC/POS gravados na porta serial',
        impressaoTeste: true,
      });
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: `Falha ao imprimir na porta serial ${cfg.porta}`,
        detalhe: this.mensagemErro(e),
        impressaoTeste: true,
      });
    } finally {
      if (handle) {
        try { await handle.close(); } catch { /* ignora */ }
      }
    }
  }

  async imprimirWindows(cfg, conteudo, inicio, timeoutMs) {
    let impressoras;
    try {
      impressoras = await this.listarImpressorasWindows(timeoutMs);
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: 'Não foi possível consultar as impressoras do Windows',
        detalhe: this.mensagemErro(e),
        impressaoTeste: true,
      });
    }

    const nomeProcurado = cfg.nome ? cfg.nome.toLowerCase() : null;
    const encontrada = nomeProcurado
      ? impressoras.find((imp) => (imp.nome || '').toLowerCase() === nomeProcurado)
      : impressoras.find((imp) => imp.padrao);

    if (!encontrada) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: cfg.nome
          ? `Impressora "${cfg.nome}" não está instalada neste computador`
          : 'Nenhuma impressora padrão definida no Windows',
        detalhe: impressoras.length
          ? `Instaladas: ${impressoras.map((imp) => imp.nome).join(', ')}`
          : 'Nenhuma impressora encontrada no spooler do Windows',
        impressaoTeste: true,
      });
    }

    const arquivo = path.join(os.tmpdir(), `sistema-tenda-teste-impressora-${Date.now()}.txt`);
    try {
      await fs.promises.writeFile(arquivo, this.gerarCupomTexto(cfg.largura || LARGURA_PADRAO), 'utf8');

      const script = [
        '$ErrorActionPreference = "Stop"',
        `$conteudo = Get-Content -LiteralPath ${this.psQuote(arquivo)} -Raw -Encoding UTF8`,
        `$conteudo | Out-Printer -Name ${this.psQuote(encontrada.nome)}`,
      ].join('\n');

      await execFileAsync(
        'powershell.exe',
        ['-NoProfile', '-NonInteractive', '-ExecutionPolicy', 'Bypass', '-Command', script],
        { timeout: timeoutMs, windowsHide: true, maxBuffer: 1024 * 1024 },
      );

      return this.montarResultado({
        cfg,
        inicio,
        sucesso: true,
        status: 'ok',
        mensagem: `Cupom de teste enviado para "${encontrada.nome}"`,
        detalhe: 'Trabalho aceito pelo spooler do Windows',
        destino: encontrada.nome,
        impressaoTeste: true,
      });
    } catch (e) {
      return this.montarResultado({
        cfg,
        inicio,
        sucesso: false,
        status: 'erro',
        mensagem: `Falha ao imprimir em "${encontrada.nome}"`,
        detalhe: this.mensagemErro(e),
        destino: encontrada.nome,
        impressaoTeste: true,
      });
    } finally {
      try { await fs.promises.unlink(arquivo); } catch { /* ignora */ }
    }
  }

  enviarTcp(host, porta, conteudo, timeoutMs) {
    return new Promise((resolve) => {
      const socket = new net.Socket();
      let finalizado = false;

      const encerrar = (ok, erro) => {
        if (finalizado) return;
        finalizado = true;
        clearTimeout(timer);
        try { socket.end(); } catch { /* ignora */ }
        try { socket.destroy(); } catch { /* ignora */ }
        resolve({ ok, erro });
      };

      const timer = setTimeout(() => encerrar(false, `Timeout de ${timeoutMs} ms ao enviar`), timeoutMs);

      socket.setTimeout(timeoutMs);
      socket.once('timeout', () => encerrar(false, `Timeout de ${timeoutMs} ms ao enviar`));
      socket.once('error', (err) => encerrar(false, err.message));

      socket.connect(porta, host, () => {
        socket.write(Buffer.from(conteudo, 'binary'), (err) => {
          if (err) {
            encerrar(false, err.message);
            return;
          }
          // Pequena espera para o buffer drenar antes de fechar.
          setTimeout(() => encerrar(true), 250);
        });
      });
    });
  }

  // ---------------------------------------------------------------------------
  // Teste completo (conexão + impressão opcional)
  // ---------------------------------------------------------------------------

  async executarTeste(destino, opcoes = {}) {
    const inicio = Date.now();
    const destinoFinal = destino || process.env.IMPRESSORA_FISCAL_DESTINO || '';
    const timeoutMs = opcoes.timeoutMs || TIMEOUT_PADRAO_MS;

    const conexao = await this.testarConexao(destinoFinal, { timeoutMs });

    let impressao = null;
    if (opcoes.imprimirTeste) {
      if (conexao.status === 'erro') {
        impressao = {
          success: false,
          status: 'erro',
          mensagem: 'Cupom de teste não enviado',
          detalhe: 'Corrija a conexão com a impressora antes de imprimir',
        };
      } else {
        impressao = await this.imprimirTeste(destinoFinal, {
          timeoutMs,
          largura: opcoes.largura,
        });
      }
    }

    const statusFinal = impressao && impressao.status === 'erro'
      ? 'erro'
      : conexao.status === 'erro'
        ? 'erro'
        : (impressao && impressao.status) || conexao.status;

    const sucesso = statusFinal !== 'erro';

    this.logger.info('Teste de impressora concluído', {
      destino: destinoFinal || 'padrão',
      status: statusFinal,
      conexao: conexao.status,
      impressao: impressao ? impressao.status : 'não solicitada',
    });

    return {
      success: sucesso,
      status: statusFinal,
      mensagem: this.mensagemFinal(sucesso, statusFinal, conexao, impressao),
      detalhe: [conexao.mensagem, impressao ? impressao.mensagem : null].filter(Boolean).join(' | '),
      destino: conexao.destino || destinoFinal || 'padrão',
      tipoDestino: conexao.tipoDestino,
      tempoMs: Date.now() - inicio,
      impressaoTeste: Boolean(opcoes.imprimirTeste),
      etapas: {
        conexao: { status: conexao.status, mensagem: conexao.mensagem, detalhe: conexao.detalhe },
        impressao: impressao
          ? { status: impressao.status, mensagem: impressao.mensagem, detalhe: impressao.detalhe }
          : null,
      },
    };
  }

  mensagemFinal(sucesso, status, conexao, impressao) {
    if (!sucesso) {
      return conexao.status === 'erro'
        ? 'Falha na comunicação com a impressora'
        : 'Falha no teste de impressão';
    }
    if (status === 'atencao') {
      return 'Comunicação verificada com ressalvas';
    }
    return impressao ? 'Impressora comunicando e cupom de teste enviado' : 'Impressora comunicando';
  }

  // ---------------------------------------------------------------------------
  // Informações e utilitários
  // ---------------------------------------------------------------------------

  async obterInformacoes() {
    const destinoConfigurado = process.env.IMPRESSORA_FISCAL_DESTINO || '';
    const cfg = this.parseDestino(destinoConfigurado);
    const info = {
      destinoConfigurado: destinoConfigurado || null,
      tipoDestino: cfg.tipo,
      descricaoDestino: cfg.descricao,
      plataforma: process.platform,
      impressorasWindows: [],
    };

    if (process.platform === 'win32') {
      try {
        info.impressorasWindows = await this.listarImpressorasWindows(TIMEOUT_PADRAO_MS);
      } catch (e) {
        info.erroImpressoras = this.mensagemErro(e);
      }
    }

    return info;
  }

  montarResultado({ cfg, inicio, sucesso, status, mensagem, detalhe, destino, impressaoTeste }) {
    return {
      success: sucesso,
      status,
      mensagem,
      detalhe: detalhe || '',
      destino: destino || cfg.bruto || 'padrão',
      tipoDestino: cfg.tipo,
      tempoMs: Date.now() - inicio,
      impressaoTeste: Boolean(impressaoTeste),
    };
  }

  gerarCupomTeste(largura) {
    const linha = '-'.repeat(Math.max(24, Number(largura) || LARGURA_PADRAO));
    const texto = this.gerarCupomTexto(largura);
    const ESC = '\x1B';
    const GS = '\x1D';
    return [
      `${ESC}@`,
      `${ESC}a\x01`,
      'SISTEMA TENDA',
      'TESTE DE IMPRESSAO',
      `${ESC}a\x00`,
      texto,
      `${GS}V\x01`,
    ].join('\n');
  }

  gerarCupomTexto(largura) {
    const linha = '-'.repeat(Math.max(24, Number(largura) || LARGURA_PADRAO));
    return [
      linha,
      `SISTEMA TENDA - TESTE DE PRODUCAO`,
      linha,
      `Data/Hora: ${new Date().toLocaleString('pt-BR')}`,
      `Computador: ${os.hostname()}`,
      `Destino: ${process.env.IMPRESSORA_FISCAL_DESTINO || 'padrão do Windows'}`,
      linha,
      'Se este cupom foi impresso, a comunicacao',
      'com a impressora fiscal esta funcionando.',
      linha,
      '',
      '',
      '',
    ].join('\r\n');
  }

  async comTimeout(promessa, timeoutMs) {
    let timer;
    const timeout = new Promise((_, reject) => {
      timer = setTimeout(() => reject(new Error(`Timeout de ${timeoutMs} ms`)), timeoutMs);
    });
    try {
      return await Promise.race([promessa, timeout]);
    } finally {
      clearTimeout(timer);
    }
  }

  psQuote(valor) {
    return `'${String(valor).replace(/'/g, "''")}'`;
  }

  mensagemErro(e) {
    if (!e) return 'Erro desconhecido';
    return e.stderr ? `${e.message}: ${String(e.stderr).trim()}` : e.message || String(e);
  }
}

module.exports = ImpressoraTest;
