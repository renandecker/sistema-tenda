import { Warning as WarningIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function AlertasEstoquePage() {
  return (
    <PlaceholderPage
      title="Alertas de Estoque"
      subtitle="Produtos com estoque baixo ou vencendo"
      icon={<WarningIcon />}
      features={[
        'Estoque mínimo por produto',
        'Alertas de validade',
        'Sugestão de reposição',
        'Notificações em tempo real',
      ]}
    />
  );
}
