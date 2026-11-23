package pl.madzierski.daniel.exception;

import lombok.Getter;

@Getter
public enum AppIllegalStateExceptionMessages {
    REVISION_NOT_USER_RESOLVER("EXCEPTION.REVISION_NOT_USER_RESOLVER"),
    REVISION_NOT_PARENT("EXCEPTION.REVISION_NOT_PARENT"),
    REVISION_NOT_OF_RECEIPT("EXCEPTION.REVISION_NOT_OF_RECEIPT"),
    BLANK_NAME("EXCEPTION.BLANK_NAME"),
    FILE_TYPE_MISMATCH("EXCEPTION.FILE_TYPE_MISMATCH");

    private final String i18nMessage;

    AppIllegalStateExceptionMessages(String i18nMessage) {
        this.i18nMessage = i18nMessage;
    }
}
