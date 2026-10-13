import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  IconButton,
  InputAdornment,
  Checkbox,
  Divider,
  FormControlLabel,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useAuth } from '@/hooks/useAuth';
import { brand } from '@/theme';

const schema = yup.object().shape({
  email: yup.string().email('Email inválido').required('Email é obrigatório'),
  senha: yup.string().min(6, 'Senha deve ter no mínimo 6 caracteres').required('Senha é obrigatória'),
});

type LoginForm = yup.InferType<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>({
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    const savedRemember = localStorage.getItem('remember_me');
    const savedUser = localStorage.getItem('user');
    if (savedRemember === 'true' && savedUser) {
      setRememberMe(true);
    }
  }, []);

  const onSubmit = async (data: LoginForm) => {
    setIsLoading(true);
    setError('');
    try {
      await login(data.email, data.senha, rememberMe);
      setIsLoading(false);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Email ou senha inválidos');
      setIsLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
        background: `linear-gradient(135deg, ${brand.amberLight} 0%, ${brand.amber} 48%, ${brand.amberDark} 100%)`,
      }}
    >
      <Card sx={{ maxWidth: 440, width: '100%', boxShadow: 8, borderRadius: 4 }}>
        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Box
              component="img"
              src="/logo.png"
              alt="Sistema Tenda"
              sx={{
                height: 128,
                width: 128,
                objectFit: 'contain',
                borderRadius: 3,
                bgcolor: brand.cream,
                p: 1,
                mb: 2,
              }}
            />
            <Typography variant="h4" fontWeight={700} color="text.primary">
              Sistema Tenda
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Gestão integrada de Fruteira e Buffet
            </Typography>
          </Box>

          <Divider sx={{ mb: 3 }} />

          <Typography variant="h6" fontWeight={600} gutterBottom>
            Entrar no sistema
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit(onSubmit)}>
            <TextField
              fullWidth
              label="Email"
              type="email"
              {...register('email')}
              error={!!errors.email}
              helperText={errors.email?.message}
              margin="normal"
              autoComplete="email"
              autoFocus
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="Senha"
              type={showPassword ? 'text' : 'password'}
              {...register('senha')}
              error={!!errors.senha}
              helperText={errors.senha?.message}
              margin="normal"
              autoComplete="current-password"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" aria-label="Mostrar senha">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 1 }}
            />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    color="primary"
                  />
                }
                label={<Typography variant="body2" color="text.secondary">Lembrar de mim</Typography>}
              />
              <Button
                type="button"
                onClick={() => { /* TODO: fluxo de recuperação de senha */ }}
                variant="text"
                size="small"
                sx={{ fontSize: '0.8rem' }}
              >
                Esqueci minha senha
              </Button>
            </Box>

            <Button
              type="submit"
              fullWidth
              size="large"
              variant="contained"
              color="primary"
              disabled={isLoading}
              sx={{ py: 1.5, fontWeight: 700 }}
            >
              {isLoading ? <CircularProgress size={24} color="inherit" /> : 'Entrar'}
            </Button>
          </form>

          <Divider sx={{ my: 3 }} />

          <Typography variant="caption" color="text.secondary" display="block" textAlign="center">
            Demo: admin@sistematenda.com / 123456
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
