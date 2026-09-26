package com.math.domain.operation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Suma: {@code num1 + num2}.
 *
 * <p>Es {@code @Component} para que el patrón Strategy la reciba inyectada; la fábrica, en
 * cambio, la crea con {@code new}. La misma clase sirve a ambos porque no guarda estado.</p>
 */
@Component
public class Addition implements MathOperation {

    @Override
    public OperationType type() {
        return OperationType.ADDITION;
    }

    @Override
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        return left.add(right).stripTrailingZeros();
    }
}
