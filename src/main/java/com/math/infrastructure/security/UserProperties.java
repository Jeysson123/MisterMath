package com.math.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Usuario de demostración que puede pedir tokens ({@code app.security.user.*}).
 *
 * @param username nombre de usuario ({@code APP_USERNAME})
 * @param password contraseña en texto plano; se cifra con BCrypt al arrancar ({@code APP_PASSWORD})
 */
@ConfigurationProperties(prefix = "app.security.user")
public record UserProperties(String username, String password) {
}
