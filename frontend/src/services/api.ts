import axios from 'axios';

export const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url: string = error.config?.url || '';
    const isAuthEndpoint = url.includes('/auth/login') || url.includes('/auth/refresh');
    if (status === 401 && !isAuthEndpoint) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const apiFruteira = {
  // Produtos
  listProdutos: (params?: any) => api.get('/fruteira/produtos', { params }),
  getProduto: (id: string) => api.get(`/fruteira/produtos/${id}`),
  getByCodigo: (codigo: string) => api.get(`/fruteira/produtos/codigo/${codigo}`),
  getByCodigoBarras: (codigoBarras: string) => api.get(`/fruteira/produtos/codigo-barras/${codigoBarras}`),
  createProduto: (data: any) => api.post('/fruteira/produtos', data),
  updateProduto: (id: string, data: any) => api.put(`/fruteira/produtos/${id}`, data),
  deleteProduto: (id: string) => api.delete(`/fruteira/produtos/${id}`),
  updateEstoque: (id: string, quantidade: number) => api.put(`/fruteira/produtos/${id}/estoque`, null, { params: { quantidade } }),
  updatePreco: (id: string, preco: number) => api.put(`/fruteira/produtos/${id}/preco`, null, { params: { preco } }),
  searchProdutos: (termo: string) => api.get('/fruteira/produtos/search', { params: { q: termo } }),
  getEstoqueBaixo: () => api.get('/fruteira/produtos/estoque-baixo'),
  getPesaveis: () => api.get('/fruteira/produtos/pesaveis'),

  // Perdas
  listPerda: (params?: any) => api.get('/fruteira/perdas', { params }),
  getPerda: (id: string) => api.get(`/fruteira/perdas/${id}`),
  createPerda: (data: any) => api.post('/fruteira/perdas', data),
  createPerdaRapida: (data: any) => api.post('/fruteira/perdas/rapida', data),
  updatePerda: (id: string, data: any) => api.put(`/fruteira/perdas/${id}`, data),
  deletePerda: (id: string) => api.delete(`/fruteira/perdas/${id}`),
  getTotalPerdas: (dataInicio: string, dataFim: string) => api.get('/fruteira/perdas/total-periodo', { params: { dataInicio, dataFim } }),

  // Etiquetas Balança
  listEtiquetas: (params?: any) => api.get('/fruteira/etiquetas-balanca', { params }),
  getEtiqueta: (id: string) => api.get(`/fruteira/etiquetas-balanca/${id}`),
  getEtiquetaByCodigo: (codigo: string) => api.get(`/fruteira/etiquetas-balanca/codigo/${codigo}`),
  createEtiqueta: (data: any) => api.post('/fruteira/etiquetas-balanca', data),
  gerarParaProduto: (produtoId: string) => api.post(`/fruteira/etiquetas-balanca/gerar/${produtoId}`),
  updateEtiqueta: (id: string, data: any) => api.put(`/fruteira/etiquetas-balanca/${id}`, data),
  deleteEtiqueta: (id: string) => api.delete(`/fruteira/etiquetas-balanca/${id}`),
  imprimir: (codigo: string) => api.post(`/fruteira/etiquetas-balanca/${codigo}/imprimir`),

  // Tabelas Preço
  listTabelas: (params?: any) => api.get('/fruteira/tabelas-preco', { params }),
  getTabela: (id: string) => api.get(`/fruteira/tabelas-preco/${id}`),
  createTabela: (data: any) => api.post('/fruteira/tabelas-preco', data),
  updateTabela: (id: string, data: any) => api.put(`/fruteira/tabelas-preco/${id}`, data),
  deleteTabela: (id: string) => api.delete(`/fruteira/tabelas-preco/${id}`),
  getVigente: (produtoId: string, data?: string) => api.get(`/fruteira/tabelas-preco/produto/${produtoId}/vigente`, { params: { data } }),
  aplicarVigente: (produtoId: string) => api.post(`/fruteira/tabelas-preco/aplicar-vigente/${produtoId}`),
  aplicarTodasVigentes: () => api.post('/fruteira/tabelas-preco/aplicar-todas-vigentes'),

  // Balança
  getBalancaStatus: () => api.get('/fruteira/balanca/status'),
  conectarBalanca: () => api.post('/fruteira/balanca/conectar'),
  desconectarBalanca: () => api.post('/fruteira/balanca/desconectar'),
  lerPeso: () => api.get('/fruteira/balanca/ler-peso'),
  streamPeso: () => api.get('/fruteira/balanca/stream'),

  // Etiquetas Impressão
  gerarCodigoBarras: (conteudo: string, largura?: number, altura?: number) => api.post('/fruteira/etiquetas/codigo-barras', null, { params: { conteudo, largura, altura } }),
  gerarQrCode: (conteudo: string, tamanho?: number) => api.post('/fruteira/etiquetas/qr-code', null, { params: { conteudo, tamanho } }),
  gerarPdfSimples: (data: any) => api.post('/fruteira/etiquetas/pdf/simples', data, { responseType: 'blob' }),
  gerarPdfLote: (data: any) => api.post('/fruteira/etiquetas/pdf/lote', data, { responseType: 'blob' }),
};

