import { Store as StoreIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function AtivosImobilizadosPage() {
  return (
    <PlaceholderPage
      title="Ativos Imobilizados"
      subtitle="Cadastro e depreciação de bens"
      icon={<StoreIcon />}
      features={[
        'Cadastro de bens e imobilizado',
        'Cálculo de depreciação',
        'Baixa e transferência de ativos',
        'Integração contábil',
      ]}
    />
  );
}
