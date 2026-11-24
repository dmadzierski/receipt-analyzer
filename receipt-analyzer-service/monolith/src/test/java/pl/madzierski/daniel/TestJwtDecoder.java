package pl.madzierski.daniel;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;

@TestConfiguration
public class TestJwtDecoder {

    static final String INVALID_TOKEN = "invalid";

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            if (INVALID_TOKEN.equals(token)) throw new BadJwtException("invalid token");
            return Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject(token)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        };
    }
}
