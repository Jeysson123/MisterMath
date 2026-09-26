package com.math.application.command;

import com.math.application.cqrs.Command;
import com.math.domain.pattern.CalculationPattern;

import java.math.BigDecimal;

/**
 * Comando "calcula {@code num1 operation num2} usando el patrón {@code pattern}".
 *
 * <p>Es un {@code record}: inmutable, con constructor, getters, {@code equals} y {@code toString}
 * generados por Java. También es lo que se guarda como <i>request</i> en PostgreSQL.</p>
 *
 * @param num1      primer número
 * @param num2      segundo número
 * @param operation símbolo de la operación
 * @param pattern   patrón que controlará el flujo
 */
public record CalculateCommand(BigDecimal num1, BigDecimal num2, String operation, CalculationPattern pattern)
        implements Command {
}
