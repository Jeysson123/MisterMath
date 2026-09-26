package com.math.domain.operation;

import com.math.domain.exception.DivisionByZeroException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Módulo (residuo): {@code num1 % num2}.
 *
 * <p>Igual que la división, no admite {@code num2 = 0}.</p>
 */
@Component
public class Modulo implements MathOperation {

    @Override
    public OperationType type() {
        return OperationType.MODULO;
    }

    @Override
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        if (right.signum() == 0) {
            throw new DivisionByZeroException();
        }
        return left.remainder(right).stripTrailingZeros();
    }
}
