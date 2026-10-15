import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Divider, Grid, IconButton, MenuItem, TextField, Tooltip, Typography,
} from '@mui/material';
import {
  Add as AddIcon,
  PointOfSale as PointOfSaleIcon,
  Refresh as RefreshIcon,
  Search as SearchIcon,
  Scale as ScaleIcon,
  Payments as PaymentsIcon,
  Cancel as CancelIcon,
  CheckCircle as CheckIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet, apiFruteira, apiPdv } from '@/services/api';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency } from '@/utils/formatters';

interface ItemResumo {
  id?: string;
  codigoProduto?: string;
  nomeProduto?: string;
  quantidade?: number;
  precoUnitario?: number;
  valorTotal?: number;
  origem?: string;
}

interface VendaAtual {
  id: string;
  numero?: string;
  subtotal?: number;
  desconto?: number;
  acrescimo?: number;
  valorTotal?: number;
  valorPago?: number;
  status?: string;
  itens?: ItemResumo[];
  pagamentos?: { formaPagamento?: string; valor?: number }[];
}

interface ProdutoBusca {
  id: string;
  codigo?: string;
  codigoBarras?: string;
  nome: string;
  precoVenda?: number;
  unidadeMedida?: string;
  unidade?: string;
  origem?: 'FRUTEIRA' | 'BUFFET';
}

