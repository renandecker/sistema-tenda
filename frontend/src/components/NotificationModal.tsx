import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  Typography,
  Box,
} from '@mui/material';
import {
  CheckCircleRounded as SuccessIcon,
  ErrorRounded as ErrorIcon,
  WarningAmberRounded as WarningIcon,
  InfoRounded as InfoIcon,
} from '@mui/icons-material';
import { brand } from '@/theme';

export type NotificationSeverity = 'success' | 'error' | 'warning' | 'info';

interface NotificationModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  message: string;
  severity?: NotificationSeverity;
  confirmText?: string;
}

const severityConfig: Record<
  NotificationSeverity,
  { color: string; icon: React.ReactNode; defaultTitle: string }
> = {
  success: {
    color: brand.green,
    icon: <SuccessIcon sx={{ fontSize: 40 }} />,
    defaultTitle: 'Tudo certo!',
  },
  error: {
    color: brand.red,
    icon: <ErrorIcon sx={{ fontSize: 40 }} />,
    defaultTitle: 'Ocorreu um erro',
  },
  warning: {
    color: '#E8590C',
    icon: <WarningIcon sx={{ fontSize: 40 }} />,
    defaultTitle: 'Atenção',
  },
  info: {
    color: '#0B7285',
    icon: <InfoIcon sx={{ fontSize: 40 }} />,
    defaultTitle: 'Informação',
  },
};

export function NotificationModal({
  open,
  onClose,
  title,
  message,
  severity = 'info',
  confirmText = 'Entendi',
}: NotificationModalProps) {
  const config = severityConfig[severity];

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{ sx: { borderRadius: 3, overflow: 'hidden' } }}
    >
      <Box
        sx={{
          pt: 4,
          pb: 1,
          textAlign: 'center',
          background: `linear-gradient(180deg, ${config.color}18 0%, transparent 100%)`,
        }}
      >
        <Box
          sx={{
            display: 'inline-grid',
            placeItems: 'center',
            width: 72,
            height: 72,
            borderRadius: '50%',
            bgcolor: `${config.color}1A`,
            color: config.color,
          }}
        >
          {config.icon}
        </Box>
      </Box>
      <DialogContent sx={{ textAlign: 'center', px: 3, pt: 0 }}>
        <Typography variant="h6" fontWeight={700} gutterBottom>
          {title || config.defaultTitle}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {message}
        </Typography>
      </DialogContent>
      <DialogActions sx={{ justifyContent: 'center', p: 3, pt: 0 }}>
        <Button
          variant="contained"
          onClick={onClose}
          autoFocus
          sx={{
            minWidth: 140,
            bgcolor: config.color,
            color: '#FFFFFF',
            '&:hover': { bgcolor: config.color, filter: 'brightness(0.9)' },
          }}
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default NotificationModal;
