package com.math.domain.pattern.factory;

import com.math.domain.exception.UnsupportedOperationSymbolException;
import com.math.domain.operation.Addition;
import com.math.domain.operation.Division;
import com.math.domain.operation.MathOperation;
import com.math.domain.operation.Modulo;
import com.math.domain.operation.Multiplication;
import com.math.domain.operation.OperationType;
import com.math.domain.operation.Power;
import com.math.domain.operation.Subtraction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MathOperationFactoryTest {

    @Test
    void createsTheRightClassForEachSymbol() {
        assertThat(MathOperationFactory.create("+")).isInstanceOf(Addition.class);
        assertThat(MathOperationFactory.create("-")).isInstanceOf(Subtraction.class);
        assertThat(MathOperationFactory.create("*")).isInstanceOf(Multiplication.class);
        assertThat(MathOperationFactory.create("/")).isInstanceOf(Division.class);
        assertThat(MathOperationFactory.create("%")).isInstanceOf(Modulo.class);
        assertThat(MathOperationFactory.create("^")).isInstanceOf(Power.class);
    }

    // Cada tipo del enum tiene su caso en la fábrica y la operación creada dice ser de ese tipo.
    @ParameterizedTest
    @EnumSource(OperationType.class)
    void coversEveryOperationType(OperationType type) {
        assertThat(MathOperationFactory.create(type).type()).isEqualTo(type);
    }

    // La diferencia clave con el Singleton: cada llamada devuelve un objeto nuevo.
    @Test
    void returnsANewInstanceOnEveryCall() {
        MathOperation first = MathOperationFactory.create("+");
        MathOperation second = MathOperationFactory.create("+");

        assertThat(first).isNotSameAs(second);
    }

    @Test
    void rejectsUnknownSymbols() {
        assertThatThrownBy(() -> MathOperationFactory.create("x"))
                .isInstanceOf(UnsupportedOperationSymbolException.class);
    }

    @Test
    void engineUsesTheFactory() {
        var outcome = new FactoryCalculationEngine().calculate(BigDecimal.TEN, "-", BigDecimal.ONE);

        assertThat(outcome.result()).isEqualByComparingTo("9");
        assertThat(outcome.engine()).isEqualTo("FactoryCalculationEngine");
        assertThat(outcome.trace()).anyMatch(step -> step.contains("new Subtraction"));
    }
}
