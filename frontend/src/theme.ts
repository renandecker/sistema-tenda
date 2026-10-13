import { createTheme } from '@mui/material/styles';

/**
 * Paleta de marca derivada do logo (logo.png):
 *  - Amarelo/Laranja âmbar  (#F5A800 / #FFC24D / #C77F00)
 *  - Preto/carvão            (#262220)
 *  - Vermelho de destaque    (#D7263D)
 *  - Fundo creme quente      (#F7F3EC)
 */
export const brand = {
  amber: '#F5A800',
  amberLight: '#FFC24D',
  amberDark: '#C77F00',
  charcoal: '#262220',
  cream: '#F7F3EC',
  red: '#D7263D',
  green: '#2E7D32',
};

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: brand.amber,
      light: brand.amberLight,
      dark: brand.amberDark,
      contrastText: brand.charcoal,
    },
    secondary: {
      main: brand.charcoal,
      light: '#4A443F',
      dark: '#000000',
      contrastText: '#FFFFFF',
    },
    error: {
      main: brand.red,
    },
    warning: {
      main: '#E8590C',
      light: '#FF8A3D',
      dark: '#B5430A',
    },
    info: {
      main: '#0B7285',
    },
    success: {
      main: brand.green,
      light: '#4CAF50',
      dark: '#1B5E20',
    },
    background: {
      default: brand.cream,
      paper: '#FFFFFF',
    },
    text: {
      primary: '#1F1B16',
      secondary: '#6B6259',
    },
    divider: 'rgba(38, 34, 32, 0.10)',
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: { fontSize: '2.5rem', fontWeight: 700, fontFamily: '"Poppins", sans-serif' },
    h2: { fontSize: '2rem', fontWeight: 700, fontFamily: '"Poppins", sans-serif' },
    h3: { fontSize: '1.75rem', fontWeight: 700, fontFamily: '"Poppins", sans-serif' },
    h4: { fontSize: '1.5rem', fontWeight: 600, fontFamily: '"Poppins", sans-serif' },
    h5: { fontSize: '1.25rem', fontWeight: 600, fontFamily: '"Poppins", sans-serif' },
    h6: { fontSize: '1.05rem', fontWeight: 600, fontFamily: '"Poppins", sans-serif' },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 10,
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: brand.cream,
        },
        '::-webkit-scrollbar': { width: 10, height: 10 },
        '::-webkit-scrollbar-thumb': {
          backgroundColor: 'rgba(38,34,32,0.25)',
          borderRadius: 8,
        },
        '::-webkit-scrollbar-thumb:hover': { backgroundColor: 'rgba(38,34,32,0.4)' },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0px 2px 10px rgba(38,34,32,0.08)',
          backgroundImage: 'none',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          padding: '8px 18px',
          fontSize: '0.875rem',
        },
        contained: {
          boxShadow: '0px 2px 6px rgba(38,34,32,0.15)',
          '&:hover': {
            boxShadow: '0px 4px 12px rgba(38,34,32,0.22)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0px 2px 12px rgba(38,34,32,0.06)',
          border: '1px solid rgba(38,34,32,0.06)',
          borderRadius: 14,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: { borderRadius: 14 },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            '&:hover fieldset': { borderColor: brand.amber },
            '&.Mui-focused fieldset': { borderColor: brand.amber, borderWidth: 2 },
          },
          '& .MuiInputLabel-root.Mui-focused': { color: brand.amberDark },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 700,
          backgroundColor: 'rgba(245,168,0,0.08)',
          color: brand.charcoal,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600 },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: '1px solid rgba(38,34,32,0.08)',
          backgroundColor: '#FFFFFF',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          '&.Mui-selected': {
            backgroundColor: 'rgba(245,168,0,0.16)',
            color: brand.charcoal,
            '& .MuiListItemIcon-root': { color: brand.amberDark },
            '&:hover': { backgroundColor: 'rgba(245,168,0,0.26)' },
          },
        },
      },
    },
    MuiToggleButton: {
      styleOverrides: {
        root: {
          '&.Mui-selected': {
            backgroundColor: brand.amber,
            color: brand.charcoal,
            '&:hover': { backgroundColor: brand.amberDark, color: '#FFFFFF' },
          },
        },
      },
    },
  },
});

export default theme;
