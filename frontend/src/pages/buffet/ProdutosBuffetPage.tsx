import { Restaurant as RestaurantIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function ProdutosBuffetPage() {
  return (
    <PlaceholderPage
      title="Produtos - Buffet"
      subtitle="Cadastro de pratos e produtos do restaurante"
      icon={<RestaurantIcon />}
      features={[
        'Preço por quilo e por porção',
        'Produtos disponíveis no balcão',
        'Vínculo com fichas técnicas',
        'Controle de disponibilidade',
      ]}
    />
  );
}
