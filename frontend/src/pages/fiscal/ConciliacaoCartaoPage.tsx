import { CreditCard as CreditCardIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function ConciliacaoCartaoPage() {
  return (
    <PlaceholderPage
      title="Conciliação de Cartão"
      subtitle="Conferência de recebíveis de cartão"
      icon={<CreditCardIcon />}
      features={[
        'Importação de extratos de adquirentes',
        'Conciliação com vendas',
        'Taxas e prazos',
        'Divergências e ajustes',
      ]}
    />
  );
}
