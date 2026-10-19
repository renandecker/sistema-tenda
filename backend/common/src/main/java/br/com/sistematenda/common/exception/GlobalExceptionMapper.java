package br.com.sistematenda.common.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof BusinessException businessException) {
            LOG.debug("Erro de negócio: " + businessException.getMessage());
            return buildBusinessErrorResponse(businessException);
        }

        if (exception instanceof jakarta.validation.ConstraintViolationException validationException) {
            LOG.debug("Erro de validação: " + validationException.getMessage());
            return buildValidationErrorResponse(validationException);
        }

        if (exception instanceof jakarta.ws.rs.WebApplicationException webApplicationException) {
            LOG.debug("Erro web: " + webApplicationException.getMessage());
            return webApplicationException.getResponse();
        }

        LOG.error("Erro não tratado", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor"))
                .build();
    }

    private Response buildBusinessErrorResponse(BusinessException exception) {
        int status = determineStatus(exception);
        ErrorResponse error = new ErrorResponse(
                exception.getErrorCode() != null ? exception.getErrorCode() : "BUSINESS_ERROR",
                exception.getMessage()
        );

        if (exception instanceof ValidationException validationException && !validationException.getErrors().isEmpty()) {
            error.setDetails(validationException.getErrors().stream()
                    .map(e -> e.getField() + ": " + e.getMessage())
                    .collect(Collectors.joining("; ")));
        }

        return Response.status(status).entity(error).build();
    }

    private Response buildValidationErrorResponse(jakarta.validation.ConstraintViolationException exception) {
        String details = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "Erro de validação");
        error.setDetails(details);

        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    private int determineStatus(BusinessException exception) {
        String code = exception.getErrorCode();
        if (code == null) return Response.Status.BAD_REQUEST.getStatusCode();

        return switch (code) {
            case "NOT_FOUND" -> Response.Status.NOT_FOUND.getStatusCode();
            case "VALIDATION_ERROR" -> Response.Status.BAD_REQUEST.getStatusCode();
            case "UNAUTHORIZED" -> Response.Status.UNAUTHORIZED.getStatusCode();
            case "FORBIDDEN" -> Response.Status.FORBIDDEN.getStatusCode();
            case "CONFLICT" -> Response.Status.CONFLICT.getStatusCode();
            default -> Response.Status.BAD_REQUEST.getStatusCode();
        };
    }

    public static class ErrorResponse {
        private String code;
        private String message;
        private String details;

        public ErrorResponse() {}

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}