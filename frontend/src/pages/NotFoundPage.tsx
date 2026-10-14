import { Link } from 'react-router-dom';
import { Box, Button, Typography } from '@mui/material';
import { Home as HomeIcon, Search as SearchIcon } from '@mui/icons-material';

export function NotFoundPage() {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', textAlign: 'center', p: 4 }}>
      <Typography variant="h1" fontSize="120px" fontWeight={700} color="primary.light" sx={{ lineHeight: 1, mb: 2 }}>
        404
      </Typography>
      <Typography variant="h4" gutterBottom>Página não encontrada</Typography>
      <Typography variant="body1" color="text.secondary" paragraph maxWidth="400">
        A página que você procura não existe ou foi movida. Verifique o endereço ou use a navegação do menu.
      </Typography>
      <Box sx={{ display: 'flex', gap: 2, mt: 3, flexWrap: 'wrap', justifyContent: 'center' }}>
        <Button variant="contained" startIcon={<HomeIcon />} component={Link} to="/dashboard">
          Ir para Dashboard
        </Button>
        <Button variant="outlined" startIcon={<SearchIcon />} component={Link} to="/vendas">
          Buscar Vendas
        </Button>
      </Box>
    </Box>
  );
}