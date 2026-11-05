import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  IconButton,
  InputAdornment,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  Business as BusinessIcon,
  Download as DownloadIcon,
  Print as PrintIcon,
  Refresh as RefreshIcon,
  Search as SearchIcon,
  SearchOff as SearchOffIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { apiFiscal } from '@/services/api';
import { maskCpfCnpj } from '@/utils/masks';

interface DanfeCnpj {
  cnpj: string;
  tipoDocumento?: string;
  razaoSocial: string;
  totalDanfes: number;
  valorTotal: number;
  ultimaEmissao: string | null;
  clienteCadastrado: boolean;
}

interface DanfeArquivo {
  id: string;
  chaveAcesso: string;
  modelo: string;
  serie: number;
  numero: number;
  status: string;
  cnpj: string;
  tipoDocumento?: string;
  razaoSocial: string;
  dataEmissao: string;
  valorTotal: number;
  nomeArquivo: string;
  arquivoFisico: boolean;
}

const MODELO_LABEL: Record<string, string> = {
  '55': 'NF-e',
  '65': 'NFC-e',
  '59': 'SAT',
  '89': 'Produtor Rural',
};

function formatCurrency(value?: number | null) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value ?? 0);
}

function formatDate(value?: string | null) {
  if (!value) return '—';
  return new Date(value).toLocaleString('pt-BR');
}

