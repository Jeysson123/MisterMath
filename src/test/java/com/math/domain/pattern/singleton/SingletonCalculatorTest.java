package com.math.domain.pattern.singleton;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SingletonCalculatorTest {

    @Test
    void alwaysReturnsTheSameInstance() {
        assertThat(SingletonCalculator.getInstance()).isSameAs(SingletonCalculator.getInstance());
    }

    // Aunque muchos hilos pidan la instancia a la vez, solo existe una.
    @Test
    void isThreadSafe() throws Exception {
        Set<SingletonCalculator> seen = ConcurrentHashMap.newKeySet();
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<? extends java.util.concurrent.Future<?>> futures = IntStream.range(0, 100)
                    .mapToObj(i -> executor.submit(() -> seen.add(SingletonCalculator.getInstance())))
                    .toList();
            for (var future : futures) {
                future.get();
            }
        }
        assertThat(seen).hasSize(1);
    }

    @Test
    void calculates() {
        assertThat(SingletonCalculator.getInstance().calculate(new BigDecimal("6"), "*", new BigDecimal("7")))
                .isEqualByComparingTo("42");
    }

    @Test
    void engineReportsTheSameInstanceAcrossCalls() {
        SingletonCalculationEngine engine = new SingletonCalculationEngine();

        var first = engine.calculate(BigDecimal.ONE, "+", BigDecimal.ONE);
        var second = engine.calculate(BigDecimal.TEN, "-", BigDecimal.ONE);

        assertThat(first.result()).isEqualByComparingTo("2");
        assertThat(second.result()).isEqualByComparingTo("9");
        assertThat(first.trace().get(0)).isEqualTo(second.trace().get(0));
    }
}
