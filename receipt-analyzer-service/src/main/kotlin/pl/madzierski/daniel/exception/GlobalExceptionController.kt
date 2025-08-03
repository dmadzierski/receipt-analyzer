package pl.madzierski.daniel.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus


@ControllerAdvice
class GlobalExceptionController {


    @ExceptionHandler(AppRuntimeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleAppRuntimeException(ex: AppRuntimeException) =
        ResponseEntity.status(ex.type.status).body(ex.type.i18nMessage)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): ResponseEntity<String> {
        ex.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: ${ex.message}")
    }

}