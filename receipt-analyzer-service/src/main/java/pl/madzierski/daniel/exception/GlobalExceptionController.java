package pl.madzierski.daniel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler({AppRuntimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)

    public ResponseEntity<String> handleAppRuntimeException(AppRuntimeException ex) {
        return ResponseEntity.status(ex.getType().getStatus()).body(ex.getType().getI18nMessage());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)

    public ResponseEntity<String> handleException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
    }
}
