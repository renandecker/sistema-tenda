import { Kitchen as KitchenIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function ReceitasBuffetPage() {
  return (
    <PlaceholderPage
      title="Receitas e Fichas Técnicas"
      subtitle="Composição de pratos e custo de produção"
      icon={<KitchenIcon />}
      features={[
        'Ficha técnica por prato',
        'Cálculo de custo e CMV',
        'Ingredientes e rendimento',
        'Baixa de insumos por produção',
      ]}
    />
  );
}
