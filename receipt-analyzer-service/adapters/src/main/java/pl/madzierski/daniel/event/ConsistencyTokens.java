package pl.madzierski.daniel.event;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ConsistencyTokens {

    static final String HEADER = "X-Consistency-Token";
    private static final String ATTRIBUTE = ConsistencyTokens.class.getName() + ".tokens";

    void add(UUID correlationId, int eventCount) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null || eventCount == 0) return;
        @SuppressWarnings("unchecked")
        List<String> tokens = (List<String>) attributes.getAttribute(ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (tokens == null) {
            tokens = new ArrayList<>();
            attributes.setAttribute(ATTRIBUTE, tokens, RequestAttributes.SCOPE_REQUEST);
        }
        tokens.add(correlationId + ":" + eventCount);
    }

    String current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        @SuppressWarnings("unchecked")
        List<String> tokens = (List<String>) attributes.getAttribute(ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return tokens == null || tokens.isEmpty() ? null : String.join(",", tokens);
    }
}
