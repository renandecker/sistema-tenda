import { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Alert,
  Divider,
  InputAdornment,
  Menu,
  MenuItem,
  Tooltip,
  TablePagination,
  TableSortLabel,
} from '@mui/material';
import {
  ReceiptLong as ReceiptLongIcon,
  Refresh as RefreshIcon,
  Search as SearchIcon,
  Delete as DeleteIcon,
  Visibility as VisibilityIcon,
  ContentCopy as ContentCopyIcon,
  Download as DownloadIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  HourglassEmpty as HourglassIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFiscal } from '@/services/api';

const STATUS_LABELS: Record<string, string> = {
  PENDENTE: 'Pendente',
  EM_PROCESSAMENTO: 'Em Processamento',
  AUTORIZADA: 'Autorizada',
  REJEITADA: 'Rejeitada',
  CANCELADA: 'Cancelada',
  DENEGADA: 'Denegada',
  INUTILIZADA: 'Inutilizada',
  PRODUTOR_RURAL: 'Produtor Rural',
};

const STATUS_COLORS: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
  PENDENTE: 'warning',
  EM_PROCESSAMENTO: 'info',
  AUTORIZADA: 'success',
  REJEITADA: 'error',
  CANCELADA: 'error',
  DENEGADA: 'error',
  INUTILIZADA: 'default',
  PRODUTOR_RURAL: 'info',
};

const MODELO_LABELS: Record<string, string> = {
  '55': 'NF-e',
  '65': 'NFC-e',
  '59': 'SAT/CFe',
  '89': 'DANFE Produtor Rural',
};

interface Nfe {
  id: string;
  chaveAcesso: string;
  modelo: string;
  serie: number;
  numero: number;
  status: string;
  naturezaOperacao: string;
  dataEmissao: string;
  clienteNome: string;
  clienteDocumento: string;
  valorTotal: number;
  protocoloAutorizacao: string;
  motivoRejeicao: string;
  ambiente: string;
  vendaId: string;
}

