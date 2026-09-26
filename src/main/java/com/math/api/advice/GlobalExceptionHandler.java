package com.math.api.advice;

import com.math.api.dto.ApiError;
import com.math.api.dto.ApiResponse;
import com.math.domain.exception.MathDomainException;
import com.math.domain.exception.TransactionNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>Manejo global de excepciones.</b> Convierte cualquier excepción que salga de un
 * controlador en un {@link ApiResponse} con un {@link ApiError}.
 *
 * <pre>
 *  MethodArgumentNotValidException   (@Valid del body)       → 400 + errores por campo
 *  HandlerMethodValidationException  (@Min/@Positive params) → 400 + errores por parámetro
 *  HttpMessageNotReadableException   (JSON mal formado)      → 400
 *  MethodArgumentTypeMismatchException (/transactions/abc)   → 400
 *  AuthenticationException           (login incorrecto)      → 401
 *  TransactionNotFoundException                              → 404
 *  NoResourceFoundException          (ruta inexistente)      → 404
 *  HttpRequestMethodNotSupportedException                    → 405
 *  MathDomainException (÷0, exponente inválido, ...)         → 422
 *  Exception (cualquier otra)                                → 500
 * </pre>
 *
 * <p>Gracias a esta clase los controladores no tienen ni un {@code try/catch}
 * (<b>Single Responsibility</b>: ellos atienden el caso feliz, esta clase los errores). El log de
 * cada respuesta lo escribe {@link LoggingResponseAdvice}; aquí solo se registra la traza
 * completa de los errores inesperados (500).</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @param exception errores de {@code @Valid} en el body
     * @return 400 con un mensaje por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleBodyValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, new ApiError("Bad Request", "Validation failed", fields));
    }

    /**
     * @param exception errores de validación en parámetros ({@code @Min}, {@code @Positive}...)
     * @return 400 con un mensaje por parámetro
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleParameterValidation(HandlerMethodValidationException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> fields.put(
                result.getMethodParameter().getParameterName(),
                result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .findFirst()
                        .orElse("invalid value")));
        return build(HttpStatus.BAD_REQUEST, new ApiError("Bad Request", "Validation failed", fields));
    }

    /**
     * @param exception violaciones lanzadas por validación manual o {@code @Validated}
     * @return 400 con un mensaje por propiedad
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fields.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return build(HttpStatus.BAD_REQUEST, new ApiError("Bad Request", "Validation failed", fields));
    }

    /**
     * @param exception body vacío, JSON roto o un número mal escrito
     * @return 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return build(HttpStatus.BAD_REQUEST, ApiError.of("Bad Request", "Malformed JSON request body"));
    }

    /**
     * @param exception parámetro con un tipo incorrecto, por ejemplo {@code /transactions/abc}
     * @return 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return build(HttpStatus.BAD_REQUEST, ApiError.of("Bad Request",
                "Parameter '" + exception.getName() + "' has an invalid value: " + exception.getValue()));
    }

    /**
     * @param exception usuario o contraseña incorrectos en el login
     * @return 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleAuthentication(AuthenticationException exception) {
        return build(HttpStatus.UNAUTHORIZED, ApiError.of("Unauthorized", "Invalid username or password"));
    }

    /**
     * @param exception la transacción pedida no existe
     * @return 404
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleNotFound(TransactionNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, ApiError.of("Not Found", exception.getMessage()));
    }

    /**
     * @param exception la ruta no existe
     * @return 404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleNoResource(NoResourceFoundException exception) {
        return build(HttpStatus.NOT_FOUND, ApiError.of("Not Found", "Endpoint /" + exception.getResourcePath() + " does not exist"));
    }

    /**
     * @param exception verbo HTTP no soportado, por ejemplo {@code DELETE /calculations}
     * @return 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ApiError.of("Method Not Allowed", exception.getMessage()));
    }

    /**
     * Una sola regla para todas las subclases: división entre cero, operando inválido,
     * símbolo o patrón no soportado.
     *
     * @param exception error matemático o de negocio
     * @return 422
     */
    @ExceptionHandler(MathDomainException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleMathDomain(MathDomainException exception) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ApiError.of("Unprocessable Entity", exception.getMessage()));
    }

    /**
     * Red de seguridad: nunca se filtra un stack trace al cliente.
     *
     * @param exception cualquier error no previsto
     * @return 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiError>> handleUnexpected(Exception exception) {
        log.error("Unexpected error", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.of("Internal Server Error", "An unexpected error occurred"));
    }

    private ResponseEntity<ApiResponse<ApiError>> build(HttpStatus status, ApiError error) {
        return ResponseEntity.status(status).body(ApiResponse.of(status, error));
    }
}
