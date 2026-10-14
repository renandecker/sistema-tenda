import { PriceChange as PriceChangeIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function TabelasPrecoPage() {
  return (
    <PlaceholderPage
      title="Tabelas de Preço"
      subtitle="Vigência e aplicação de preços por período"
      icon={<PriceChangeIcon />}
      features={[
        'Preços com vigência por data',
        'Aplicação em lote',
        'Histórico de alterações',
        'Diferentes tabelas por canal',
      ]}
    />
  );
}
