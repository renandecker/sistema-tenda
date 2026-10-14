import { TableRestaurant as TableRestaurantIcon } from '@mui/icons-material';
import { PlaceholderPage } from '@/components/PlaceholderPage';

export function MesasBuffetPage() {
  return (
    <PlaceholderPage
      title="Mesas"
      subtitle="Mapa e status das mesas do buffet"
      icon={<TableRestaurantIcon />}
      features={[
        'Mapa visual de mesas',
        'Estados: livre, ocupada, reservada',
        'Reserva e transferência de mesas',
        'Ocupação em tempo real',
      ]}
    />
  );
}
