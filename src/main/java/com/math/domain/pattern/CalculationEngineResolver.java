package com.math.domain.pattern;

import com.math.domain.exception.UnsupportedPatternException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Elige el {@link CalculationEngine} que corresponde al {@code pattern} del payload.
 *
 * <p>Spring le inyecta <b>todos</b> los beans que implementan {@link CalculationEngine}; el
 * constructor los indexa por su {@link CalculationPattern}. Por eso no hay ningún
 * {@code switch}: el día que exista un {@code PrototypeCalculationEngine}, basta con crearlo
 * (Open/Closed).</p>
 *
 * <pre>
 *  "STRATEGY" ──► CalculationPattern.STRATEGY ──► Map.get(...) ──► StrategyCalculationEngine
 * </pre>
 */
@Component
public class CalculationEngineResolver {

    private final Map<CalculationPattern, CalculationEngine> engines = new EnumMap<>(CalculationPattern.class);

    /**
     * @param engines todos los motores registrados en el contexto de Spring
     * @throws IllegalStateException si dos motores dicen atender el mismo patrón
     */
    public CalculationEngineResolver(List<CalculationEngine> engines) {
        for (CalculationEngine engine : engines) {
            CalculationEngine previous = this.engines.put(engine.pattern(), engine);
            if (previous != null) {
                throw new IllegalStateException("Two engines registered for pattern " + engine.pattern());
            }
        }
    }

    /**
     * Devuelve el motor del patrón pedido.
     *
     * @param pattern patrón elegido por el cliente
     * @return el motor que lo implementa
     * @throws UnsupportedPatternException si no hay motor para ese patrón
     */
    public CalculationEngine resolve(CalculationPattern pattern) {
        CalculationEngine engine = engines.get(pattern);
        if (engine == null) {
            throw new UnsupportedPatternException(String.valueOf(pattern));
        }
        return engine;
    }
}
