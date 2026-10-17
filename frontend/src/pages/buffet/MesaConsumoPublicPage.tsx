import { useState, useEffect, useCallback } from 'react';
import {
  Box, Button, Card, CardContent, Typography, Grid, TextField, MenuItem,
  Divider, Chip, Dialog, DialogTitle, DialogContent, DialogActions, Alert, CircularProgress, Paper
} from '@mui/material';
import {
  Restaurant as RestaurantIcon, Add as AddIcon, Refresh as RefreshIcon,
  TableRestaurant as TableRestaurantIcon, Search as SearchIcon, CheckCircle as CheckIcon
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiBuffet } from '@/services/api';
import { formatCurrency } from '@/utils/formatters';
import { useParams, useNavigate } from 'react-router-dom';
import { maskQuantity, parseDecimal } from '@/utils/masks';

interface Mesa {
  id: string;
  numero?: string;
  nome?: string;
  capacidade?: number;
  status?: string;
  area?: string;
  comandaAtiva?: {
    id: string;
    numero: string;
    clienteNome?: string;
  };
}

interface ProdutoBusca {
  id: string;
  codigo?: string;
  nome: string;
  precoVenda: number;
  categoria?: string;
}

interface ItemComanda {
  id: string;
  nomeProduto: string;
  quantidade: number;
  precoUnitario: number;
  valorTotal: number;
  cancelado?: boolean;
}