export const apiBuffet = {
  // Comandas
  listComandas: (params?: any) => api.get('/buffet/comandas', { params }),
  getComanda: (id: string) => api.get(`/buffet/comandas/${id}`),
  getByNumero: (numero: string) => api.get(`/buffet/comandas/numero/${numero}`),
  createComanda: (data: any) => api.post('/buffet/comandas', data),
  updateComanda: (id: string, data: any) => api.put(`/buffet/comandas/${id}`, data),
  deleteComanda: (id: string) => api.delete(`/buffet/comandas/${id}`),
  fecharComanda: (id: string) => api.post(`/buffet/comandas/${id}/fechar`),
  reabrirComanda: (id: string) => api.post(`/buffet/comandas/${id}/reabrir`),
  cancelarComanda: (id: string, motivo: string) => api.post(`/buffet/comandas/${id}/cancelar`, null, { params: { motivo } }),
  aplicarDesconto: (id: string, valor: number) => api.put(`/buffet/comandas/${id}/desconto`, null, { params: { valor } }),
  aplicarAcrescimo: (id: string, valor: number) => api.put(`/buffet/comandas/${id}/acrescimo`, null, { params: { valor } }),
  getTotalVendas: (dataInicio: string, dataFim: string) => api.get('/buffet/comandas/total-vendas', { params: { dataInicio, dataFim } }),

  // Itens Comanda
  listItensComanda: (comandaId: string) => api.get(`/buffet/itens-comanda/comanda/${comandaId}`),
  getTotalComanda: (comandaId: string) => api.get(`/buffet/itens-comanda/comanda/${comandaId}/total`),
  createItem: (data: any) => api.post('/buffet/itens-comanda', data),
  lancarPratoBalanca: (data: any) => api.post('/buffet/itens-comanda/prato-balanca', data),
  lancarProdutoBuffet: (data: any) => api.post('/buffet/itens-comanda/produto-buffet', data),
  cancelarItem: (id: string, motivo: string) => api.post(`/buffet/itens-comanda/${id}/cancelar`, null, { params: { motivo } }),

  // Mesas
  listMesas: (params?: any) => api.get('/buffet/mesas', { params }),
  getMesa: (id: string) => api.get(`/buffet/mesas/${id}`),
  getMesaByNumero: (numero: string) => api.get(`/buffet/mesas/numero/${numero}`),
  createMesa: (data: any) => api.post('/buffet/mesas', data),
  updateMesa: (id: string, data: any) => api.put(`/buffet/mesas/${id}`, data),
  deleteMesa: (id: string) => api.delete(`/buffet/mesas/${id}`),
  ocuparMesa: (id: string) => api.post(`/buffet/mesas/${id}/ocupar`),
  liberarMesa: (id: string) => api.post(`/buffet/mesas/${id}/liberar`),
  reservarMesa: (id: string) => api.post(`/buffet/mesas/${id}/reservar`),
  getDisponiveis: () => api.get('/buffet/mesas/disponiveis'),

  // Clientes Buffet
  listClientes: () => api.get('/buffet/clientes'),
  getCliente: (id: string) => api.get(`/buffet/clientes/${id}`),
  createCliente: (data: any) => api.post('/buffet/clientes', data),
  updateCliente: (id: string, data: any) => api.put(`/buffet/clientes/${id}`, data),
  rankingFrequentes: (limit?: number) => api.get('/buffet/clientes/ranking/frequentes', { params: { limit } }),
  rankingConsumo: (limit?: number) => api.get('/buffet/clientes/ranking/consumo', { params: { limit } }),
  resumoClientes: () => api.get('/buffet/clientes/resumo'),

  // Produtos Buffet
  listProdutos: (params?: any) => api.get('/buffet/produtos', { params }),
  getProduto: (id: string) => api.get(`/buffet/produtos/${id}`),
  getByCodigo: (codigo: string) => api.get(`/buffet/produtos/codigo/${codigo}`),
  getByCodigoBarras: (codigoBarras: string) => api.get(`/buffet/produtos/codigo-barras/${codigoBarras}`),
  createProduto: (data: any) => api.post('/buffet/produtos', data),
  updateProduto: (id: string, data: any) => api.put(`/buffet/produtos/${id}`, data),
  deleteProduto: (id: string) => api.delete(`/buffet/produtos/${id}`),
  updateEstoque: (id: string, quantidade: number) => api.put(`/buffet/produtos/${id}/estoque`, null, { params: { quantidade } }),
  searchProdutos: (termo: string) => api.get('/buffet/produtos/search', { params: { q: termo } }),
  getDisponiveisBalcao: () => api.get('/buffet/produtos/disponiveis-balcao'),
  getEstoqueBaixo: () => api.get('/buffet/produtos/estoque-baixo'),

  // Receitas
  listReceitas: (params?: any) => api.get('/buffet/receitas', { params }),
  getReceita: (id: string) => api.get(`/buffet/receitas/${id}`),
  getByProduto: (produtoId: string) => api.get(`/buffet/receitas/produto/${produtoId}`),
  createReceita: (data: any) => api.post('/buffet/receitas', data),
  updateReceita: (id: string, data: any) => api.put(`/buffet/receitas/${id}`, data),
  deleteReceita: (id: string) => api.delete(`/buffet/receitas/${id}`),

  // Transferências
  listTransferencias: (params?: any) => api.get('/buffet/transferencias', { params }),
  getTransferencia: (id: string) => api.get(`/buffet/transferencias/${id}`),
  createTransferencia: (data: any) => api.post('/buffet/transferencias', data),
  aprovar: (id: string, aprovadoPor: string) => api.post(`/buffet/transferencias/${id}/aprovar`, null, { params: { aprovadoPor } }),
  rejeitar: (id: string, motivo: string, rejeitadoPor: string) => api.post(`/buffet/transferencias/${id}/rejeitar`, null, { params: { motivo, rejeitadoPor } }),
  concluir: (id: string, recebidoPor: string) => api.post(`/buffet/transferencias/${id}/concluir`, null, { params: { recebidoPor } }),
  cancelar: (id: string, motivo: string) => api.post(`/buffet/transferencias/${id}/cancelar`, null, { params: { motivo } }),
  getPendentes: () => api.get('/buffet/transferencias/pendentes'),

  // Balança Buffet
  getBalancaStatus: () => api.get('/buffet/balanca/status'),
  conectarBalanca: () => api.post('/buffet/balanca/conectar'),
  desconectarBalanca: () => api.post('/buffet/balanca/desconectar'),
  lerPeso: () => api.get('/buffet/balanca/ler-peso'),
};

