package com.math.domain.operation;

import com.math.domain.exception.InvalidOperandException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Potencia: {@code num1 ^ num2}.
 *
 * <p>El exponente debe ser un entero entre {@value #MIN_EXPONENT} y {@value #MAX_EXPONENT};
 * así evitamos potencias fraccionarias (raíces) y números gigantes que colgarían el servidor.</p>
 */
@Component
public class Power implements MathOperation {

    /** Exponente mínimo permitido. */
    public static final int MIN_EXPONENT = -999;

    /** Exponente máximo permitido. */
    public static final int MAX_EXPONENT = 999;

    @Override
    public OperationType type() {
        return OperationType.POWER;
    }

    @Override
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        int exponent = toExponent(right);
        if (left.signum() == 0 && exponent < 0) {
            throw new InvalidOperandException("0 cannot be raised to a negative exponent");
        }
        return left.pow(exponent, MathContext.DECIMAL64).stripTrailingZeros();
    }

    /**
     * Convierte {@code num2} en un exponente entero válido.
     *
     * @param right segundo operando
     * @return el exponente como {@code int}
     * @throws InvalidOperandException si tiene decimales o se sale del rango permitido
     */
    private int toExponent(BigDecimal right) {
        BigDecimal normalized = right.stripTrailingZeros();
        if (normalized.scale() > 0) {
            throw new InvalidOperandException("The exponent (num2) must be an integer");
        }
        if (normalized.compareTo(BigDecimal.valueOf(MIN_EXPONENT)) < 0
                || normalized.compareTo(BigDecimal.valueOf(MAX_EXPONENT)) > 0) {
            throw new InvalidOperandException(
                    "The exponent (num2) must be between " + MIN_EXPONENT + " and " + MAX_EXPONENT);
        }
        return normalized.intValueExact();
    }
}
