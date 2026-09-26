package com.math.infrastructure.security;

import com.math.api.dto.LoginRequest;
import com.math.api.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Cambia usuario y contraseña por un token JWT.
 *
 * <pre>
 *  LoginRequest ─► AuthenticationManager (BCrypt) ─ falla ─► BadCredentialsException → 401
 *                          │ ok
 *                          ▼
 *                 TokenProvider.generate(username) ─► TokenResponse
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    /**
     * @param request credenciales
     * @return el token y su tiempo de vida
     * @throws org.springframework.security.core.AuthenticationException si las credenciales no son válidas
     */
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return TokenResponse.bearer(tokenProvider.generate(authentication.getName()), tokenProvider.expirationSeconds());
    }
}
