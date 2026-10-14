import { AccountBalance as AccountBalanceIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function SpedFiscalPage() {
  return (
    <PlaceholderPage
      title="SPED Fiscal"
      subtitle="Geração de arquivos SPED Fiscal e Contribuições"
      icon={<AccountBalanceIcon />}
      features={[
        'SPED Fiscal (EFD ICMS/IPI)',
        'SPED Contribuições',
        'Validação de registros',
        'Exportação de arquivos',
      ]}
    />
  );
}
