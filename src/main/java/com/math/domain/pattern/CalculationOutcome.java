package com.math.domain.pattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que devuelve un {@link CalculationEngine}: el resultado y la "bitácora" del patrón.
 *
 * <p>El {@code trace} existe solo para aprender: cada motor anota los pasos que dio, así en la
 * respuesta HTTP se ve cómo cambió el flujo según el patrón elegido.</p>
 *
 * @param result resultado de la operación
 * @param engine nombre de la clase que resolvió el cálculo
 * @param trace  pasos que siguió el patrón, en orden
 */
public record CalculationOutcome(BigDecimal result, String engine, List<String> trace) {

    /**
     * Copia defensiva para que la lista no se pueda modificar desde fuera.
     */
    public CalculationOutcome {
        trace = List.copyOf(trace);
    }
}