interface PageResponse {
  content: Nfe[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export function NfePage() {
  const [nfeList, setNfeList] = useState<Nfe[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalElements, setTotalElements] = useState(0);
  const [sortModel, setSortModel] = useState<{ field: string; sort: 'asc' | 'desc' }>({ field: 'dataEmissao', sort: 'desc' });
  const [filters, setFilters] = useState({
    status: '',
    modelo: '',
    busca: '',
    dataInicio: '',
    dataFim: '',
  });
  const [selectedNfe, setSelectedNfe] = useState<Nfe | null>(null);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [cancelJustificativa, setCancelJustificativa] = useState('');

  const loadNfe = async () => {
    setLoading(true);
    try {
      const params: any = {
        page,
        size,
        sortBy: sortModel.field,
        sortDirection: sortModel.sort.toUpperCase(),
      };
      if (filters.status) params.status = filters.status;
      if (filters.modelo) params.modelo = filters.modelo;
      if (filters.busca) params.busca = filters.busca;
      if (filters.dataInicio) params.dataInicio = filters.dataInicio;
      if (filters.dataFim) params.dataFim = filters.dataFim;

      const response = await apiFiscal.listNfe(params);
      const data = response.data;
      setNfeList(data.content || data);
      setTotalElements(data.totalElements || 0);
    } catch (error) {
      toast.error('Erro ao carregar NFC-e/NF-e');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNfe();
  }, [page, size, sortModel, filters]);

  const handleSort = (field: string) => {
    setSortModel(prev => ({
      field,
      sort: prev.field === field && prev.sort === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handleCancelar = async () => {
    if (!selectedNfe || !cancelJustificativa.trim()) return;

    try {
      await apiFiscal.cancelarNfe(selectedNfe.id, cancelJustificativa);
      toast.success('NFC-e cancelada com sucesso!');
      setCancelDialogOpen(false);
      setCancelJustificativa('');
      loadNfe();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao cancelar NFC-e');
    }
  };

  const handleConsultar = async (nfe: Nfe) => {
    try {
      await apiFiscal.consultarNfe(nfe.id);
      toast.success('Status consultado com sucesso!');
      loadNfe();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao consultar status');
    }
  };

  const handleDownloadXml = async (nfe: Nfe) => {
    try {
      const response = await apiFiscal.downloadXml(nfe.id);
      const blob = new Blob([response.data], { type: 'application/xml' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `nfe-${nfe.chaveAcesso}.xml`;
      link.click();
      window.URL.revokeObjectURL(url);
      toast.success('XML baixado com sucesso!');
    } catch (error) {
      toast.error('Erro ao baixar XML');
    }
  };

  const handleDownloadDanfe = async (nfe: Nfe) => {
    try {
      const response = await apiFiscal.downloadDanfe(nfe.id);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `danfe-${nfe.chaveAcesso}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
      toast.success('DANFE baixado com sucesso!');
    } catch (error) {
      toast.error('Erro ao baixar DANFE');
    }
  };

  const handleCopyChave = (chave: string) => {
    navigator.clipboard.writeText(chave);
    toast.success('Chave de acesso copiada!');
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('pt-BR');
  };

  const formatChave = (chave: string) => {
    if (!chave) return '';
    return chave.replace(/(\d{4})(\d{4})(\d{4})(\d{4})(\d{4})(\d{4})(\d{4})(\d{4})(\d{4})(\d{4})/, '$1 $2 $3 $4 $5 $6 $7 $8 $9 $10');
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" component="h1">
            <ReceiptLongIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            NFC-e / Notas Fiscais
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Emissão e gestão de NFC-e, NF-e e Nota do Produtor Rural
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={loadNfe}
          disabled={loading}
        >
          Atualizar
        </Button>
      </Box>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} sx={{ mb: 2 }}>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                label="Buscar"
                placeholder="Chave, nº, cliente..."
                value={filters.busca}
                onChange={(e) => setFilters({ ...filters, busca: e.target.value })}
                fullWidth
                size="small"
                InputProps={{
                  startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>,
                }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                select
                label="Status"
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                fullWidth
                size="small"
                SelectProps={{ native: true }}
              >
                <option value="">Todos</option>
                <option value="PENDENTE">Pendente</option>
                <option value="EM_PROCESSAMENTO">Em Processamento</option>
                <option value="AUTORIZADA">Autorizada</option>
                <option value="REJEITADA">Rejeitada</option>
                <option value="CANCELADA">Cancelada</option>
                <option value="DENEGADA">Denegada</option>
                <option value="INUTILIZADA">Inutilizada</option>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                select
                label="Modelo"
                value={filters.modelo}
                onChange={(e) => setFilters({ ...filters, modelo: e.target.value })}
                fullWidth
                size="small"
                SelectProps={{ native: true }}
              >
                <option value="">Todos</option>
                <option value="55">NF-e (55)</option>
                <option value="65">NFC-e (65)</option>
                <option value="59">SAT/CFe (59)</option>
                <option value="89">Produtor Rural (89)</option>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                type="date"
                label="Data Início"
                value={filters.dataInicio}
                onChange={(e) => setFilters({ ...filters, dataInicio: e.target.value })}
                fullWidth
                size="small"
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                type="date"
                label="Data Fim"
                value={filters.dataFim}
                onChange={(e) => setFilters({ ...filters, dataFim: e.target.value })}
                fullWidth
                size="small"
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
          </Grid>

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>
                    <TableSortLabel
                      active={sortModel.field === 'dataEmissao'}
                      direction={sortModel.field === 'dataEmissao' ? sortModel.sort : 'asc'}
                      onClick={() => handleSort('dataEmissao')}
                    >
                      Emissão
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={sortModel.field === 'chaveAcesso'}
                      direction={sortModel.field === 'chaveAcesso' ? sortModel.sort : 'asc'}
                      onClick={() => handleSort('chaveAcesso')}
                    >
                      Chave de Acesso
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Modelo</TableCell>
                  <TableCell>Série/Número</TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={sortModel.field === 'clienteNome'}
                      direction={sortModel.field === 'clienteNome' ? sortModel.sort : 'asc'}
                      onClick={() => handleSort('clienteNome')}
                    >
                      Cliente
                    </TableSortLabel>
                  </TableCell>
                  <TableCell align="right">
                    <TableSortLabel
                      active={sortModel.field === 'valorTotal'}
                      direction={sortModel.field === 'valorTotal' ? sortModel.sort : 'asc'}
                      onClick={() => handleSort('valorTotal')}
                    >
                      Valor Total
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="center">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {nfeList.map((nfe) => (
                  <TableRow key={nfe.id} hover>
                    <TableCell>
                      <Typography variant="body2">{formatDate(nfe.dataEmissao)}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                        {formatChave(nfe.chaveAcesso)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={MODELO_LABELS[nfe.modelo] || nfe.modelo}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">Série: {nfe.serie}</Typography>
                      <Typography variant="body2">Nº: {nfe.numero}</Typography>
                    </TableCell>
                    <TableCell>
                      {nfe.clienteNome ? (
                        <>
                          <Typography variant="body2">{nfe.clienteNome}</Typography>
                          {nfe.clienteDocumento && (
                            <Typography variant="caption" color="text.secondary">
                              {nfe.clienteDocumento}
                            </Typography>
                          )}
                        </>
                      ) : (
                        <Typography variant="body2" color="text.secondary">Consumidor Final</Typography>
                      )}
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {formatCurrency(nfe.valorTotal)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={STATUS_LABELS[nfe.status] || nfe.status}
                        size="small"
                        color={STATUS_COLORS[nfe.status] || 'default'}
                        icon={
                          nfe.status === 'AUTORIZADA' ? <CheckCircleIcon fontSize="small" /> :
                          nfe.status === 'REJEITADA' || nfe.status === 'CANCELADA' || nfe.status === 'DENEGADA' ? <ErrorIcon fontSize="small" /> :
                          nfe.status === 'PENDENTE' || nfe.status === 'EM_PROCESSAMENTO' ? <HourglassIcon fontSize="small" /> : null
                        }
                      />
                      {nfe.motivoRejeicao && (
                        <Tooltip title={nfe.motivoRejeicao}>
                          <WarningIcon fontSize="small" color="error" sx={{ ml: 0.5, cursor: 'help' }} />
                        </Tooltip>
                      )}
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Copiar Chave">
                        <IconButton size="small" onClick={() => handleCopyChave(nfe.chaveAcesso)}>
                          <ContentCopyIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Baixar XML">
                        <IconButton size="small" onClick={() => handleDownloadXml(nfe)}>
                          <DownloadIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Baixar DANFE">
                        <IconButton size="small" onClick={() => handleDownloadDanfe(nfe)}>
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                      {nfe.status === 'AUTORIZADA' && (
                        <Tooltip title="Cancelar">
                          <IconButton size="small" color="error" onClick={() => {
                            setSelectedNfe(nfe);
                            setCancelDialogOpen(true);
                          }}>
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      )}
                      <Tooltip title="Consultar Status">
                        <IconButton size="small" onClick={() => handleConsultar(nfe)}>
                          <RefreshIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            component="div"
            count={totalElements}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setSize(parseInt(e.target.value, 10));
              setPage(0);
            }}
            rowsPerPage={size}
            rowsPerPageOptions={[10, 20, 50, 100]}
          />

          {nfeList.length === 0 && !loading && (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="h6" color="text.secondary">
                Nenhuma nota fiscal encontrada
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Dialog Cancelar */}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          <Typography variant="h6">
            Cancelar NFC-e - {selectedNfe ? `Nº ${selectedNfe.serie}/${selectedNfe.numero}` : ''}
          </Typography>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            <Alert severity="warning" sx={{ mb: 2 }}>
              Esta ação cancela a NFC-e na SEFAZ. A justificativa deve ter no mínimo 15 caracteres.
            </Alert>
            <TextField
              multiline
              rows={4}
              label="Justificativa do Cancelamento"
              value={cancelJustificativa}
              onChange={(e) => setCancelJustificativa(e.target.value)}
              fullWidth
              helperText={`${cancelJustificativa.length}/15 caracteres mínimos`}
              error={cancelJustificativa.length > 0 && cancelJustificativa.length < 15}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>Cancelar</Button>
          <Button
            variant="contained"
            color="error"
            onClick={handleCancelar}
            disabled={cancelJustificativa.length < 15}
          >
            Confirmar Cancelamento
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default NfePage;