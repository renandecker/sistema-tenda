package br.com.sistematenda.common.pix;

import br.com.sistematenda.common.enums.PixProvider;

public interface PixProviderService {

    String getProviderName();

    PixProvider getProviderType();

    PixChargeResponse createCharge(PixChargeRequest request) throws PixException;

    PixChargeResponse getCharge(String txid) throws PixException;

    void cancelCharge(String txid) throws PixException;

    boolean verifyWebhookSignature(String payload, String signature, String timestamp) throws PixException;

    PixWebhookNotification parseWebhook(String payload, String signature, String timestamp) throws PixException;

    boolean supportsWebhook();

    String getWebhookUrl();

    default boolean isSandbox() {
        return false;
    }
}