export function DanfeCnpjPage() {
  const [grupos, setGrupos] = useState<DanfeCnpj[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [total, setTotal] = useState(0);
  const [busca, setBusca] = useState('');

  const [grupoSelecionado, setGrupoSelecionado] = useState<DanfeCnpj | null>(null);
  const [notas, setNotas] = useState<DanfeArquivo[]>([]);
  const [notasLoading, setNotasLoading] = useState(false);
  const [notasPage, setNotasPage] = useState(0);
  const [notasSize, setNotasSize] = useState(10);
  const [notasTotal, setNotasTotal] = useState(0);
  const [processandoId, setProcessandoId] = useState<string | null>(null);

  const carregarCnpjs = async () => {
    setLoading(true);
    try {
      const { data } = await apiFiscal.listDanfeCnpjs({ page, size, busca: busca || undefined });
      setGrupos(data?.content ?? []);
      setTotal(data?.totalElements ?? 0);
    } catch {
      toast.error('Erro ao carregar os CNPJs com DANFEs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    carregarCnpjs();
  }, [page, size, busca]);

  const carregarNotas = async (grupo: DanfeCnpj, pagina = notasPage, tamanho = notasSize) => {
    setNotasLoading(true);
    try {
      const { data } = await apiFiscal.listDanfePorCnpj(grupo.cnpj, { page: pagina, size: tamanho });
      setNotas(data?.content ?? []);
      setNotasTotal(data?.totalElements ?? 0);
    } catch {
      toast.error('Erro ao carregar os DANFEs deste CNPJ');
    } finally {
      setNotasLoading(false);
    }
  };

  const abrirGrupo = (grupo: DanfeCnpj) => {
    setGrupoSelecionado(grupo);
    setNotas([]);
    setNotasPage(0);
    setNotasTotal(0);
    carregarNotas(grupo, 0, notasSize);
  };

  const fecharGrupo = () => {
    setGrupoSelecionado(null);
    setNotas([]);
  };

  const abrirPdf = async (nota: DanfeArquivo, download = false) => {
    setProcessandoId(nota.id);
    try {
      const response = await apiFiscal.getDanfePdf(nota.id, download);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);

      if (download) {
        const link = document.createElement('a');
        link.href = url;
        link.download = nota.nomeArquivo || `danfe-${nota.chaveAcesso}.pdf`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
        toast.success('DANFE baixado com sucesso!');
        return;
      }

      const iframe = document.createElement('iframe');
      iframe.style.position = 'fixed';
      iframe.style.right = '0';
      iframe.style.bottom = '0';
      iframe.style.width = '0';
      iframe.style.height = '0';
      iframe.style.border = '0';
      iframe.src = url;
      iframe.onload = () => {
        setTimeout(() => {
          const janela = iframe.contentWindow;
          if (janela) {
            janela.focus();
            janela.print();
          }
        }, 600);
      };
      document.body.appendChild(iframe);
      setTimeout(() => {
        iframe.remove();
        URL.revokeObjectURL(url);
      }, 60_000);
    } catch (error: any) {
      const mensagem = error?.response?.data?.message || 'Erro ao abrir o DANFE';
      toast.error(mensagem);
    } finally {
      setProcessandoId(null);
    }
  };

  const regenerar = async (nota: DanfeArquivo) => {
    setProcessandoId(nota.id);
    try {
      await apiFiscal.gerarDanfe(nota.id);
      toast.success('PDF do DANFE gerado novamente');
      if (grupoSelecionado) {
        await carregarNotas(grupoSelecionado, notasPage, notasSize);
      }
    } catch (error: any) {
      toast.error(error?.response?.data?.message || 'Erro ao gerar o PDF do DANFE');
    } finally {
      setProcessandoId(null);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, gap: 2, flexWrap: 'wrap' }}>
        <Box>
          <Typography variant="h4" component="h1">
            <BusinessIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            DANFEs por CNPJ
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Consulte os CNPJs, a razão social e imprima o DANFE armazenado no servidor.
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<RefreshIcon />} onClick={carregarCnpjs} disabled={loading}>
          Atualizar
        </Button>
      </Box>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <TextField
            label="Buscar por CNPJ ou razão social"
            placeholder="Digite o CNPJ ou parte da razão social"
            value={busca}
            onChange={(event) => {
              setBusca(event.target.value);
              setPage(0);
            }}
            fullWidth
            size="small"
            sx={{ mb: 2, maxWidth: 520 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />

          {grupos.length > 0 && grupos.every((grupo) => grupo.tipoDocumento === 'CPF') && (
            <Alert severity="info" sx={{ mb: 2 }}>
              Nenhum CNPJ encontrado nas notas ou nos clientes. Exibindo os CPFs para que a consulta não fique vazia.
              Assim que houver uma NF-e para pessoa jurídica, o CNPJ aparecerá aqui.
            </Alert>
          )}

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>CNPJ / CPF</TableCell>
                  <TableCell>Razão Social</TableCell>
                  <TableCell align="center">DANFEs</TableCell>
                  <TableCell align="right">Valor total</TableCell>
                  <TableCell>Última emissão</TableCell>
                  <TableCell align="center">Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading && (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                      <CircularProgress size={28} />
                    </TableCell>
                  </TableRow>
                )}

                {!loading && grupos.map((grupo) => (
                  <TableRow key={grupo.cnpj} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace', fontWeight: 600 }}>
                        {maskCpfCnpj(grupo.cnpj)}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5, flexWrap: 'wrap' }}>
                        <Chip label={grupo.tipoDocumento || 'CNPJ'} size="small" variant="outlined" />
                        {grupo.clienteCadastrado && (
                          <Chip label="Cliente cadastrado" size="small" variant="outlined" />
                        )}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {grupo.razaoSocial || 'Razão social não informada'}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={grupo.totalDanfes}
                        size="small"
                        color={grupo.totalDanfes > 0 ? 'primary' : 'default'}
                        variant={grupo.totalDanfes > 0 ? 'filled' : 'outlined'}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {formatCurrency(grupo.valorTotal)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{formatDate(grupo.ultimaEmissao)}</Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Button
                        size="small"
                        variant="outlined"
                        startIcon={<SearchIcon />}
                        onClick={() => abrirGrupo(grupo)}
                      >
                        Consultar DANFEs
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}

                {!loading && grupos.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 5 }}>
                      <SearchOffIcon sx={{ fontSize: 42, color: 'text.disabled', mb: 1 }} />
                      <Typography variant="h6" color="text.secondary">
                        Nenhum CNPJ encontrado
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Os CNPJs aparecem aqui a partir das notas emitidas e dos clientes pessoa jurídica cadastrados.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            component="div"
            count={total}
            page={page}
            onPageChange={(_, novaPagina) => setPage(novaPagina)}
            onRowsPerPageChange={(event) => {
              setSize(parseInt(event.target.value, 10));
              setPage(0);
            }}
            rowsPerPage={size}
            rowsPerPageOptions={[10, 20, 50, 100]}
          />
        </CardContent>
      </Card>

      <Dialog open={Boolean(grupoSelecionado)} onClose={fecharGrupo} maxWidth="lg" fullWidth>
        <DialogTitle>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <Box>
              <Typography variant="h6">{grupoSelecionado?.razaoSocial}</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                {grupoSelecionado?.tipoDocumento || 'CNPJ'} {grupoSelecionado ? maskCpfCnpj(grupoSelecionado.cnpj) : ''}
              </Typography>
            </Box>
            <Chip
              label={`${notasTotal} DANFE${notasTotal === 1 ? '' : 's'}`}
              color="primary"
              variant="outlined"
            />
          </Box>
        </DialogTitle>
        <DialogContent dividers>
          {notasLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 5 }}>
              <CircularProgress />
            </Box>
          ) : notas.length === 0 ? (
            <Alert severity="info">
              Nenhum DANFE encontrado para este CNPJ. Assim que uma NF-e ou NFC-e for emitida para ele, o PDF aparecerá aqui.
            </Alert>
          ) : (
            <>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Emissão</TableCell>
                      <TableCell>Documento</TableCell>
                      <TableCell>Série / Nº</TableCell>
                      <TableCell>Chave de acesso</TableCell>
                      <TableCell align="right">Valor</TableCell>
                      <TableCell>PDF</TableCell>
                      <TableCell align="center">Imprimir / Baixar</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {notas.map((nota) => (
                      <TableRow key={nota.id} hover>
                        <TableCell>{formatDate(nota.dataEmissao)}</TableCell>
                        <TableCell>
                          <Chip
                            label={MODELO_LABEL[nota.modelo] || nota.modelo}
                            size="small"
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell>{nota.serie} / {nota.numero}</TableCell>
                        <TableCell>
                          <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                            {nota.chaveAcesso}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">{formatCurrency(nota.valorTotal)}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            color={nota.arquivoFisico ? 'success' : 'warning'}
                            label={nota.arquivoFisico ? 'Salvo no disco' : 'Gerar ao imprimir'}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell align="center">
                          <Tooltip title="Imprimir DANFE">
                            <span>
                              <IconButton
                                color="primary"
                                onClick={() => abrirPdf(nota, false)}
                                disabled={processandoId === nota.id}
                              >
                                <PrintIcon />
                              </IconButton>
                            </span>
                          </Tooltip>
                          <Tooltip title="Baixar PDF">
                            <span>
                              <IconButton
                                onClick={() => abrirPdf(nota, true)}
                                disabled={processandoId === nota.id}
                              >
                                <DownloadIcon />
                              </IconButton>
                            </span>
                          </Tooltip>
                          <Tooltip title="Gerar PDF novamente">
                            <span>
                              <IconButton
                                onClick={() => regenerar(nota)}
                                disabled={processandoId === nota.id}
                              >
                                <RefreshIcon />
                              </IconButton>
                            </span>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <TablePagination
                component="div"
                count={notasTotal}
                page={notasPage}
                onPageChange={(_, novaPagina) => {
                  setNotasPage(novaPagina);
                  if (grupoSelecionado) carregarNotas(grupoSelecionado, novaPagina, notasSize);
                }}
                onRowsPerPageChange={(event) => {
                  const novoTamanho = parseInt(event.target.value, 10);
                  setNotasSize(novoTamanho);
                  setNotasPage(0);
                  if (grupoSelecionado) carregarNotas(grupoSelecionado, 0, novoTamanho);
                }}
                rowsPerPage={notasSize}
                rowsPerPageOptions={[10, 20, 50]}
              />
            </>
          )}

          <Divider sx={{ my: 2 }} />
          <Typography variant="caption" color="text.secondary">
            O PDF é salvo fisicamente no servidor, organizado pela pasta do CNPJ/CPF do destinatário. Ao imprimir, se o arquivo
            ainda não existir, ele é gerado automaticamente a partir dos dados da nota.
          </Typography>
        </DialogContent>
      </Dialog>
    </Box>
  );
}

export default DanfeCnpjPage;
