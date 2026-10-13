export function formatCurrency(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === '') return 'R$ 0,00';
  const num = typeof value === 'string' ? parseFloat(value) : value;
  if (isNaN(num)) return 'R$ 0,00';
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: 2,
  }).format(num);
}

export function formatNumber(value: number | string | null | undefined, decimals = 0): string {
  if (value === null || value === undefined || value === '') return '0';
  const num = typeof value === 'string' ? parseFloat(value) : value;
  if (isNaN(num)) return '0';
  return new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num);
}

export function formatDate(value: string | Date | null | undefined, options?: Intl.DateTimeFormatOptions): string {
  if (!value) return '-';
  const date = typeof value === 'string' ? new Date(value) : value;
  if (isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    ...options,
  }).format(date);
}

export function formatDateTime(value: string | Date | null | undefined): string {
  return formatDate(value, { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

export function formatCPF_CNPJ(value: string): string {
  if (!value) return '';
  const numbers = value.replace(/\D/g, '');
  if (numbers.length === 11) {
    return numbers.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
  if (numbers.length === 14) {
    return numbers.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  }
  return value;
}

export function formatPhone(value: string): string {
  if (!value) return '';
  const numbers = value.replace(/\D/g, '');
  if (numbers.length === 11) {
    return numbers.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  }
  if (numbers.length === 10) {
    return numbers.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  }
  return value;
}

export function formatCEP(value: string): string {
  if (!value) return '';
  const numbers = value.replace(/\D/g, '');
  if (numbers.length === 8) {
    return numbers.replace(/(\d{5})(\d{3})/, '$1-$2');
  }
  return value;
}

export function truncateText(text: string, maxLength: number): string {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength).trim() + '...';
}

export function getStatusColor(status: string): 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' {
  const statusLower = status?.toLowerCase();
  if (['paga', 'finalizada', 'concluida', 'aprovada', 'autorizada', 'ativa', 'liberada'].includes(statusLower)) return 'success';
  if (['cancelada', 'rejeitada', 'negada', 'estornada', 'baixa', 'fechada', 'indisponivel'].includes(statusLower)) return 'error';
  if (['pendente', 'em_andamento', 'processando', 'em_uso', 'ocupada', 'reservada'].includes(statusLower)) return 'warning';
  if (['aberta', 'planejada', 'aguardando'].includes(statusLower)) return 'info';
  return 'default';
}

export function getStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    ABERTA: 'Aberta',
    EM_USO: 'Em Uso',
    FECHADA: 'Fechada',
    PAGA: 'Paga',
    CANCELADA: 'Cancelada',
    EM_ATRASO: 'Em Atraso',
    PENDENTE: 'Pendente',
    AUTORIZADO: 'Autorizado',
    NEGADO: 'Negado',
    ESTORNADO: 'Estornado',
    LIVRE: 'Livre',
    OCUPADA: 'Ocupada',
    RESERVADA: 'Reservada',
    MANUTENCAO: 'Manutenção',
    INATIVA: 'Inativa',
    ATIVO: 'Ativo',
    INATIVO: 'Inativo',
    CONCLUIDA: 'Concluída',
    REJEITADA: 'Rejeitada',
  };
  return labels[status] || status;
}