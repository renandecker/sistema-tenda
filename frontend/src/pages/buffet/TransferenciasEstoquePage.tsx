import { SwapHoriz as SwapHorizIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function TransferenciasEstoquePage() {
  return (
    <PlaceholderPage
      title="Transferências de Estoque"
      subtitle="Movimentação entre fruteira, buffet e estoque"
      icon={<SwapHorizIcon />}
      features={[
        'Solicitação e aprovação',
        'Transferência entre unidades',
        'Conferência de recebimento',
        'Rastreabilidade dos movimentos',
      ]}
    />
  );
}
