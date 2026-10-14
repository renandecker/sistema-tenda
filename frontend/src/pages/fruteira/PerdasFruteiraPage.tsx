import { Warning as WarningIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function PerdasFruteiraPage() {
  return (
    <PlaceholderPage
      title="Perdas e Quebras"
      subtitle="Registro de perdas do hortifrúti"
      icon={<WarningIcon />}
      features={[
        'Registro rápido de perdas',
        'Motivos e responsáveis',
        'Baixa automática de estoque',
        'Relatório de perdas por período',
      ]}
    />
  );
}
