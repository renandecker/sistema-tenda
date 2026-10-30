import { useCallback, useEffect, useState } from 'react';
import {
  Alert, Autocomplete, Box, Button, Card, CardContent, Chip, Dialog, DialogActions,
  DialogContent, DialogTitle, Grid, IconButton, TextField, Tooltip, Typography,
} from '@mui/material';
import {
  MenuBook as CadernoIcon,
  Refresh as RefreshIcon,
  Visibility as ViewIcon,
  Payments as PaymentsIcon,
  AddCircleOutline as AddIcon,
  Block as BlockIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiCaderno, apiClientes } from '@/services/api';
import { DataTable } from '@/components/DataTable';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatDate, formatDateTime } from '@/utils/formatters';
import { maskCurrency, parseCurrency } from '@/utils/masks';

interface ClienteDivida {
  id?: string;
  clienteId?: string;
  clienteNome: string;
  clienteDocumento?: string;
  clienteTelefone?: string;
  totalDivida: number;
  quantidadeLancamentos: number;
  ultimaData?: string;
  possuiAtraso?: boolean;
}

interface Lancamento {
  id: string;
  clienteId?: string;
  clienteNome: string;
  clienteDocumento?: string;
  clienteTelefone?: string;
  numeroVenda?: string;
  descricao?: string;
  valorTotal: number;
  valorPago: number;
  saldoDevedor: number;
  status: string;
  dataLancamento?: string;
  dataVencimento?: string;
  vencida?: boolean;
  observacoes?: string;
}

interface Totais {
  totalEmAberto: number;
  totalRecebido: number;
  quantidadeLancamentosEmAberto: number;
  quantidadeClientesDevedores: number;
  quantidadeLancamentosEmAtraso: number;
}

const STATUS_LABEL: Record<string, string> = {
  ABERTA: 'Aberta',
  PARCIAL: 'Parcial',
  QUITADA: 'Quitada',
  CANCELADA: 'Cancelada',
};

