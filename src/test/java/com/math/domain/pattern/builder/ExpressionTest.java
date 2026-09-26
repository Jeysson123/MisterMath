package com.math.domain.pattern.builder;

import com.math.domain.exception.UnsupportedOperationSymbolException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionTest {

    @Test
    void buildsAndEvaluatesStepByStep() {
        Expression expression = Expression.builder()
                .left(new BigDecimal("10"))
                .operator("*")
                .right(new BigDecimal("4"))
                .build();

        assertThat(expression.evaluate()).isEqualByComparingTo("40");
        assertThat(expression).hasToString("10 * 4");
    }

    @Test
    void refusesToBuildWhenAPartIsMissing() {
        assertThatThrownBy(() -> Expression.builder().left(BigDecimal.ONE).operator("+").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("right operand is required");
        assertThatThrownBy(() -> Expression.builder().left(BigDecimal.ONE).right(BigDecimal.ONE).build())
                .hasMessage("operator is required");
        assertThatThrownBy(() -> Expression.builder().operator("+").right(BigDecimal.ONE).build())
                .hasMessage("left operand is required");
    }

    @Test
    void refusesUnknownOperators() {
        assertThatThrownBy(() -> Expression.builder().left(BigDecimal.ONE).operator("?").right(BigDecimal.ONE).build())
                .isInstanceOf(UnsupportedOperationSymbolException.class);
    }

    @Test
    void engineTracesTheBuilderSteps() {
        var outcome = new BuilderCalculationEngine().calculate(new BigDecimal("2"), "^", new BigDecimal("3"));

        assertThat(outcome.result()).isEqualByComparingTo("8");
        assertThat(outcome.trace()).hasSize(3).anyMatch(step -> step.contains("[2 ^ 3]"));
    }
}
