package com.math.application.command;

import com.math.application.model.TransactionRecord;
import com.math.application.port.TransactionCacheWriter;
import com.math.application.port.TransactionWriter;
import com.math.domain.exception.DivisionByZeroException;
import com.math.domain.pattern.CalculationEngineResolver;
import com.math.domain.pattern.CalculationPattern;
import com.math.domain.pattern.factory.FactoryCalculationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Dependency Inversion hace fácil este test: los puertos son interfaces y se reemplazan por mocks.
@ExtendWith(MockitoExtension.class)
class CalculateCommandHandlerTest {

    @Mock
    private TransactionWriter transactionWriter;

    @Mock
    private TransactionCacheWriter cacheWriter;

    private CalculateCommandHandler handler;

    @BeforeEach
    void setUp() {
        CalculationEngineResolver resolver = new CalculationEngineResolver(List.of(new FactoryCalculationEngine()));
        handler = new CalculateCommandHandler(resolver, transactionWriter, cacheWriter);
    }

    @Test
    void calculatesPersistsAndCaches() {
        CalculateCommand command = new CalculateCommand(BigDecimal.TEN, new BigDecimal("5"), "+", CalculationPattern.FACTORY);
        TransactionRecord saved = new TransactionRecord(7L, "{}", "{}", Instant.now());
        when(transactionWriter.save(eq(command), any(CalculationResult.class))).thenReturn(saved);

        CalculationResult result = handler.handle(command);

        assertThat(result.transactionId()).isEqualTo(7L);
        assertThat(result.result()).isEqualByComparingTo("15");
        assertThat(result.pattern()).isEqualTo(CalculationPattern.FACTORY);
        assertThat(result.engine()).isEqualTo("FactoryCalculationEngine");
        assertThat(result.trace()).isNotEmpty();

        ArgumentCaptor<CalculationResult> storedResponse = ArgumentCaptor.forClass(CalculationResult.class);
        verify(transactionWriter).save(eq(command), storedResponse.capture());
        assertThat(storedResponse.getValue().result()).isEqualByComparingTo("15");
        verify(cacheWriter).push(saved);
    }

    @Test
    void storesNothingWhenTheMathFails() {
        CalculateCommand command = new CalculateCommand(BigDecimal.ONE, BigDecimal.ZERO, "/", CalculationPattern.FACTORY);

        assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(DivisionByZeroException.class);

        verifyNoInteractions(transactionWriter, cacheWriter);
    }
}
