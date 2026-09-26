package com.math.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "a-test-secret-that-is-at-least-32-bytes-long";
    private static final Instant NOW = Instant.parse("2026-09-25T12:00:00Z");

    private final JwtTokenProvider provider =
            new JwtTokenProvider(new JwtProperties(SECRET, 60), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void generatedTokensValidateBackToTheSubject() {
        String token = provider.generate("admin");

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(provider.validate(token)).contains("admin");
        assertThat(provider.expirationSeconds()).isEqualTo(60);
    }

    @Test
    void rejectsExpiredTokens() {
        String token = provider.generate("admin");
        JwtTokenProvider later = new JwtTokenProvider(
                new JwtProperties(SECRET, 60), Clock.fixed(NOW.plusSeconds(61), ZoneOffset.UTC));

        assertThat(later.validate(token)).isEmpty();
    }

    @Test
    void rejectsTokensSignedWithAnotherSecret() {
        JwtTokenProvider other = new JwtTokenProvider(
                new JwtProperties("another-secret-that-is-at-least-32-bytes", 60), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(provider.validate(other.generate("admin"))).isEmpty();
    }

    @Test
    void rejectsGarbage() {
        assertThat(provider.validate("not.a.jwt")).isEmpty();
        assertThat(provider.validate("")).isEmpty();
    }

    @Test
    void refusesShortSecrets() {
        assertThatThrownBy(() -> new JwtTokenProvider(new JwtProperties("short", 60)))
                .isInstanceOf(IllegalStateException.class);
    }
}
