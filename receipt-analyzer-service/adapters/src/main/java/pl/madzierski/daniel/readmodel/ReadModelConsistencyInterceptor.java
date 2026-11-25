package pl.madzierski.daniel.readmodel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.madzierski.daniel.event.ConsistencyTokens;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
class ReadModelConsistencyInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    static final String STALE_HEADER = "X-Read-Model-Stale";
    private static final long POLL_INTERVAL_MILLIS = 50;

    private final ReadModelProjector projector;

    @Value("${read-model.consistency-timeout:PT3S}")
    private Duration timeout;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws InterruptedException {
        String header = request.getHeader(ConsistencyTokens.HEADER);
        if (!HttpMethod.GET.matches(request.getMethod()) || header == null || header.isBlank()) return true;
        List<Token> tokens = parse(header);
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!caughtUp(tokens)) {
            if (System.nanoTime() >= deadline) {
                response.setHeader(STALE_HEADER, "true");
                return true;
            }
            Thread.sleep(POLL_INTERVAL_MILLIS);
        }
        return true;
    }

    private boolean caughtUp(List<Token> tokens) {
        return tokens.stream().allMatch(token -> projector.processedCount(token.correlationId()) >= token.eventCount());
    }

    private static List<Token> parse(String header) {
        return Arrays.stream(header.split(","))
            .map(ReadModelConsistencyInterceptor::parseToken)
            .filter(Objects::nonNull)
            .toList();
    }

    private static Token parseToken(String value) {
        String[] parts = value.trim().split(":");
        if (parts.length != 2) return null;
        try {
            return new Token(UUID.fromString(parts[0]), Integer.parseInt(parts[1]));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private record Token(UUID correlationId, int eventCount) {
    }
}
