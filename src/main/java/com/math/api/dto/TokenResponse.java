package com.math.api.dto;

/**
 * Respuesta del login.
 *
 * <pre>
 *  { "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", "expiresIn": 3600 }
 * </pre>
 *
 * @param accessToken JWT a enviar en {@code Authorization: Bearer <accessToken>}
 * @param tokenType   siempre {@code "Bearer"}
 * @param expiresIn   segundos de vida del token
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    /**
     * @param accessToken token firmado
     * @param expiresIn   segundos de vida
     * @return respuesta de tipo Bearer
     */
    public static TokenResponse bearer(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }

    /**
     * El logger de respuestas imprime el {@code data}; así el token nunca queda en los logs.
     *
     * @return representación segura
     */
    @Override
    public String toString() {
        return "TokenResponse[accessToken=****, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