export const apiPdv = {
  // Terminais/PDVs
  listTerminaisAtivos: () => api.get('/pdv/terminais/ativos'),

  // Vendas
  listVendas: (params?: any) => api.get('/pdv/vendas', { params }),
  getVenda: (id: string) => api.get(`/pdv/vendas/${id}`),
  getByNumero: (numero: string) => api.get(`/pdv/vendas/numero/${numero}`),
  getByComanda: (comandaId: string) => api.get(`/pdv/vendas/comanda/${comandaId}`),
  createVenda: (data: any) => api.post('/pdv/vendas', data),
  updateVenda: (id: string, data: any) => api.put(`/pdv/vendas/${id}`, data),
  deleteVenda: (id: string) => api.delete(`/pdv/vendas/${id}`),
  addItemFruteira: (id: string, produtoId: string, quantidade: number) => api.post(`/pdv/vendas/${id}/item-fruteira`, null, { params: { produtoId, quantidade } }),
  addItemBuffet: (id: string, produtoId: string, quantidade: number) => api.post(`/pdv/vendas/${id}/item-buffet`, null, { params: { produtoId, quantidade } }),
  addPratoBalanca: (id: string, pesoBruto: number, tara?: number) => api.post(`/pdv/vendas/${id}/prato-balanca`, null, { params: { pesoBruto, tara } }),
  addItemComanda: (id: string, comandaId: string) => api.post(`/pdv/vendas/${id}/item-comanda`, null, { params: { comandaId } }),
  aplicarDesconto: (id: string, valor: number) => api.put(`/pdv/vendas/${id}/desconto`, null, { params: { valor } }),
  aplicarAcrescimo: (id: string, valor: number) => api.put(`/pdv/vendas/${id}/acrescimo`, null, { params: { valor } }),
  processarPagamento: (id: string, data: any) => api.post(`/pdv/vendas/${id}/pagamento`, data),
  finalizarVenda: (id: string, cpfConsumidor?: string) => api.post(`/pdv/vendas/${id}/finalizar`, null, { params: { cpfConsumidor } }),
  cancelarVenda: (id: string, motivo: string) => api.post(`/pdv/vendas/${id}/cancelar`, null, { params: { motivo } }),
  getTotalVendas: (dataInicio: string, dataFim: string) => api.get('/pdv/vendas/total-vendas', { params: { dataInicio, dataFim } }),
  getTotalFruteira: (dataInicio: string, dataFim: string) => api.get('/pdv/vendas/total-fruteira', { params: { dataInicio, dataFim } }),
  getTotalBuffet: (dataInicio: string, dataFim: string) => api.get('/pdv/vendas/total-buffet', { params: { dataInicio, dataFim } }),

  // PIX
  gerarPix: (id: string, valor: number, txid?: string) => api.post(`/pdv/vendas/${id}/pix/gerar`, null, { params: { valor, txid } }),
  consultarPix: (id: string, txid: string) => api.get(`/pdv/vendas/${id}/pix/status`, { params: { txid } }),

  // Itens Venda
  listItensVenda: (vendaId: string) => api.get(`/pdv/itens-venda/venda/${vendaId}`),

  // Pagamentos
  listPagamentos: (vendaId: string) => api.get(`/pdv/pagamentos/venda/${vendaId}`),
  getTotalPago: (vendaId: string) => api.get(`/pdv/pagamentos/venda/${vendaId}/total`),

  // PDVs
  listPdvs: (params?: any) => api.get('/pdv/terminais', { params }),
  getPdv: (id: string) => api.get(`/pdv/terminais/${id}`),
  getByCodigo: (codigo: string) => api.get(`/pdv/terminais/codigo/${codigo}`),
  createPdv: (data: any) => api.post('/pdv/terminais', data),
  updatePdv: (id: string, data: any) => api.put(`/pdv/terminais/${id}`, data),
  deletePdv: (id: string) => api.delete(`/pdv/terminais/${id}`),

  // Sessões Caixa
  listSessoes: (params?: any) => api.get('/pdv/sessoes-caixa', { params }),
  getSessao: (id: string) => api.get(`/pdv/sessoes-caixa/${id}`),
  getAbertaByPdv: (pdvId: string) => api.get(`/pdv/sessoes-caixa/pdv/${pdvId}/aberta`),
  abrirCaixa: (data: any) => api.post('/pdv/sessoes-caixa/abrir', data),
  abrirCaixaAutomatico: (data: any) => api.post('/pdv/sessoes-caixa/abrir-automatico', data),
  validarVinculoPdv: (sessaoId: string, pdvId: string) => api.get(`/pdv/sessoes-caixa/validar-vinculo/${sessaoId}/${pdvId}`),
  fecharCaixa: (id: string, data: any) => api.post(`/pdv/sessoes-caixa/${id}/fechar`, data),
  sangria: (id: string, data: any) => api.post(`/pdv/sessoes-caixa/${id}/sangria`, data),
  reforco: (id: string, data: any) => api.post(`/pdv/sessoes-caixa/${id}/reforco`, data),
  getTotalVendasPeriodo: (dataInicio: string, dataFim: string) => api.get('/pdv/sessoes-caixa/total-vendas', { params: { dataInicio, dataFim } }),
};

