import {
  Box,
  Card,
  CardContent,
  Grid,
  Avatar,
  Typography,
  Divider,
  Button,
  Chip,
  TextField,
  Select,
  MenuItem,
  InputLabel,
  IconButton,
} from '@mui/material';
import {
  Person as PersonIcon,
  Cash as CashIcon,
  PanVert as PanVertIcon,
  CheckCircle as CheckCircleIcon,
  CalendarMonth as CalendarMonthIcon,
} from '@mui/icons-material';
import { useAuth } from '@/hooks/useAuth';
import { usePdv } from '@/contexts/PdvContext';
import { brand } from '@/theme';
import { apiPdv } from '@/services/api';

export function UsuarioPage() {
  const { user } = useAuth();
  const userData = user as any;
  const { selectedPdv, sessaoAtiva, abrirCaixa, fecharCaixa } = usePdv();

  const [aberto, setAberto] = React.useState(false);
  const [valorInicial, setValorInicial] = React.useState('');
  const [operador, setOperador] = React.useState(userData?.nome || '');

  const handleAbrirCaixa = async () => {
    if (!valorInicial) {
      return;
    }
    try {
      await apiPdv.abrirCaixaAutomatico({
        pdvId: selectedPdv?.id,
        operador,
        valorInicial: parseFloat(valorInicial),
      });
      setAberto(true);
    } catch (error) {
      console.error('Erro ao abrir caixa:', error);
    }
  };

  React.useEffect(() => {
    const timeout = setTimeout(() => setAberto(false), 5000);
    return () => clearTimeout(timeout);
  }, [aberto]);

  return (
    <Box sx={{ p: 3, width: '100%', bgcolor: 'background.default' }}>
      <PageHeader
        title="Painel do Usuário"
        subtitle="Dados do usuário e operação de abertura de caixa"
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
                Perfil e Permissões
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <PersonIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Nome completo</Typography>
                      <Typography variant="body1" fontWeight={600}>
                        {userData?.nome || 'Operador Tenda'}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <EmailIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">E-mail</Typography>
                      <Typography variant="body1" fontWeight={600}>
                        {userData?.email || 'operador@sistematenda.com.br'}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <SecurityIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Perfil / Papel</Typography>
                      <Typography variant="body1" fontWeight={600}>
                        {userData?.role || 'ADMIN'}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                    <CashIcon color="action" />
                    <Box>
                      <Typography variant="caption" color="text.secondary">PDV Ativo</Typography>
                      <Typography variant="body1" fontWeight={600}>
                        {selectedPdv?.codigo || 'Nenhum PDV selecionado'}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
              </Grid>
              <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                <Button variant="outlined" disabled>
                  Alterar senha
                </Button>
                <Button variant="contained" disabled>Salvar alterações</Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Divider sx={{ my: 4, borderColor: 'divider' }} />

      <Card>
        <CardContent>
          <Typography variant="h6" fontWeight={700} gutterBottom>
            Abertura de Caixa — Exemplo
          </Typography>
          <Divider sx={{ mb: 2 }} />
          <Box sx={{ mb: 2 }}>
            <Typography variant="body1" fontWeight={600}>
              PDV Ativo:
            </Typography>
            <Chip
              label={selectedPdv?.codigo || 'Nenhum PDV'}
              size="small"
              color={selectedPdv ? 'primary' : 'default'}
              variant="outlined"
            />
          </Box>

          {sessaoAtiva ? (
            <Box sx={{ mt: 2, p: 2, bgcolor: 'rgba(46,139,87,0.1)', borderRadius: 2, border: '1px solid rgba(46,139,87,0.3)' }}>
              <Typography variant="body2" color="success">
                <CheckCircleIcon fontSize="small" sx={{ mr: 1 }} /> Caixa já aberto
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Operador: {sessaoAtiva.operador} | Valor Inicial: R$ {sessaoAtiva.valorInicial?.toFixed(2)} | Status: {sessaoAtiva.status}
              </Typography>
            </Box>
          ) : (
            <Box sx={{ mt: 2 }}>
              <TextField
                label="Valor Inicial do Caixa"
                type="number"
                fullWidth
                sx={{ mb: 2 }}
                defaultValue="0"
                InputLabelProps={{ shrink: true }}
                InputProps={{
                  endAdornment: 'start',
                }}
              />
              <Select
                fullWidth
                label="Operador"
                sx={{ mb: 2, display: 'flex', alignItems: 'center' }}
                value={operador}
                onChange={(e: any) => setOperador(e.target.value)}
                MenuProps={{ PaperProps: { sx: { maxHeight: 200 } } }}
              >
                <MenuItem value={userData?.nome || ''}>
                  {userData?.nome || 'Operador Padrão'}
                </MenuItem>
                <MenuItem value="OUTRO">
                  Outro
                </MenuItem>
              </Select>

              <IconButton
                variant="contained"
                color="primary"
                sx={{ width: '100%', mt: 2 }}
                onClick={handleAbrirCaixa}
              >
                <PanVertIcon sx={{ mr: 1 }} /> Abrir Caixa
              </IconButton>
            </Box>
          )}
        </CardContent>
      </Card>

      {aberto && (
        <Box sx={{ mt: 3, p: 2, bgcolor: 'rgba(46,139,87,0.1)', borderRadius: 2, border: '1px solid rgba(46,139,87,0.3)' }}>
          <Typography variant="body2" color="success">
            <CheckCircleIcon fontSize="small" sx={{ mr: 1 }} /> Caixa aberto com sucesso!
          </Typography>
          <Typography variant="caption" color="text.secondary">
            O caixa foi aberto com valor inicial de R$ {valorInicial} pelo operador {operador}.
          </Typography>
        </Box>
      )}
    </Box>
  );
}

export default UsuarioPage;