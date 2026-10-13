package br.com.sistematenda.fruteira.label;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class LabelPdfService {

    private static final Logger LOG = Logger.getLogger(LabelPdfService.class);

    @Inject
    BarcodeService barcodeService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateEtiquetaSimples(EtiquetaData data) {
        String html = buildEtiquetaSimplesHtml(data);
        return htmlToPdf(html);
    }

    public byte[] generateEtiquetasLote(List<EtiquetaData> etiquetas, int colunas, int linhas) {
        StringBuilder html = new StringBuilder();
        html.append(buildHtmlHeader());
        html.append("<div class='page'>");
        
        for (int i = 0; i < etiquetas.size(); i++) {
            if (i > 0 && i % (colunas * linhas) == 0) {
                html.append("</div><div class='page'>");
            }
            html.append(buildEtiquetaHtml(etiquetas.get(i), colunas));
        }
        
        html.append("</div>");
        html.append(buildHtmlFooter());
        
        return htmlToPdf(html.toString());
    }

    private String buildEtiquetaSimplesHtml(EtiquetaData data) {
        StringBuilder html = new StringBuilder();
        html.append(buildHtmlHeader());
        html.append("<div class='page single'>");
        html.append(buildEtiquetaHtml(data, 1));
        html.append("</div>");
        html.append(buildHtmlFooter());
        return html.toString();
    }

    private String buildHtmlHeader() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @page { size: 100mm 150mm; margin: 0; }
                    body { margin: 0; padding: 0; font-family: 'DejaVu Sans', Arial, sans-serif; }
                    .page { display: flex; flex-wrap: wrap; width: 100%; height: 100%; }
                    .page.single { justify-content: center; align-items: center; }
                    .etiqueta { 
                        box-sizing: border-box; 
                        padding: 4mm; 
                        border: 1px solid #000; 
                        text-align: center; 
                        page-break-inside: avoid;
                    }
                    .etiqueta-codigo { font-size: 10pt; font-weight: bold; margin-bottom: 2mm; }
                    .etiqueta-nome { font-size: 9pt; margin-bottom: 2mm; word-wrap: break-word; }
                    .etiqueta-preco { font-size: 18pt; font-weight: bold; color: #000; margin-bottom: 2mm; }
                    .etiqueta-info { font-size: 7pt; color: #666; margin-top: 1mm; }
                    .barcode-img { margin: 2mm auto; }
                    .qrcode-img { margin: 2mm auto; width: 30mm; height: 30mm; }
                    .col-2 { width: 50%; }
                    .col-3 { width: 33.33%; }
                    .col-4 { width: 25%; }
                </style>
            </head>
            <body>
            """;
    }

    private String buildEtiquetaHtml(EtiquetaData data, int colunas) {
        String barcodeBase64 = barcodeService.generateCode128(data.codigoBarras, 200, 60);
        String qrCodeBase64 = barcodeService.generateQRCode(buildQrContent(data), 100);
        
        String colClass = switch (colunas) {
            case 2 -> "col-2";
            case 3 -> "col-3";
            case 4 -> "col-4";
            default -> "";
        };
        
        String validadeStr = data.validade != null ? data.validade.format(DATE_FORMATTER) : "N/A";
        String taraStr = data.tara != null ? String.format("%.3f kg", data.tara) : "N/A";
        
        return String.format("""
            <div class="etiqueta %s">
                <div class="etiqueta-codigo">%s</div>
                <div class="etiqueta-nome">%s</div>
                <div class="etiqueta-preco">R$ %.2f</div>
                <div class="barcode-img">
                    <img src="data:image/png;base64,%s" alt="%s">
                </div>
                <div class="qrcode-img">
                    <img src="data:image/png;base64,%s" alt="QR Code">
                </div>
                <div class="etiqueta-info">
                    Val: %s | Tara: %s | Un: %s
                </div>
            </div>
            """, 
            colClass,
            data.codigo,
            escapeHtml(data.nome),
            data.preco,
            barcodeBase64,
            escapeHtml(data.codigoBarras),
            qrCodeBase64,
            validadeStr,
            taraStr,
            data.unidadeMedida
        );
    }

    private String buildQrContent(EtiquetaData data) {
        return String.format("PROD:%s|NOME:%s|PRECO:%.2f|VALIDADE:%s|TARA:%.3f|UN:%s",
            data.codigo,
            data.nome,
            data.preco,
            data.validade != null ? data.validade.format(DATE_FORMATTER) : "",
            data.tara != null ? data.tara.doubleValue() : 0.0,
            data.unidadeMedida
        );
    }

    private String buildHtmlFooter() {
        return "</body></html>";
    }

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (IOException e) {
            LOG.error("Erro ao gerar PDF da etiqueta", e);
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&")
                   .replace("<", "<")
                   .replace(">", ">")
                   .replace("\"", """)
                   .replace("'", "'");
    }

    public static class EtiquetaData {
        public String codigo;
        public String codigoBarras;
        public String nome;
        public BigDecimal preco;
        public LocalDate validade;
        public BigDecimal tara;
        public String unidadeMedida;
    }
}