export const apiClientes = {
  list: (params?: any) => api.get('/clientes', { params }),
  get: (id: string) => api.get(`/clientes/${id}`),
  ativos: () => api.get('/clientes/ativos'),
  search: (q: string) => api.get('/clientes/search', { params: { q } }),
  getByDocumento: (cpfCnpj: string) => api.get(`/clientes/documento/${cpfCnpj}`),
  create: (data: any) => api.post('/clientes', data),
  update: (id: string, data: any) => api.put(`/clientes/${id}`, data),
  toggleStatus: (id: string) => api.post(`/clientes/${id}/status`),
  remove: (id: string) => api.delete(`/clientes/${id}`),
};

export const apiCaderno = {
  listar: (params?: any) => api.get('/pdv/caderno', { params }),
  clientesComDivida: () => api.get('/pdv/caderno/clientes'),
  totais: () => api.get('/pdv/caderno/totais'),
  situacaoCliente: (clienteId: string) => api.get(`/pdv/caderno/clientes/${clienteId}`),
  situacaoPorDocumento: (documento: string) => api.get(`/pdv/caderno/documento/${documento}`),
  get: (id: string) => api.get(`/pdv/caderno/${id}`),
  lancar: (data: any) => api.post('/pdv/caderno', data),
  registrarPagamento: (id: string, valor: number) => api.post(`/pdv/caderno/${id}/pagamento`, null, { params: { valor } }),
  cancelar: (id: string, motivo?: string) => api.post(`/pdv/caderno/${id}/cancelar`, null, { params: { motivo } }),
  excluir: (id: string) => api.delete(`/pdv/caderno/${id}`),
};

