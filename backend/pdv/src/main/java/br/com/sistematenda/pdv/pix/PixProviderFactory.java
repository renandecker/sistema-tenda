package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.config.PixConfig;
import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.pix.PixProviderService;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PixProviderFactory {

    private static final Logger LOG = Logger.getLogger(PixProviderFactory.class);

    @Inject
    PixConfig config;

    @Inject
    EfiPixProvider efiProvider;

    @Inject
    MercadoPagoPixProvider mercadoPagoProvider;

    @Inject
    AsaasPixProvider asaasProvider;

    @Inject
    OpenFinancePixProvider openFinanceProvider;

    @Inject
    SimuladoPixProvider simuladoProvider;

    public PixProviderService getProvider() {
        return getProvider(config.provider());
    }

    public PixProviderService getProvider(PixProvider providerType) {
        return switch (providerType) {
            case EFI -> {
                if (!isEfiConfigured()) {
                    throw new IllegalStateException("Configuração da Efí não encontrada");
                }
                LOG.info("Usando provedor PIX: Efí (Gerencianet)");
                yield efiProvider;
            }
            case MERCADO_PAGO -> {
                if (!isMercadoPagoConfigured()) {
                    throw new IllegalStateException("Configuração do Mercado Pago não encontrada");
                }
                LOG.info("Usando provedor PIX: Mercado Pago");
                yield mercadoPagoProvider;
            }
            case ASAAS -> {
                if (!isAsaasConfigured()) {
                    throw new IllegalStateException("Configuração do Asaas não encontrada");
                }
                LOG.info("Usando provedor PIX: Asaas");
                yield asaasProvider;
            }
            case ITAU, BRADESCO, SICOOB, SICREDI -> {
                if (!isOpenFinanceConfigured()) {
                    throw new IllegalStateException("Configuração Open Finance (Itaú/Bradesco) não encontrada");
                }
                LOG.info("Usando provedor PIX: Open Finance (" + providerType.getDescricao() + ")");
                yield openFinanceProvider;
            }
            case SIMULADO -> {
                LOG.warn("Usando provedor PIX SIMULADO: nenhuma cobrança real será feita");
                yield simuladoProvider;
            }
        };
    }

    public PixProviderService getProviderOrDefault() {
        try {
            return getProvider();
        } catch (IllegalStateException e) {
            LOG.warn("Provedor PIX configurado não disponível: " + e.getMessage());

            PixProviderService fallback = getFirstConfiguredProvider();
            if (fallback != null) {
                return fallback;
            }

            if (isDevOrTestProfile()) {
                LOG.warn("Nenhum PSP PIX configurado. Usando o provedor SIMULADO em ambiente de desenvolvimento; "
                        + "nenhuma cobrança real será feita.");
                return simuladoProvider;
            }

            throw new IllegalStateException("Nenhum provedor PIX configurado. Configure as credenciais do PSP "
                    + "(ex.: PIX_EFI_CLIENT_ID/PIX_EFI_CLIENT_SECRET) ou defina PIX_PROVIDER=SIMULADO para testes.");
        }
    }

    private PixProviderService getFirstConfiguredProvider() {
        if (isEfiConfigured()) {
            LOG.info("Fallback PIX: Efí (Gerencianet)");
            return efiProvider;
        }
        if (isMercadoPagoConfigured()) {
            LOG.info("Fallback PIX: Mercado Pago");
            return mercadoPagoProvider;
        }
        if (isAsaasConfigured()) {
            LOG.info("Fallback PIX: Asaas");
            return asaasProvider;
        }
        if (isOpenFinanceConfigured()) {
            LOG.info("Fallback PIX: Open Finance");
            return openFinanceProvider;
        }
        return null;
    }

    private boolean isEfiConfigured() {
        return config.efi()
                .map(efi -> efi.clientId().isPresent() && efi.clientSecret().isPresent())
                .orElse(false);
    }

    private boolean isMercadoPagoConfigured() {
        return config.mercadoPago()
                .map(mercadoPago -> mercadoPago.accessToken().isPresent())
                .orElse(false);
    }

    private boolean isAsaasConfigured() {
        return config.asaas()
                .map(asaas -> asaas.apiKey().isPresent())
                .orElse(false);
    }

    private boolean isOpenFinanceConfigured() {
        boolean itauConfigurado = config.itau()
                .map(itau -> itau.clientId().isPresent() && itau.certificadoPath().isPresent())
                .orElse(false);
        boolean bradescoConfigurado = config.bradesco()
                .map(bradesco -> bradesco.clientId().isPresent() && bradesco.certificadoPath().isPresent())
                .orElse(false);
        return itauConfigurado || bradescoConfigurado;
    }

    private boolean isDevOrTestProfile() {
        return LaunchMode.current().isDevOrTest();
    }
}
