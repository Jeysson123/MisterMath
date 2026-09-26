package com.math.domain.operation;

import com.math.domain.exception.DivisionByZeroException;
import com.math.domain.exception.InvalidOperandException;
import com.math.domain.exception.UnsupportedOperationSymbolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Cada operación concreta, probada a través de la interfaz MathOperation (polimorfismo).
class MathOperationTest {

    @ParameterizedTest(name = "{0} {1} {2} = {3}")
    @CsvSource({
            "10, +, 5, 15",
            "0.1, +, 0.2, 0.3",
            "10, -, 15, -5",
            "7, *, 6, 42",
            "2.5, *, 4, 10",
            "10, /, 4, 2.5",
            "10, /, 3, 3.333333333333333",
            "10, %, 3, 1",
            "-10, %, 3, -1",
            "2, ^, 10, 1024",
            "2, ^, -2, 0.25",
            "5, ^, 0, 1"
    })
    void appliesEveryOperation(String left, String symbol, String right, String expected) {
        MathOperation operation = pick(symbol);

        BigDecimal result = operation.apply(new BigDecimal(left), new BigDecimal(right));

        assertThat(result).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"/", "%"})
    void rejectsZeroAsDivisor(String symbol) {
        assertThatThrownBy(() -> pick(symbol).apply(BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(DivisionByZeroException.class)
                .hasMessage("Division by zero is not allowed");
    }

    @ParameterizedTest
    @CsvSource({"2.5", "1000", "-1000"})
    void rejectsInvalidExponents(String exponent) {
        assertThatThrownBy(() -> new Power().apply(BigDecimal.TEN, new BigDecimal(exponent)))
                .isInstanceOf(InvalidOperandException.class);
    }

    @Test
    void rejectsZeroRaisedToNegativeExponent() {
        assertThatThrownBy(() -> new Power().apply(BigDecimal.ZERO, new BigDecimal("-1")))
                .isInstanceOf(InvalidOperandException.class);
    }

    @Test
    void operationTypeKnowsItsSymbols() {
        assertThat(OperationType.fromSymbol("*")).isEqualTo(OperationType.MULTIPLICATION);
        assertThat(OperationType.isSupported("^")).isTrue();
        assertThat(OperationType.isSupported("x")).isFalse();
        assertThat(OperationType.symbols()).containsExactly("+", "-", "*", "/", "%", "^");
        assertThatThrownBy(() -> OperationType.fromSymbol("x"))
                .isInstanceOf(UnsupportedOperationSymbolException.class)
                .hasMessageContaining("'x'");
    }

    private MathOperation pick(String symbol) {
        return switch (symbol) {
            case "+" -> new Addition();
            case "-" -> new Subtraction();
            case "*" -> new Multiplication();
            case "/" -> new Division();
            case "%" -> new Modulo();
            case "^" -> new Power();
            default -> throw new IllegalArgumentException(symbol);
        };
    }
}
