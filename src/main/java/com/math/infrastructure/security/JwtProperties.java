package com.math.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del JWT ({@code app.security.jwt.*}).
 *
 * @param secret            clave HMAC-SHA; mínimo 32 caracteres. En producción llega por la
 *                          variable de entorno {@code JWT_SECRET}
 * @param expirationSeconds vida del token en segundos
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, long expirationSeconds) {
}
