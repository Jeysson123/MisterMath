package com.math.domain.exception;

/**
 * Se lanza cuando un operando no es válido para la operación pedida
 * (por ejemplo, un exponente con decimales).
 */
public class InvalidOperandException extends MathDomainException {

    /**
     * @param message qué operando falló y por qué
     */
    public InvalidOperandException(String message) {
        super(message);
    }
}
