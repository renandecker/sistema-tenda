package br.com.sistematenda.common.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public NotFoundException(String resource, Object id) {
        super("NOT_FOUND", String.format("%s não encontrado com ID: %s", resource, id));
    }

    public NotFoundException(Class<?> resourceClass, Object id) {
        this(resourceClass.getSimpleName(), id);
    }
}