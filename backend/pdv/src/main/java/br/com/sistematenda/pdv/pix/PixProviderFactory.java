package br.com.sistematenda.pdv.pix;

import br.com.sistematenda.common.config.PixConfig;
import br.com.sistematenda.common.enums.PixProvider;
import br.com.sistematenda.common.pix.PixProviderService;
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

    public PixProviderService getProvider() {
        return getProvider(config.provider());
    }

    public PixProviderService getProvider(PixProvider providerType) {
        return switch (providerType) {
            case EFI -> {
                if (!config.efi().isPresent()) {
                    throw new IllegalStateException("Configuração da Efí não encontrada");
                }
                LOG.info("Usando provedor PIX: Efí (Gerencianet)");
                yield efiProvider;
            }
            case MERCADO_PAGO -> {
                if (!config.mercadoPago().isPresent()) {
                    throw new IllegalStateException("Configuração do Mercado Pago não encontrada");
                }
                LOG.info("Usando provedor PIX: Mercado Pago");
                yield mercadoPagoProvider;
            }
            case ASAAS -> {
                if (!config.asaas().isPresent()) {
                    throw new IllegalStateException("Configuração do Asaas não encontrada");
                }
                LOG.info("Usando provedor PIX: Asaas");
                yield asaasProvider;
            }
            case ITAU, BRADESCO, SICOOB, SICREDI -> {
                if (!config.itau().isPresent() && !config.bradesco().isPresent()) {
                    throw new IllegalStateException("Configuração Open Finance (Itaú/Bradesco) não encontrada");
                }
                LOG.info("Usando provedor PIX: Open Finance (" + providerType.getDescricao() + ")");
                yield openFinanceProvider;
            }
        };
    }

    public PixProviderService getProviderOrDefault() {
        try {
            return getProvider();
        } catch (IllegalStateException e) {
            LOG.warn("Provedor PIX configurado não disponível, tentando fallback para Efí: " + e.getMessage());
            if (config.efi().isPresent()) {
                return efiProvider;
            }
            if (config.mercadoPago().isPresent()) {
                return mercadoPagoProvider;
            }
            if (config.asaas().isPresent()) {
                return asaasProvider;
            }
            throw new IllegalStateException("Nenhum provedor PIX configurado");
        }
    }
}