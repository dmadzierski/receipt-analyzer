package pl.madzierski.daniel.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTypes {

    private static final String BASE_PACKAGE = "pl.madzierski.daniel";
    private static final Map<String, Class<? extends IntegrationEvent>> TYPES = scan();

    public static String typeOf(IntegrationEvent event) {
        String type = event.getClass().getSimpleName();
        if (!TYPES.containsKey(type)) throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_TYPE_UNKNOWN);
        return type;
    }

    public static Class<? extends IntegrationEvent> classOf(String type) {
        Class<? extends IntegrationEvent> eventClass = TYPES.get(type);
        if (eventClass == null) throw new AppRuntimeException(AppRuntimeExceptionMessages.EVENT_TYPE_UNKNOWN);
        return eventClass;
    }

    private static Map<String, Class<? extends IntegrationEvent>> scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(IntegrationEvent.class));
        return scanner.findCandidateComponents(BASE_PACKAGE).stream()
            .map(candidate -> ClassUtils.resolveClassName(candidate.getBeanClassName(), EventTypes.class.getClassLoader()))
            .<Class<? extends IntegrationEvent>>map(eventClass -> eventClass.asSubclass(IntegrationEvent.class))
            .collect(Collectors.toUnmodifiableMap(Class::getSimpleName, Function.identity()));
    }
}
