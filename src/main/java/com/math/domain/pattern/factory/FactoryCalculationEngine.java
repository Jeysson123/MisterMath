package com.math.domain.pattern.factory;

import com.math.domain.operation.MathOperation;
import com.math.domain.pattern.CalculationEngine;
import com.math.domain.pattern.CalculationOutcome;
import com.math.domain.pattern.CalculationPattern;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motor para {@code "pattern": "FACTORY"}.
 *
 * <pre>
 *  1. MathOperationFactory.create(symbol)  → objeto NUEVO
 *  2. operation.apply(num1, num2)          → polimorfismo
 * </pre>
 */
@Component
public class FactoryCalculationEngine implements CalculationEngine {

    @Override
    public CalculationPattern pattern() {
        return CalculationPattern.FACTORY;
    }

    @Override
    public CalculationOutcome calculate(BigDecimal num1, String symbol, BigDecimal num2) {
        MathOperation operation = MathOperationFactory.create(symbol);
        BigDecimal result = operation.apply(num1, num2);
        return new CalculationOutcome(result, getClass().getSimpleName(), List.of(
                "MathOperationFactory.create(\"" + symbol + "\") built a new "
                        + operation.getClass().getSimpleName() + " instance",
                operation.getClass().getSimpleName() + ".apply(" + num1.toPlainString() + ", "
                        + num2.toPlainString() + ") = " + result.toPlainString()));
    }
}
