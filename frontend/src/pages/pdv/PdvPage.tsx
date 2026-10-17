import { useCallback, useEffect, useState } from 'react';
import {
  Alert, Autocomplete, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Divider, Grid, IconButton, MenuItem, Paper, Tabs, Tab, TextField, Tooltip, Typography,
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
  TableRestaurant as TableRestaurantIcon,
  MenuBook as MenuBookIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { apiBuffet, apiClientes, apiCaderno, apiFruteira, apiPdv } from '@/services/api';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency } from '@/utils/formatters';
import { maskCurrency, maskQuantity, parseCurrency, parseDecimal } from '@/utils/masks';

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

interface SituacaoCliente {
  clienteId?: string;
  clienteNome?: string;
  clienteDocumento?: string;
  totalDivida?: number;
  quantidadeLancamentos?: number;
  possuiAtraso?: boolean;
}

const FORMAS_PAGAMENTO = ['DINHEIRO', 'PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'VALE_ALIMENTACAO', 'VALE_REFEICAO', 'VALE_CULTURA'];

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
  const [tabValue, setTabValue] = useState<'buffet' | 'fruteira'>('buffet');
  const [contadorItens, setContadorItens] = useState({ BUFFET: 0, FRUTEIRA: 0 });
  const [mesaDialog, setMesaDialog] = useState(false);
  const [mesasComComanda, setMesasComComanda] = useState<any[]>([]);
  const navigate = useNavigate();

  const [cliente, setCliente] = useState<any>(null);
  const [opcoesClientes, setOpcoesClientes] = useState<any[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCliente | null>(null);
  const [fiadoDialog, setFiadoDialog] = useState(false);
  const [fiadoValor, setFiadoValor] = useState('');
  const [fiadoVencimento, setFiadoVencimento] = useState('');
  const [fiadoDescricao, setFiadoDescricao] = useState('');

  const buscarClientes = async (termo: string) => {
    if (!termo || termo.trim().length < 2) { setOpcoesClientes([]); return; }
    try {
      const { data } = await apiClientes.search(termo);
      setOpcoesClientes(Array.isArray(data) ? data : []);
    } catch {
      setOpcoesClientes([]);
    }
  };

  const carregarSituacao = async (c: any) => {
    if (!c) { setSituacao(null); return; }
    try {
      if (c.id) {
        const { data } = await apiCaderno.situacaoCliente(c.id);
        setSituacao(data ?? null);
      } else if (c.cpfCnpj) {
        const { data } = await apiCaderno.situacaoPorDocumento(String(c.cpfCnpj).replace(/\D/g, ''));
        setSituacao(data ?? null);
      } else {
        setSituacao(null);
      }
    } catch {
      setSituacao(null);
    }
  };

  const selecionarCliente = (c: any) => {
    setCliente(c);
    carregarSituacao(c);
  };

  const abrirFiado = () => {
    if (!cliente) { toast.warning('Selecione o cliente para lançar a dívida'); return; }
    setFiadoValor(''); setFiadoVencimento(''); setFiadoDescricao('');
    setFiadoDialog(true);
  };

  const lancarFiado = async () => {
    if (!cliente) return;
    const valor = parseCurrency(fiadoValor);
    if (valor <= 0) { toast.warning('Informe o valor da dívida'); return; }
    try {
      await apiCaderno.lancar({
        clienteId: cliente?.id,
        clienteNome: cliente?.nome,
        clienteDocumento: cliente?.cpfCnpj,
        clienteTelefone: cliente?.telefone,
        valor,
        dataVencimento: fiadoVencimento || undefined,
        descricao: fiadoDescricao || 'Fiado lançado no PDV',
        vendaId: venda && !venda.id.startsWith('local-') ? venda.id : undefined,
        numeroVenda: venda?.numero,
        observacoes: 'Lançado via PDV',
      });
      toast.success('Dívida lançada no caderno');
      setFiadoDialog(false);
      carregarSituacao(cliente);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao lançar dívida');
    }
  };

  const abrirMesaDialog = async () => {
    try {
      const { data } = await apiBuffet.listMesas({ size: 200 });
      const list = data?.content ?? (Array.isArray(data) ? data : []);
      const comComanda = list.filter((m: any) => m.comandaAtiva);
      setMesasComComanda(comComanda);
      setMesaDialog(true);
    } catch {
      toast.error('Erro ao listar mesas com comanda');
    }
  };

  const carregarMesaPdv = async (comandaId: string, mesaNumero: string) => {
    let vendaAtualId = venda?.id;
    try {
      if (!vendaAtualId || vendaAtualId.startsWith('local-')) {
        const { data: novaV } = await apiPdv.createVenda({ observacoes: `Venda Mesa ${mesaNumero}` });
        vendaAtualId = novaV.id;
        setVenda(novaV);
      }
      const idToUse = vendaAtualId || 'local-temp';
      const { data } = await apiPdv.addItemComanda(idToUse, comandaId);
      setVenda(data);
      setMesaDialog(false);
      toast.success(`Consumo da Mesa ${mesaNumero} carregado para pagamento!`);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao carregar comanda da mesa');
    }
  };

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
      const { data } = await apiPdv.createVenda({ observacoes: 'Venda PDV' });
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

  const obterCodigoProduto = (origem: string, contador: number) => {
    return origem === 'BUFFET' ? `B${contador + 1}` : `F${contador + 1}`;
  };

  const adicionarItem = async (p: ProdutoBusca) => {
    if (!venda) { toast.warning('Inicie uma venda primeiro'); return; }
    const qtd = parseDecimal(quantidade, 3) || 1;
    const novoContador = { ...contadorItens } as { BUFFET: number; FRUTEIRA: number };
    const origem = p.origem ?? 'FRUTEIRA';
    novoContador[origem] = (novoContador[origem] ?? 0) + 1;
    setContadorItens(novoContador);
    const codigoProd = obterCodigoProduto(origem, (contadorItens[origem] ?? 0) as number);
    if (venda.id.startsWith('local-')) {
      const item: ItemResumo = { id: `i-${Date.now()}`, codigoProduto: codigoProd, nomeProduto: p.nome, quantidade: qtd, precoUnitario: p.precoVenda ?? 0, valorTotal: (p.precoVenda ?? 0) * qtd, origem: p.origem };
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
    const peso = parseDecimal(pesoBruto, 3);
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
    const v = parseCurrency(desconto);
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
    const valor = parseCurrency(valorPagamento);
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

  useEffect(() => {
    const handler = (_e: KeyboardEvent) => {
      // Alt+1: trocar para aba buffet
      if (_e.altKey && _e.key === '1') {
        setTabValue('buffet');
        _e.preventDefault();
      }
      // Alt+2: trocar para aba fruteira
      if (_e.altKey && _e.key === '2') {
        setTabValue('fruteira');
        _e.preventDefault();
      }
      // Alt+N: adicionar produto da busca (N=1..9 corresponde ao índice)
      if (_e.altKey && _e.key >= '1' && _e.key <= '9' && venda) {
        const idx = parseInt(_e.key, 10) - 1;
        if (resultados[idx]) {
          adicionarItem(resultados[idx]);
          _e.preventDefault();
        }
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [tabValue, resultados, venda, adicionarItem]);

  const saldo = (venda?.valorTotal ?? 0) - (venda?.valorPago ?? 0);

  return (
    <Box>
      <PageHeader
        title="PDV — Frente de Caixa"
        subtitle="Venda de produtos da fruteira e buffet por código de barras, peso e múltiplos pagamentos"
        icon={<PointOfSaleIcon />}
         action={
           <Box sx={{ display: 'flex', gap: 1 }}>
             <Button variant="outlined" startIcon={<TableRestaurantIcon />} onClick={abrirMesaDialog}>Selecionar Mesa</Button>
             <Button variant="outlined" color="warning" startIcon={<MenuBookIcon />} onClick={abrirFiado}>Caderno (fiado)</Button>
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
                  onChange={(e) => setBusca(e.target.value)} onKeyDown={(e) => (e.key === 'Enter' || (e.altKey && e.key === 'Enter')) && buscarProdutos()} />
                <TextField size="small" label="Qtd" value={maskQuantity(quantidade)} onChange={(e) => setQuantidade(maskQuantity(e.target.value))} sx={{ width: 90 }} inputMode="decimal" />
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
              {!venda && <Typography variant="body2" color="text.secondary">Nenhuma venda aberta. Clique em "Nova venda".</Typography>}
              <Box sx={{ mt: 1 }}>
                <Tabs value={tabValue} onChange={(_, value) => setTabValue(value)} sx={{ mb: 1 }}>
                  <Tab label="Buffet" value="buffet" />
                  <Tab label="Fruteira" value="fruteira" />
                </Tabs>
                {tabValue === 'buffet' && (
                  <Box>
                    {venda?.itens?.filter((it) => it.origem === 'BUFFET')?.map((it) => (
                      <Box key={it.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.75, borderBottom: 1, borderColor: 'divider' }}>
                        <Box>
                          <Typography variant="body2">{it.quantidade}x {it.nomeProduto}</Typography>
                          <Typography variant="caption" color="text.secondary">{it.codigoProduto} · {formatCurrency(it.precoUnitario ?? 0)} un.</Typography>
                        </Box>
                        <Typography variant="body2" fontWeight={700}>{formatCurrency(it.valorTotal ?? 0)}</Typography>
                      </Box>
                    ))}
                    {venda?.itens?.filter((it) => it.origem !== 'BUFFET')?.length === 0 && <Typography variant="caption" color="text.secondary">Nenhum item de buffet.</Typography>}
                  </Box>
                )}
                {tabValue === 'fruteira' && (
                  <Box>
                    {venda?.itens?.filter((it) => it.origem === 'FRUTEIRA')?.map((it) => (
                      <Box key={it.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.75, borderBottom: 1, borderColor: 'divider' }}>
                        <Box>
                          <Typography variant="body2">{it.quantidade}x {it.nomeProduto}</Typography>
                          <Typography variant="caption" color="text.secondary">{it.codigoProduto} · {formatCurrency(it.precoUnitario ?? 0)} un.</Typography>
                        </Box>
                        <Typography variant="body2" fontWeight={700}>{formatCurrency(it.valorTotal ?? 0)}</Typography>
                      </Box>
                    ))}
                    {venda?.itens?.filter((it) => it.origem !== 'FRUTEIRA')?.length === 0 && <Typography variant="caption" color="text.secondary">Nenhum item de fruteira.</Typography>}
                  </Box>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={5}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Cliente</Typography>
              <Autocomplete
                size="small"
                options={opcoesClientes}
                getOptionLabel={(o: any) => `${o.nome ?? ''}${o.cpfCnpj ? ` — ${o.cpfCnpj}` : ''}`}
                isOptionEqualToValue={(o: any, v: any) => o.id === v?.id}
                value={cliente}
                onChange={(_, v) => selecionarCliente(v)}
                onInputChange={(_, v, reason) => { if (reason === 'input') buscarClientes(v); }}
                renderInput={(params) => (
                  <TextField {...params} placeholder="Buscar cliente por nome ou CPF/CNPJ" />
                )}
              />

              {situacao && Number(situacao.totalDivida ?? 0) > 0 && (
                <Alert
                  severity={situacao.possuiAtraso ? 'error' : 'warning'}
                  icon={<WarningIcon fontSize="inherit" />}
                  sx={{ mt: 1.5 }}
                  action={
                    <Button color="inherit" size="small" onClick={() => navigate('/caderno')}>
                      Ver caderno
                    </Button>
                  }
                >
                  <Typography variant="body2" fontWeight={700}>
                    Cliente está devendo {formatCurrency(Number(situacao.totalDivida ?? 0))}
                  </Typography>
                  <Typography variant="caption" display="block">
                    {situacao.quantidadeLancamentos ?? 0} lançamento(s) em aberto
                    {situacao.possuiAtraso ? ' · HÁ DÍVIDAS VENCIDAS' : ''}
                  </Typography>
                </Alert>
              )}

              {situacao && Number(situacao.totalDivida ?? 0) === 0 && cliente && (
                <Alert severity="success" sx={{ mt: 1.5 }}>
                  Cliente sem dívidas em aberto.
                </Alert>
              )}

              <Box sx={{ display: 'flex', gap: 1, mt: 1.5 }}>
                <Button fullWidth variant="outlined" color="warning" startIcon={<MenuBookIcon />} onClick={abrirFiado}>
                  Lançar fiado
                </Button>
                {cliente && (
                  <Button fullWidth variant="text" onClick={() => selecionarCliente(null)}>Limpar</Button>
                )}
              </Box>
            </CardContent>
          </Card>
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
                <TextField size="small" label="Desconto (R$)" value={maskCurrency(desconto)} onChange={(e) => setDesconto(maskCurrency(e.target.value))} placeholder="0,00" sx={{ flex: 1 }} inputMode="decimal" />
                <Button variant="outlined" onClick={aplicarDesconto} disabled={!venda}>Aplicar</Button>
              </Box>
              <Grid container spacing={1}>
                <Grid item xs={6}><Button fullWidth variant="outlined" startIcon={<PaymentsIcon />} onClick={() => { setValorPagamento(saldo > 0 ? maskCurrency(saldo) : ''); setPagDialog(true); }} disabled={!venda}>Pagamento</Button></Grid>
                <Grid item xs={6}><Button fullWidth variant="contained" startIcon={<CheckIcon />} onClick={finalizar} disabled={venda == null || (venda?.pagamentos?.length === 0 && venda?.valorPago === 0)}>Finalizar</Button></Grid>
                <Grid item xs={12}><Button fullWidth color="error" startIcon={<CancelIcon />} onClick={cancelar} disabled={!venda}>Cancelar venda</Button></Grid>
              </Grid>
              {venda?.pagamentos && venda.pagamentos.length > 0 && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="caption" fontWeight={700}>PAGAMENTOS</Typography>
                  {venda.pagamentos.map((p, i) => (
                    <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
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
          <TextField fullWidth label="Valor recebido (R$)" value={maskCurrency(valorPagamento)} onChange={(e) => setValorPagamento(maskCurrency(e.target.value))} placeholder="0,00" inputMode="decimal" />
        </DialogContent>
        <DialogActions><Button onClick={() => setPagDialog(false)}>Fechar</Button><Button variant="contained" onClick={confirmarPagamento}>Confirmar</Button></DialogActions>
      </Dialog>

       <Dialog open={pesoDialog} onClose={() => setPesoDialog(false)} maxWidth="xs" fullWidth>
         <DialogTitle>Lançar prato por peso</DialogTitle>
         <DialogContent>
           <TextField fullWidth label="Peso bruto (kg)" value={maskQuantity(pesoBruto)} onChange={(e) => setPesoBruto(maskQuantity(e.target.value))} placeholder="Ex: 0,532" sx={{ mt: 1 }} inputMode="decimal" />
           <Typography variant="caption" color="text.secondary">Integração com balanças Toledo/Filizola via leitura do peso.</Typography>
         </DialogContent>
         <DialogActions><Button onClick={() => setPesoDialog(false)}>Fechar</Button><Button variant="contained" onClick={lancarPeso}>Lançar</Button></DialogActions>
       </Dialog>

       {/* Dialog Selecionar Mesa para PDV */}
       <Dialog open={mesaDialog} onClose={() => setMesaDialog(false)} maxWidth="sm" fullWidth>
         <DialogTitle>Selecionar Mesa / Consumo</DialogTitle>
         <DialogContent>
           <Typography variant="body2" color="text.secondary" gutterBottom>
             Selecione a mesa para carregar os consumos e fechar a conta no caixa:
           </Typography>
           <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
             {mesasComComanda.map((m: any) => (
               <Paper key={m.id} variant="outlined" sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                 <Box>
                   <Typography variant="subtitle1" fontWeight={600}>Mesa {m.numero} — {m.nome}</Typography>
                   <Typography variant="body2" color="text.secondary">
                     Comanda: #{m.comandaAtiva?.numero} · Cliente: {m.comandaAtiva?.clienteNome || 'Cliente'}
                   </Typography>
                 </Box>
                 <Button variant="contained" size="small" onClick={() => carregarMesaPdv(m.comandaAtiva.id, m.numero)}>
                   Carregar Consumo
                 </Button>
               </Paper>
             ))}
             {mesasComComanda.length === 0 && (
               <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                 Nenhuma mesa com comanda ativa no momento.
               </Typography>
             )}
           </Box>
         </DialogContent>
          <DialogActions>
            <Button onClick={() => setMesaDialog(false)}>Fechar</Button>
          </DialogActions>
        </Dialog>

        {/* Dialog: lançar fiado no caderno */}
        <Dialog open={fiadoDialog} onClose={() => setFiadoDialog(false)} maxWidth="xs" fullWidth>
          <DialogTitle>Lançar fiado no caderno</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Cliente: <strong>{cliente?.nome}</strong>
              {cliente?.cpfCnpj ? ` · ${cliente.cpfCnpj}` : ''}
              {situacao && Number(situacao.totalDivida ?? 0) > 0 && (
                <> · já deve {formatCurrency(Number(situacao.totalDivida ?? 0))}</>
              )}
            </Typography>
            <TextField fullWidth label="Valor da dívida (R$)" value={maskCurrency(fiadoValor)}
              onChange={(e) => setFiadoValor(maskCurrency(e.target.value))} placeholder="0,00" inputMode="decimal" sx={{ mb: 2 }} />
            <TextField fullWidth type="date" label="Vencimento (opcional)" InputLabelProps={{ shrink: true }}
              value={fiadoVencimento} onChange={(e) => setFiadoVencimento(e.target.value)} sx={{ mb: 2 }} />
            <TextField fullWidth label="Descrição" value={fiadoDescricao}
              onChange={(e) => setFiadoDescricao(e.target.value)} placeholder="Ex.: compra do dia" />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setFiadoDialog(false)}>Cancelar</Button>
            <Button variant="contained" color="warning" onClick={lancarFiado}>Lançar dívida</Button>
          </DialogActions>
        </Dialog>
      </Box>
  );
}
export default PdvPage;
