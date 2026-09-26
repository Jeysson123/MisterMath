package com.math.domain.operation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Resta: {@code num1 - num2}.
 */
@Component
public class Subtraction implements MathOperation {

    @Override
    public OperationType type() {
        return OperationType.SUBTRACTION;
    }

    @Override
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        return left.subtract(right).stripTrailingZeros();
    }
}
