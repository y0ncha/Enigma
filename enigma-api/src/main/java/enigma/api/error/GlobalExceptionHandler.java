package enigma.api.error;

import enigma.api.dto.error.ApiErrorResponse;
import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.engine.exception.MachineNotConfiguredException;
import enigma.engine.exception.MachineNotLoadedException;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ConflictException;
import enigma.sessions.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ApiValidationException.class,
            InvalidConfigurationException.class,
            InvalidMessageException.class,
            MachineNotConfiguredException.class,
            MachineNotLoadedException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> badRequest(Exception exception, HttpServletRequest request) {
        String message;
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException
                && methodArgumentNotValidException.getBindingResult().getFieldError() != null) {
            message = methodArgumentNotValidException.getBindingResult().getFieldError().getDefaultMessage();
        }
        else {
            message = exception.getMessage();
        }

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> conflict(ConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
