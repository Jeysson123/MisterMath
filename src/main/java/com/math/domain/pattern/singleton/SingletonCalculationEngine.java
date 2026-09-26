package com.math.domain.pattern.singleton;

import com.math.domain.pattern.CalculationEngine;
import com.math.domain.pattern.CalculationOutcome;
import com.math.domain.pattern.CalculationPattern;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motor para {@code "pattern": "SINGLETON"}.
 *
 * <pre>
 *  1. SingletonCalculator.getInstance()   → SIEMPRE el mismo objeto (mismo identityHashCode)
 *  2. calculator.calculate(num1, s, num2) → usa su tabla de operaciones precargada
 * </pre>
 *
 * <p>Envía dos requests seguidos y compara el {@code identityHashCode} del {@code trace}: no cambia.</p>
 */
@Component
public class SingletonCalculationEngine implements CalculationEngine {

    @Override
    public CalculationPattern pattern() {
        return CalculationPattern.SINGLETON;
    }

    @Override
    public CalculationOutcome calculate(BigDecimal num1, String symbol, BigDecimal num2) {
        SingletonCalculator calculator = SingletonCalculator.getInstance();
        BigDecimal result = calculator.calculate(num1, symbol, num2);
        return new CalculationOutcome(result, getClass().getSimpleName(), List.of(
                "SingletonCalculator.getInstance() returned the shared instance @"
                        + Integer.toHexString(System.identityHashCode(calculator)),
                "The singleton reused its preloaded operation for '" + symbol + "' = " + result.toPlainString()));
    }
}
