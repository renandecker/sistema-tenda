import { AttachMoney as AttachMoneyIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function SessoesCaixaPage() {
  return (
    <PlaceholderPage
      title="Sessões de Caixa"
      subtitle="Abertura, fechamento, sangria e reforço"
      icon={<AttachMoneyIcon />}
      features={[
        'Abertura e fechamento de caixa',
        'Sangria e reforço com justificativa',
        'Conferência de valores por operador',
        'Relatório de divergências',
      ]}
    />
  );
}
