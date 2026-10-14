import { Assessment as AssessmentIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function DrePage() {
  return (
    <PlaceholderPage
      title="DRE"
      subtitle="Demonstrativo de Resultado do Exercício"
      icon={<AssessmentIcon />}
      features={[
        'Receita bruta e líquida',
        'CMV por unidade de negócio',
        'Despesas operacionais',
        'Lucro líquido e margens',
      ]}
    />
  );
}
