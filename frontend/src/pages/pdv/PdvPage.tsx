import { PointOfSale as PointOfSaleIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function PdvPage() {
  return (
    <PlaceholderPage
      title="PDV"
      subtitle="Ponto de venda - Fruteira e Buffet"
      icon={<PointOfSaleIcon />}
      features={[
        'Venda de produtos por código de barras',
        'Peso integrado com balança',
        'Múltiplas formas de pagamento',
        'Impressão de comprovante não fiscal',
      ]}
    />
  );
}