export const apiEstoque = {
  listMovimentos: (params?: any) => api.get('/estoque/movimentos', { params }),
  createMovimento: (data: any) => api.post('/estoque/movimentos', data),
  listInventarios: (params?: any) => api.get('/estoque/inventarios', { params }),
  listAlertas: (params?: any) => api.get('/estoque/alertas', { params }),

  // Entradas
  listEntradas: (params?: any) => api.get('/estoque/movimentos/entradas', { params }),
  registrarEntradaCompra: (data: any) => api.post('/estoque/movimentos/entrada-compra', data),
  registrarEntradaTransferencia: (data: any) => api.post('/estoque/movimentos/entrada-transferencia', data),

  // Saídas
  listSaidas: (params?: any) => api.get('/estoque/movimentos/saidas', { params }),
  registrarSaidaVenda: (data: any) => api.post('/estoque/movimentos/saida-venda', data),
  registrarAjuste: (data: any) => api.post('/estoque/movimentos/ajuste', data),

  // Defeitos
  listDefeitos: (params?: any) => api.get('/estoque/movimentos/defeitos', { params }),
  registrarDefeito: (data: any) => api.post('/estoque/movimentos/defeito', data),
};

export const apiFiscal = {
  listNfe: (params?: any) => api.get('/fiscal/nfe', { params }),
  getNfe: (id: string) => api.get(`/fiscal/nfe/${id}`),
  getNfeByChave: (chaveAcesso: string) => api.get(`/fiscal/nfe/chave/${chaveAcesso}`),
  cancelarNfe: (id: string, justificativa: string) => api.post(`/fiscal/nfe/${id}/cancelar`, null, { params: { justificativa } }),
  consultarNfe: (id: string) => api.post(`/fiscal/nfe/${id}/consultar`),
  downloadXml: (id: string) => api.get(`/fiscal/nfe/${id}/xml`, { responseType: 'blob' }),
  downloadDanfe: (id: string) => api.get(`/fiscal/nfe/${id}/danfe`, { responseType: 'blob' }),
  inutilizarNumeracao: (justificativa: string, numeroInicial: number, numeroFinal: number) => 
    api.post('/fiscal/nfe/inutilizar', null, { params: { justificativa, numeroInicial, numeroFinal } }),
  listSpedFiscal: (params?: any) => api.get('/fiscal/sped-fiscal', { params }),
  listDre: (params?: any) => api.get('/fiscal/dre', { params }),
  listAtivos: (params?: any) => api.get('/fiscal/ativos', { params }),
  listConciliacao: (params?: any) => api.get('/fiscal/conciliacao-cartao', { params }),
};