package com.math.domain.pattern.strategy;

import com.math.domain.exception.UnsupportedOperationSymbolException;
import com.math.domain.operation.Addition;
import com.math.domain.operation.Division;
import com.math.domain.operation.MathOperation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationStrategyContextTest {

    private final Addition addition = new Addition();
    private final Division division = new Division();
    private final OperationStrategyContext context = new OperationStrategyContext(List.<MathOperation>of(addition, division));

    // El contexto no crea nada: devuelve exactamente el objeto que recibió (el bean de Spring).
    @Test
    void selectsTheRegisteredStrategy() {
        assertThat(context.strategyFor("+")).isSameAs(addition);
        assertThat(context.strategyFor("/")).isSameAs(division);
    }

    // Solo conoce las estrategias que le inyectaron.
    @Test
    void failsForStrategiesThatWereNotRegistered() {
        assertThatThrownBy(() -> context.strategyFor("*"))
                .isInstanceOf(UnsupportedOperationSymbolException.class);
    }

    @Test
    void engineDelegatesToTheContext() {
        var outcome = new StrategyCalculationEngine(context).calculate(BigDecimal.TEN, "/", new BigDecimal("4"));

        assertThat(outcome.result()).isEqualByComparingTo("2.5");
        assertThat(outcome.trace().get(0)).contains("Division");
    }
}
