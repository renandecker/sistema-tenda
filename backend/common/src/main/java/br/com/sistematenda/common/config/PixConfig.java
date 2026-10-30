package br.com.sistematenda.common.config;

import br.com.sistematenda.common.enums.PixProvider;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "pix")
public interface PixConfig {

    @WithDefault("EFI")
    PixProvider provider();

    @WithDefault("true")
    boolean sandbox();

    @WithName("chave-pix")
    Optional<String> chavePix();

    @WithName("webhook-base-url")
    Optional<String> webhookBaseUrl();

    @WithName("expiracao-padrao-segundos")
    @WithDefault("3600")
    int expiracaoPadraoSegundos();

    /**
     * Tempo (em segundos) após o qual uma cobrança do provedor SIMULADO é
     * confirmada automaticamente. Use 0 para confirmar somente via webhook
     * manual em /api/pix/webhook/simulado.
     */
    @WithName("simulado-confirmar-segundos")
    @WithDefault("15")
    int simuladoConfirmarSegundos();

    Optional<EfiConfig> efi();

    @WithName("mercado-pago")
    Optional<MercadoPagoConfig> mercadoPago();

    Optional<AsaasConfig> asaas();

    Optional<ItauConfig> itau();

    Optional<BradescoConfig> bradesco();

    interface EfiConfig {

        @WithName("client-id")
        Optional<String> clientId();

        @WithName("client-secret")
        Optional<String> clientSecret();

        @WithName("certificado-p12-path")
        Optional<String> certificadoP12Path();

        @WithName("certificado-senha")
        Optional<String> certificadoSenha();

        @WithName("pix-certificado-path")
        Optional<String> pixCertificadoPath();
    }

    interface MercadoPagoConfig {

        @WithName("access-token")
        Optional<String> accessToken();

        @WithName("public-key")
        Optional<String> publicKey();

        @WithName("webhook-secret")
        Optional<String> webhookSecret();
    }

    interface AsaasConfig {

        @WithName("api-key")
        Optional<String> apiKey();

        @WithName("webhook-token")
        Optional<String> webhookToken();
    }

    interface ItauConfig {

        @WithName("client-id")
        Optional<String> clientId();

        @WithName("client-secret")
        Optional<String> clientSecret();

        @WithName("certificado-path")
        Optional<String> certificadoPath();

        @WithName("certificado-senha")
        Optional<String> certificadoSenha();

        @WithName("chave-pix")
        Optional<String> chavePix();

        @WithDefault("sandbox")
        String ambiente();
    }

    interface BradescoConfig {

        @WithName("client-id")
        Optional<String> clientId();

        @WithName("client-secret")
        Optional<String> clientSecret();

        @WithName("certificado-path")
        Optional<String> certificadoPath();

        @WithName("certificado-senha")
        Optional<String> certificadoSenha();

        @WithName("chave-pix")
        Optional<String> chavePix();

        @WithDefault("sandbox")
        String ambiente();
    }
}
