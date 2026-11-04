package pl.madzierski.daniel.exception;

import lombok.Getter;

@Getter
public class AppIllegalStateException extends IllegalStateException {

    private final AppIllegalStateExceptionMessages type;

    public AppIllegalStateException(AppIllegalStateExceptionMessages type) {
        super(type.getI18nMessage());
        this.type = type;
    }
}
