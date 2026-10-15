import { Box, Card, CardContent, Grid, Avatar, Typography, Divider, Button, Chip } from '@mui/material';
import { Person as PersonIcon, Security as SecurityIcon, Badge as BadgeIcon, Email as EmailIcon } from '@mui/icons-material';
import { useAuth } from '@/hooks/useAuth';
import { PageHeader } from '@/components/PageHeader';

export function PerfilPage() {
  const { user } = useAuth();
  const userData = user as any;

  return (
    <Box>
      <PageHeader
        title="Meu Perfil"
        subtitle="Informações do usuário logado e permissões no Sistema Tenda"
        icon={<PersonIcon />}
      />
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card sx={{ textAlign: 'center', p: 3 }}>
            <Avatar
              sx={{
                width: 96,
                height: 96,
                mx: 'auto',
                mb: 2,
                bgcolor: 'primary.main',
                color: 'primary.contrastText',
                fontSize: 40,
                fontWeight: 700,
              }}
            >
              {userData?.nome?.charAt(0).toUpperCase() || 'U'}
            </Avatar>
            <Typography variant="h5" fontWeight={700}>
              {userData?.nome || 'Usuário Sistema Tenda'}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {userData?.email || 'operador@sistematenda.com.br'}
            </Typography>
            <Chip
              label={userData?.role || 'OPERADOR'}
              color="primary"
              size="small"
              sx={{ mt: 2, fontWeight: 700 }}
            />
          </Card>
        </Grid>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={700} gutterBottom>
                Detalhes da Conta
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <BadgeIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Nome completo</Typography>
                      <Typography variant="body1" fontWeight={600}>{userData?.nome || 'Operador Tenda'}</Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <EmailIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">E-mail</Typography>
                      <Typography variant="body1" fontWeight={600}>{userData?.email || 'operador@sistematenda.com.br'}</Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <SecurityIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Perfil / Papel</Typography>
                      <Typography variant="body1" fontWeight={600}>{userData?.role || 'ADMIN'}</Typography>
                    </Box>
                  </Box>
                </Grid>
              </Grid>
              <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                <Button variant="outlined" disabled>Alterar senha</Button>
                <Button variant="contained" disabled>Salvar alterações</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
export default PerfilPage;
