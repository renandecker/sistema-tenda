package br.com.sistematenda.fruteira.scale;

import com.fazecast.jSerialComm.SerialPort;
import br.com.sistematenda.fruteira.entity.Produto;
import br.com.sistematenda.fruteira.entity.EtiquetaBalanca;
import br.com.sistematenda.fruteira.service.ProdutoService;
import br.com.sistematenda.common.enums.UnidadeMedida;
import br.com.sistematenda.common.exception.BusinessException;
import br.com.sistematenda.common.exception.NotFoundException;
import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ApplicationScoped
public class ScaleService {

    private static final Logger LOG = Logger.getLogger(ScaleService.class);

    @Inject
    ScaleConfig config;

    @Inject
    ProdutoService produtoService;

    private SerialPort serialPort;
    private Thread readThread;
    private volatile boolean running = false;
    private Consumer<String> dataListener;
    private boolean modeAuto = true; // true = automático (buffet), false = manual (hortifrúti)

    public void setAutoMode(boolean auto) {
        this.modeAuto = auto;
    }

    public void connect() {
        if (serialPort != null && serialPort.isOpen()) {
            LOG.warn("Porta serial já está aberta");
            return;
        }

        serialPort = SerialPort.getCommPort(config.port);
        serialPort.setBaudRate(config.baudRate);
        serialPort.setNumDataBits(config.dataBits);
        serialPort.setNumStopBits(config.stopBits);
        serialPort.setParity(getParity(config.parity));
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, config.timeoutMs, 0);

