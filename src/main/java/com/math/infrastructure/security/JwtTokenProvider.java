package com.math.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Implementación de {@link TokenProvider} con la librería <b>jjwt</b> (HMAC-SHA256).
 *
 * <pre>
 *  header.payload.signature
 *  payload = { "sub": "admin", "iat": 1727200000, "exp": 1727203600 }
 *  signature = HMAC-SHA256(header.payload, JWT_SECRET)
 * </pre>
 *
 * <p>Si alguien cambia el payload, la firma deja de coincidir y {@link #validate} devuelve vacío.</p>
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    /** HS256 exige una clave de al menos 256 bits (32 bytes). */
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationSeconds;
    private final Clock clock;

    /**
     * Constructor usado por Spring (reloj del sistema).
     *
     * <p>{@code @Autowired} es obligatorio aquí: con dos constructores, Spring no sabe cuál usar
     * y buscaría uno vacío que no existe.</p>
     *
     * @param properties secreto y expiración
     */
    @Autowired
    public JwtTokenProvider(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    /**
     * Constructor que permite fijar el reloj en los tests.
     *
     * @param properties secreto y expiración
     * @param clock      reloj para calcular fechas de emisión y expiración
     * @throws IllegalStateException si el secreto es demasiado corto
     */
    JwtTokenProvider(JwtProperties properties, Clock clock) {
        byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.security.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.expirationSeconds = properties.expirationSeconds();
        this.clock = clock;
    }

    @Override
    public String generate(String subject) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<String> validate(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public long expirationSeconds() {
        return expirationSeconds;
    }
}
