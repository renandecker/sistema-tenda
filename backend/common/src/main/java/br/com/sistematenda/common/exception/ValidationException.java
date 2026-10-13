package br.com.sistematenda.common.exception;

import java.util.List;

public class ValidationException extends BusinessException {

    private final List<ValidationError> errors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.errors = List.of();
    }

    public ValidationException(List<ValidationError> errors) {
        super("VALIDATION_ERROR", "Erro de validação");
        this.errors = errors;
    }

    public ValidationException(String message, List<ValidationError> errors) {
        super("VALIDATION_ERROR", message);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }
    }
}