package pl.madzierski.daniel.event;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
class ConsistencyTokenFilter extends OncePerRequestFilter {

    private static final Set<HttpMethod> COMMAND_METHODS = Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE);

    private final ConsistencyTokens consistencyTokens;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !COMMAND_METHODS.contains(HttpMethod.valueOf(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
            String token = consistencyTokens.current();
            if (token != null) wrapper.setHeader(ConsistencyTokens.HEADER, token);
        } finally {
            wrapper.copyBodyToResponse();
        }
    }
}
