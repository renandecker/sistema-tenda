package br.com.sistematenda.common.pix;

public class PixException extends RuntimeException {

    private final String providerCode;
    private final String providerMessage;
    private final int httpStatus;

    public PixException(String message) {
        super(message);
        this.providerCode = null;
        this.providerMessage = message;
        this.httpStatus = 500;
    }

    public PixException(String message, Throwable cause) {
        super(message, cause);
        this.providerCode = null;
        this.providerMessage = message;
        this.httpStatus = 500;
    }

    public PixException(String providerCode, String providerMessage, int httpStatus) {
        super("[" + providerCode + "] " + providerMessage);
        this.providerCode = providerCode;
        this.providerMessage = providerMessage;
        this.httpStatus = httpStatus;
    }

    public PixException(String providerCode, String providerMessage, int httpStatus, Throwable cause) {
        super("[" + providerCode + "] " + providerMessage, cause);
        this.providerCode = providerCode;
        this.providerMessage = providerMessage;
        this.httpStatus = httpStatus;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getProviderMessage() {
        return providerMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isRetryable() {
        return httpStatus >= 500 || httpStatus == 429;
    }
}