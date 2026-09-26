package com.math.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header {@code Authorization: Bearer <token>} en cada request.
 *
 * <pre>
 *  request ─► ¿trae "Bearer "? ─ no ─► sigue sin usuario (si el endpoint es privado → 401)
 *                   │ sí
 *                   ▼
 *        tokenProvider.validate(token) ─ inválido ─► sigue sin usuario → 401
 *                   │ válido
 *                   ▼
 *        SecurityContext = usuario autenticado ─► controlador
 * </pre>
 *
 * <p>No es un {@code @Component} a propósito: si lo fuera, Spring Boot también lo registraría
 * como filtro global del servlet y se ejecutaría dos veces. Se crea en {@link SecurityConfig}.</p>
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            tokenProvider.validate(header.substring(BEARER_PREFIX.length()))
                    .ifPresent(username -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))));
        }
        chain.doFilter(request, response);
    }
}
