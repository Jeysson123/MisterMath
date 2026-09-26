package com.math.domain.exception;

/**
 * Se lanza cuando {@code num2 = 0} en una división o un módulo.
 */
public class DivisionByZeroException extends MathDomainException {

    /** Crea la excepción con un mensaje fijo. */
    public DivisionByZeroException() {
        super("Division by zero is not allowed");
    }
}
