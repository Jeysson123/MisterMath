package com.math.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.api.dto.ApiError;
import com.math.api.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde los errores de seguridad (401 y 403) con el mismo wrapper {@link ApiResponse} que el
 * resto de la API.
 *
 * <p>¿Por qué no lo hace el {@code GlobalExceptionHandler}? Porque estos errores ocurren en los
 * filtros de Spring Security, <b>antes</b> de llegar a un controlador, y un
 * {@code @ControllerAdvice} solo ve lo que pasa dentro de los controladores. Por la misma razón
 * este componente escribe su propia línea de log.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /** Sin token, token inválido o token expirado. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, "A valid Bearer token is required");
    }

    /** Token válido pero sin permisos suficientes. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "You are not allowed to access this resource");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        ApiError error = ApiError.of(status.getReasonPhrase(), message);
        log.warn("method={} endpoint={} code={} status={} error={}",
                request.getMethod(), request.getRequestURI(), status.value(), status.name(), error);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.of(status, error));
    }
}