        if (serialPort.openPort()) {
            LOG.info("Conectado à balança na porta " + config.port);
            startReading();
        } else {
            LOG.error("Falha ao conectar à balança na porta " + config.port);
            throw new RuntimeException("Não foi possível conectar à balança");
        }
    }

    public void disconnect() {
        running = false;
        if (readThread != null) {
            readThread.interrupt();
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            LOG.info("Desconectado da balança");
        }
    }

    public void setDataListener(Consumer<String> listener) {
        this.dataListener = listener;
    }

    public void setProduct(UUID produtoId) {
        // Define o produto atual para balança
        // Em modo manual, o produto precisa ser definido antes da pesagem
    }

    public CompletableFuture<String> readWeight() {
        CompletableFuture<String> future = new CompletableFuture<>();

        Consumer<String> tempListener = data -> {
            processWeightData(data);
            future.complete(data);
            setDataListener(this.dataListener);
        };

        Consumer<String> previousListener = this.dataListener;
        setDataListener(tempListener);

        sendCommand(getReadCommand());

        future.orTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
              .exceptionally(ex -> {
                  setDataListener(previousListener);
                  return null;
              });

        return future;
    }

    private void processWeightData(String rawData) {
        if (rawData == null || rawData.trim().isEmpty()) {
            return;
        }

        String trimmed = rawData.trim();
        LOG.debugf("Dados da balança: %s", trimmed);

        // Parse do peso dependendo do protocolo
        BigDecimal pesoLiquido = null;
        String codigoProduto = null;
        BigDecimal precoUnitario = null;
        String observacao = null;

        try {
            // Tentar parse baseado no protocolo configurado
            switch (config.protocol.toUpperCase()) {
                case "TOLEDO":
                    pesoLiquido = parseToledo(trimmed);
                    break;
                case "FILIZOLA":
                    pesoLiquido = parseFilizola(trimmed);
                    break;
                case "URANO":
                    pesoLiquido = parseUrano(trimmed);
                    break;
                default:
                    // Parse genérico
                    pesoLiquido = parseGeneric(trimmed);
                    break;
            }

            if (pesoLiquido != null && pesoLiquido.compareTo(BigDecimal.ZERO) > 0) {
                // Em modo automático (buffet), associar ao último produto do caixa
                if (modeAuto) {
                    // Modo automático: gerar comprovante automático
                    // TODO: Associar à comanda/PDV atual
                    emitirComprovanteAutomatico(pesoLiquido);
                } else {
                    // Modo manual (hortifrúti): aguardar definição do caixa
                    observacao = "PESAGEM MANUAL - Definir produto no PDV";
                }
            }

        } catch (Exception e) {
            LOG.error("Erro ao processar dados da balança", e);
        }
    }

    // Parse protocols
    private BigDecimal parseToledo(String data) {
        // Padrão Toledo: peso[ST|ND] unidade
        try {
            // Exemplo: "1,525 kg ST" ou "1.525 kg"
            String[] parts = data.split("\\s+");
            if (parts.length >= 2) {
                String pesoStr = parts[0].replace(",", ".");
                BigDecimal peso = new BigDecimal(pesoStr);
                String unidade = parts[parts.length - 1].toLowerCase();
                if ("kg".equals(unidade) || "".equals(unidade)) {
                    return peso;
                } else if ("g".equals(unidade)) {
                    return peso.divide(new BigDecimal("1000"));
                }
            }
        } catch (Exception e) {
            LOG.error("Erro ao parsear Toledo", e);
        }
        return null;
    }

    private BigDecimal parseFilizola(String data) {
        // Padrão Filizola: ST peso
        try {
            // Exemplo: "ST 1,525" ou "Peso: 1.525"
            String cleaned = data.replace("ST", "").replace("st", "").trim();
            String[] parts = cleaned.split("\\s+");
            if (parts.length >= 1) {
                String pesoStr = parts[0].replace(",", ".");
                return new BigDecimal(pesoStr);
            }
        } catch (Exception e) {
            LOG.error("Erro ao parsear Filizola", e);
        }
        return null;
    }

    private BigDecimal parseUrano(String data) {
        // Padrão Urano: peso[unit]
        try {
            String[] parts = data.split("\\s+");
            if (parts.length >= 1) {
                String pesoStr = parts[0].replace(",", ".");
                return new BigDecimal(pesoStr);
            }
        } catch (Exception e) {
            LOG.error("Erro ao parsear Urano", e);
        }
        return null;
    }

    private BigDecimal parseGeneric(String data) {
        // Parse genérico: tentar extrair número
        try {
            String numeric = data.replaceAll("[^,-0-9]", "").replace(",", ".");
            if (!numeric.isEmpty()) {
                return new BigDecimal(numeric);
            }
        } catch (Exception e) {
            LOG.error("Erro ao parsear genérico", e);
        }
        return null;
    }

    private void emitirComprovanteAutomatico(BigDecimal pesoLiquido) {
        // Em modo automático, gerar comprovante automático
        // Buscar produto ativo mais recente ou do PDV
        // TODO: Implementar lógica de associação à comanda/PDV
        
        // Por enquanto, apenas logar
        LOG.info("Pesagem automática: {:.3f} kg - Comprovante a ser gerado", pesoLiquido);
    }

    public void sendCommand(String command) {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Porta serial não está aberta");
        }

        try (OutputStream output = serialPort.getOutputStream()) {
            byte[] cmdBytes = (command + "\r\n").getBytes(StandardCharsets.US_ASCII);
            output.write(cmdBytes);
            output.flush();
            LOG.debugf("Comando enviado: %s", command);
        } catch (Exception e) {
            LOG.error("Erro ao enviar comando para balança", e);
            throw new RuntimeException("Erro ao comunicar com balança", e);
        }
    }

    private int getParity(String parity) {
        return switch (parity.toUpperCase()) {
            case "ODD" -> SerialPort.ODD_PARITY;
            case "EVEN" -> SerialPort.EVEN_PARITY;
            case "MARK" -> SerialPort.MARK_PARITY;
            case "SPACE" -> SerialPort.SPACE_PARITY;
            default -> SerialPort.NO_PARITY;
        };
    }

    private String getReadCommand() {
        return switch (config.protocol.toUpperCase()) {
            case "TOLEDO" -> "P";
            case "FILIZOLA" -> "W";
            case "URANO" -> "S";
            case "BALANCA_GENERICA" -> "R";
            default -> "P";
        };
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    public boolean isModeAuto() {
        return modeAuto;
    }

    public boolean isModeManual() {
        return !modeAuto;
    }
}