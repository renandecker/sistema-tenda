package br.com.sistematenda.fruteira.resource;

import br.com.sistematenda.fruteira.label.BarcodeService;
import br.com.sistematenda.fruteira.label.LabelPdfService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Path("/fruteira/etiquetas")
@Produces(MediaType.APPLICATION_JSON)
public class EtiquetaImpressaoResource {

    @Inject
    BarcodeService barcodeService;

    @Inject
    LabelPdfService labelPdfService;

    @POST
    @Path("/codigo-barras")
    @Produces(MediaType.APPLICATION_JSON)
    public BarcodeResponse gerarCodigoBarras(@QueryParam("conteudo") String conteudo,
                                              @QueryParam("largura") @DefaultValue("300") int largura,
                                              @QueryParam("altura") @DefaultValue("100") int altura) {
        String base64 = barcodeService.generateCode128(conteudo, largura, altura);
        return new BarcodeResponse(base64, "CODE128");
    }

    @POST
    @Path("/qr-code")
    @Produces(MediaType.APPLICATION_JSON)
    public BarcodeResponse gerarQrCode(@QueryParam("conteudo") String conteudo,
                                        @QueryParam("tamanho") @DefaultValue("200") int tamanho) {
        String base64 = barcodeService.generateQRCode(conteudo, tamanho);
        return new BarcodeResponse(base64, "QR_CODE");
    }

    @POST
    @Path("/pdf/simples")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response gerarPdfSimples(EtiquetaRequest request) {
        LabelPdfService.EtiquetaData data = buildEtiquetaData(request);
        byte[] pdf = labelPdfService.generateEtiquetaSimples(data);
        
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=etiqueta_" + request.codigo + ".pdf")
                .build();
    }

    @POST
    @Path("/pdf/lote")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response gerarPdfLote(LoteEtiquetasRequest request) {
        List<LabelPdfService.EtiquetaData> etiquetas = request.etiquetas.stream()
                .map(this::buildEtiquetaData)
                .toList();
        
        byte[] pdf = labelPdfService.generateEtiquetasLote(etiquetas, request.colunas, request.linhas);
        
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=etiquetas_lote.pdf")
                .build();
    }

    @GET
    @Path("/pdf/etiqueta-balanca/{produtoId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response gerarEtiquetaBalanca(@PathParam("produtoId") java.util.UUID produtoId,
                                          @QueryParam("preco") BigDecimal preco,
                                          @QueryParam("validade") LocalDate validade,
                                          @QueryParam("tara") BigDecimal tara) {
        // TODO: buscar produto do banco
        LabelPdfService.EtiquetaData data = new LabelPdfService.EtiquetaData();
        data.codigo = "000001"; // TODO: buscar código da balança
        data.codigoBarras = "7891234567890"; // TODO: buscar código de barras
        data.nome = "Produto Teste"; // TODO: buscar nome
        data.preco = preco != null ? preco : BigDecimal.ZERO;
        data.validade = validade;
        data.tara = tara;
        data.unidadeMedida = "kg";
        
        byte[] pdf = labelPdfService.generateEtiquetaSimples(data);
        
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=etiqueta_balanca_" + produtoId + ".pdf")
                .build();
    }

    private LabelPdfService.EtiquetaData buildEtiquetaData(EtiquetaRequest request) {
        LabelPdfService.EtiquetaData data = new LabelPdfService.EtiquetaData();
        data.codigo = request.codigo;
        data.codigoBarras = request.codigoBarras;
        data.nome = request.nome;
        data.preco = request.preco;
        data.validade = request.validade;
        data.tara = request.tara;
        data.unidadeMedida = request.unidadeMedida;
        return data;
    }

    public static class BarcodeResponse {
        public String base64;
        public String tipo;

        public BarcodeResponse(String base64, String tipo) {
            this.base64 = base64;
            this.tipo = tipo;
        }
    }

    public static class EtiquetaRequest {
        public String codigo;
        public String codigoBarras;
        public String nome;
        public BigDecimal preco;
        public LocalDate validade;
        public BigDecimal tara;
        public String unidadeMedida;
    }

    public static class LoteEtiquetasRequest {
        public List<EtiquetaRequest> etiquetas;
        public int colunas = 2;
        public int linhas = 5;
    }
}