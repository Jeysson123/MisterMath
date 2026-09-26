package com.math.infrastructure.security;

import java.util.Optional;

/**
 * Emite y valida tokens de acceso.
 *
 * <p><b>D - Dependency Inversion:</b> el filtro y el servicio de login dependen de esta interfaz,
 * no de la librería jjwt. Cambiar de librería solo toca {@link JwtTokenProvider}.</p>
 */
public interface TokenProvider {

    /**
     * @param subject usuario autenticado
     * @return token firmado
     */
    String generate(String subject);

    /**
     * @param token token recibido en {@code Authorization: Bearer ...}
     * @return el usuario del token, o vacío si es inválido o expiró
     */
    Optional<String> validate(String token);

    /**
     * @return segundos de vida de cada token emitido
     */
    long expirationSeconds();
}
