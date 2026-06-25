package pl.madzierski.daniel.exception

class AppRuntimeException(val type: AppRuntimeExceptionMessages) : RuntimeException(type.i18nMessage)