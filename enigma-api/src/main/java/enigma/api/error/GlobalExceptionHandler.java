package enigma.api.error;

import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.engine.exception.MachineNotConfiguredException;
import enigma.engine.exception.MachineNotLoadedException;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ConflictException;
import enigma.sessions.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
    public ResponseEntity<String> badRequest(Exception exception) {
        String message;
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException
                && methodArgumentNotValidException.getBindingResult().getFieldError() != null) {
            message = methodArgumentNotValidException.getBindingResult().getFieldError().getDefaultMessage();
        }
        else {
            message = exception.getMessage();
        }

        log.warn("Bad request handled: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> notFound(ResourceNotFoundException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
        return build(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> conflict(ConflictException exception) {
        log.warn("Conflict: {}", exception.getMessage());
        return build(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> unsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        log.warn("Unsupported media type: {}", exception.getMessage());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> notAcceptable(HttpMediaTypeNotAcceptableException exception) {
        log.warn("Not acceptable: {}", exception.getMessage());
        return build(HttpStatus.NOT_ACCEPTABLE, "Requested response media type is not supported");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> unexpected(Exception exception) {
        log.error("Unexpected server error", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ResponseEntity<String> build(HttpStatus status, String message) {
        String body = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_PLAIN)
                .body(body);
    }
}
