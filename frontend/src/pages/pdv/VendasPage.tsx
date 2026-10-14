import { ReceiptLong as ReceiptLongIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function VendasPage() {
  return (
    <PlaceholderPage
      title="Vendas"
      subtitle="Consulta e gestão de vendas realizadas"
      icon={<ReceiptLongIcon />}
      features={[
        'Filtro por período, PDV e status',
        'Cancelamento e estorno',
        'Reimpressão de comprovantes',
        'Totais por forma de pagamento',
      ]}
    />
  );
}
