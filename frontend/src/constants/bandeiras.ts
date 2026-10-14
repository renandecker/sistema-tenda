export enum BandeiraCartao {
  ALELO_ALIMENTACAO = 'ALELO_ALIMENTACAO',
  ALELO_REFEICAO = 'ALELO_REFEICAO',
  ALELO_TUDO = 'ALELO_TUDO',
  BANRICOMPRAS_BANRICARD = 'BANRICOMPRAS_BANRICARD',
  TICKET_EDENRED = 'TICKET_EDENRED',
  PLUXEE = 'PLUXEE',
  VR_BENEFICIOS = 'VR_BENEFICIOS',
  GREEN_CARD = 'GREEN_CARD',
  VALECARD_VOLUS = 'VALECARD_VOLUS',
}

export const BANDEIRA_LABELS: Record<BandeiraCartao, string> = {
  [BandeiraCartao.ALELO_ALIMENTACAO]: 'Alelo Alimentação',
  [BandeiraCartao.ALELO_REFEICAO]: 'Alelo Refeição',
  [BandeiraCartao.ALELO_TUDO]: 'Alelo Tudo (Flexível)',
  [BandeiraCartao.BANRICOMPRAS_BANRICARD]: 'Banricompras & Banricard (Grupo Banrisul)',
  [BandeiraCartao.TICKET_EDENRED]: 'Ticket (Edenred)',
  [BandeiraCartao.PLUXEE]: 'Pluxee (antiga Sodexo)',
  [BandeiraCartao.VR_BENEFICIOS]: 'VR Benefícios',
  [BandeiraCartao.GREEN_CARD]: 'Green Card',
  [BandeiraCartao.VALECARD_VOLUS]: 'ValeCard / Vólus',
};

export const BANDEIRA_DESCRICOES: Record<BandeiraCartao, string> = {
  [BandeiraCartao.ALELO_ALIMENTACAO]: 'Voltado para compras de ingredientes de cesta básica em supermercados, açougues, mercearias e padarias.',
  [BandeiraCartao.ALELO_REFEICAO]: 'Voltado para refeições prontas em restaurantes, lanchonetes e redes de fast food.',
  [BandeiraCartao.ALELO_TUDO]: 'Reúne em um único cartão os saldos de Alimentação, Refeição, Mobilidade, Farmácia e Saúde.',
  [BandeiraCartao.BANRICOMPRAS_BANRICARD]: 'O Banrisul atua no segmento de benefícios e pagamentos por meio dos cartões corporativos Banricard e pelo tradicional cartão de débito/crédito Banricompras.',
  [BandeiraCartao.TICKET_EDENRED]: 'Uma das operadoras mais antigas do mercado, aceita em milhares de supermercados e restaurantes.',
  [BandeiraCartao.PLUXEE]: 'Uma das líderes do segmento no Brasil, com ampla aceitação nacional em redes de alimentação e refeição.',
  [BandeiraCartao.VR_BENEFICIOS]: 'Tradicional no setor comercial e corporativo, oferecendo soluções integradas para empresas e trabalhadores.',
  [BandeiraCartao.GREEN_CARD]: 'Bastante forte em diversas regiões do Brasil, atuando no setor de benefícios do PAT (Programa de Alimentação do Trabalhador).',
  [BandeiraCartao.VALECARD_VOLUS]: 'Opções focadas em soluções corporativas regionais e gestão de benefícios.',
};

export const BANDEIRA_OPTIONS = Object.values(BandeiraCartao).map(value => ({
  value,
  label: BANDEIRA_LABELS[value],
}));

export function getBandeiraLabel(bandeira: string): string {
  return BANDEIRA_LABELS[bandeira as BandeiraCartao] || bandeira;
}

export function getBandeiraDescricao(bandeira: string): string {
  return BANDEIRA_DESCRICOES[bandeira as BandeiraCartao] || '';
}