export function MesaConsumoPublicPage() {
  const { mesaId } = useParams<{ mesaId?: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [mesas, setMesas] = useState<Mesa[]>([]);
  const [mesaSelecionada, setMesaSelecionada] = useState<Mesa | null>(null);

  const [termoBusca, setTermoBusca] = useState('');
  const [resultadosBusca, setResultadosBusca] = useState<ProdutoBusca[]>([]);
  const [quantidadeMap, setQuantidadeMap] = useState<Record<string, string>>({});

  const [itensComanda, setItensComanda] = useState<ItemComanda[]>([]);
  const [totalComanda, setTotalComanda] = useState(0);

  const [abrirDialog, setAbrirDialog] = useState(false);
  const [clienteNome, setClienteNome] = useState('');
  const [modalidade, setModalidade] = useState('POR_PESO');

  const loadMesaEComanda = useCallback(async (id: string) => {
    setLoading(true);
    try {
      const { data: mesaData } = await apiBuffet.getMesa(id);
      setMesaSelecionada(mesaData);

      if (mesaData?.comandaAtiva?.id) {
        const comandaId = mesaData.comandaAtiva.id;
        const [itensRes, totalRes] = await Promise.all([
          apiBuffet.listItensComanda(comandaId),
          apiBuffet.getTotalComanda(comandaId),
        ]);
        setItensComanda(itensRes.data ?? []);
        setTotalComanda(totalRes.data ?? 0);
      } else {
        setItensComanda([]);
        setTotalComanda(0);
      }
    } catch {
      toast.error('Erro ao carregar dados da mesa');
      setMesaSelecionada(null);
    } finally {
      setLoading(false);
    }
  }, []);

  const loadMesasDisponiveis = useCallback(async () => {
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

  useEffect(() => {
    if (mesaId) {
      loadMesaEComanda(mesaId);
    } else {
      loadMesasDisponiveis();
    }
  }, [mesaId, loadMesaEComanda, loadMesasDisponiveis]);

  const buscarProdutos = async () => {
    if (!termoBusca.trim()) {
      setResultadosBusca([]);
      return;
    }
    try {
      const { data } = await apiBuffet.searchProdutos(termoBusca);
      const list = Array.isArray(data) ? data : (data?.content ?? []);
      setResultadosBusca(list);
      if (list.length === 0) {
        toast.info('Nenhum produto encontrado');
      }
    } catch {
      toast.error('Erro ao buscar produtos');
    }
  };

  const abrirComandaParaMesa = async () => {
    if (!mesaSelecionada) return;
    try {
      const payload = {
        mesa: { id: mesaSelecionada.id },
        modalidade: modalidade,
        clienteNome: clienteNome || `Cliente Mesa ${mesaSelecionada.numero}`,
      };
      await apiBuffet.createComanda(payload);
      toast.success('Comanda aberta para a mesa!');
      setAbrirDialog(false);
      setClienteNome('');
      loadMesaEComanda(mesaSelecionada.id);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao abrir comanda');
    }
  };

  const adicionarProduto = async (produto: ProdutoBusca) => {
    if (!mesaSelecionada?.comandaAtiva?.id) {
      toast.warning('A comanda da mesa não está aberta');
      return;
    }
    const qtdStr = quantidadeMap[produto.id] || '1';
    const quantidade = parseDecimal(qtdStr, 3) || 1;
    try {
      await apiBuffet.lancarProdutoBuffet({
        comandaId: mesaSelecionada.comandaAtiva.id,
        produtoId: produto.id,
        quantidade: quantidade,
      });
      toast.success(`${produto.nome} (${quantidade}x) adicionado ao pedido!`);
      setQuantidadeMap({ ...quantidadeMap, [produto.id]: '1' });
      loadMesaEComanda(mesaSelecionada.id);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao adicionar produto');
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', bgcolor: '#f5f5f5' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!mesaId || !mesaSelecionada) {
    return (
      <Box sx={{ p: 4, maxWidth: 900, mx: 'auto', bgcolor: '#fff', minHeight: '100vh', borderRadius: 2 }}>
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <RestaurantIcon color="primary" sx={{ fontSize: 52 }} />
          <Typography variant="h4" fontWeight={700} gutterBottom>Sistema Tenda — Consumo na Mesa</Typography>
          <Typography variant="subtitle1" color="text.secondary">Selecione sua mesa para iniciar o consumo</Typography>
        </Box>
        <Grid container spacing={2}>
          {mesas.map((m) => (
            <Grid item xs={12} sm={6} md={4} key={m.id}>
              <Card sx={{ cursor: 'pointer', '&:hover': { boxShadow: 4 }, borderTop: 4, borderColor: m.status === 'LIVRE' ? 'success.main' : 'warning.main' }}
                onClick={() => navigate(`/mesa-consumo/${m.id}`)}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h6">Mesa {m.numero}</Typography>
                    <Chip size="small" label={m.status} color={m.status === 'LIVRE' ? ('success' as const) : ('warning' as const)} />
                  </Box>
                  <Typography variant="body2" color="text.secondary">{m.nome} · Cap. {m.capacidade} · {m.area}</Typography>
                  {m.comandaAtiva && (
                    <Typography variant="caption" color="warning.main" sx={{ display: 'block', mt: 1 }}>
                      Comanda ativa: {m.comandaAtiva.numero}
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
          {mesas.length === 0 && (
            <Grid item xs={12}>
              <Alert severity="info">Nenhuma mesa cadastrada no sistema.</Alert>
            </Grid>
          )}
        </Grid>
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, md: 4 }, maxWidth: 1000, mx: 'auto', bgcolor: '#f9f9f9', minHeight: '100vh' }}>
      <Paper elevation={2} sx={{ p: 3, mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TableRestaurantIcon color="primary" fontSize="large" />
            <Typography variant="h5" fontWeight={700}>Mesa {mesaSelecionada.numero} — {mesaSelecionada.nome}</Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            Área: {mesaSelecionada.area} · Capacidade: {mesaSelecionada.capacidade} pessoas
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={() => loadMesaEComanda(mesaSelecionada.id)}>Atualizar</Button>
          <Button variant="text" onClick={() => navigate('/mesa-consumo')}>Trocar Mesa</Button>
        </Box>
      </Paper>

      {!mesaSelecionada.comandaAtiva ? (
        <Card sx={{ textAlign: 'center', p: 4, mb: 4 }}>
          <Typography variant="h6" gutterBottom>Mesa pronta para iniciar pedido.</Typography>
          <Typography variant="body2" color="text.secondary" paragraph>Abra a comanda da mesa para adicionar produtos do buffet, bebidas e sobremesas.</Typography>
          <Button variant="contained" size="large" startIcon={<AddIcon />} onClick={() => setAbrirDialog(true)}>
            Abrir Comanda / Iniciar Mesa {mesaSelecionada.numero}
          </Button>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {/* Busca de Produtos */}
          <Grid item xs={12} md={7}>
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>Buscar Produto para Consumo</Typography>
                <Typography variant="body2" color="text.secondary" paragraph>Digite o nome do produto do buffet, bebida ou sobremesa:</Typography>
                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <TextField
                    fullWidth
                    size="small"
                    placeholder="Ex: Suco, Arroz, Sobremesa..."
                    value={termoBusca}
                    onChange={(e) => setTermoBusca(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && buscarProdutos()}
                  />
                  <Button variant="contained" onClick={buscarProdutos} startIcon={<SearchIcon />}>Buscar</Button>
                </Box>

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                  {resultadosBusca.map((p) => {
                    const qtd = quantidadeMap[p.id] || '1';
                    return (
                      <Paper key={p.id} variant="outlined" sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                        <Box>
                          <Typography variant="body1" fontWeight={600}>{p.nome}</Typography>
                          <Typography variant="caption" color="text.secondary">{p.categoria ?? 'Geral'} · <strong style={{ color: '#2e7d32' }}>{formatCurrency(p.precoVenda)}</strong></Typography>
                        </Box>
                        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                          <TextField
                            size="small"
                            label="Qtd"
                            value={maskQuantity(qtd)}
                            onChange={(e) => setQuantidadeMap({ ...quantidadeMap, [p.id]: maskQuantity(e.target.value) })}
                            sx={{ width: 80 }}
                            inputMode="decimal"
                          />
                          <Button variant="contained" size="small" startIcon={<AddIcon />} onClick={() => adicionarProduto(p)}>
                            Adicionar
                          </Button>
                        </Box>
                      </Paper>
                    );
                  })}
                  {resultadosBusca.length === 0 && (
                    <Typography variant="body2" color="text.secondary" sx={{ py: 2, textAlign: 'center' }}>
                      Utilize a busca acima para encontrar e adicionar produtos.
                    </Typography>
                  )}
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Resumo Consumo da Mesa */}
          <Grid item xs={12} md={5}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">Itens Solicitados</Typography>
                  <Chip label={`Comanda #${mesaSelecionada.comandaAtiva.numero}`} color="warning" />
                </Box>
                {mesaSelecionada.comandaAtiva.clienteNome && (
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Cliente / Responsável: {mesaSelecionada.comandaAtiva.clienteNome}
                  </Typography>
                )}
                <Divider sx={{ my: 1.5 }} />
                <Box sx={{ maxHeight: 350, overflowY: 'auto', mb: 2 }}>
                  {itensComanda.map((it) => (
                    <Box key={it.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 1, borderBottom: 1, borderColor: 'divider' }}>
                      <Box>
                        <Typography variant="body2" fontWeight={it.cancelado ? 400 : 600} sx={{ textDecoration: it.cancelado ? 'line-through' : 'none' }}>
                          {it.quantidade}x {it.nomeProduto}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">{formatCurrency(it.precoUnitario)} un.</Typography>
                      </Box>
                      <Typography variant="body2" fontWeight={700}>{formatCurrency(it.valorTotal)}</Typography>
                    </Box>
                  ))}
                  {itensComanda.length === 0 && (
                    <Typography variant="body2" color="text.secondary" sx={{ py: 2, textAlign: 'center' }}>
                      Nenhum item adicionado ainda.
                    </Typography>
                  )}
                </Box>
                <Divider sx={{ my: 1.5 }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                  <Typography variant="h6">Total Parcial</Typography>
                  <Typography variant="h6" color="primary">{formatCurrency(totalComanda)}</Typography>
                </Box>
                <Button fullWidth variant="contained" color="success" startIcon={<CheckIcon />} onClick={() => {
                  toast.success('Pedido enviado para o caixa / cozinha!');
                  loadMesaEComanda(mesaSelecionada.id);
                }}>
                  Finalizar / Enviar Pedido
                </Button>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2, textAlign: 'center' }}>
                  O pagamento será realizado posteriormente no PDV.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Dialog Abrir Comanda */}
      <Dialog open={abrirDialog} onClose={() => setAbrirDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Abrir Comanda para Mesa {mesaSelecionada.numero}</DialogTitle>
        <DialogContent>
          <TextField fullWidth size="small" label="Nome do Cliente (opcional)" value={clienteNome} onChange={(e) => setClienteNome(e.target.value)} sx={{ mt: 1, mb: 2 }} />
          <TextField select fullWidth size="small" label="Modalidade" value={modalidade} onChange={(e) => setModalidade(e.target.value)}>
            <MenuItem value="POR_PESO">Por Peso (Self-Service)</MenuItem>
            <MenuItem value="LIVRE">Consumo Livre</MenuItem>
            <MenuItem value="PRATO_FEITO">Prato Feito / Executivo</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAbrirDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={abrirComandaParaMesa}>Iniciar Mesa</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default MesaConsumoPublicPage;
