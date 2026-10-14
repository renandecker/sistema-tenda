import { Assessment as AssessmentIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function InventarioPage() {
  return (
    <PlaceholderPage
      title="Inventário"
      subtitle="Contagem física e ajuste de estoque"
      icon={<AssessmentIcon />}
      features={[
        'Abertura de inventário',
        'Contagem cega e conferência',
        'Ajuste de divergências',
        'Fechamento e histórico',
      ]}
    />
  );
}
