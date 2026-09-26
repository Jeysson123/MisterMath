package com.math.domain.pattern;

import com.math.domain.exception.UnsupportedPatternException;
import com.math.domain.pattern.builder.BuilderCalculationEngine;
import com.math.domain.pattern.factory.FactoryCalculationEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculationEngineResolverTest {

    private final FactoryCalculationEngine factory = new FactoryCalculationEngine();
    private final BuilderCalculationEngine builder = new BuilderCalculationEngine();

    @Test
    void resolvesTheEngineOfEachPattern() {
        CalculationEngineResolver resolver = new CalculationEngineResolver(List.of(factory, builder));

        assertThat(resolver.resolve(CalculationPattern.FACTORY)).isSameAs(factory);
        assertThat(resolver.resolve(CalculationPattern.BUILDER)).isSameAs(builder);
    }

    @Test
    void failsWhenNoEngineIsRegistered() {
        CalculationEngineResolver resolver = new CalculationEngineResolver(List.of(factory));

        assertThatThrownBy(() -> resolver.resolve(CalculationPattern.SINGLETON))
                .isInstanceOf(UnsupportedPatternException.class);
    }

    @Test
    void refusesTwoEnginesForTheSamePattern() {
        assertThatThrownBy(() -> new CalculationEngineResolver(List.of(factory, new FactoryCalculationEngine())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parsesPatternsIgnoringCase() {
        assertThat(CalculationPattern.from("strategy")).isEqualTo(CalculationPattern.STRATEGY);
        assertThat(CalculationPattern.from(" Builder ")).isEqualTo(CalculationPattern.BUILDER);
        assertThatThrownBy(() -> CalculationPattern.from("OBSERVER")).isInstanceOf(UnsupportedPatternException.class);
        assertThatThrownBy(() -> CalculationPattern.from(null)).isInstanceOf(UnsupportedPatternException.class);
    }
}
