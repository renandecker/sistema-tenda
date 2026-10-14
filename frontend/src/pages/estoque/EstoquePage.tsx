import { Inventory as InventoryIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function EstoquePage() {
  return (
    <PlaceholderPage
      title="Movimentação de Estoque"
      subtitle="Entradas, saídas e saldos"
      icon={<InventoryIcon />}
      features={[
        'Entradas e saídas de estoque',
        'Saldos por produto e local',
        'Histórico de movimentações',
        'Integração com compras e vendas',
      ]}
    />
  );
}
