package pl.madzierski.daniel.exception;

import lombok.Getter;


@Getter
public class AppRuntimeException extends RuntimeException {

    private final AppRuntimeExceptionMessages type;

    public AppRuntimeException(AppRuntimeExceptionMessages type) {
        super(type.getI18nMessage());
        this.type = type;
    }

}