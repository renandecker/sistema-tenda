package br.com.sistematenda.fruteira.label;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@ApplicationScoped
public class BarcodeService {

    private static final Logger LOG = Logger.getLogger(BarcodeService.class);

    public String generateCode128(String content, int width, int height) {
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            return imageToBase64(image);
        } catch (WriterException e) {
            LOG.error("Erro ao gerar Code128", e);
            throw new RuntimeException("Erro ao gerar código de barras", e);
        }
    }

    public String generateQRCode(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            return imageToBase64(image);
        } catch (WriterException e) {
            LOG.error("Erro ao gerar QR Code", e);
            throw new RuntimeException("Erro ao gerar QR Code", e);
        }
    }

    public byte[] generateCode128Bytes(String content, int width, int height) {
        try {
            Code128Writer writer = new Code128Writer();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            return imageToBytes(image);
        } catch (WriterException e) {
            LOG.error("Erro ao gerar Code128", e);
            throw new RuntimeException("Erro ao gerar código de barras", e);
        }
    }

    public byte[] generateQRCodeBytes(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            return imageToBytes(image);
        } catch (WriterException e) {
            LOG.error("Erro ao gerar QR Code", e);
            throw new RuntimeException("Erro ao gerar QR Code", e);
        }
    }

    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            LOG.error("Erro ao converter imagem para Base64", e);
            throw new RuntimeException("Erro ao processar imagem", e);
        }
    }

    private byte[] imageToBytes(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            LOG.error("Erro ao converter imagem para bytes", e);
            throw new RuntimeException("Erro ao processar imagem", e);
        }
    }

    public String generateEtiquetaBalancaContent(String codigoProduto, BigDecimal preco, 
                                                  String validade, BigDecimal tara) {
        // Formato para balanças Toledo: código + preço + validade + tara
        // Exemplo: 000001|12.99|20241231|0.005
        return String.format("%s|%.2f|%s|%.3f", 
            codigoProduto, 
            preco, 
            validade != null ? validade : "", 
            tara != null ? tara.doubleValue() : 0.0);
    }
}