import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, MenuItem, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Edit as EditIcon, Refresh as RefreshIcon, TableRestaurant as TableRestaurantIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet } from '@/services/api';
import { PageHeader } from '@/components/PageHeader';
import { getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Mesa {
  id: string;
  numero?: string;
  nome?: string;
  capacidade?: number;
  status?: string;
  area?: string;
  ativa?: boolean;
  observacoes?: string;
  comandaAtiva?: { numero?: string; clienteNome?: string };
}

const emptyForm = { numero: '', nome: '', capacidade: '4', area: 'SALAO', observacoes: '' };

export function MesasBuffetPage() {
  const [mesas, setMesas] = useState<Mesa[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState<Mesa | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [filtro, setFiltro] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiBuffet.listMesas({ size: 200 });
      setMesas(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setMesas([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const salvar = async () => {
    if (!form.numero) { toast.warning('Informe o número da mesa'); return; }
    try {
      const payload = { numero: form.numero, nome: form.nome || `Mesa ${form.numero}`, capacidade: Number(form.capacidade) || 4, area: form.area, observacoes: form.observacoes, ativa: true };
      if (editing) await apiBuffet.updateMesa(editing.id, payload);
      else await apiBuffet.createMesa(payload);
      toast.success('Mesa salva!');
      setDialog(false); load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao salvar');
    }
  };

  const acao = async (id: string, fn: (id: string) => Promise<unknown>, label: string) => {
    try {
      await fn(id);
      toast.success(`Mesa ${label}!`);
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || `Erro ao ${label}`);
    }
  };

  const excluir = async (id: string) => {
    if (!window.confirm('Excluir mesa?')) return;
    try {
      await apiBuffet.deleteMesa(id);
      toast.success('Mesa excluída');
      load();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao excluir');
    }
  };

  const filtradas = mesas.filter((m) => !filtro || m.status === filtro);
  const resumo = {
    livres: mesas.filter((m) => m.status === 'LIVRE').length,
    ocupadas: mesas.filter((m) => m.status === 'OCUPADA').length,
    reservadas: mesas.filter((m) => m.status === 'RESERVADA').length,
  };

  return (
    <Box>
      <PageHeader
        title="Mesas — Buffet"
        subtitle={`Mapa em tempo real · ${resumo.livres} livres · ${resumo.ocupadas} ocupadas · ${resumo.reservadas} reservadas`}
        icon={<TableRestaurantIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField select size="small" label="Filtrar" value={filtro} onChange={(e) => setFiltro(e.target.value)} sx={{ minWidth: 140 }}>
              <MenuItem value="">Todas</MenuItem>
              {['LIVRE', 'OCUPADA', 'RESERVADA', 'MANUTENCAO'].map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
            <Tooltip title="Atualizar"><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditing(null); setForm(emptyForm); setDialog(true); }}>Nova mesa</Button>
          </Box>
        }
      />
      {loading && <Typography variant="body2" color="text.secondary">Carregando mapa de mesas...</Typography>}
      {!loading && filtradas.length === 0 && <Typography variant="body2" color="text.secondary">Nenhuma mesa encontrada. Cadastre as mesas do salão.</Typography>}
      <Grid container spacing={2}>
        {filtradas.map((m) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={m.id}>
            <Card sx={{ borderTop: 4, borderColor: m.status === 'LIVRE' ? 'success.main' : m.status === 'OCUPADA' ? 'warning.main' : 'info.main' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="h6">Mesa {m.numero}</Typography>
                  <Chip size="small" label={getStatusLabel(m.status ?? '')} color={getStatusColor(m.status ?? '')} />
                </Box>
                <Typography variant="body2" color="text.secondary">{m.nome} · Cap. {m.capacidade ?? '-'} · {m.area ?? ''}</Typography>
                {m.comandaAtiva && <Typography variant="caption" color="warning.main">Comanda {m.comandaAtiva.numero} — {m.comandaAtiva.clienteNome}</Typography>}
                <Box sx={{ display: 'flex', gap: 0.5, mt: 1.5, flexWrap: 'wrap' }}>
                  <Button size="small" variant="outlined" color="warning" onClick={() => acao(m.id, apiBuffet.ocuparMesa, 'ocupada')}>Ocupar</Button>
                  <Button size="small" variant="outlined" color="success" onClick={() => acao(m.id, apiBuffet.liberarMesa, 'liberada')}>Liberar</Button>
                  <Button size="small" variant="outlined" onClick={() => acao(m.id, apiBuffet.reservarMesa, 'reservada')}>Reservar</Button>
                  <IconButton size="small" onClick={() => { setEditing(m); setForm({ numero: m.numero ?? '', nome: m.nome ?? '', capacidade: String(m.capacidade ?? 4), area: m.area ?? 'SALAO', observacoes: m.observacoes ?? '' }); setDialog(true); }}><EditIcon fontSize="small" /></IconButton>
                  <IconButton size="small" color="error" onClick={() => excluir(m.id)}><DeleteIcon fontSize="small" /></IconButton>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{editing ? 'Editar mesa' : 'Nova mesa'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={6}><TextField fullWidth size="small" label="Número" value={form.numero} onChange={(e) => setForm({ ...form, numero: e.target.value })} /></Grid>
            <Grid item xs={6}><TextField fullWidth size="small" label="Capacidade" value={form.capacidade} onChange={(e) => setForm({ ...form, capacidade: e.target.value })} /></Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Nome" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} /></Grid>
            <Grid item xs={12}>
              <TextField select fullWidth size="small" label="Área" value={form.area} onChange={(e) => setForm({ ...form, area: e.target.value })}>
                {['SALAO', 'VARANDA', 'EXTERNA', 'RESERVADO', 'BALCAO'].map((a) => <MenuItem key={a} value={a}>{a}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}><TextField fullWidth size="small" label="Observações" value={form.observacoes} onChange={(e) => setForm({ ...form, observacoes: e.target.value })} /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={salvar}>Salvar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default MesasBuffetPage;
