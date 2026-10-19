const DB_NAMES = [
  'broadcast-channel',
  'broadcastchannel',
  'react-query',
  'tanstack-query',
];

export function limparIndexedDBCorrompido() {
  if (!('indexedDB' in window)) return;

  DB_NAMES.forEach((name) => {
    try {
      const req = indexedDB.deleteDatabase(name);
      req.onsuccess = () => console.info(`IndexedDB "${name}" limpo`);
      req.onerror = () => console.warn(`Erro ao limpar IndexedDB "${name}"`);
      req.onblocked = () => console.warn(`IndexedDB "${name}" bloqueado para limpeza`);
    } catch {
      // ignora
    }
  });
}

export function setupIndexedDBCleanup() {
  window.addEventListener('error', (event) => {
    const msg = event.message || '';
    if (msg.includes('Corruption') || msg.includes('checksum mismatch')) {
      console.warn('IndexedDB corrompido detectado, limpando...');
      limparIndexedDBCorrompido();
    }
  });

  window.addEventListener('unhandledrejection', (event) => {
    const msg = String(event.reason?.message || event.reason || '');
    if (msg.includes('Corruption') || msg.includes('checksum mismatch')) {
      console.warn('IndexedDB corrompido detectado (promise), limpando...');
      limparIndexedDBCorrompido();
    }
  });
}
