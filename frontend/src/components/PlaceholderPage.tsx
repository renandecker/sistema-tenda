import React from 'react';
import { Box, Typography, Chip, Paper, Grid } from '@mui/material';
import { Construction as ConstructionIcon } from '@mui/icons-material';
import { PageHeader } from './PageHeader';

interface PlaceholderPageProps {
  title: string;
  subtitle?: string;
  icon?: React.ReactNode;
  description?: string;
  features?: string[];
}

export function PlaceholderPage({ title, subtitle, icon, description, features }: PlaceholderPageProps) {
  return (
    <Box>
      <PageHeader
        title={title}
        subtitle={subtitle}
        icon={icon}
        action={<Chip label="Em desenvolvimento" color="warning" size="small" />}
      />
      <Paper sx={{ p: { xs: 3, md: 5 }, textAlign: 'center' }}>
        <Box
          sx={{
            display: 'grid',
            placeItems: 'center',
            width: 84,
            height: 84,
            mx: 'auto',
            mb: 2,
            borderRadius: '50%',
            bgcolor: 'rgba(245,168,0,0.12)',
            color: 'warning.main',
          }}
        >
          <ConstructionIcon sx={{ fontSize: 46 }} />
        </Box>
        <Typography variant="h6" gutterBottom>
          Módulo em desenvolvimento
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 620, mx: 'auto' }}>
          {description || `Esta tela de ${title} faz parte do escopo do Sistema Tenda e está sendo construída. O layout e a identidade visual já estão aplicados.`}
        </Typography>

        {features && features.length > 0 && (
          <Grid container spacing={2} sx={{ mt: 3, maxWidth: 720, mx: 'auto', textAlign: 'left' }}>
            {features.map((feature) => (
              <Grid item xs={12} sm={6} key={feature}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'primary.main', flexShrink: 0 }} />
                  <Typography variant="body2" color="text.secondary">
                    {feature}
                  </Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        )}
      </Paper>
    </Box>
  );
}

export default PlaceholderPage;
