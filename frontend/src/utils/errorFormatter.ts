export function formatarMensagemErro(error: any): { tipo: 'fluxo' | 'sistema'; titulo: string; mensagem: string } {
  const status = error?.response?.status;
  const data = error?.response?.data;
  const msgBackend = data?.message || error?.message || '';

  // Erros de Fluxo (Negócio / Validação / Estado / Permissão / Credenciais)
  const isFluxo =
    status === 400 ||
    status === 401 ||
    status === 403 ||
    status === 404 ||
    status === 409 ||
    status === 422 ||
    msgBackend.includes('caixa') ||
    msgBackend.includes('sessão') ||
    msgBackend.includes('estoque insuficiente') ||
    msgBackend.includes('comanda') ||
    msgBackend.includes('mesa') ||
    msgBackend.includes('senha') ||
    msgBackend.includes('usuário') ||
    msgBackend.includes('saldo') ||
    msgBackend.includes('inválido') ||
    msgBackend.includes('não encontrado');

  if (isFluxo) {
    let titulo = 'Erro de Fluxo / Regra de Negócio';
    let mensagem = msgBackend;

    if (status === 400) {
      titulo = 'Erro de Fluxo: Requisição Inválida';
      mensagem = msgBackend || 'Os dados enviados para a operação estão incorretos ou incompletos.';
    } else if (status === 401) {
      titulo = 'Erro de Fluxo: Não Autenticado';
      mensagem = 'Sua sessão expirou ou você não está logado. Por favor, faça login novamente.';
    } else if (status === 403) {
      titulo = 'Erro de Fluxo: Acesso Negado';
      mensagem = 'Você não possui permissão para realizar esta operação.';
    } else if (status === 404) {
      titulo = 'Erro de Fluxo: Recurso Não Encontrado';
      mensagem = msgBackend || 'O item ou registro procurado não existe ou foi removido.';
    } else if (status === 409 || status === 422) {
      titulo = 'Erro de Fluxo / Validação';
      mensagem = msgBackend || 'A operação conflita com o estado atual do sistema.';
    }

    return {
      tipo: 'fluxo',
      titulo,
      mensagem: `[Fluxo] ${mensagem}`,
    };
  }

  // Erros de Sistema (Servidor / Banco de Dados / Rede / Timeout)
  let titulo = 'Erro do Sistema';
  let mensagem = msgBackend || 'Ocorreu um erro interno inesperado no servidor.';

  if (status >= 500) {
    titulo = 'Erro do Sistema: Falha no Servidor';
    mensagem = `O servidor encontrou um problema interno (${status}). Tente novamente em instantes ou contate o suporte técnico.`;
  } else if (!error?.response && (error?.code === 'ECONNABORTED' || error?.message?.includes('Network Error'))) {
    titulo = 'Erro do Sistema: Falha de Conexão';
    mensagem = 'Não foi possível conectar ao servidor. Verifique sua conexão com a rede ou se o serviço está ativo.';
  }

  return {
    tipo: 'sistema',
    titulo,
    mensagem: `[Sistema] ${mensagem}`,
  };
}
