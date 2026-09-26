package com.math.domain.pattern;

import com.math.domain.exception.DivisionByZeroException;
import com.math.domain.operation.Addition;
import com.math.domain.operation.Division;
import com.math.domain.operation.Modulo;
import com.math.domain.operation.Multiplication;
import com.math.domain.operation.Power;
import com.math.domain.operation.Subtraction;
import com.math.domain.pattern.builder.BuilderCalculationEngine;
import com.math.domain.pattern.factory.FactoryCalculationEngine;
import com.math.domain.pattern.singleton.SingletonCalculationEngine;
import com.math.domain.pattern.strategy.OperationStrategyContext;
import com.math.domain.pattern.strategy.StrategyCalculationEngine;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Liskov en acción: los cuatro motores son sustituibles entre sí. Con la misma entrada dan el
 * mismo resultado y fallan con el mismo tipo de excepción; solo cambia el camino interno.
 */
class LiskovSubstitutionTest {

    static Stream<CalculationEngine> engines() {
        OperationStrategyContext context = new OperationStrategyContext(List.of(
                new Addition(), new Subtraction(), new Multiplication(), new Division(), new Modulo(), new Power()));
        return Stream.of(
                new SingletonCalculationEngine(),
                new FactoryCalculationEngine(),
                new StrategyCalculationEngine(context),
                new BuilderCalculationEngine());
    }

    static Stream<Arguments> enginesAndOperations() {
        return engines().flatMap(engine -> Stream.of(
                Arguments.of(engine, "+", "15"),
                Arguments.of(engine, "-", "5"),
                Arguments.of(engine, "*", "50"),
                Arguments.of(engine, "/", "2"),
                Arguments.of(engine, "%", "0"),
                Arguments.of(engine, "^", "100000")));
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("enginesAndOperations")
    void everyEngineGivesTheSameResult(CalculationEngine engine, String symbol, String expected) {
        CalculationOutcome outcome = engine.calculate(BigDecimal.TEN, symbol, new BigDecimal("5"));

        assertThat(outcome.result()).isEqualByComparingTo(expected);
        assertThat(outcome.engine()).isEqualTo(engine.getClass().getSimpleName());
        assertThat(outcome.trace()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("engines")
    void everyEngineFailsTheSameWay(CalculationEngine engine) {
        assertThatThrownBy(() -> engine.calculate(BigDecimal.TEN, "/", BigDecimal.ZERO))
                .isInstanceOf(DivisionByZeroException.class);
    }
}
