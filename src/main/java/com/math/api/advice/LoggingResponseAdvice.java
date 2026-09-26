package com.math.api.advice;

import com.math.api.dto.ApiError;
import com.math.api.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <b>Logger global de respuestas.</b> Es un {@code @RestControllerAdvice} que implementa
 * {@link ResponseBodyAdvice}: Spring lo llama justo antes de escribir el body de <b>cualquier</b>
 * respuesta, tanto de los controladores como del {@link GlobalExceptionHandler}.
 *
 * <pre>
 *  Controlador / ExceptionHandler
 *            │  ApiResponse
 *            ▼
 *  LoggingResponseAdvice.beforeBodyWrite ──► log
 *            │  (mismo body, sin cambios)
 *            ▼
 *  Jackson → JSON → cliente
 * </pre>
 *
 * Ejemplo de salida:
 * <pre>
 *  INFO  method=POST endpoint=/api/v1/calculations code=201 status=CREATED result=CalculationResult[...]
 *  WARN  method=POST endpoint=/api/v1/calculations code=422 status=UNPROCESSABLE_ENTITY error=ApiError[...]
 * </pre>
 *
 * <p>Éxitos se registran en {@code INFO} y errores en {@code WARN}. Los 401/403 que nacen en los
 * filtros de seguridad no pasan por aquí; los registra {@code JsonSecurityErrorHandler}.</p>
 */
@Slf4j
@RestControllerAdvice
public class LoggingResponseAdvice implements ResponseBodyAdvice<Object> {

    /** Aplica a todas las respuestas. */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Registra método, endpoint, código y resultado (o error) y devuelve el body intacto.
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse<?> apiResponse) {
            String method = request.getMethod().name();
            String endpoint = request.getURI().getPath();
            if (apiResponse.getData() instanceof ApiError error) {
                log.warn("method={} endpoint={} code={} status={} error={}",
                        method, endpoint, apiResponse.getCode(), apiResponse.getStatus(), error);
            } else {
                log.info("method={} endpoint={} code={} status={} result={}",
                        method, endpoint, apiResponse.getCode(), apiResponse.getStatus(), apiResponse.getData());
            }
        }
        return body;
    }
}
