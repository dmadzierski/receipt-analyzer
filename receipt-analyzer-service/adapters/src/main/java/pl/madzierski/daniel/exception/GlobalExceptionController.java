package pl.madzierski.daniel.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
class GlobalExceptionController {

    private static final String UNKNOWN_ERROR_CODE = "EXCEPTION.UNKNOWN_ERROR";

    @ExceptionHandler({AppRuntimeException.class})
    ResponseEntity<ErrorResponse> handleAppRuntimeException(AppRuntimeException ex, HttpServletRequest request) {
        log.warn("{} {} failed: {}", request.getMethod(), request.getRequestURI(), ex.getType().getI18nMessage());
        return errorResponse(ex.getType().getStatus(), ex.getType().getI18nMessage());
    }

    @ExceptionHandler({Exception.class})
    ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("{} {} failed", request.getMethod(), request.getRequestURI(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_CODE);
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String code) {
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse(code, status.value(), status.getReasonPhrase(), Instant.now()));
    }

    record ErrorResponse(String code, int status, String error, Instant timestamp) {
    }
}
