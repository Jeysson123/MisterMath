package com.math.domain.pattern.builder;

import com.math.domain.pattern.CalculationEngine;
import com.math.domain.pattern.CalculationOutcome;
import com.math.domain.pattern.CalculationPattern;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motor para {@code "pattern": "BUILDER"}.
 *
 * <pre>
 *  1. Expression.builder().left(num1).operator(s).right(num2) → piezas sueltas
 *  2. .build()                                               → valida y congela (inmutable)
 *  3. expression.evaluate()                                  → resultado
 * </pre>
 */
@Component
public class BuilderCalculationEngine implements CalculationEngine {

    @Override
    public CalculationPattern pattern() {
        return CalculationPattern.BUILDER;
    }

    @Override
    public CalculationOutcome calculate(BigDecimal num1, String symbol, BigDecimal num2) {
        Expression expression = Expression.builder()
                .left(num1)
                .operator(symbol)
                .right(num2)
                .build();
        BigDecimal result = expression.evaluate();
        return new CalculationOutcome(result, getClass().getSimpleName(), List.of(
                "Expression.builder() collected left, operator and right step by step",
                "build() validated the parts and created the immutable expression [" + expression + "]",
                "expression.evaluate() = " + result.toPlainString()));
    }
}