const FORMAS_PAGAMENTO = ['DINHEIRO', 'PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'VALE_ALIMENTACAO', 'CREDIARIO'];

export function PdvPage() {
  const [venda, setVenda] = useState<VendaAtual | null>(null);
  const [loading, setLoading] = useState(false);
  const [busca, setBusca] = useState('');
  const [resultados, setResultados] = useState<ProdutoBusca[]>([]);
  const [buscando, setBuscando] = useState(false);
  const [quantidade, setQuantidade] = useState('1');
  const [pagDialog, setPagDialog] = useState(false);
  const [formaPagamento, setFormaPagamento] = useState('DINHEIRO');
  const [valorPagamento, setValorPagamento] = useState('');
  const [desconto, setDesconto] = useState('');
  const [pesoDialog, setPesoDialog] = useState(false);
  const [pesoBruto, setPesoBruto] = useState('');

  const recarregarVenda = useCallback(async (id: string) => {
    try {
      const { data } = await apiPdv.getVenda(id);
      setVenda(data);
    } catch {
      /* mantém estado local */
    }
  }, []);

  const novaVenda = async () => {
    setLoading(true);
    try {
      const { data } = await apiPdv.createVenda({ data: new Date().toISOString(), observacoes: 'Venda PDV' });
      setVenda(data);
      toast.success(`Venda ${data?.numero ?? ''} iniciada`);
    } catch {
      // fallback local para operar mesmo sem backend
      const local: VendaAtual = { id: `local-${Date.now()}`, numero: 'LOCAL', subtotal: 0, valorTotal: 0, valorPago: 0, status: 'ABERTA', itens: [], pagamentos: [] };
      setVenda(local);
      toast.info('Backend indisponível — operando em modo local');
    } finally {
      setLoading(false);
    }
  };

  const buscarProdutos = async () => {
    if (!busca.trim()) return;
    setBuscando(true);
    try {
      const [f, b] = await Promise.allSettled([
        apiFruteira.searchProdutos(busca),
        apiBuffet.searchProdutos(busca),
      ]);
      const listF: ProdutoBusca[] = (f.status === 'fulfilled' ? (Array.isArray(f.value.data) ? f.value.data : f.value.data?.content ?? []) : []).map((p: any) => ({ ...p, origem: 'FRUTEIRA' as const }));
      const listB: ProdutoBusca[] = (b.status === 'fulfilled' ? (Array.isArray(b.value.data) ? b.value.data : b.value.data?.content ?? []) : []).map((p: any) => ({ ...p, origem: 'BUFFET' as const }));
      // fallback por código de barras exato
      if (listF.length === 0 && listB.length === 0) {
        const byCode = await Promise.allSettled([
          apiFruteira.getByCodigoBarras(busca).catch(() => apiFruteira.getByCodigo(busca)),
          apiBuffet.getByCodigoBarras(busca).catch(() => apiBuffet.getByCodigo(busca)),
        ]);
        const extra: ProdutoBusca[] = [];
        if (byCode[0].status === 'fulfilled' && byCode[0].value?.data) extra.push({ ...byCode[0].value.data, origem: 'FRUTEIRA' });
        if (byCode[1].status === 'fulfilled' && byCode[1].value?.data) extra.push({ ...byCode[1].value.data, origem: 'BUFFET' });
        setResultados(extra);
        if (extra.length === 0) toast.info('Nenhum produto encontrado');
      } else {
        setResultados([...listF, ...listB].slice(0, 30));
      }
    } catch {
      toast.error('Erro ao buscar produtos');
    } finally {
      setBuscando(false);
    }
  };

  const adicionarItem = async (p: ProdutoBusca) => {
    if (!venda) { toast.warning('Inicie uma venda primeiro'); return; }
    const qtd = parseFloat(quantidade.replace(',', '.')) || 1;
    if (venda.id.startsWith('local-')) {
      const item: ItemResumo = { id: `i-${Date.now()}`, codigoProduto: p.codigo ?? p.codigoBarras, nomeProduto: p.nome, quantidade: qtd, precoUnitario: p.precoVenda ?? 0, valorTotal: (p.precoVenda ?? 0) * qtd, origem: p.origem };
      setVenda((v) => {
        if (!v) return v;
        const itens = [...(v.itens ?? []), item];
        const subtotal = itens.reduce((s, i) => s + (i.valorTotal ?? 0), 0);
        return { ...v, itens, subtotal, valorTotal: subtotal - (v.desconto ?? 0) + (v.acrescimo ?? 0) };
      });
      return;
    }
    try {
      const api = p.origem === 'BUFFET' ? apiPdv.addItemBuffet(venda.id, p.id, qtd) : apiPdv.addItemFruteira(venda.id, p.id, qtd);
      const { data } = await api;
      setVenda(data);
      toast.success('Item adicionado');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao adicionar item');
    }
  };

  const lancarPeso = async () => {
    if (!venda) return;
    const peso = parseFloat(pesoBruto.replace(',', '.'));
    if (!peso || peso <= 0) { toast.warning('Informe o peso válido'); return; }
    if (venda.id.startsWith('local-')) { toast.info('Balança disponível apenas com backend conectado'); setPesoDialog(false); return; }
    try {
      const { data } = await apiPdv.addPratoBalanca(venda.id, peso);
      setVenda(data);
      setPesoDialog(false);
      setPesoBruto('');
      toast.success('Prato por peso lançado');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao lançar peso');
    }
  };

  const aplicarDesconto = async () => {
    if (!venda) return;
    const v = parseFloat(desconto.replace(',', '.')) || 0;
    if (venda.id.startsWith('local-')) {
      setVenda((s) => s ? { ...s, desconto: v, valorTotal: (s.subtotal ?? 0) - v } : s);
      setDesconto('');
      return;
    }
    try {
      const { data } = await apiPdv.aplicarDesconto(venda.id, v);
      setVenda(data);
      setDesconto('');
      toast.success('Desconto aplicado');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao aplicar desconto');
    }
  };

  const confirmarPagamento = async () => {
    if (!venda) return;
    const valor = parseFloat(valorPagamento.replace(',', '.')) || 0;
    if (valor <= 0) { toast.warning('Informe o valor recebido'); return; }
    if (venda.id.startsWith('local-')) {
      setVenda((s) => s ? { ...s, valorPago: (s.valorPago ?? 0) + valor, pagamentos: [...(s.pagamentos ?? []), { formaPagamento, valor }] } : s);
      setPagDialog(false);
      setValorPagamento('');
      return;
    }
    try {
      const { data } = await apiPdv.processarPagamento(venda.id, { formaPagamento, valor });
      setVenda(data);
      setPagDialog(false);
      setValorPagamento('');
      toast.success('Pagamento registrado');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao processar pagamento');
    }
  };

  const finalizar = async () => {
    if (!venda) return;
    if (venda.id.startsWith('local-')) {
      setVenda((s) => s ? { ...s, status: 'FINALIZADA' } : s);
      toast.success('Venda finalizada (local)');
      return;
    }
    try {
      const { data } = await apiPdv.finalizarVenda(venda.id);
      setVenda(data);
      toast.success('Venda finalizada!');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao finalizar venda');
    }
  };

  const cancelar = async () => {
    if (!venda) return;
    if (!window.confirm('Cancelar esta venda?')) return;
    if (venda.id.startsWith('local-')) { setVenda(null); return; }
    try {
      await apiPdv.cancelarVenda(venda.id, 'Cancelada no PDV');
      toast.success('Venda cancelada');
      setVenda(null);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao cancelar');
    }
  };

  useEffect(() => { /* sessão inicial não cria venda automaticamente */ }, []);

  const saldo = (venda?.valorTotal ?? 0) - (venda?.valorPago ?? 0);

  return (
    <Box>
      <PageHeader
        title="PDV — Frente de Caixa"
        subtitle="Venda de produtos da fruteira e buffet por código de barras, peso e múltiplos pagamentos"
        icon={<PointOfSaleIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button variant="outlined" startIcon={<RefreshIcon />} onClick={() => venda && recarregarVenda(venda.id)} disabled={!venda || venda.id.startsWith('local-')}>Atualizar</Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={novaVenda} disabled={loading}>Nova venda</Button>
          </Box>
        }
      />
      <Grid container spacing={2}>
        <Grid item xs={12} md={7}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Buscar produto</Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <TextField fullWidth size="small" placeholder="Código, código de barras ou nome + Enter" value={busca}
                  onChange={(e) => setBusca(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && buscarProdutos()} />
                <TextField size="small" label="Qtd" value={quantidade} onChange={(e) => setQuantidade(e.target.value)} sx={{ width: 90 }} />
                <Button variant="contained" onClick={buscarProdutos} disabled={buscando} startIcon={<SearchIcon />}>Buscar</Button>
                <Tooltip title="Lançar prato por peso (balança)"><span><IconButton color="primary" onClick={() => setPesoDialog(true)} disabled={!venda}><ScaleIcon /></IconButton></span></Tooltip>
              </Box>
              {resultados.map((p) => (
                <Box key={`${p.origem}-${p.id}`} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', py: 1, borderBottom: 1, borderColor: 'divider' }}>
                  <Box>
                    <Typography variant="body2" fontWeight={600}>{p.nome}</Typography>
                    <Typography variant="caption" color="text.secondary">{p.codigo ?? p.codigoBarras ?? '—'} · {p.origem} · {formatCurrency(p.precoVenda ?? 0)}</Typography>
                  </Box>
                  <Button size="small" variant="outlined" onClick={() => adicionarItem(p)} disabled={!venda}>Adicionar</Button>
                </Box>
              ))}
              {resultados.length === 0 && <Typography variant="caption" color="text.secondary">Busque por nome, código ou código de barras. Produtos da fruteira e do buffet aparecem aqui.</Typography>}
            </CardContent>
          </Card>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Itens da venda {venda?.numero ? `#${venda.numero}` : ''}</Typography>
              {!venda && <Typography variant="body2" color="text.secondary">Nenhuma venda aberta. Clique em “Nova venda”.</Typography>}
              {venda?.itens?.map((it) => (
                <Box key={it.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.75, borderBottom: 1, borderColor: 'divider' }}>
                  <Box>
                    <Typography variant="body2">{it.quantidade}x {it.nomeProduto}</Typography>
                    <Typography variant="caption" color="text.secondary">{it.codigoProduto} · {formatCurrency(it.precoUnitario ?? 0)} un.</Typography>
                  </Box>
                  <Typography variant="body2" fontWeight={700}>{formatCurrency(it.valorTotal ?? 0)}</Typography>
                </Box>
              ))}
              {venda && (venda.itens?.length ?? 0) === 0 && <Typography variant="caption" color="text.secondary">Venda sem itens.</Typography>}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700}>Resumo</Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}><Typography variant="body2">Subtotal</Typography><Typography fontWeight={600}>{formatCurrency(venda?.subtotal ?? 0)}</Typography></Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="body2">Desconto</Typography><Typography>{formatCurrency(venda?.desconto ?? 0)}</Typography></Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="body2">Pago</Typography><Typography>{formatCurrency(venda?.valorPago ?? 0)}</Typography></Box>
              <Divider sx={{ my: 1 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="h6">Total</Typography><Typography variant="h6" color="primary">{formatCurrency(venda?.valorTotal ?? 0)}</Typography></Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}><Typography variant="body2">Saldo devedor</Typography><Chip size="small" label={formatCurrency(saldo)} color={saldo <= 0 ? 'success' : 'warning'} /></Box>
              <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                <TextField size="small" label="Desconto R$" value={desconto} onChange={(e) => setDesconto(e.target.value)} sx={{ flex: 1 }} />
                <Button variant="outlined" onClick={aplicarDesconto} disabled={!venda}>Aplicar</Button>
              </Box>
              <Grid container spacing={1}>
                <Grid item xs={6}><Button fullWidth variant="outlined" startIcon={<PaymentsIcon />} onClick={() => { setValorPagamento(saldo > 0 ? String(saldo.toFixed(2)) : ''); setPagDialog(true); }} disabled={!venda}>Pagamento</Button></Grid>
                <Grid item xs={6}><Button fullWidth variant="contained" startIcon={<CheckIcon />} onClick={finalizar} disabled={!venda}>Finalizar</Button></Grid>
                <Grid item xs={12}><Button fullWidth color="error" startIcon={<CancelIcon />} onClick={cancelar} disabled={!venda}>Cancelar venda</Button></Grid>
              </Grid>
              {venda?.pagamentos && venda.pagamentos.length > 0 && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="caption" fontWeight={700}>PAGAMENTOS</Typography>
                  {venda.pagamentos.map((p, i) => (
                    <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="caption">{p.formaPagamento}</Typography>
                      <Typography variant="caption">{formatCurrency(p.valor ?? 0)}</Typography>
                    </Box>
                  ))}
                </Box>
              )}
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
                Status: {venda?.status ?? '—'} · Formas aceitas: dinheiro, PIX, cartão, vale. O comprovante não fiscal pode ser reimpresso em Vendas.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Dialog open={pagDialog} onClose={() => setPagDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Registrar pagamento</DialogTitle>
        <DialogContent>
          <TextField select fullWidth label="Forma de pagamento" value={formaPagamento} onChange={(e) => setFormaPagamento(e.target.value)} sx={{ mt: 1, mb: 2 }}>
            {FORMAS_PAGAMENTO.map((f) => <MenuItem key={f} value={f}>{f}</MenuItem>)}
          </TextField>
          <TextField fullWidth label="Valor recebido" value={valorPagamento} onChange={(e) => setValorPagamento(e.target.value)} placeholder="0,00" />
        </DialogContent>
        <DialogActions><Button onClick={() => setPagDialog(false)}>Fechar</Button><Button variant="contained" onClick={confirmarPagamento}>Confirmar</Button></DialogActions>
      </Dialog>

      <Dialog open={pesoDialog} onClose={() => setPesoDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Lançar prato por peso</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Peso bruto (kg)" value={pesoBruto} onChange={(e) => setPesoBruto(e.target.value)} placeholder="Ex: 0,532" sx={{ mt: 1 }} />
          <Typography variant="caption" color="text.secondary">Integração com balanças Toledo/Filizola via leitura do peso.</Typography>
        </DialogContent>
        <DialogActions><Button onClick={() => setPesoDialog(false)}>Fechar</Button><Button variant="contained" onClick={lancarPeso}>Lançar</Button></DialogActions>
      </Dialog>
    </Box>
  );
}
export default PdvPage;
