package com.math.infrastructure.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la caché, leída de {@code app.cache.*} en {@code application.yml}.
 *
 * @param transactionsKey llave de la lista en Redis
 * @param maxSize         cuántas transacciones recientes se conservan
 */
@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(String transactionsKey, int maxSize) {
}
