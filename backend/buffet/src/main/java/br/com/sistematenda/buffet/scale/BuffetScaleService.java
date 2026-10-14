package br.com.sistematenda.buffet.scale;

import com.fazecast.jSerialComm.SerialPort;
import br.com.sistematenda.common.config.ScaleConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ApplicationScoped
public class BuffetScaleService {

    private static final Logger LOG = Logger.getLogger(BuffetScaleService.class);

    @Inject
    ScaleConfig config;

    private SerialPort serialPort;
    private Thread readThread;
    private volatile boolean running = false;
    private Consumer<String> dataListener;

    public void connect() {
        if (serialPort != null && serialPort.isOpen()) {
            LOG.warn("Porta serial da balança do buffet já está aberta");
            return;
        }

        // Usar porta diferente da fruteira se configurado
        String port = config.port().replace("COM1", "COM2"); // Default to COM2 for buffet
        
        serialPort = SerialPort.getCommPort(port);
        serialPort.setBaudRate(config.baudRate());
        serialPort.setNumDataBits(config.dataBits());
        serialPort.setNumStopBits(config.stopBits());
        serialPort.setParity(getParity(config.parity()));
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, config.timeoutMs(), 0);

        if (serialPort.openPort()) {
            LOG.info("Conectado à balança do buffet na porta " + port);
            startReading();
        } else {
            LOG.error("Falha ao conectar à balança do buffet na porta " + port);
            throw new RuntimeException("Não foi possível conectar à balança do buffet");
        }
    }

    public void disconnect() {
        running = false;
        if (readThread != null) {
            readThread.interrupt();
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            LOG.info("Desconectado da balança do buffet");
        }
    }

    public void setDataListener(Consumer<String> listener) {
        this.dataListener = listener;
    }

    public CompletableFuture<String> readWeight() {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        Consumer<String> tempListener = data -> {
            future.complete(data);
            setDataListener(this.dataListener);
        };
        
        Consumer<String> previousListener = this.dataListener;
        setDataListener(tempListener);
        
        sendCommand(getReadCommand());
        
        future.orTimeout(config.timeoutMs(), TimeUnit.MILLISECONDS)
              .exceptionally(ex -> {
                  setDataListener(previousListener);
                  return null;
              });
        
        return future;
    }

    public void sendCommand(String command) {
        if (serialPort == null || !serialPort.isOpen()) {
            throw new IllegalStateException("Porta serial não está aberta");
        }
        
        try (OutputStream output = serialPort.getOutputStream()) {
            byte[] cmdBytes = (command + "\r\n").getBytes(StandardCharsets.US_ASCII);
            output.write(cmdBytes);
            output.flush();
            LOG.debugf("Comando enviado para balança buffet: %s", command);
        } catch (Exception e) {
            LOG.error("Erro ao enviar comando para balança do buffet", e);
            throw new RuntimeException("Erro ao comunicar com balança do buffet", e);
        }
    }

    private void startReading() {
        running = true;
        readThread = new Thread(() -> {
            try (InputStream input = serialPort.getInputStream()) {
                byte[] buffer = new byte[1024];
                StringBuilder sb = new StringBuilder();
                
                while (running && serialPort.isOpen()) {
                    int bytesRead = input.read(buffer);
                    if (bytesRead > 0) {
                        String data = new String(buffer, 0, bytesRead, StandardCharsets.US_ASCII);
                        sb.append(data);
                        
                        int newlineIndex;
                        while ((newlineIndex = sb.indexOf("\n")) != -1) {
                            String line = sb.substring(0, newlineIndex).trim();
                            sb.delete(0, newlineIndex + 1);
                            
                            if (!line.isEmpty() && dataListener != null) {
                                dataListener.accept(line);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (running) {
                    LOG.error("Erro na leitura da balança do buffet", e);
                }
            }
        });
        readThread.setDaemon(true);
        readThread.start();
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
        return switch (config.protocol().toUpperCase()) {
            case "TOLEDO" -> "P";
            case "FILIZOLA" -> "W";
            case "URANO" -> "S";
            default -> "P";
        };
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
}