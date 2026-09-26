package com.math.application.command;

import com.math.application.cqrs.CommandHandler;
import com.math.application.model.TransactionRecord;
import com.math.application.port.TransactionCacheWriter;
import com.math.application.port.TransactionWriter;
import com.math.domain.pattern.CalculationEngine;
import com.math.domain.pattern.CalculationEngineResolver;
import com.math.domain.pattern.CalculationOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ejecuta {@link CalculateCommand}: el lado <b>Command</b> de CQRS.
 *
 * <pre>
 *  CalculateCommand
 *     │ 1. resolver.resolve(pattern)          ¿qué motor usa este patrón?
 *     │ 2. engine.calculate(num1, op, num2)   el patrón controla el flujo
 *     │ 3. transactionWriter.save(req, res)   PostgreSQL (request, response, fecha)
 *     │ 4. cacheWriter.push(record)           Redis (lista de recientes)
 *     ▼
 *  CalculationResult (con transactionId)
 * </pre>
 *
 * <h2>SOLID aplicado aquí</h2>
 * <ul>
 *   <li><b>S:</b> solo orquesta. No calcula (motores), no sabe SQL (writer), no sabe Redis (cache).</li>
 *   <li><b>D:</b> sus cuatro dependencias son abstracciones o componentes inyectados por constructor.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculateCommandHandler implements CommandHandler<CalculateCommand, CalculationResult> {

    private final CalculationEngineResolver engineResolver;
    private final TransactionWriter transactionWriter;
    private final TransactionCacheWriter cacheWriter;

    /**
     * {@inheritDoc}
     *
     * <p>Si el cálculo falla (por ejemplo, división entre cero) la excepción sube al
     * {@code GlobalExceptionHandler} y no se guarda nada.</p>
     */
    @Override
    @Transactional
    public CalculationResult handle(CalculateCommand command) {
        CalculationEngine engine = engineResolver.resolve(command.pattern());
        CalculationOutcome outcome = engine.calculate(command.num1(), command.operation(), command.num2());

        CalculationResult result = CalculationResult.builder()
                .num1(command.num1())
                .num2(command.num2())
                .operation(command.operation())
                .pattern(command.pattern())
                .result(outcome.result())
                .engine(outcome.engine())
                .trace(outcome.trace())
                .build();

        TransactionRecord saved = transactionWriter.save(command, result);
        cacheWriter.push(saved);
        log.debug("Transaction {} stored in PostgreSQL and Redis", saved.id());

        return result.toBuilder().transactionId(saved.id()).build();
    }
}
