import { Print as PrintIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function EtiquetasBalancaPage() {
  return (
    <PlaceholderPage
      title="Etiquetas de Balança"
      subtitle="Geração e impressão de etiquetas com código de barras"
      icon={<PrintIcon />}
      features={[
        'Geração de código de barras EAN-13',
        'Impressão simples e em lote',
        'Integração com balanças TOLEDO/FILIZOLA',
        'Layouts personalizados de etiqueta',
      ]}
    />
  );
}
