package com.math.domain.operation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Multiplicación: {@code num1 * num2}.
 */
@Component
public class Multiplication implements MathOperation {

    @Override
    public OperationType type() {
        return OperationType.MULTIPLICATION;
    }

    @Override
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        return left.multiply(right).stripTrailingZeros();
    }
}
