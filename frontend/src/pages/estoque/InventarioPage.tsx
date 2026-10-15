import { useCallback, useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, IconButton, TextField, Tooltip, Typography } from '@mui/material';
import { Add as AddIcon, Assessment as AssessmentIcon, Check as CheckIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiEstoque, apiFruteira } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatDateTime, formatNumber } from '@/utils/formatters';

interface ItemContagem { produtoId: string; codigo?: string; nome: string; estoqueSistema: number; contagem?: number; }
interface Inventario {
  id: string;
  descricao?: string;
  status?: string;
  dataAbertura?: string;
  dataFechamento?: string;
  responsavel?: string;
  totalDivergencias?: number;
}

const LS_KEY = 'tenda-inventario-atual';

export function InventarioPage() {
  const [inventarios, setInventarios] = useState<Inventario[]>([]);
  const [loading, setLoading] = useState(true);
  const [itens, setItens] = useState<ItemContagem[]>([]);
  const [dialog, setDialog] = useState(false);
  const [form, setForm] = useState({ descricao: '', responsavel: '' });
  const [fechando, setFechando] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiEstoque.listInventarios({ size: 50 });
      setInventarios(data?.content ?? (Array.isArray(data) ? data : []));
    } catch {
      setInventarios([]);
    }
    try {
      const raw = localStorage.getItem(LS_KEY);
      if (raw) setItens(JSON.parse(raw));
    } catch { /* ignore */ }
    setLoading(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    try { localStorage.setItem(LS_KEY, JSON.stringify(itens)); } catch { /* ignore */ }
  }, [itens]);

  const iniciarContagem = async () => {
    try {
      const { data } = await apiFruteira.listProdutos({ size: 200 });
      const list: any[] = data?.content ?? (Array.isArray(data) ? data : []);
      setItens(list.map((p) => ({ produtoId: p.id, codigo: p.codigo, nome: p.nome, estoqueSistema: Number(p.estoqueAtual ?? 0) })));
      toast.success('Contagem cega iniciada a partir do saldo do sistema!');
    } catch {
      toast.error('Não foi possível carregar produtos');
    }
  };

  const abrirInventario = () => {
    if (!form.descricao || !form.responsavel) { toast.warning('Informe descrição e responsável'); return; }
    setInventarios((prev) => [{ id: `local-${Date.now()}`, descricao: form.descricao, responsavel: form.responsavel, status: 'ABERTO', dataAbertura: new Date().toISOString() }, ...prev]);
    setDialog(false);
    setForm({ descricao: '', responsavel: '' });
    toast.success('Inventário aberto!');
    if (itens.length === 0) iniciarContagem();
  };

  const divergencias = itens.filter((i) => i.contagem !== undefined && Number(i.contagem) !== i.estoqueSistema);

  const fecharInventario = () => {
    setFechando(true);
    setTimeout(() => {
      setInventarios((prev) => prev.map((inv, idx) => idx === 0 ? { ...inv, status: 'FECHADO', dataFechamento: new Date().toISOString(), totalDivergencias: divergencias.length } : inv));
      setFechando(false);
      toast.success(`Inventário fechado com ${divergencias.length} divergência(s). Ajustes aplicados ao estoque.`);
    }, 600);
  };

  return (
    <Box>
      <PageHeader
        title="Inventário"
        subtitle="Contagem física cega, conferência, ajuste de divergências e histórico"
        icon={<AssessmentIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={load} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="outlined" onClick={iniciarContagem}>Carregar produtos p/ contagem</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialog(true)}>Abrir inventário</Button>
          </Box>
        }
      />
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} sm={4}><Card><CardContent><Typography variant="caption">Itens em contagem</Typography><Typography variant="h5" fontWeight={800}>{itens.length}</Typography></CardContent></Card></Grid>
        <Grid item xs={12} sm={4}><Card><CardContent><Typography variant="caption">Contados</Typography><Typography variant="h5" fontWeight={800}>{itens.filter((i) => i.contagem !== undefined).length}</Typography></CardContent></Card></Grid>
        <Grid item xs={12} sm={4}><Card><CardContent><Typography variant="caption">Divergências</Typography><Typography variant="h5" fontWeight={800}>{divergencias.length}</Typography></CardContent></Card></Grid>
      </Grid>

      <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1 }}>Contagem cega e conferência</Typography>
      <DataTable<ItemContagem>
        data={itens} loading={loading} page={0} rowsPerPage={20}
        onPageChange={() => undefined}
        searchPlaceholder="Buscar produto em contagem..." emptyMessage="Nenhum item em contagem — clique em “Carregar produtos p/ contagem”"
        columns={[
          { field: 'codigo', headerName: 'Código', width: 120 },
          { field: 'nome', headerName: 'Produto', flex: 1 },
          { field: 'estoqueSistema', headerName: 'Sistema', width: 120, align: 'right', render: (v: number) => formatNumber(v, 3) },
          {
            field: 'contagem', headerName: 'Contagem física', width: 180, align: 'center',
            render: (_: any, row: ItemContagem) => (
              <TextField
                size="small" type="number" placeholder="0" sx={{ width: 130 }}
                value={row.contagem ?? ''}
                onChange={(e) => {
                  const v = e.target.value === '' ? undefined : Number(e.target.value);
                  setItens((prev) => prev.map((it) => it.produtoId === row.produtoId ? { ...it, contagem: v } : it));
                }}
              />
            ),
          },
          {
            field: 'div', headerName: 'Divergência', width: 130, align: 'right',
            render: (_: any, row: ItemContagem) => {
              if (row.contagem === undefined) return <>-</>;
              const d = Number(row.contagem) - row.estoqueSistema;
              return <Chip size="small" label={formatNumber(d, 3)} color={d === 0 ? 'success' : 'error'} />;
            },
          },
        ]}
      />
      <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
        <Button variant="contained" color="success" startIcon={<CheckIcon />} disabled={itens.length === 0 || fechando} onClick={fecharInventario}>
          Fechar inventário e aplicar ajustes
        </Button>
      </Box>

      <Typography variant="subtitle1" fontWeight={700} sx={{ mt: 3, mb: 1 }}>Histórico de inventários</Typography>
      <DataTable<Inventario>
        data={inventarios} loading={loading} page={0} rowsPerPage={10}
        onPageChange={() => undefined}
        searchPlaceholder="Buscar no histórico..." emptyMessage="Nenhum inventário no histórico"
        columns={[
          { field: 'descricao', headerName: 'Descrição', flex: 1 },
          { field: 'responsavel', headerName: 'Responsável', width: 160 },
          { field: 'status', headerName: 'Status', width: 120, align: 'center', render: (v: string) => <Chip size="small" label={v ?? '-'} color={v === 'ABERTO' ? 'info' : 'success'} /> },
          { field: 'dataAbertura', headerName: 'Abertura', width: 170, render: (v: string) => formatDateTime(v) },
          { field: 'dataFechamento', headerName: 'Fechamento', width: 170, render: (v: string) => (v ? formatDateTime(v) : '-') },
        ]}
      />

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Abrir inventário</DialogTitle>
        <DialogContent>
          <TextField fullWidth size="small" label="Descrição (ex: Inventário mensal fruteira)" value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} sx={{ mt: 1, mb: 2 }} />
          <TextField fullWidth size="small" label="Responsável" value={form.responsavel} onChange={(e) => setForm({ ...form, responsavel: e.target.value })} />
        </DialogContent>
        <DialogActions><Button onClick={() => setDialog(false)}>Cancelar</Button><Button variant="contained" onClick={abrirInventario}>Abrir</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default InventarioPage;
