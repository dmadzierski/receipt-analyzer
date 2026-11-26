package pl.madzierski.daniel.readmodel.consistency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import pl.madzierski.daniel.readmodel.ReadModelProjector;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
class ReadModelConsistencyInterceptor implements HandlerInterceptor {

    static final String STALE_HEADER = "X-Read-Model-Stale";
    private static final long POLL_INTERVAL_MILLIS = 50;
    private static final int MAX_TOKENS = 10;

    private final ReadModelProjector projector;
    private final Duration timeout;

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
            .limit(MAX_TOKENS)
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
