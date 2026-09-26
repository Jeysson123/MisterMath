package com.math.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload de {@code POST /api/v1/auth/login}.
 *
 * @param username usuario
 * @param password contraseña (nunca se imprime en los logs)
 */
public record LoginRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password) {

    /**
     * Oculta la contraseña si el objeto llega a un log.
     *
     * @return representación segura
     */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=****]";
    }
}
