package com.math.domain.operation;

import com.math.domain.exception.DivisionByZeroException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * División: {@code num1 / num2}.
 *
 * <p>Usa {@link MathContext#DECIMAL64} (16 dígitos significativos) para que divisiones
 * infinitas como {@code 10 / 3} no lancen {@link ArithmeticException}.</p>
 *
 * <p><b>Liskov:</b> dividir entre cero lanza {@link DivisionByZeroException}, que es un
 * {@code MathDomainException}; justo lo que el contrato de {@link MathOperation} anuncia.</p>
 */
@Component
public class Division implements MathOperation {

    @Override
    public OperationType type() {
        return OperationType.DIVISION;
    }

    @Override
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        if (right.signum() == 0) {
            throw new DivisionByZeroException();
        }
        return left.divide(right, MathContext.DECIMAL64).stripTrailingZeros();
    }
}