export function CadernoPage() {
  const [clientes, setClientes] = useState<ClienteDivida[]>([]);
  const [totais, setTotais] = useState<Totais | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalRows, setTotalRows] = useState(0);

  const [dialogCliente, setDialogCliente] = useState<ClienteDivida | null>(null);
  const [lancamentos, setLancamentos] = useState<Lancamento[]>([]);
  const [carregandoLanc, setCarregandoLanc] = useState(false);

  const [novoDialog, setNovoDialog] = useState(false);
  const [novoCliente, setNovoCliente] = useState<any>(null);
  const [opcoesCliente, setOpcoesCliente] = useState<any[]>([]);
  const [novoNome, setNovoNome] = useState('');
  const [novoDocumento, setNovoDocumento] = useState('');
  const [novoTelefone, setNovoTelefone] = useState('');
  const [novoValor, setNovoValor] = useState('');
  const [novoVencimento, setNovoVencimento] = useState('');
  const [novoDescricao, setNovoDescricao] = useState('');

  const [receber, setReceber] = useState<{ lancamento: Lancamento; valor: string } | null>(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    try {
      const [resClientes, resTotais] = await Promise.allSettled([
        apiCaderno.clientesComDivida({ page, size: rowsPerPage }),
        apiCaderno.totais(),
      ]);
      const resData = resClientes.status === 'fulfilled' ? resClientes.value.data : null;
      const lista: any[] = Array.isArray(resData) ? resData : (resData?.content ?? []);
      setClientes(lista.map((c: any) => ({ ...c, id: c.clienteId ?? c.clienteDocumento ?? c.clienteNome })));
      setTotalRows(resData?.totalElements ?? lista.length);
      setTotais(resTotais.status === 'fulfilled' ? resTotais.value.data : null);
    } catch {
      setClientes([]);
      setTotalRows(0);
    } finally {
      setLoading(false);
    }
  }, [page, rowsPerPage]);

  useEffect(() => { carregar(); }, [carregar]);

  const buscarClientes = async (termo: string) => {
    if (!termo || termo.trim().length < 2) { setOpcoesCliente([]); return; }
    try {
      const { data } = await apiClientes.search(termo);
      setOpcoesCliente(Array.isArray(data) ? data : []);
    } catch {
      setOpcoesCliente([]);
    }
  };

  const abrirCliente = async (cliente: ClienteDivida) => {
    setDialogCliente(cliente);
    setLancamentos([]);
    setCarregandoLanc(true);
    try {
      let lista: Lancamento[] = [];
      if (cliente.clienteId) {
        const { data } = await apiCaderno.situacaoCliente(cliente.clienteId);
        lista = data?.dividas ?? [];
      } else if (cliente.clienteDocumento) {
        const { data } = await apiCaderno.situacaoPorDocumento(String(cliente.clienteDocumento).replace(/\D/g, ''));
        lista = data?.dividas ?? [];
      } else {
        const { data } = await apiCaderno.listar({ q: cliente.clienteNome, emAberto: true });
        lista = (Array.isArray(data) ? data : (data?.content ?? [])).filter((d: Lancamento) => d.clienteNome === cliente.clienteNome);
      }
      setLancamentos(lista);
    } catch {
      toast.error('Erro ao carregar lançamentos do cliente');
    } finally {
      setCarregandoLanc(false);
    }
  };

  const salvarNovoLancamento = async () => {
    const valor = parseCurrency(novoValor);
    if (valor <= 0) { toast.warning('Informe um valor de dívida válido'); return; }
    if (!novoCliente && !novoNome.trim()) { toast.warning('Selecione um cliente ou informe o nome'); return; }
    try {
      await apiCaderno.lancar({
        clienteId: novoCliente?.id,
        clienteNome: novoCliente?.nome ?? novoNome,
        clienteDocumento: novoCliente?.cpfCnpj ?? novoDocumento,
        clienteTelefone: novoCliente?.telefone ?? novoTelefone,
        valor,
        dataVencimento: novoVencimento || undefined,
        descricao: novoDescricao || undefined,
        observacoes: 'Lançamento manual no caderno',
      });
      toast.success('Dívida lançada no caderno');
      setNovoDialog(false);
      setNovoCliente(null); setNovoNome(''); setNovoDocumento(''); setNovoTelefone('');
      setNovoValor(''); setNovoVencimento(''); setNovoDescricao('');
      carregar();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao lançar dívida');
    }
  };

  const confirmarRecebimento = async () => {
    if (!receber) return;
    const valor = parseCurrency(receber.valor);
    if (valor <= 0) { toast.warning('Informe o valor recebido'); return; }
    try {
      await apiCaderno.registrarPagamento(receber.lancamento.id, valor);
      toast.success('Pagamento registrado');
      const clienteAtual = dialogCliente;
      setReceber(null);
      if (clienteAtual) abrirCliente(clienteAtual);
      carregar();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao registrar pagamento');
    }
  };

  const cancelarLancamento = async (lancamento: Lancamento) => {
    if (!window.confirm(`Cancelar a dívida de ${formatCurrency(lancamento.saldoDevedor)}?`)) return;
    try {
      await apiCaderno.cancelar(lancamento.id, 'Cancelado na tela do caderno');
      toast.success('Lançamento cancelado');
      const clienteAtual = dialogCliente;
      if (clienteAtual) abrirCliente(clienteAtual);
      carregar();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao cancelar');
    }
  };

  return (
    <Box>
      <PageHeader
        title="Caderno de Dívidas"
        subtitle="Fiado dos clientes — consulte quem está devendo e registre os pagamentos"
        icon={<CadernoIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Atualizar"><span><IconButton onClick={carregar} disabled={loading}><RefreshIcon /></IconButton></span></Tooltip>
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setNovoDialog(true)}>Novo lançamento</Button>
          </Box>
        }
      />

      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent>
            <Typography variant="caption" color="text.secondary">Total a receber</Typography>
            <Typography variant="h5" color="warning.main" fontWeight={700}>{formatCurrency(totais?.totalEmAberto ?? 0)}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent>
            <Typography variant="caption" color="text.secondary">Clientes devendo</Typography>
            <Typography variant="h5" fontWeight={700}>{totais?.quantidadeClientesDevedores ?? 0}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent>
            <Typography variant="caption" color="text.secondary">Lançamentos em aberto</Typography>
            <Typography variant="h5" fontWeight={700}>{totais?.quantidadeLancamentosEmAberto ?? 0}</Typography>
          </CardContent></Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card><CardContent>
            <Typography variant="caption" color="text.secondary">Em atraso</Typography>
            <Typography variant="h5" color="error.main" fontWeight={700}>{totais?.quantidadeLancamentosEmAtraso ?? 0}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <Alert severity="info" sx={{ mb: 2 }}>
        As dívidas abaixo estão em aberto. Use “Ver” para conferir os lançamentos e registrar pagamentos (total ou parcial).
      </Alert>

      <DataTable<ClienteDivida>
        data={clientes}
        loading={loading}
        serverSide
        totalCount={totalRows}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={(p, r) => { setPage(p); setRowsPerPage(r); }}
        searchable
        searchPlaceholder="Buscar cliente por nome ou documento..."
        emptyMessage="Nenhum cliente devendo no momento"
        columns={[
          { field: 'clienteNome', headerName: 'Cliente', flex: 1 },
          { field: 'clienteDocumento', headerName: 'CPF/CNPJ', width: 160 },
          { field: 'clienteTelefone', headerName: 'Telefone', width: 140 },
          { field: 'quantidadeLancamentos', headerName: 'Lançamentos', width: 120, align: 'center' },
          { field: 'totalDivida', headerName: 'Total devido', width: 150, align: 'right', render: (v: number) => <Typography fontWeight={700} color="warning.main">{formatCurrency(v)}</Typography> },
          {
            field: 'possuiAtraso', headerName: 'Situação', width: 130, align: 'center',
            render: (v: boolean) => <Chip size="small" label={v ? 'Em atraso' : 'Em dia'} color={v ? 'error' : 'success'} />,
          },
          {
            field: 'acoes', headerName: 'Ações', width: 110, align: 'center',
            render: (_: any, row: ClienteDivida) => (
              <Tooltip title="Ver dívidas / receber">
                <IconButton size="small" onClick={() => abrirCliente(row)}><ViewIcon fontSize="small" /></IconButton>
              </Tooltip>
            ),
          },
        ]}
      />

      {/* Dialog: lançamentos do cliente */}
      <Dialog open={!!dialogCliente} onClose={() => setDialogCliente(null)} maxWidth="md" fullWidth>
        <DialogTitle>
          Cliente: {dialogCliente?.clienteNome}
          {dialogCliente?.possuiAtraso && <Chip size="small" color="error" label="Em atraso" sx={{ ml: 1 }} />}
        </DialogTitle>
        <DialogContent dividers>
          {carregandoLanc ? (
            <Typography variant="body2" color="text.secondary">Carregando lançamentos...</Typography>
          ) : lancamentos.length === 0 ? (
            <Typography variant="body2" color="text.secondary">Nenhuma dívida em aberto para este cliente.</Typography>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              <Typography variant="subtitle2">
                Total devido: <strong>{formatCurrency(lancamentos.reduce((s, l) => s + (l.saldoDevedor ?? 0), 0))}</strong>
              </Typography>
              {lancamentos.map((l) => (
                <Card key={l.id} variant="outlined">
                  <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                    <Box>
                      <Typography variant="body2" fontWeight={600}>
                        {l.descricao || 'Dívida no caderno'}
                        {l.numeroVenda ? ` · Venda #${l.numeroVenda}` : ''}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Lançado em {formatDateTime(l.dataLancamento)}
                        {l.dataVencimento ? ` · Vence ${formatDate(l.dataVencimento)}` : ''}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                        <Chip size="small" label={STATUS_LABEL[l.status] ?? l.status} color={l.status === 'QUITADA' ? 'success' : l.vencida ? 'error' : 'warning'} />
                        <Typography variant="caption" color="text.secondary">Total {formatCurrency(l.valorTotal)} · Pago {formatCurrency(l.valorPago)}</Typography>
                      </Box>
                    </Box>
                    <Box sx={{ textAlign: 'right' }}>
                      <Typography variant="subtitle1" fontWeight={700} color="warning.main">{formatCurrency(l.saldoDevedor)}</Typography>
                      <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5, justifyContent: 'flex-end' }}>
                        <Button size="small" variant="contained" startIcon={<PaymentsIcon />}
                          onClick={() => setReceber({ lancamento: l, valor: maskCurrency(l.saldoDevedor) })}>Receber</Button>
                        <Button size="small" color="error" startIcon={<BlockIcon />} onClick={() => cancelarLancamento(l)}>Cancelar</Button>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              ))}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogCliente(null)}>Fechar</Button>
        </DialogActions>
      </Dialog>

      {/* Dialog: receber pagamento */}
      <Dialog open={!!receber} onClose={() => setReceber(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Registrar recebimento</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Saldo devedor do lançamento: {formatCurrency(receber?.lancamento.saldoDevedor ?? 0)}
          </Typography>
          <TextField fullWidth label="Valor recebido (R$)" value={receber?.valor ?? ''} inputMode="decimal"
            onChange={(e) => setReceber((r) => r ? { ...r, valor: maskCurrency(e.target.value) } : r)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReceber(null)}>Fechar</Button>
          <Button variant="contained" onClick={confirmarRecebimento}>Confirmar</Button>
        </DialogActions>
      </Dialog>

      {/* Dialog: novo lançamento */}
      <Dialog open={novoDialog} onClose={() => setNovoDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Novo lançamento no caderno</DialogTitle>
        <DialogContent>
          <Autocomplete
            sx={{ mt: 1 }}
            options={opcoesCliente}
            getOptionLabel={(o: any) => `${o.nome}${o.cpfCnpj ? ` — ${o.cpfCnpj}` : ''}`}
            value={novoCliente}
            onChange={(_, v) => { setNovoCliente(v); if (v) { setNovoNome(v.nome); } }}
            onInputChange={(_, v, reason) => { if (reason === 'input') buscarClientes(v); }}
            renderInput={(params) => (
              <TextField {...params} label="Cliente cadastrado (opcional)" placeholder="Digite o nome para buscar" />
            )}
          />
          {!novoCliente && (
            <>
              <TextField fullWidth label="Nome do cliente" value={novoNome} onChange={(e) => setNovoNome(e.target.value)} sx={{ mt: 2 }} />
              <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                <TextField fullWidth label="CPF/CNPJ (opcional)" value={novoDocumento} onChange={(e) => setNovoDocumento(e.target.value)} />
                <TextField fullWidth label="Telefone (opcional)" value={novoTelefone} onChange={(e) => setNovoTelefone(e.target.value)} />
              </Box>
            </>
          )}
          <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
            <TextField fullWidth label="Valor da dívida (R$)" value={novoValor} inputMode="decimal" onChange={(e) => setNovoValor(maskCurrency(e.target.value))} />
            <TextField fullWidth type="date" label="Vencimento (opcional)" InputLabelProps={{ shrink: true }} value={novoVencimento} onChange={(e) => setNovoVencimento(e.target.value)} />
          </Box>
          <TextField fullWidth label="Descrição / observação" value={novoDescricao} onChange={(e) => setNovoDescricao(e.target.value)} sx={{ mt: 1 }} multiline rows={2} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNovoDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={salvarNovoLancamento}>Lançar dívida</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
export default CadernoPage;
