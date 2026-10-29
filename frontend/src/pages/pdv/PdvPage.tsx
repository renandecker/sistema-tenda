import { useCallback, useEffect, useState } from 'react';
import {
  Alert, Autocomplete, Box, Button, Card, CardContent, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  Divider, Grid, IconButton, Tabs, Tab, TextField, Tooltip, Typography,
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
  MenuBook as MenuBookIcon,
  Warning as WarningIcon,
  ListAlt as ListAltIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { api, apiBuffet, apiClientes, apiCaderno, apiFruteira, apiPdv } from '@/services/api';
import { PageHeader } from '@/components/PageHeader';
import { formatCurrency, formatNumber } from '@/utils/formatters';
import { maskCurrency, maskCpfCnpj, maskQuantity, parseCurrency, parseDecimal, parseDocument } from '@/utils/masks';

interface ItemResumo {
  id?: string;
  codigoProduto?: string;
  nomeProduto?: string;
  quantidade?: number;
  precoUnitario?: number;
  valorTotal?: number;
  origem?: string;
}

interface PagamentoForma {
  forma: string;
  valor: number;
  shortcut: string;
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
  pagamentos?: PagamentoForma[];
}

interface ProdutoBusca {
  id: string;
  codigo?: string;
  codigoBarras?: string;
  nome: string;
  precoVenda?: number;
  unidadeMedida?: string;
  unidade?: string;
  estoqueAtual?: number;
  controlaEstoque?: boolean;
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

const SHORTCUTS_PAGAMENTO = [
  { forma: 'DINHEIRO', label: 'Dinheiro', shortcut: 'F1' },
  { forma: 'PIX', label: 'PIX', shortcut: 'F2' },
  { forma: 'CARTAO_CREDITO', label: 'Cartão Crédito', shortcut: 'F3' },
  { forma: 'CARTAO_DEBITO', label: 'Cartão Débito', shortcut: 'F4' },
  { forma: 'VALE_ALIMENTACAO', label: 'Vale Alimentação', shortcut: 'F5' },
  { forma: 'VALE_REFEICAO', label: 'Vale Refeição', shortcut: 'F6' },
  { forma: 'VALE_CULTURA', label: 'Vale Cultura', shortcut: 'F7' },
];

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
  const [tabValue, setTabValue] = useState<'buffet' | 'fruteira' | 'mesas'>('buffet');
  const [contadorItens, setContadorItens] = useState({ BUFFET: 0, FRUTEIRA: 0 });
  const [mesasComComanda, setMesasComComanda] = useState<any[]>([]);
  const [estoqueBuffet, setEstoqueBuffet] = useState<ProdutoBusca[]>([]);
  const [estoqueFruteira, setEstoqueFruteira] = useState<ProdutoBusca[]>([]);
  const [buscou, setBuscou] = useState(false);
  const [shortcutDialog, setShortcutDialog] = useState(false);
  const [itensModalOpen, setItensModalOpen] = useState(false);
  const navigate = useNavigate();

  const [cliente, setCliente] = useState<any>(null);
  const [opcoesClientes, setOpcoesClientes] = useState<any[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCliente | null>(null);
  const [documento, setDocumento] = useState('');
  const [clienteNaoEncontrado, setClienteNaoEncontrado] = useState(false);
  const [cadClienteDialog, setCadClienteDialog] = useState(false);
  const [cadClienteForm, setCadClienteForm] = useState({ nome: '', telefone: '' });
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
    setDocumento(c?.cpfCnpj ? maskCpfCnpj(c.cpfCnpj) : '');
    setClienteNaoEncontrado(false);
    carregarSituacao(c);
  };

  // CPF/CNPJ com máscara: ao completar 11 ou 14 dígitos, busca o cliente e o mantém
  // vinculado à venda enquanto os produtos são adicionados
  const onDocumentoChange = async (valor: string) => {
    const masked = maskCpfCnpj(valor);
    setDocumento(masked);
    const digits = parseDocument(masked);
    if (digits.length === 11 || digits.length === 14) {
      try {
        const { data } = await apiClientes.getByDocumento(digits);
        if (data?.id) {
          setCliente(data);
          setClienteNaoEncontrado(false);
          carregarSituacao(data);
          return;
        }
      } catch { /* cliente não cadastrado */ }
      setCliente(null);
      setSituacao(null);
      setClienteNaoEncontrado(true);
    } else {
      setClienteNaoEncontrado(false);
    }
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

  const carregarMesas = useCallback(async () => {
    try {
      const { data } = await apiBuffet.listMesas({ size: 200 });
      const list = data?.content ?? (Array.isArray(data) ? data : []);
      setMesasComComanda(list.filter((m: any) => m.comandaAtiva));
    } catch {
      setMesasComComanda([]);
    }
  }, []);

  const carregarEstoque = useCallback(async () => {
    try {
      const [f, b] = await Promise.allSettled([
        apiFruteira.listProdutos({ size: 200, ativo: true }),
        apiBuffet.listProdutos({ size: 200, ativo: true }),
      ]);
      const listaF: any[] = f.status === 'fulfilled' ? (f.value.data?.content ?? (Array.isArray(f.value.data) ? f.value.data : [])) : [];
      const listaB: any[] = b.status === 'fulfilled' ? (b.value.data?.content ?? (Array.isArray(b.value.data) ? b.value.data : [])) : [];
      setEstoqueFruteira(
        listaF
          .map((p) => ({ ...p, origem: 'FRUTEIRA' as const }))
          .filter((p) => Number(p.estoqueAtual ?? 0) > 0),
      );
      setEstoqueBuffet(
        listaB
          .map((p) => ({ ...p, origem: 'BUFFET' as const }))
          .filter((p) => p.controlaEstoque === false || Number(p.estoqueAtual ?? 0) > 0),
      );
    } catch {
      // ignore
    }
  }, []);

  useEffect(() => { carregarEstoque(); }, [carregarEstoque]);
  useEffect(() => { carregarMesas(); }, [carregarMesas]);

  const mesaCorresponde = (m: any, termo: string) => {
    const t = termo.trim().toLowerCase();
    if (!t) return true;
    return (
      String(m.numero ?? '').toLowerCase().includes(t) ||
      String(m.nome ?? '').toLowerCase().includes(t) ||
      String(m.comandaAtiva?.numero ?? '').toLowerCase().includes(t) ||
      String(m.comandaAtiva?.clienteNome ?? '').toLowerCase().includes(t)
    );
  };

  const carregarVendaComItens = useCallback(async (vendaId: string) => {
    try {
      const [resVenda, resItens, resPags] = await Promise.all([
        apiPdv.getVenda(vendaId),
        apiPdv.listItensVenda(vendaId).catch(() => ({ data: [] })),
        apiPdv.listPagamentos(vendaId).catch(() => ({ data: [] })),
      ]);
      const v = resVenda.data;
      const itens = Array.isArray(resItens.data) ? resItens.data : (resItens.data?.content ?? []);
      const pagamentos = Array.isArray(resPags.data) ? resPags.data : (resPags.data?.content ?? []);
      setVenda({ ...v, itens, pagamentos });
    } catch {
      // fallback
    }
  }, []);

  const carregarMesaPdv = async (comandaId: string, mesaNumero: string) => {
    let vendaAtualId = venda?.id;
    try {
      if (!vendaAtualId || vendaAtualId.startsWith('local-')) {
        const { data: novaV } = await apiPdv.createVenda({ observacoes: `Venda Mesa ${mesaNumero}` });
        vendaAtualId = novaV.id;
        setVenda(novaV);
      }
      const idToUse = vendaAtualId || 'local-temp';
      await apiPdv.addItemComanda(idToUse, comandaId);
      await carregarVendaComItens(idToUse);
      toast.success(`Consumo da Mesa ${mesaNumero} carregado para pagamento!`);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao carregar comanda da mesa');
    }
  };

  const recarregarVenda = useCallback(async (id: string) => {
    await carregarVendaComItens(id);
  }, [carregarVendaComItens]);

  const limparEstado = () => {
    setBusca('');
    setResultados([]);
    setBuscou(false);
    setQuantidade('1');
    setDesconto('');
    setCliente(null);
    setDocumento('');
    setSituacao(null);
    setClienteNaoEncontrado(false);
    setOpcoesClientes([]);
    setContadorItens({ BUFFET: 0, FRUTEIRA: 0 });
    setFormaPagamento('DINHEIRO');
    setValorPagamento('');
  };

  const novaVenda = async () => {
    setLoading(true);
    limparEstado();
    try {
      const { data } = await apiPdv.createVenda({ observacoes: 'Venda PDV' });
      await carregarVendaComItens(data.id);
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
      let combined = [...listF, ...listB];
      // fallback por código de barras exato
      if (combined.length === 0) {
        const byCode = await Promise.allSettled([
          apiFruteira.getByCodigoBarras(busca).catch(() => apiFruteira.getByCodigo(busca)),
          apiBuffet.getByCodigoBarras(busca).catch(() => apiBuffet.getByCodigo(busca)),
        ]);
        const extra: ProdutoBusca[] = [];
        if (byCode[0].status === 'fulfilled' && byCode[0].value?.data) extra.push({ ...byCode[0].value.data, origem: 'FRUTEIRA' });
        if (byCode[1].status === 'fulfilled' && byCode[1].value?.data) extra.push({ ...byCode[1].value.data, origem: 'BUFFET' });
        combined = extra;
      }
      setResultados(combined.slice(0, 30));
      setBuscou(true);

      // Exibe nas abas a origem onde o item/mesa foi encontrado
      const hasB = combined.some((p) => p.origem === 'BUFFET');
      const hasF = combined.some((p) => p.origem === 'FRUTEIRA');
      const hasM = mesasComComanda.some((m) => mesaCorresponde(m, busca));
      if ((hasB ? 1 : 0) + (hasF ? 1 : 0) + (hasM ? 1 : 0) === 1) {
        if (hasB) setTabValue('buffet');
        else if (hasF) setTabValue('fruteira');
        else setTabValue('mesas');
      }
      if (combined.length === 0 && !hasM) toast.info('Nenhum item ou mesa encontrado');
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
    let currentVenda = venda;
    if (!currentVenda) {
      try {
        const { data: novaV } = await apiPdv.createVenda({ observacoes: 'Venda PDV' });
        currentVenda = novaV;
        setVenda(novaV);
      } catch {
        const local: VendaAtual = { id: `local-${Date.now()}`, numero: 'LOCAL', subtotal: 0, valorTotal: 0, valorPago: 0, status: 'ABERTA', itens: [], pagamentos: [] };
        currentVenda = local;
        setVenda(local);
      }
    }
    if (!currentVenda) return;
    const qtd = parseDecimal(quantidade, 3) || 1;
    const novoContador = { ...contadorItens } as { BUFFET: number; FRUTEIRA: number };
    const origem = p.origem ?? 'FRUTEIRA';
    novoContador[origem] = (novoContador[origem] ?? 0) + 1;
    setContadorItens(novoContador);
    const codigoProd = obterCodigoProduto(origem, (contadorItens[origem] ?? 0) as number);
    if (currentVenda.id.startsWith('local-')) {
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
      const apiCall = p.origem === 'BUFFET' ? apiPdv.addItemBuffet(currentVenda.id, p.id, qtd) : apiPdv.addItemFruteira(currentVenda.id, p.id, qtd);
      await apiCall;
      await carregarVendaComItens(currentVenda.id);
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
      await apiPdv.addPratoBalanca(venda.id, peso);
      await carregarVendaComItens(venda.id);
      setPesoDialog(false);
      setPesoBruto('');
      toast.success('Prato por peso lançado');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao lançar peso');
    }
  };

  const removerItem = async (itemId: string) => {
    if (!venda) return;
    if (venda.id.startsWith('local-')) {
      setVenda((v) => {
        if (!v) return v;
        const itens = v.itens?.filter((i) => i.id !== itemId) ?? [];
        const subtotal = itens.reduce((s, i) => s + (i.valorTotal ?? 0), 0);
        return { ...v, itens, subtotal, valorTotal: subtotal - (v.desconto ?? 0) + (v.acrescimo ?? 0) };
      });
      return;
    }
    try {
      await api.post(`/pdv/itens-venda/${itemId}/cancelar`, null, { params: { motivo: 'Removido no PDV' } });
      await carregarVendaComItens(venda.id);
      toast.success('Item removido');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao remover item');
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
      await apiPdv.aplicarDesconto(venda.id, v);
      await carregarVendaComItens(venda.id);
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
      setPagDialog(false);
      setValorPagamento('');
      // Add payment to the existing payments or create new array
      const novoPagamento: { forma: string; valor: number; shortcut: string } = { forma: formaPagamento, valor, shortcut: '' };
      setVenda((s) => s ? { ...s, pagamentos: [...(s.pagamentos ?? []), novoPagamento] } : s);
      return;
    }
    try {
      await apiPdv.processarPagamento(venda.id, { formaPagamento, valor });
      await carregarVendaComItens(venda.id);
      setPagDialog(false);
      setValorPagamento('');
      toast.success('Pagamento registrado');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao processar pagamento');
    }
  };

  const finalizarVendaInterna = async (cpfConsumidor?: string) => {
    if (!venda) return;
    if (venda.id.startsWith('local-')) {
      setVenda((s) => s ? { ...s, status: 'FINALIZADA' } : s);
      toast.success('Venda finalizada (local)');
      return;
    }
    try {
      const { data } = await apiPdv.finalizarVenda(venda.id, cpfConsumidor);
      setVenda(data);
      toast.success('Venda finalizada!');
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao finalizar venda');
    }
  };

  const finalizar = async () => {
    if (!venda) return;
    const docDigits = parseDocument(documento);
    const docCliente = cliente?.cpfCnpj ? parseDocument(cliente.cpfCnpj) : '';
    if (docDigits && !cliente) {
      // Cliente informado mas não cadastrado: pede o nome para cadastro
      setCadClienteForm({ nome: '', telefone: '' });
      setCadClienteDialog(true);
      return;
    }
    await finalizarVendaInterna(docCliente || docDigits || undefined);
  };

  const confirmarCadastroCliente = async () => {
    if (!cadClienteForm.nome.trim()) { toast.warning('Informe o nome do cliente'); return; }
    const doc = parseDocument(documento);
    try {
      const { data: novo } = await apiClientes.create({
        nome: cadClienteForm.nome.trim(),
        telefone: cadClienteForm.telefone || undefined,
        cpfCnpj: maskCpfCnpj(doc),
        tipoPessoa: doc.length > 11 ? 'JURIDICA' : 'FISICA',
        status: 'ATIVO',
      });
      setCliente(novo);
      setClienteNaoEncontrado(false);
      setCadClienteDialog(false);
      toast.success('Cliente cadastrado!');
      await finalizarVendaInterna(doc);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Erro ao cadastrar cliente');
    }
  };

  const cancelar = async () => {
    if (!venda) return;
    if (!window.confirm("Cancelar esta venda?")) return;
    // Clear all fields and start new venda
    limparEstado();
    setVenda(null);
    toast.success("Venda cancelada - nova venda iniciada");
  };

  const saldo = (venda?.valorTotal ?? 0) - (venda?.valorPago ?? 0);

  useEffect(() => {
    const handler = (_e: KeyboardEvent) => {
      if (_e.key >= 'F1' && _e.key <= 'F12') {
        _e.preventDefault();
        const keyNum = parseInt(_e.key.replace('F', ''), 10);
        switch (keyNum) {
          case 1:
            novaVenda();
            break;
          case 2:
            document.getElementById('pdv-busca-input')?.focus();
            break;
          case 3:
            document.getElementById('pdv-doc-input')?.focus();
            break;
          case 4:
            if (venda) setPesoDialog(true);
            else toast.warning('Inicie uma venda primeiro');
            break;
          case 5:
            break;
          case 6:
            if (venda) {
              setValorPagamento(saldo > 0 ? maskCurrency(saldo) : '');
              setPagDialog(true);
            } else {
              toast.warning('Inicie uma venda primeiro');
            }
            break;
          case 7:
            finalizar();
            break;
          case 8:
            cancelar();
            break;
          case 9:
            abrirFiado();
            break;
          case 10:
            if (venda && !venda.id.startsWith('local-')) recarregarVenda(venda.id);
            break;
          case 11:
            setTabValue((prev) => (prev === 'buffet' ? 'fruteira' : prev === 'fruteira' ? 'mesas' : 'buffet'));
            break;
          case 12:
            setShortcutDialog(true);
            break;
          default:
            break;
        }
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [venda, saldo, recarregarVenda, finalizar, cancelar, abrirFiado, novaVenda]);

  const vendaIniciada = !!venda && ((venda.itens?.length ?? 0) > 0 || !!cliente);
  const filtrarProdutosDaAbas = (tab: 'buffet' | 'fruteira' | 'mesas') => {
    if (tab === 'buffet') {
      return buscou ? resultados.filter((p) => p.origem === 'BUFFET') : estoqueBuffet;
    }
    if (tab === 'fruteira') {
      return buscou ? resultados.filter((p) => p.origem === 'FRUTEIRA') : estoqueFruteira;
    }
    return mesasFiltradas;
  };
  const mesasFiltradas = busca.trim() ? mesasComComanda.filter((m) => mesaCorresponde(m, busca)) : mesasComComanda;

  const renderProdutos = (produtos: ProdutoBusca[], vazio: string) => (
    <Box>
      {produtos.map((p) => (
        <Box key={`${p.origem}-${p.id}`} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, py: 0.75, borderBottom: 1, borderColor: 'divider' }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="body2" fontWeight={600} noWrap>{p.nome}</Typography>
            <Typography variant="caption" color="text.secondary">
              {p.codigo ?? p.codigoBarras ?? '—'} · {formatCurrency(p.precoVenda ?? 0)} · Est: {formatNumber(Number(p.estoqueAtual ?? 0), 3)}{p.unidadeMedida ? ` ${p.unidadeMedida}` : ''}
            </Typography>
          </Box>
          <Button size="small" variant="outlined" startIcon={<AddIcon />} onClick={() => adicionarItem(p)} disabled={!venda}>Adicionar</Button>
        </Box>
      ))}
      {produtos.length === 0 && <Typography variant="caption" color="text.secondary">{vazio}</Typography>}
    </Box>
  );

  return (
    <Box>
      <PageHeader
        title="PDV — Frente de Caixa"
        subtitle="Venda de produtos da fruteira e buffet por código de barras, peso e múltiplos pagamentos"
        icon={<PointOfSaleIcon />}
        action={
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button variant="outlined" color="warning" startIcon={<MenuBookIcon />} onClick={abrirFiado}>Caderno (fiado) <Typography variant="caption" color="text.secondary">(F9)</Typography></Button>
            <Button variant="outlined" startIcon={<RefreshIcon />} onClick={() => venda && recarregarVenda(venda.id)} disabled={!venda || venda.id.startsWith('local-')}>Atualizar <Typography variant="caption" color="text.secondary">(F10)</Typography></Button>
            <Button variant="contained" startIcon={<AddIcon />} onClick={novaVenda} disabled={loading}>Nova venda <Typography variant="caption" color="text.secondary">(F1)</Typography></Button>
          </Box>
        }
      />

      <Grid container spacing={2}>
        <Grid item xs={12} md={8}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Buscar produto / mesa / comanda</Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <TextField id="pdv-busca-input" fullWidth size="small" placeholder="Código, código de barras, nome, mesa ou comanda + Enter" value={busca}
                  onChange={(e) => setBusca(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter' || (e.altKey && e.key === 'Enter')) { e.preventDefault(); buscarProdutos(); carregarMesas(); } }} />
                <TextField size="small" label="Qtd" value={maskQuantity(quantidade)} onChange={(e) => setQuantidade(maskQuantity(e.target.value))} sx={{ width: 90 }} inputMode="decimal" />
                <Button variant="contained" onClick={() => { buscarProdutos(); carregarMesas(); }} disabled={buscando} startIcon={<SearchIcon />}>Buscar</Button>
                <Tooltip title="Lançar prato por peso (balança)"><span><IconButton color="primary" onClick={() => setPesoDialog(true)} disabled={!venda}><ScaleIcon /></IconButton></span></Tooltip>
              </Box>
            </CardContent>
          </Card>
          <Tabs value={tabValue} onChange={(_, v) => setTabValue(v as 'buffet' | 'fruteira' | 'mesas')} sx={{ mb: 2 }}>
            <Tab value="buffet" label="Buffet" />
            <Tab value="fruteira" label="Fruteira" />
            <Tab value="mesas" label="Mesas" />
          </Tabs>
          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Itens a venda</Typography>
{tabValue === 'buffet' ? renderProdutos(filtrarProdutosDaAbas('buffet'), 'Nenhum produto do buffet encontrado') :
                 tabValue === 'fruteira' ? renderProdutos(filtrarProdutosDaAbas('fruteira'), 'Nenhum produto da fruteira encontrado') :
                (() => {
                  const mesas = mesasFiltradas;
                  if (mesas.length === 0) return <Typography variant="caption" color="text.secondary">Nenhuma mesa com comanda aberta encontrada</Typography>;
                  return (
                    <Box>
                      {mesas.map((m: any) => (
                        <Box key={m.id} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 1, py: 0.75, borderBottom: 1, borderColor: 'divider' }}>
                          <Box sx={{ minWidth: 0 }}>
                            <Typography variant="body2" fontWeight={600} noWrap>Mesa {m.numero}</Typography>
                            <Typography variant="caption" color="text.secondary">
                              {m.nome ?? '—'} · {m.comandaAtiva?.clienteNome ?? 'sem cliente'} · comanda {m.comandaAtiva?.numero ?? '—'}
                            </Typography>
                          </Box>
                          <Button size="small" variant="outlined" startIcon={<AddIcon />} onClick={() => carregarMesaPdv(m.comandaAtiva?.id ?? m.id, String(m.numero))} disabled={!venda}>Adicionar</Button>
                        </Box>
                      ))}
                    </Box>
                  );
                })()}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Busca do cliente</Typography>
               <TextField
                 id="pdv-doc-input"
                 fullWidth size="small" label="CPF/CNPJ do cliente"
                value={documento}
                onChange={(e) => onDocumentoChange(e.target.value)}
                placeholder="000.000.000-00"
                inputMode="numeric"
                sx={{ mb: 1.5 }}
              />
              {clienteNaoEncontrado && (
                <Alert severity="info" sx={{ mb: 1.5 }}>
                  Cliente não encontrado no cadastro. Informe o nome ao finalizar a venda para cadastrá-lo.
                </Alert>
              )}
              <Autocomplete
                size="small"
                options={opcoesClientes}
                getOptionLabel={(o: any) => `${o.nome ?? ''}${o.cpfCnpj ? ` — ${o.cpfCnpj}` : ''}`}
                isOptionEqualToValue={(o: any, v: any) => o.id === v?.id}
                value={cliente}
                onChange={(_, v) => selecionarCliente(v)}
                onInputChange={(_, v, reason) => { if (reason === 'input') buscarClientes(v); }}
                renderInput={(params) => (
                  <TextField {...params} placeholder="Ou busque cliente por nome" />
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
                    {situacao.quantidadeLancamentos ?? 0} lançamento(es) em aberto
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
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>Resumo</Typography>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<ListAltIcon />}
                onClick={() => setItensModalOpen(true)}
                sx={{ mb: 2 }}
              >
                Itens adicionados ({venda?.itens?.length ?? 0})
              </Button>
              {venda?.subtotal !== undefined && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}><Typography variant="body2">Subtotal</Typography><Typography fontWeight={600}>{formatCurrency(venda?.subtotal ?? 0)}</Typography></Box>
              )}
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="body2">Desconto</Typography><Typography>{formatCurrency(venda?.desconto ?? 0)}</Typography></Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="body2">Pago</Typography><Typography>{formatCurrency(venda?.valorPago ?? 0)}</Typography></Box>
              <Divider sx={{ my: 1 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="h6">Total</Typography><Typography variant="h6" color="primary">{formatCurrency(venda?.valorTotal ?? 0)}</Typography></Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography variant="body2">Saldo devedor</Typography><Chip size="small" label={formatCurrency(saldo)} color={saldo <= 0 ? 'success' : 'warning'} /></Box>
              <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                <TextField size="small" label="Desconto (R$)" value={maskCurrency(desconto)} onChange={(e) => setDesconto(maskCurrency(e.target.value))} placeholder="0,00" sx={{ flex: 1 }} inputMode="decimal" />
                <Button variant="outlined" onClick={aplicarDesconto} disabled={!venda}>Aplicar <Typography variant="caption" color="text.secondary">(F5)</Typography></Button>
              </Box>
              <Grid container spacing={1}>
                <Grid item xs={6}><Button fullWidth variant="outlined" startIcon={<PaymentsIcon />} onClick={() => { setValorPagamento(saldo > 0 ? maskCurrency(saldo) : ''); setPagDialog(true); }} disabled={!venda}>Pagamento <Typography variant="caption" color="text.secondary">(F6)</Typography></Button></Grid>
                <Grid item xs={6}><Button fullWidth variant="contained" startIcon={<CheckIcon />} onClick={finalizar} disabled={venda == null || (venda?.pagamentos?.length === 0 && venda?.valorPago === 0)}>Finalizar <Typography variant="caption" color="text.secondary">(F7)</Typography></Button></Grid>
                <Grid item xs={12}><Button fullWidth color="error" startIcon={<CancelIcon />} onClick={cancelar} disabled={!vendaIniciada && !venda}>Cancelar venda <Typography variant="caption" color="text.secondary">(F8)</Typography></Button></Grid>
              </Grid>
              {venda?.pagamentos && venda.pagamentos.length > 0 && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="caption" fontWeight={700}>PAGAMENTOS</Typography>
                  {venda.pagamentos.map((p, i) => (
                    <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="caption">{p.forma}</Typography>
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
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 2 }}>
            {SHORTCUTS_PAGAMENTO.map((item) => (
              <Button key={item.forma} variant="outlined" sx={{ width: '100%' }} onClick={() => setFormaPagamento(item.forma)}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2" fontWeight={600}>{item.label}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {item.shortcut}
                  </Typography>
                </Box>
              </Button>
            ))}
          </Box>
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

      {/* Dialog: cadastrar cliente inexistente ao finalizar a venda */}
      <Dialog open={cadClienteDialog} onClose={() => setCadClienteDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Cadastrar cliente</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            O cliente <strong>{maskCpfCnpj(documento)}</strong> não está cadastrado. Informe o nome para concluir a venda:
          </Typography>
          <TextField fullWidth label="Nome do cliente" value={cadClienteForm.nome}
            onChange={(e) => setCadClienteForm({ ...cadClienteForm, nome: e.target.value })} sx={{ mb: 2 }} autoFocus />
          <TextField fullWidth label="Telefone (opcional)" value={cadClienteForm.telefone}
            onChange={(e) => setCadClienteForm({ ...cadClienteForm, telefone: e.target.value })} placeholder="(00) 00000-0000" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCadClienteDialog(false)}>Cancelar</Button>
          <Button variant="contained" onClick={confirmarCadastroCliente}>Cadastrar e finalizar</Button>
        </DialogActions>
      </Dialog>

      {/* Dialog: Itens Adicionados */}
      <Dialog open={itensModalOpen} onClose={() => setItensModalOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Itens adicionados</DialogTitle>
        <DialogContent dividers>
          {venda?.itens && venda.itens.length > 0 ? (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {venda.itens.map((it) => (
                <Box key={it.id} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 1, border: 1, borderColor: 'divider', borderRadius: 1 }}>
                  <Box sx={{ minWidth: 0, flex: 1, mr: 1 }}>
                    <Typography variant="body2" fontWeight={600} noWrap>{it.nomeProduto}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {it.quantidade}x {formatCurrency(it.precoUnitario ?? 0)} · Código: {it.codigoProduto ?? '—'}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2" fontWeight={700}>{formatCurrency(it.valorTotal ?? 0)}</Typography>
                    <Button size="small" color="error" variant="outlined" onClick={() => removerItem(it.id!)} startIcon={<CancelIcon />}>
                      Remover
                    </Button>
                  </Box>
                </Box>
              ))}
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 3 }}>
              Nenhum item adicionado nesta venda.
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button variant="contained" onClick={() => setItensModalOpen(false)}>Fechar</Button>
        </DialogActions>
      </Dialog>

      {/* Dialog: Ajuda de Atalhos F1 a F12 */}
      <Dialog open={shortcutDialog} onClose={() => setShortcutDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Teclas de Atalho do PDV (F1 - F12)</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            <Typography variant="body2"><strong>[F1]</strong> Nova Venda — Inicia uma nova venda no PDV.</Typography>
            <Typography variant="body2"><strong>[F2]</strong> Focar Busca — Move o cursor para o campo de código/nome do produto.</Typography>
            <Typography variant="body2"><strong>[F3]</strong> Focar Cliente — Move o cursor para o campo CPF/CNPJ do cliente.</Typography>
            <Typography variant="body2"><strong>[F4]</strong> Prato por Peso (Balança) — Abre o diálogo para lançamento de peso em kg.</Typography>
            <Typography variant="body2"><strong>[F5]</strong> Desconto — Atalho para campo de desconto.</Typography>
            <Typography variant="body2"><strong>[F6]</strong> Registrar Pagamento — Abre a tela de formas de pagamento (Dinheiro, PIX, Cartão, etc.).</Typography>
            <Typography variant="body2"><strong>[F7]</strong> Finalizar Venda — Conclui a venda atual e emite comprovante.</Typography>
            <Typography variant="body2"><strong>[F8]</strong> Cancelar Venda — Cancela a venda em andamento.</Typography>
            <Typography variant="body2"><strong>[F9]</strong> Caderno / Fiado — Lança a dívida no caderno do cliente selecionado.</Typography>
            <Typography variant="body2"><strong>[F10]</strong> Atualizar — Atualiza os dados da venda no servidor.</Typography>
            <Typography variant="body2"><strong>[F11]</strong> Alternar Abas — Alterna entre Buffet, Fruteira e Mesas.</Typography>
            <Typography variant="body2"><strong>[F12]</strong> Ajuda de Atalhos — Exibe esta janela de ajuda.</Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button variant="contained" onClick={() => setShortcutDialog(false)}>Fechar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
export default PdvPage;