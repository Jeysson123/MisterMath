package com.math.domain.pattern.strategy;

import com.math.domain.operation.MathOperation;
import com.math.domain.pattern.CalculationEngine;
import com.math.domain.pattern.CalculationOutcome;
import com.math.domain.pattern.CalculationPattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motor para {@code "pattern": "STRATEGY"}.
 *
 * <pre>
 *  1. context.strategyFor(symbol) → selecciona un bean ya existente
 *  2. strategy.apply(num1, num2)  → polimorfismo
 * </pre>
 *
 * <p>{@code @RequiredArgsConstructor} (Lombok) genera el constructor con el campo {@code final};
 * Spring lo usa para inyectar el contexto (inyección por constructor).</p>
 */
@Component
@RequiredArgsConstructor
public class StrategyCalculationEngine implements CalculationEngine {

    private final OperationStrategyContext context;

    @Override
    public CalculationPattern pattern() {
        return CalculationPattern.STRATEGY;
    }

    @Override
    public CalculationOutcome calculate(BigDecimal num1, String symbol, BigDecimal num2) {
        MathOperation strategy = context.strategyFor(symbol);
        BigDecimal result = strategy.apply(num1, num2);
        return new CalculationOutcome(result, getClass().getSimpleName(), List.of(
                "OperationStrategyContext selected the Spring bean " + strategy.getClass().getSimpleName()
                        + " for '" + symbol + "'",
                "Strategy " + strategy.getClass().getSimpleName() + " applied = " + result.toPlainString()));
    }
}
