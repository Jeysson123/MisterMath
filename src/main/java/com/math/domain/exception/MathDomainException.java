package com.math.domain.exception;

/**
 * Error de negocio: los datos llegaron bien formados, pero la matemática no se puede resolver
 * (dividir entre cero, exponente inválido, ...).
 *
 * <p>El {@code GlobalExceptionHandler} convierte cualquier subclase en un
 * {@code 422 UNPROCESSABLE_ENTITY}. Al ser la clase padre, basta con un solo
 * {@code @ExceptionHandler} para todas.</p>
 */
public class MathDomainException extends RuntimeException {

    /**
     * @param message explicación legible del problema
     */
    public MathDomainException(String message) {
        super(message);
    }
}
